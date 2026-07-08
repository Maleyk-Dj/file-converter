package com.example.file_converter.inbox;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class InboxService {

    private final InboxMessageRepository repository;

    @Transactional
    public Boolean isAlreadyProcessed(String messageId) {
        return repository.existsByMessageIdAndStatus(messageId,"PROCESSED");
    }

    @Transactional
    public void saveReceived (String messageId) {
        if (repository.existsById(messageId)) {
            return;
        }
        InboxMessage inboxMessage = new InboxMessage();
        inboxMessage.setMessageId(messageId);
        inboxMessage.setStatus("RECEIVED");
        inboxMessage.setCreatedAt(LocalDateTime.now());
        repository.save(inboxMessage);
    }

    @Transactional
    public void markProcessed(String messageId) {
        repository.findById(messageId).ifPresent(msg->{
            msg.setStatus("PROCESSED");
            repository.save(msg);
        });
    }
}
