package com.example.file_converter.converter;

import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfReader;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Component
public class ZipToPdfConverter implements FileConverter {
    private final List<FileConverter> converters;

    public ZipToPdfConverter(List<FileConverter> converters) {
        this.converters = converters;
    }

    @Override
    public byte[] convert(byte[] fileBytes, String fileName) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            Document document = new Document();
            PdfCopy copy = new PdfCopy(document, out);
            document.open();

            ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(fileBytes));
            ZipEntry entry;
            while ((entry = zip.getNextEntry()) != null) {
                if (entry.isDirectory()) continue;
                String ext = getExtension(entry.getName());
                byte[] entryBytes = zip.readAllBytes();
                FileConverter converter = converters.stream()
                        .filter(c -> !(c instanceof ZipToPdfConverter))
                        .filter(c -> c.support(ext))
                        .findFirst()
                        .orElse(null);
                if (converter == null) continue;
                byte[] pdf = converter.convert(entryBytes, entry.getName());
                PdfReader reader = new PdfReader(pdf);
                for (int i = 1; i <= reader.getNumberOfPages(); i++) {
                    copy.addPage(copy.getImportedPage(reader, i));
                }
                reader.close();
            }
            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert ZIP to PDF", e);
        }
    }

    @Override
    public boolean support(String extension) {
        return "zip".equalsIgnoreCase(extension);
    }

    private String getExtension(String fileName) {
        int idx = fileName.lastIndexOf('.');
        return idx >= 0 ? fileName.substring(idx + 1) : "";
    }
}
