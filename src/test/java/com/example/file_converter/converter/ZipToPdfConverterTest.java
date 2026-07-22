package com.example.file_converter.converter;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

 class ZipToPdfConverterTest {

    @Test
    void convert_shouldReturnPdfFromZip() throws Exception {

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zip = new ZipOutputStream(baos);

        zip.putNextEntry(new ZipEntry("hello.txt"));
        zip.write("Hello World".getBytes());
        zip.closeEntry();
        zip.close();

        byte[] zipBytes = baos.toByteArray();
        List<LeafConverter> converters = List.of(
                new TxtToPdfConverter(),
                new ImageToPdfConverter()
        );
        ZipToPdfConverter converter = new ZipToPdfConverter(new ConverterFactory(converters));

        byte[] result = converter.convert(zipBytes, "test.zip");

        assertNotNull(result);
        assertTrue(result.length > 0);
        assertTrue(new String(result).startsWith("%PDF"));
    }
}
