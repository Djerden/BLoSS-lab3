package com.djeno.lab1.persistence.DTO.error;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Schema(description = "Error response format")
public class ErrorResponse {
    @Schema(description = "Timestamp when error occurred", example = "2023-05-20T14:30:45.123")
    private LocalDateTime timestamp;

    @Schema(description = "HTTP status code", example = "400")
    private int status;

    @Schema(description = "Error type", example = "Bad Request")
    private String error;

    @Schema(description = "Detailed error message", example = "APK file is required")
    private String message;
}
