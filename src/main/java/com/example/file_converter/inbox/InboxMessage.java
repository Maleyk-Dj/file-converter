package com.example.file_converter.inbox;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "inbox-messages")
public class InboxMessage {
    @Id
    private String messageId;
    private String status; //RECEIVED, PROCESSED
    private LocalDateTime createdAt;
}
