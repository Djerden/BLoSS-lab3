package com.djeno.lab1.persistence.DTO.payment;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Schema(description = "Информация о платежной карте")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentCardDTO {

    @Schema(description = "ID карты", example = "1")
    private Long id;

    @Schema(description = "Маскированный номер карты", example = "**** **** **** 1111")
    private String maskedCardNumber;

    @Schema(description = "Является ли карта основной", example = "true")
    private boolean isPrimary;
}
