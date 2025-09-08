package com.djeno.lab1.services;

import com.djeno.lab1.persistence.DTO.statistics.StatisticsDto;
import com.djeno.lab1.persistence.enums.PurchaseStatus;
import com.djeno.lab1.persistence.repositories.AppRepository;
import com.djeno.lab1.persistence.repositories.PurchaseRepository;
import com.djeno.lab1.persistence.repositories.ReviewRepository;
import com.djeno.lab1.persistence.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatisticsService {

    private final AppRepository appRepository;
    private final PurchaseRepository purchaseRepository;
    private final UserRepository userRepository;
    private final ReviewRepository reviewRepository;

    public StatisticsDto collectStatistics() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime yesterday = now.minusDays(1);

        // Общая статистика по приложениям
        long totalApps = appRepository.count();
        long newAppsLastDay = appRepository.countByCreatedAtAfter(yesterday);

        List<StatisticsDto.AppSummary> topAppsByDownloads = appRepository.findTop5ByOrderByDownloadsDesc()
                .stream()
                .map(app -> StatisticsDto.AppSummary.builder()
                        .name(app.getName())
                        .downloads(app.getDownloads())
                        .rating(app.getAverageRating())
                        .build())
                .toList();

        List<StatisticsDto.AppSummary> topAppsByRating = appRepository.findTop5ByOrderByAverageRatingDesc()
                .stream()
                .map(app -> StatisticsDto.AppSummary.builder()
                        .name(app.getName())
                        .downloads(app.getDownloads())
                        .rating(app.getAverageRating())
                        .build())
                .toList();

        // Финансовая статистика
        long totalPurchases = purchaseRepository.countByStatus(PurchaseStatus.PAID);
        BigDecimal totalRevenue = purchaseRepository.sumTotalPriceByStatus(PurchaseStatus.PAID)
                .orElse(BigDecimal.ZERO);
        long newPurchasesLastDay = purchaseRepository.countByStatusAndPurchaseDateAfter(PurchaseStatus.PAID, yesterday);

        List<StatisticsDto.AppSummary> topAppsByRevenue = purchaseRepository.findTopAppsByRevenue(PageRequest.of(0, 5))
                .stream()
                .map(obj -> StatisticsDto.AppSummary.builder()
                        .name((String) obj[0])
                        .revenue((BigDecimal) obj[1])
                        .build())
                .toList();

        // Пользователи
        long totalUsers = userRepository.count();
        long newUsersLastDay = userRepository.countByCreatedAtAfter(yesterday);

        List<StatisticsDto.UserSummary> topBuyers = purchaseRepository.findTopUsersByPurchaseCount(PageRequest.of(0, 5))
                .stream()
                .map(obj -> StatisticsDto.UserSummary.builder()
                        .username((String) obj[0])
                        .purchasesCount((Long) obj[1])
                        .build())
                .toList();

        // Отзывы
        long totalReviews = reviewRepository.count();
        double averageRating = reviewRepository.getAverageRating().orElse(0.0);
        long newReviewsLastDay = reviewRepository.countByCreatedAtAfter(yesterday);

        List<StatisticsDto.AppSummary> topAppsByReviews = reviewRepository.findTopAppsByReviewCount(5)
                .stream()
                .map(obj -> StatisticsDto.AppSummary.builder()
                        .name((String) obj[0])
                        .reviews((Long) obj[1])
                        .build())
                .toList();

        return StatisticsDto.builder()
                .totalApps(totalApps)
                .newAppsLastDay(newAppsLastDay)
                .topAppsByDownloads(topAppsByDownloads)
                .topAppsByRating(topAppsByRating)
                .totalPurchases(totalPurchases)
                .totalRevenue(totalRevenue)
                .newPurchasesLastDay(newPurchasesLastDay)
                .topAppsByRevenue(topAppsByRevenue)
                .totalUsers(totalUsers)
                .newUsersLastDay(newUsersLastDay)
                .topBuyers(topBuyers)
                .totalReviews(totalReviews)
                .averageRating(averageRating)
                .newReviewsLastDay(newReviewsLastDay)
                .topAppsByReviews(topAppsByReviews)
                .build();
    }
}
