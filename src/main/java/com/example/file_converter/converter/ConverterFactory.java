package com.example.file_converter.converter;

import com.example.file_converter.exception.FileConversionException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ConverterFactory {

    private final List<LeafConverter> converters;

    public FileConverter getConverter(String extension) {
        return converters.stream()
                .filter(c -> c.support(extension))
                .findFirst()
                .orElseThrow(() -> new FileConversionException(
                        "Конвертер для расширения не найден: " + extension));
    }

    public Optional <LeafConverter> findConverter (String extension) {
        return converters.stream()
                .filter(c -> c.support(extension))
                .findFirst();
    }
}
