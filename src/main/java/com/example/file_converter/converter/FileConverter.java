package com.example.file_converter.converter;

public interface FileConverter {

     byte[] convert(byte [] fileBytes, String fileName);

     boolean support (String extension);
}
