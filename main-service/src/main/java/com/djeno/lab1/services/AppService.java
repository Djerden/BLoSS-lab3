package com.djeno.lab1.services;

import com.djeno.lab1.exceptions.*;
import com.djeno.lab1.persistence.DTO.app.*;
import com.djeno.lab1.persistence.DTO.app.FilePart;
import com.djeno.lab1.persistence.DTO.review.ReviewDTO;
import com.djeno.lab1.persistence.enums.AppStatus;
import com.djeno.lab1.persistence.enums.Role;
import com.djeno.lab1.persistence.models.*;
import com.djeno.lab1.persistence.repositories.AppRepository;
import com.djeno.lab1.persistence.repositories.CategoryRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
@Service
public class AppService {

    private final AppRepository appRepository;
    private final CategoryRepository categoryRepository;
    private final MinioService minioService;
    private final UserService userService;
    private final PurchaseService purchaseService;
    private final JmsTemplate jmsTemplate; // настройка ниже
    private final ObjectMapper objectMapper;

    public App getAppById(Long id) {
        return appRepository.findById(id)
                .orElseThrow(() -> new AppNotFoundException(id));
    }

    @Async("taskExecutor")
    public void sendAppToUploader(App app,
                                  CreateAppRequest appData,
                                  MultipartFile icon,
                                  MultipartFile apk,
                                  List<MultipartFile> screenshots) {
        try {
            AppUploadMessage msg = new AppUploadMessage();
            msg.setAppId(app.getId());
            msg.setOwnerId(app.getOwner().getId());
            msg.setAppData(appData);

            String correlationId = UUID.randomUUID().toString();
            msg.setCorrelationId(correlationId);

            if (icon != null && !icon.isEmpty()) {
                msg.setIconOriginalName(icon.getOriginalFilename());
                msg.setIconBase64(Base64.getEncoder().encodeToString(icon.getBytes()));
            }

            msg.setApkOriginalName(apk.getOriginalFilename());
            msg.setApkBase64(Base64.getEncoder().encodeToString(apk.getBytes()));

            if (screenshots != null && !screenshots.isEmpty()) {
                List<FilePart> parts = new ArrayList<>();
                for (MultipartFile s : screenshots) {
                    if (!s.isEmpty()) {
                        parts.add(new FilePart(s.getOriginalFilename(),
                                Base64.getEncoder().encodeToString(s.getBytes())));
                    }
                }
                msg.setScreenshots(parts);
            }

            // Отправляем в очередь app.upload
            String payload = objectMapper.writeValueAsString(msg);

            jmsTemplate.convertAndSend("app.upload", payload, m -> {
                m.setStringProperty("correlationId", correlationId);
                return m;
            });

            log.info("Sent upload message for appId={} correlation={}", app.getId(), correlationId);

        } catch (Exception e) {
            log.error("Failed to build/send upload message: {}", e.getMessage(), e);
            // В случае ошибки отправки — пометить запись как FAIL
            app.setStatus(AppStatus.FAIL);
            appRepository.save(app);
        }
    }

    public App createApp(
            CreateAppRequest appData,
            MultipartFile icon,
            MultipartFile file,
            List<MultipartFile> screenshots) {

        User owner = userService.getCurrentUser();

        if (file == null || file.isEmpty()) {
            throw new InvalidFileException("APK файл не загружен");
        }

        List<Category> categories = new ArrayList<>();
        if (appData.getCategoryIds() != null && !appData.getCategoryIds().isEmpty()) {
            categories = categoryRepository.findAllById(appData.getCategoryIds());
        }

        App app = new App();
        app.setName(appData.getName());
        app.setDescription(appData.getDescription());
        app.setPrice(appData.getPrice());
        app.setOwner(owner);
        app.setCategories(categories);

        App savedApp = appRepository.save(app);

        sendAppToUploader(app, appData, icon, file, screenshots);

        return savedApp;
    }

    // Listener для результатов
    @JmsListener(destination = "app.upload.result")
    public void handleUploadResult(String payload, @Header("correlationId") String correlationId) {
        try {
            UploadResultMessage res = objectMapper.readValue(payload, UploadResultMessage.class);
            Long appId = res.getAppId();
            Optional<App> opt = appRepository.findById(appId);
            if (opt.isEmpty()) {
                log.warn("Received upload result for unknown appId={}", appId);
                return;
            }
            App app = opt.get();
            if (res.isSuccess()) {
                app.setStatus(AppStatus.SUCCESS);
                app.setIconId(res.getIconId());
                app.setFileId(res.getFileId());
                app.setScreenshotsIds(res.getScreenshotsIds());
            } else {
                app.setStatus(AppStatus.FAIL);
                log.warn("Upload failed for appId={}, error={}", appId, res.getErrorMessage());
            }
            appRepository.save(app);
        } catch (Exception e) {
            log.error("Failed to process upload result: {}", e.getMessage(), e);
        }
    }

    // метод для endpoint /apps/{id}/status
    public AppStatus getStatus(Long id) {
        App a = getAppById(id);
        return a.getStatus();
    }

