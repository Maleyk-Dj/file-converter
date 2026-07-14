package com.example.file_converter.config;

import com.example.file_converter.model.FileConversionRequest;
import com.example.file_converter.model.FileConversionResult;
import com.example.file_converter.service.ConversionResultService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.ConsumerRecordRecoverer;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Slf4j
@Configuration
public class KafkaConfig {

    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<Object, Object> kafkaTemplate,
                                             ConversionResultService conversionResultService,
                                             ObjectMapper objectMapper,
                                             @Value("${kafka.topics.output}") String outputTopic) {
        DeadLetterPublishingRecoverer dltRecoverer = new DeadLetterPublishingRecoverer(kafkaTemplate);

        // Когда ретраи исчерпаны: оригинал уходит в DLT-топик (для ручного разбора),
        // а в files.output через transactional outbox публикуется FAILED-событие —
        // именно на него реагирует дальнейшая цепочка SAGA, а не на DLT.
        ConsumerRecordRecoverer recoverer = (record, exception) -> {
            dltRecoverer.accept(record, exception);
            try {
                FileConversionRequest request = objectMapper.readValue(
                        (String) record.value(), FileConversionRequest.class);
                FileConversionResult failedResult = FileConversionResult.failed(
                        request.getMessageId(), rootCauseMessage(exception));
                String payload = objectMapper.writeValueAsString(failedResult);
                conversionResultService.markFailedAndSaveOutbox(
                        request.getMessageId(), outputTopic, payload);
            } catch (Exception e) {
                log.error("Не удалось опубликовать событие о неуспехе конвертации: {}", record.value(), e);
            }
        };

        FixedBackOff backOff = new FixedBackOff(2000L, 3L);
        return new DefaultErrorHandler(recoverer, backOff);
    }

    private String rootCauseMessage(Throwable throwable) {
        Throwable rootCause = throwable;
        while (rootCause.getCause() != null) {
            rootCause = rootCause.getCause();
        }
        return rootCause.getMessage();
    }
}
