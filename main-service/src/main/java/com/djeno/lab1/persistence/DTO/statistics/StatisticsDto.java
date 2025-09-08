package com.djeno.lab1.persistence.DTO.statistics;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class StatisticsDto {
    private long totalApps;
    private long newAppsLastDay;
    private List<AppSummary> topAppsByDownloads;
    private List<AppSummary> topAppsByRating;

    private long totalPurchases;
    private BigDecimal totalRevenue;
    private long newPurchasesLastDay;
    private List<AppSummary> topAppsByRevenue;

    private long totalUsers;
    private long newUsersLastDay;
    private List<UserSummary> topBuyers;

    private long totalReviews;
    private double averageRating;
    private long newReviewsLastDay;
    private List<AppSummary> topAppsByReviews;

    @Data
    @Builder
    public static class AppSummary {
        private String name;
        private long downloads;
        private double rating;
        private BigDecimal revenue;
        private long reviews;
    }

    @Data
    @Builder
    public static class UserSummary {
        private String username;
        private long purchasesCount;
    }
}
