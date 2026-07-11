package com.example.file_converter.converter;

import com.lowagie.text.Document;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.springframework.stereotype.Component;

@Component
public class TxtToPdfConverter implements LeafConverter {

    private static final String TXT = "txt";

    @Override
    public byte[] convert(byte[] fileBytes, String fileName) {
        try {
            Document document = new Document();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            PdfWriter.getInstance(document, out);
            document.open();
            String text = new String(fileBytes);
            document.add(new Paragraph(text));
            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Не удалось конвертировать TXT в PDF.", e);
        }
    }

    @Override
    public boolean support(String extension) {
        return TXT.equalsIgnoreCase(extension);
    }
}
