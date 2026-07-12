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
}
