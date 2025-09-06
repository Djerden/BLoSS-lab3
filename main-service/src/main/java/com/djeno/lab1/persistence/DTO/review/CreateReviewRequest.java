package com.djeno.lab1.persistence.DTO.review;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "Запрос на создание отзыва")
@Data
public class CreateReviewRequest {

    @Schema(description = "ID приложения, к которому относится отзыв", example = "123")
    @NotNull(message = "ID приложения обязательно")
    private Long appId;

    @Schema(description = "Оценка приложения (от 1 до 5 звезд)", example = "5")
    @NotNull(message = "Рейтинг обязателен")
    @Min(value = 1, message = "Рейтинг должен быть от 1 до 5")
    @Max(value = 5, message = "Рейтинг должен быть от 1 до 5")
    private Integer rating;

    @Schema(description = "Текст отзыва (максимум 1000 символов)", example = "Отличное приложение, всем рекомендую!", maxLength = 1000)
    @Size(max = 1000, message = "Комментарий не должен превышать 1000 символов")
    private String comment;
}
