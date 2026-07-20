package com.example.file_converter.inbox;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "inbox_messages")
public class InboxMessage {
    @Id
    private String messageId;
    private InboxStatus status;
    private LocalDateTime createdAt;
}
