package com.example.file_converter.service;

import com.example.file_converter.config.MinioProperties;
import com.example.file_converter.converter.ConverterFactory;
import com.example.file_converter.converter.FileConverter;
import com.example.file_converter.exception.FileConversionException;
import com.example.file_converter.model.FileConversionRequest;
import com.example.file_converter.model.FileConversionResult;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

@Service
@RequiredArgsConstructor
public class ConversionService {

    private final MinioClient minioClient;
    private final ConverterFactory converterFactory;
    private final MinioProperties minioProperties;

    public FileConversionResult convert(FileConversionRequest request) {
        try {
            InputStream inputStream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(request.getBucket())
                            .object(request.getFilePath())
                            .build()
            );
            byte[] fileBytes = inputStream.readAllBytes();

            int index = request.getFilePath().lastIndexOf('.');
            String extension = request.getFilePath().substring(index + 1);

            FileConverter converter = converterFactory.getConverter(extension);
            byte[] pdfBytes = converter.convert(fileBytes, request.getFilePath());

            String pdfFileName = request.getFilePath().substring(0, index) + ".pdf";

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(minioProperties.getConvertedBucket())
                            .object(pdfFileName)
                            .stream(new ByteArrayInputStream(pdfBytes), pdfBytes.length, -1)
                            .contentType("application/pdf")
                            .build()
            );

            return new FileConversionResult(
                    request.getMessageId(),
                    minioProperties.getConvertedBucket(),
                    pdfFileName
            );
        } catch (Exception e) {
            throw new FileConversionException("Не удалось преобразовать файл: " + request.getFilePath(), e);
        }
    }
}
