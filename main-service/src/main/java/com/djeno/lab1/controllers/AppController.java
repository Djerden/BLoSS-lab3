package com.djeno.lab1.controllers;

import com.djeno.lab1.persistence.DTO.app.AppDetailsDto;
import com.djeno.lab1.persistence.DTO.app.AppListDto;
import com.djeno.lab1.persistence.DTO.app.CreateAppRequest;
import com.djeno.lab1.persistence.DTO.error.ErrorResponse;
import com.djeno.lab1.persistence.enums.AppStatus;
import com.djeno.lab1.persistence.models.App;
import com.djeno.lab1.persistence.models.Purchase;
import com.djeno.lab1.persistence.models.User;
import com.djeno.lab1.services.AppService;
import com.djeno.lab1.services.PurchaseService;
import com.djeno.lab1.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;

@Tag(name = "Взаимодействие с приложениями")
@RequiredArgsConstructor
@RequestMapping("/apps")
@RestController
public class AppController {

    private final UserService userService;
    private final AppService appService;
    private final PurchaseService purchaseService;

    @Operation(
            summary = "Получение списка оплаченных приложений пользователя",
            description = "Возвращает пагинированный список приложений, которые пользователь приобрел",
            parameters = {
                    @Parameter(name = "page", description = "Номер страницы", example = "0"),
                    @Parameter(name = "size", description = "Размер страницы", example = "10"),
                    @Parameter(name = "sort", description = "Поле сортировки (name,price,downloads)", example = "name,asc")
            }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список оплаченных приложений получен",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "400", description = "Неверные параметры запроса"),
            @ApiResponse(responseCode = "403", description = "Нет прав для просмотра списка оплаченных приложений")
    })
    @PreAuthorize("hasAuthority('VIEW_PURCHASED_APP_LIST')")
    @GetMapping("/purchased")
    public ResponseEntity<Page<AppListDto>> getPurchasedApps(
            @PageableDefault(size = 10) Pageable pageable
    ) {
        User currentUser = userService.getCurrentUser();
        Page<Purchase> purchasesPage = purchaseService.getPurchasesByUser(currentUser, pageable);
        Page<AppListDto> appsPage = appService.getPurchasedApps(currentUser, pageable);
        return ResponseEntity.ok(appsPage);
    }

    @Operation(
            summary = "Загрузка нового приложения",
            description = "Позволяет разработчику загрузить новое приложение с метаданными, иконкой, скриншотами и APK/файлом",
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Успешная загрузка"),
            @ApiResponse(responseCode = "400", description = "Невалидные данные"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен")
    })
    @PreAuthorize("hasAuthority('PUBLISH_APP')")
    @PostMapping( consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> publishApp(
            @Parameter(description = "Метаданные приложения в формате JSON", required = true)
            @RequestPart("appData") CreateAppRequest appData,

            @Parameter(description = "APK-файл", required = true)
            @RequestPart("file") MultipartFile file,

            @Parameter(description = "Иконка приложения (опционально)")
            @RequestPart(name = "icon", required = false) MultipartFile icon,

            @Parameter(description = "Скриншоты (опционально)")
            @RequestPart(name = "screenshots", required = false) List<MultipartFile> screenshots
    ) {
        if (appData.getName() == null || appData.getName().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Название приложения обязательно");
        }

        if (appData.getDescription() == null || appData.getDescription().trim().isEmpty()) {
            return ResponseEntity.badRequest().body("Описание приложения обязательно");
        }

        if (appData.getPrice() == null) {
            return ResponseEntity.badRequest().body("Цена приложения обязательна, укажите 0, если бесплатное");
        }

        if (appData.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            return ResponseEntity.badRequest().body("Цена не может быть отрицательной");
        }

        if (file == null || file.isEmpty()) {
            return ResponseEntity.badRequest().body("APK-файл обязателен");
        }

        App savedApp = appService.createApp(appData, icon, file, screenshots);
        return ResponseEntity.ok("Приложение отправлено на сохранение, id приложения для отслеживания: " + savedApp.getId());
    }

    @PreAuthorize("hasAuthority('CHECK_LOADING_STATUS_APP')")
    @GetMapping("/{id}/status")
    public ResponseEntity<String> getUploadStatus(@PathVariable Long id) {
        AppStatus status = appService.getStatus(id);
        return ResponseEntity.ok(status.name().toLowerCase());
    }

    @Operation(
            summary = "Получение списка приложений",
            description = "Возвращает пагинированный список приложений с возможностью фильтрации по категории",
            parameters = {
                    @Parameter(name = "categoryId", description = "ID категории для фильтрации", example = "1"),
                    @Parameter(name = "page", description = "Номер страницы", example = "0"),
                    @Parameter(name = "size", description = "Размер страницы", example = "10"),
                    @Parameter(name = "sort", description = "Поле сортировки (name,price,downloads)", example = "name,asc")
            }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Список приложений получен",
                    content = @Content(schema = @Schema(implementation = Page.class))),
            @ApiResponse(responseCode = "400", description = "Неверные параметры запроса")
    })
    @GetMapping()
    public ResponseEntity<Page<AppListDto>> getApps(
            @RequestParam(required = false) Long categoryId,
            @PageableDefault(size = 10) Pageable pageable
    ) {

        Page<AppListDto> apps = appService.getApps(categoryId, pageable);
        return ResponseEntity.ok(apps);
    }

    @Operation(
            summary = "Получение всей информации по приложению",
            description = "Возвращает полную информацию о приложении по его ID",
            parameters = {
                    @Parameter(name = "id", description = "ID приложения", required = true, example = "1")
            }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Детали приложения получены",
                    content = @Content(schema = @Schema(implementation = AppDetailsDto.class))),
            @ApiResponse(responseCode = "404", description = "Приложение не найдено")
    })
    @GetMapping("/{id}")
    public ResponseEntity<AppDetailsDto> getAppDetails(@PathVariable Long id) {
        AppDetailsDto appDetails = appService.getAppDetails(id);
        return ResponseEntity.ok(appDetails);
    }

    @Operation(
            summary = "Удалить приложение",
            description = "Удаляет приложение по ID (требуются права DEVELOPER или ADMIN)",
            security = @SecurityRequirement(name = "bearerAuth"),
            parameters = {
                    @Parameter(name = "id", description = "ID приложения", required = true, example = "1")
            }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Приложение успешно удалено"),
            @ApiResponse(responseCode = "403", description = "Доступ запрещен"),
            @ApiResponse(responseCode = "404", description = "Приложение не найдено")
    })
    @PreAuthorize("hasAuthority('DELETE_APP')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteApp(@PathVariable Long id) {
        appService.deleteApp(id);
        return ResponseEntity.ok("Приложение успешно удалено");
    }

    @Operation(
            summary = "Скачать приложение",
            description = "Скачивает APK-файл приложения по ID",
            parameters = {
                    @Parameter(name = "id", description = "ID приложения", required = true, example = "1")
            }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Файл приложения",
                    content = @Content(schema = @Schema(type = "string", format = "binary"))),
            @ApiResponse(responseCode = "404", description = "Файл не найден"),
            @ApiResponse(responseCode = "402", description = "Приложение не оплачено")
    })
    @PreAuthorize("hasAuthority('DOWNLOAD_APP')")
    @GetMapping("/{id}/download")
    public ResponseEntity<?> downloadApp(@PathVariable Long id) {
        return appService.downloadApp(id);
    }

    @Operation(
            summary = "Оплатить приложение",
            description = "Совершает покупку приложения по ID для текущего пользователя",
            security = @SecurityRequirement(name = "bearerAuth"),
            parameters = {
                    @Parameter(name = "id", description = "ID приложения", required = true, example = "1")
            }
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Приложение успешно оплачено"),
            @ApiResponse(responseCode = "400", description = "Приложение уже куплено"),
            @ApiResponse(responseCode = "404", description = "Приложение не найдено"),
            @ApiResponse(responseCode = "402", description = "Недостаточно средств")
    })
    @PreAuthorize("hasAuthority('PURCHASE_APP')")
    @PostMapping("/{id}/purchase")
    public ResponseEntity<String> purchaseApp(@PathVariable Long id) {
        App app = appService.getAppById(id);
        User user = userService.getCurrentUser();
        Purchase purchase = purchaseService.purchaseApp(app, user);
        return ResponseEntity.ok("Приложение было успешно оплачено");
    }
}
