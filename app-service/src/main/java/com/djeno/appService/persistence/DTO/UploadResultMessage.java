package com.djeno.appService.persistence.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO для получения асинхронного результата от сервиса сохранения приложения
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UploadResultMessage {
    private Long appId;
    private boolean success;
    private String fileId;            // apk object name in minio
    private String iconId;            // icon object name
    private List<String> screenshotsIds;
    private String errorMessage;      // при fail
    private String correlationId;
}
