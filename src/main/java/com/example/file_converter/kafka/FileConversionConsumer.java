package com.example.file_converter.kafka;

import com.example.file_converter.inbox.InboxService;
import com.example.file_converter.model.FileConversionRequest;
import com.example.file_converter.model.FileConversionResult;
import com.example.file_converter.service.ConversionResultService;
import com.example.file_converter.service.ConversionService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class FileConversionConsumer {

    @Value("${kafka.topics.output}")
    private String outputTopic;
    private final InboxService inboxService;
    private final ConversionService conversionService;
    private final ObjectMapper objectMapper;
    private final ConversionResultService conversionResultService;

    @KafkaListener(topics = "${kafka.topics.input}", groupId = "${spring.kafka.consumer.group-id}")
    public void consumer(String message, Acknowledgment ack) {
        try {
            FileConversionRequest request = objectMapper.readValue(message, FileConversionRequest.class);
            if (inboxService.isAlreadyProcessed(request.getMessageId())) {
                log.info("Дубль сообщения, пропускаю: {}", request.getMessageId());
                ack.acknowledge();
                return;
            }
            inboxService.saveReceived(request.getMessageId());
            FileConversionResult result = conversionService.convert(request);
            String payload = objectMapper.writeValueAsString(result);
            conversionResultService.markProcessedAndSaveOutbox(
                    request.getMessageId(),
                    outputTopic,
                    payload
            );
            ack.acknowledge();
        } catch (JsonProcessingException e) {
            log.error("Неверный формат сообщения, пропускаю: {}", message, e);
            ack.acknowledge();
        } catch (Exception e) {
            log.error("Не удалось обработать сообщение, попробуем снова: {}", message, e);
            throw new RuntimeException(e);
        }
    }
}
