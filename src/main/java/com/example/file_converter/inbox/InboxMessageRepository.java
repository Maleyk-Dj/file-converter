package com.example.file_converter.inbox;

import org.springframework.data.jpa.repository.JpaRepository;

public interface InboxMessageRepository
        extends JpaRepository<InboxMessage, String> {

    boolean existsByMessageIdAndStatus(String messageId, InboxStatus status);
}
