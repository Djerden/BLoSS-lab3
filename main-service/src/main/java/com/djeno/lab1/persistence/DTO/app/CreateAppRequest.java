package com.djeno.lab1.persistence.DTO.app;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Модель запроса для создания приложения")
@Data
public class CreateAppRequest {

    @Schema(
            description = "Название приложения",
            example = "My Awesome App",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank
    private String name;

    @Schema(
            description = "Описание приложения",
            example = "Это лучшее приложение в мире",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotBlank
    private String description;

    @Schema(
            description = "Цена приложения",
            example = "4.99",
            requiredMode = Schema.RequiredMode.REQUIRED
    )
    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal price;

    @Schema(
            description = "Список ID категорий приложения",
            example = "[1, 2, 3]"
    )
    private List<Long> categoryIds;
}
