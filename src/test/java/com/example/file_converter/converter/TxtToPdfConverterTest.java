package com.example.file_converter.converter;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class TxtToPdfConverterTest {

    @Test
    void convert_shouldReturnPdf() {
        TxtToPdfConverter converter = new TxtToPdfConverter();
        byte[] input = "Hello World".getBytes();
        byte[] result = converter.convert(input, "test.txt");

        assertNotNull(result);
        assertTrue(new String(result).startsWith("%PDF"));
    }
}
