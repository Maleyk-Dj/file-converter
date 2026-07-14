package com.example.file_converter.converter;

import com.lowagie.text.Document;
import com.lowagie.text.pdf.PdfCopy;
import com.lowagie.text.pdf.PdfReader;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.Optional;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Component
public class ZipToPdfConverter implements FileConverter {

    private final ConverterFactory converterFactory;

    public ZipToPdfConverter(ConverterFactory converterFactory) {
        this.converterFactory = converterFactory;
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
                Optional<LeafConverter> converter = converterFactory.findConverter(ext);
                if (converter.isEmpty()) continue;
                byte[] pdf = converter.get().convert(entryBytes, entry.getName());
                PdfReader reader = new PdfReader(pdf);
                for (int i = 1; i <= reader.getNumberOfPages(); i++) {
                    copy.addPage(copy.getImportedPage(reader, i));
                }
                reader.close();
            }
            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Не удалось конвертировать ZIP в PDF", e);
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
