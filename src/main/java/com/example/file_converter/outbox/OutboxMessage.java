package com.example.file_converter.outbox;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "outbox_messages")
public class OutboxMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String topic;
    private String payload;
    private OutboxStatus status;
    private LocalDateTime createdAt;

}
