package com.example.file_converter.model;

import lombok.Data;

@Data
public class FileConversionRequest {
    String messageId;
    String bucket;
    String filePath;
}
