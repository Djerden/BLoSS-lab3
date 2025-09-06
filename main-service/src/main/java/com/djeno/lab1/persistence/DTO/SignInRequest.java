package com.djeno.lab1.persistence.DTO;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "Запрос на аутентификацию")
public class SignInRequest {

    @Schema(description = "Имя пользователя", example = "Jon")
    @Size(max = 255, message = "Имя пользователя или почта может содержать до 255 символов")
    @NotBlank(message = "Имя пользователя не может быть пустыми")
    private String usernameOrEmail;

    @Schema(description = "Пароль", example = "my_1secret1_password")
    @Size(max = 255, message = "Длина пароля может быть до 255 символов")
    @NotBlank(message = "Пароль не может быть пустыми")
    private String password;
}