package com.example.file_converter.converter;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class ConverterFactoryTest {

    private ConverterFactory factory;

    @BeforeEach
    void setUp() {
        List<LeafConverter> converters = List.of(
                new TxtToPdfConverter(),
                new ImageToPdfConverter()
        );
        factory = new ConverterFactory(converters);
    }
    @ParameterizedTest
    @MethodSource("provideExtensions")
    void shouldReturnCorrectConverter(String extension, Class<?> expectedType) {
        FileConverter converter = factory.getConverter(extension);
        assertInstanceOf(expectedType, converter);
    }

    static Stream<Arguments> provideExtensions() {
        return Stream.of(
                Arguments.of("txt", TxtToPdfConverter.class),
                Arguments.of("jpg", ImageToPdfConverter.class)
        );
    }

    @Test
    void shouldThrowExceptionForUnknownExtension() {
        assertThrows(RuntimeException.class, () -> factory.getConverter("docx"));
    }
}
