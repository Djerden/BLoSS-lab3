package com.djeno.appService.listeners;

import com.djeno.appService.persistence.DTO.AppUploadMessage;
import com.djeno.appService.persistence.DTO.FilePart;
import com.djeno.appService.persistence.DTO.UploadResultMessage;
import com.djeno.appService.services.MinioService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UploadListener {

    private final MinioService minioService;
    private final ObjectMapper objectMapper;
    private final JmsTemplate jmsTemplate;

    @JmsListener(destination = "app.upload")
    public void onAppUpload(String payload, @Header("correlationId") String correlationId) {
        try {
            AppUploadMessage msg = objectMapper.readValue(payload, AppUploadMessage.class);
            Long appId = msg.getAppId();

            // сохраняем icon (если есть)
            String iconObjectName = null;
            if (msg.getIconBase64() != null) {
                byte[] iconBytes = Base64.getDecoder().decode(msg.getIconBase64());
                String uniqueName = UUID.randomUUID().toString() + "_" + msg.getIconOriginalName();
                minioService.uploadBytes(iconBytes, uniqueName, MinioService.ICONS_BUCKET, msg.getIconOriginalName());
                iconObjectName = uniqueName;
            }

            // сохраняем apk
            String apkObjectName = null;
            try {
                byte[] apkBytes = Base64.getDecoder().decode(msg.getApkBase64());
                String uniqueName = UUID.randomUUID().toString() + "_" + msg.getApkOriginalName();
                minioService.uploadBytes(apkBytes, uniqueName, MinioService.APK_BUCKET, msg.getApkOriginalName());
                apkObjectName = uniqueName;
            } catch (Exception e) {
                // при неудаче отправим FAIL
                sendResult(appId, false, null, null, null, "Failed to upload apk: " + e.getMessage(), msg.getCorrelationId());
                return;
            }

            // скриншоты
            List<String> screenshotsIds = new ArrayList<>();
            if (msg.getScreenshots() != null) {
                for (FilePart fp : msg.getScreenshots()) {
                    byte[] b = Base64.getDecoder().decode(fp.getBase64());
                    String uniqueName = UUID.randomUUID().toString() + "_" + fp.getFilename();
                    minioService.uploadBytes(b, uniqueName, MinioService.SCREENSHOTS_BUCKET, fp.getFilename());
                    screenshotsIds.add(uniqueName);
                }
            }

            // все успешно
            sendResult(appId, true, apkObjectName, iconObjectName, screenshotsIds, null, msg.getCorrelationId());
        } catch (Exception e) {
            log.error("Failed to process upload message", e);
            // если парсинг/декодирование упал и нет appId — некуда вернуть. В остальных случаях — отправляем fail
            try {
                AppUploadMessage msg = objectMapper.readValue(payload, AppUploadMessage.class);
                sendResult(msg.getAppId(), false, null, null, null, e.getMessage(), msg.getCorrelationId());
            } catch (Exception ex) {
                log.error("Also failed to send fail-result: {}", ex.getMessage());
            }
        }
    }

    private void sendResult(Long appId, boolean success,
                            String fileId, String iconId, List<String> screenshotsIds,
                            String error, String correlationId) {
        try {
            UploadResultMessage res = new UploadResultMessage();
            res.setAppId(appId);
            res.setSuccess(success);
            res.setFileId(fileId);
            res.setIconId(iconId);
            res.setScreenshotsIds(screenshotsIds);
            res.setErrorMessage(error);
            res.setCorrelationId(correlationId);

            String payload = objectMapper.writeValueAsString(res);
            jmsTemplate.convertAndSend("app.upload.result", payload, m -> {
                if (correlationId != null) m.setStringProperty("correlationId", correlationId);
                return m;
            });
        } catch (JsonProcessingException ex) {
            log.error("Failed to serialise upload result", ex);
        }
    }
}