    public Page<AppListDto> getPurchasedApps(User user, Pageable pageable) {
        Page<Purchase> purchasesPage = purchaseService.getPaidPurchasesByUser(user, pageable);
        return purchasesPage.map(purchase -> convertToAppListDto(purchase.getApp()));
    }

    public Page<AppListDto> getApps(Long categoryId, Pageable pageable) {
        Page<App> appsPage;

        if (categoryId != null) {
            appsPage = appRepository.findByCategories_Id(categoryId, pageable);
        } else {
            appsPage = appRepository.findAll(pageable);
        }

        return appsPage.map(this::convertToAppListDto);
    }

    public AppDetailsDto getAppDetails(Long id) {
        App app = getAppById(id);

        return convertToAppDetailsDto(app);
    }

    public void deleteApp(Long id) {
        User currentUser = userService.getCurrentUser();
        App app = getAppById(id);

        if (!app.getOwner().getId().equals(currentUser.getId()) &&
                !currentUser.getRole().equals(Role.ROLE_ADMIN)) {
            throw new NotEnoughPrivileges("Недостаточно прав для удаления приложения");
        }

        if (app.getIconId() != null) {
            try {
                minioService.deleteFile(app.getIconId(), MinioService.ICONS_BUCKET);
            } catch (Exception e) {
                log.warn("Не удалось удалить иконку приложения из Minio: {}", e.getMessage());
            }
        }

        if (app.getFileId() != null) {
            try {
                minioService.deleteFile(app.getFileId(), MinioService.APK_BUCKET);
            } catch (Exception e) {
                log.warn("Не удалось удалить APK файл из Minio: {}", e.getMessage());
            }
        }

        if (app.getScreenshotsIds() != null) {
            for (String screenId : app.getScreenshotsIds()) {
                try {
                    minioService.deleteFile(screenId, MinioService.SCREENSHOTS_BUCKET);
                } catch (Exception e) {
                    log.warn("Не удалось удалить скриншот из Minio: {}", e.getMessage());
                }
            }
        }

        appRepository.delete(app);
    }

    public ResponseEntity<?> downloadApp(Long id) {
        User currentUser = userService.getCurrentUser();
        App app = getAppById(id);

        if (!isFreeOrOwnedOrPurchased(app, currentUser)) {
            throw new PaymentRequiredException("Для скачивания необходимо приобрести приложение");
        }

        app.setDownloads(app.getDownloads() + 1);
        appRepository.save(app);

        return buildDownloadResponse(app);

    }

    private boolean isFreeOrOwnedOrPurchased(App app, User user) {
        return isFreeApp(app) || isAppOwner(app, user) || isAppPurchasedByUser(app, user);
    }

    private boolean isFreeApp(App app) {
        return app.getPrice().compareTo(BigDecimal.ZERO) == 0;
    }

    private boolean isAppOwner(App app, User user) {
        return app.getOwner().getId().equals(user.getId());
    }

    private boolean isAppPurchasedByUser(App app, User user) {
        return purchaseService.hasUserPurchasedApp(user, app);
    }

    private ResponseEntity<?> buildDownloadResponse(App app) {
        String downloadUrl = "http://212.113.102.152:9000/" + MinioService.APK_BUCKET + "/" + app.getFileId();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + app.getName() + ".apk\"")
                .body(downloadUrl);
    }

    private AppDetailsDto convertToAppDetailsDto(App app) {
        return AppDetailsDto.builder()
                .id(app.getId())
                .name(app.getName())
                .description(app.getDescription())
                .iconUrl(app.getIconId() != null ?
                        "http://212.113.102.152:9000/" + MinioService.ICONS_BUCKET + "/" + app.getIconId() : null)
                .screenshotUrls(app.getScreenshotsIds() == null || app.getScreenshotsIds().isEmpty() ?
                        Collections.emptyList() :
                        app.getScreenshotsIds().stream()
                                .filter(Objects::nonNull)
                                .map(id -> "http://212.113.102.152:9000/" + MinioService.SCREENSHOTS_BUCKET + "/" + id)
                                .collect(Collectors.toList()))
                .price(app.getPrice())
                .averageRating(app.getAverageRating())
                .downloads(app.getDownloads())
                .createdAt(app.getCreatedAt())
                .ownerUsername(app.getOwner().getUsername())
                .categories(app.getCategories().stream()
                        .map(Category::getName)
                        .collect(Collectors.toList()))
                .reviews(convertReviewsToDtos(app.getReviews()))
                .build();
    }

    private List<ReviewDTO> convertReviewsToDtos(List<Review> reviews) {
        if (reviews == null) {
            return Collections.emptyList();
        }

        return reviews.stream()
                .map(review -> ReviewDTO.builder()
                        .id(review.getId())
                        .userUsername(review.getUser().getUsername())
                        .rating(review.getRating())
                        .comment(review.getComment())
                        .createdAt(review.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    private AppListDto convertToAppListDto(App app) {
        return AppListDto.builder()
                .id(app.getId())
                .name(app.getName())
                .iconUrl(app.getIconId() != null ?
                        "http://212.113.102.152:9000/" + MinioService.ICONS_BUCKET + "/" + app.getIconId() : null)
                .price(app.getPrice())
                .averageRating(app.getAverageRating())
                .downloads(app.getDownloads())
                .build();
    }
}
