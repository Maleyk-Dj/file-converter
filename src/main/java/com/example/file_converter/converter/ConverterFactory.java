package com.example.file_converter.converter;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ConverterFactory {

    private final List<LeafConverter> converters;

    public FileConverter getConverter(String extension) {
        return converters.stream()
                .filter(c -> c.support(extension))
                .findFirst()
                .orElseThrow(() -> new RuntimeException(
                        "Конвертер для расширения не найден: " + extension));
    }
}
