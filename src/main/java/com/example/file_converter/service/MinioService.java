package com.example.file_converter.service;

import com.example.file_converter.config.MinioConfig;
import com.example.file_converter.config.MinioProperties;
import io.minio.GetObjectArgs;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class MinioService {

    private final MinioConfig config;
    private final MinioProperties properties;
    private final MinioClient minioClient;

    public byte[] download(String bucket, String path) throws Exception {
        byte[] fileBytes;
        try (InputStream inputStream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(bucket)
                        .object(path)
                        .build()
        )) {
            fileBytes = inputStream.readAllBytes();
        }
        return fileBytes;
    }

    public void upload(String bucket, String path, byte[] bytes, String contentType) throws Exception {
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucket)
                        .object(path)
                        .stream(new ByteArrayInputStream(bytes), bytes.length, -1)
                        .contentType(contentType)
                        .build()
        );
    }

    public String presignedDownloadUrl(String bucket, String key) throws Exception {
        return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.GET)
                        .bucket(bucket)
                        .object(key)
                        .expiry(15, TimeUnit.MINUTES)
                        .build()
        );

    }

    public String presignedUploadUrl(String bucket, String key) throws Exception {
        return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .method(Method.PUT)
                        .bucket(bucket)
                        .object(key)
                        .expiry(15, TimeUnit.MINUTES)
                        .build()
        );

    }
}
