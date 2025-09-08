package com.djeno.lab1.persistence.DTO.app;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppUploadMessage {
    private Long appId;
    private Long ownerId;
    private CreateAppRequest appData;
    private String iconBase64;
    private String iconOriginalName;
    private String apkBase64;
    private String apkOriginalName;
    private List<FilePart> screenshots;
    private String correlationId;
}
