package com.example.file_converter.converter;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

 class ImageToPdfConverterTest {

    @Test
    void convert_shouldReturnPngToPdf() throws Exception {
        ImageToPdfConverter converter = new ImageToPdfConverter();

        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, 100, 100);
        g.dispose();

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "jpg", baos); // JPG вместо PNG
        byte[] input = baos.toByteArray();

        byte[] result = converter.convert(input, "test.jpg");
        assertNotNull(result);
        assertTrue(new String(result).startsWith("%PDF"));
    }
}
