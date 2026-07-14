package com.example.file_converter.service;

import com.example.file_converter.inbox.InboxMessageRepository;
import com.example.file_converter.outbox.OutboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConversionResultService {

    private final InboxMessageRepository inboxRepository;
    private final OutboxService outboxService;

    @Transactional
    public void markProcessedAndSaveOutbox(String messageId, String topic, String payload) {
        inboxRepository.findById(messageId).ifPresent(msg -> {
            msg.setStatus("PROCESSED");
            outboxService.save(topic,payload);
            inboxRepository.save(msg);
        });
    }

    // Вызывается из ErrorHandler'а Kafka-контейнера после исчерпания ретраев конвертации:
    // статус в inbox и FAILED-событие в outbox сохраняются в одной транзакции,
    // публикация в files.output идёт через тот же OutboxPoller, что и для успеха.
    @Transactional
    public void markFailedAndSaveOutbox(String messageId, String topic, String payload) {
        inboxRepository.findById(messageId).ifPresent(msg -> {
            msg.setStatus("FAILED");
            outboxService.save(topic, payload);
            inboxRepository.save(msg);
        });
    }
}
