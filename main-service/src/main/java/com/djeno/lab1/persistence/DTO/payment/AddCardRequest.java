package com.djeno.lab1.persistence.DTO.payment;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Schema(description = "Запрос на добавление карты")
@Data
public class AddCardRequest {

    @Schema(description = "Номер карты (16-19 цифр)", example = "4111111111111111")
    @NotBlank
    @Pattern(regexp = "^[0-9]{13,19}$", message = "Номер карты должен содержать 13-19 цифр")
    private String cardNumber;

    @Schema(description = "Имя владельца карты", example = "IVAN IVANOV", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    @Pattern(regexp = "^[A-Z ]*$", message = "Имя владельца должно содержать только латинские заглавные буквы и пробелы")
    @Size(max = 26, message = "Имя владельца не должно превышать 26 символов")
    private String cardHolder;

    @Schema(description = "Срок действия в формате MM/YY", example = "12/25")
    @NotBlank
    @Pattern(regexp = "^(0[1-9]|1[0-2])/[0-9]{2}$", message = "Неверный формат срока действия. Используйте MM/YY")
    private String expirationDate;

    @Schema(description = "CVV/CVC код (3 или 4 цифры)", example = "123")
    @NotBlank(message = "CVV/CVC код обязателен")
    @Pattern(regexp = "^[0-9]{3,4}$", message = "CVV/CVC код должен содержать 3 или 4 цифры")
    private String cvv;
}
