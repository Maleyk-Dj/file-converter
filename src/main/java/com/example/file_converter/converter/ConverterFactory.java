package com.example.file_converter.converter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ConverterFactory {

    private final List<FileConverter> converters;

    public FileConverter getConverter(String extension) {
        return converters.stream()
                .filter(c -> c.support(extension))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        "No converter found for extension: " + extension));
    }
}
