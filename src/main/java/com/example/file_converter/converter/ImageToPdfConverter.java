package com.example.file_converter.converter;

import com.lowagie.text.Document;
import com.lowagie.text.Image;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Component;

import java.io.ByteArrayOutputStream;
import java.util.Set;

@Component
public class ImageToPdfConverter implements FileConverter {

    private static final Set<String> SUPPORTED = Set.of("png", "jpg", "jpeg");

    @Override
    public byte[] convert(byte[] fileBytes, String fileName) {
        try {
            Document document = new Document();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, out);
            document.open();
            Image image = Image.getInstance(fileBytes);
            image.scaleToFit(document.getPageSize().getWidth(),
                    document.getPageSize().getHeight());
            document.add(image);
            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert image to PDF", e);
        }
    }

    @Override
    public boolean support(String extension) {
        return SUPPORTED.contains(extension.toLowerCase());
    }
}
