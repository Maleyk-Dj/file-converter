package com.example.file_converter.outbox;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OutboxService {

    private final OutboxMessageRepository repository;

    @Transactional
    public void save(String topic, String payload) {
        OutboxMessage outboxMessage = new OutboxMessage();
        outboxMessage.setTopic(topic);
        outboxMessage.setPayload(payload);
        outboxMessage.setStatus("NEW");
        outboxMessage.setCreatedAt(LocalDateTime.now());
        repository.save(outboxMessage);
    }

    @Transactional(readOnly = true)
    public List<OutboxMessage> findAll() {
        return repository.findAllByStatus("NEW");
    }

    @Transactional
    public void markSent(Long id) {
        repository.findById(id).ifPresent(outboxMessage -> {
            outboxMessage.setStatus("SENT");
            repository.save(outboxMessage);
        });
    }
}
