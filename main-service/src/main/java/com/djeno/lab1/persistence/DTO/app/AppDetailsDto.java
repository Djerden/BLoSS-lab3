package com.djeno.lab1.persistence.DTO.app;

import com.djeno.lab1.persistence.DTO.review.ReviewDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Полная информация о приложении")
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppDetailsDto {

    @Schema(description = "ID приложения", example = "1")
    private Long id;

    @Schema(description = "Название приложения", example = "My Awesome App")
    private String name;

    @Schema(description = "Описание приложения", example = "Это лучшее приложение в мире")
    private String description;

    @Schema(description = "URL иконки приложения", example = "https://example.com/icon.png")
    private String iconUrl;

    @Schema(description = "Список URL скриншотов",
            example = "[\"https://example.com/screen1.png\", \"https://example.com/screen2.png\"]")
    private List<String> screenshotUrls;

    @Schema(description = "Цена приложения", example = "4.99")
    private BigDecimal price;

    @Schema(description = "Средний рейтинг", example = "4.5")
    private double averageRating;

    @Schema(description = "Количество загрузок", example = "1000")
    private int downloads;

    @Schema(description = "Дата создания", example = "2023-05-20T14:30:45")
    private LocalDateTime createdAt;

    @Schema(description = "Имя владельца", example = "developer123")
    private String ownerUsername;

    @Schema(description = "Список категорий", example = "[\"Игры\", \"Образование\"]")
    private List<String> categories;

    @Schema(description = "Список отзывов")
    private List<ReviewDTO> reviews;
}
