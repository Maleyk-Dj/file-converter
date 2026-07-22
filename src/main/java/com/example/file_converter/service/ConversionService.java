package com.example.file_converter.service;

import com.example.file_converter.config.MinioProperties;
import com.example.file_converter.converter.FileConverter;
import com.example.file_converter.exception.FileConversionException;
import com.example.file_converter.model.FileConversionRequest;
import com.example.file_converter.model.FileConversionResult;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ConversionService {

    private final List<FileConverter> converters;
    private final MinioProperties minioProperties;
    private final MinioService minioService;

    public FileConversionResult convert(FileConversionRequest request) {
        try {
            int index = request.getFilePath().lastIndexOf('.');
            if (index <= 0) {
                throw new FileConversionException("У файла нет расширения: " + request.getFilePath());
            }

            byte[] fileBytes = minioService.download(request.getBucket(), request.getFilePath());

            String extension = request.getFilePath().substring(index + 1);

            FileConverter converter = converters.stream()
                    .filter(c -> c.support(extension))
                    .findFirst()
                    .orElseThrow(() -> new FileConversionException(
                            "Конвертер для расширения не найден: " + extension));
            byte[] pdfBytes = converter.convert(fileBytes, request.getFilePath());

            String pdfFileName = request.getFilePath().substring(0, index) + ".pdf";

            minioService.upload(minioProperties.getConvertedBucket(), pdfFileName,
                    pdfBytes, "application/pdf");

            return FileConversionResult.success(
                    request.getMessageId(),
                    minioProperties.getConvertedBucket(),
                    pdfFileName
            );
        } catch (Exception e) {
            throw new FileConversionException("Не удалось преобразовать файл: " + request.getFilePath(), e);
        }
    }
}
