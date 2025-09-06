package com.djeno.lab1.services;

import io.minio.*;
import io.minio.http.Method;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class MinioService {
    public final static String ICONS_BUCKET = "icons";
    public final static String SCREENSHOTS_BUCKET = "screenshots";
    public static final String APK_BUCKET = "apk";

    private final MinioClient minioClient;

    public MinioService(@Value("${minio.endpoint}") String endpoint,
                        @Value("${minio.accessKey}") String accessKey,
                        @Value("${minio.secretKey}") String secretKey) {
        this.minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    @SneakyThrows
    public String uploadFile(MultipartFile file, String bucketName) {
        String originalFileName = file.getOriginalFilename();
        String uniqueFileName = UUID.randomUUID().toString() + "_" + originalFileName; // UUID для получения файла

        createBucketIfNotExist(bucketName); // imported-files

        try (InputStream inputStream = file.getInputStream()) {
            Map<String, String> metadata = new HashMap<>();
            metadata.put("filename", originalFileName);

            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucketName)
                    .object(uniqueFileName)
                    .stream(inputStream, file.getSize(), -1)
                    .contentType(file.getContentType())
                    .userMetadata(metadata)
                    .build());
            return uniqueFileName;
        }
    }

    @SneakyThrows
    public InputStream downloadFile(String uniqueFileName, String bucketName) {
        createBucketIfNotExist(bucketName);

        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucketName)
                        .object(uniqueFileName)
                        .build());
    }

    @SneakyThrows
    public void deleteFile(String uniqueFileName, String bucketName) {
        createBucketIfNotExist(bucketName);

        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(bucketName)
                        .object(uniqueFileName)
                        .build());
    }

    @SneakyThrows
    public void createBucketIfNotExist(String bucketName) {
        if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }
    }

    @SneakyThrows
    public String getFileUrl(String fileName, String bucket) throws Exception {
        return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(bucket)
                        .object(fileName)
                        .expiry(3600)
                        .build()
        );
    }
}
