package com.example.file_converter.outbox;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxMessageRepository
        extends JpaRepository<OutboxMessage, Long> {
    List<OutboxMessage> findAllByStatus(String status);

}
