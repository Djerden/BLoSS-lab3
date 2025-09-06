package com.djeno.lab1.persistence.DTO.app;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Schema(description = "Краткая информация о приложении для списка")
@Builder
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppListDto {
    @Schema(description = "ID приложения", example = "1")
    private Long id;

    @Schema(description = "Название приложения", example = "My Awesome App")
    private String name;

    @Schema(description = "URL иконки приложения", example = "https://example.com/icon.png")
    private String iconUrl;

    @Schema(description = "Цена приложения", example = "4.99")
    private BigDecimal price;

    @Schema(description = "Средний рейтинг", example = "4.5")
    private double averageRating;

    @Schema(description = "Количество загрузок", example = "1000")
    private int downloads;
}
