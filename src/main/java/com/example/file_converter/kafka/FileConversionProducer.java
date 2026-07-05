package com.example.file_converter.kafka;

import com.example.file_converter.model.FileConversionResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.ExecutionException;

@Component
@RequiredArgsConstructor
public class FileConversionProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${kafka.topics.output}")
    private String outTopic;
    ObjectMapper objectMapper = new ObjectMapper();

    public void send (FileConversionResult fileConversionResult) throws JsonProcessingException,
            ExecutionException, InterruptedException {

            String result=objectMapper.writeValueAsString(fileConversionResult);
            kafkaTemplate.send(outTopic, result).get();
    }
}
