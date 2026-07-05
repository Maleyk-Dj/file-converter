package com.example.file_converter.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileConversionResult {
    String originalMessageId;
    String bucket;
    String pdfPath;
}
