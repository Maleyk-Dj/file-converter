package com.example.file_converter;

import com.example.file_converter.inbox.InboxMessage;
import com.example.file_converter.inbox.InboxMessageRepository;
import com.example.file_converter.inbox.InboxStatus;
import com.example.file_converter.outbox.OutboxMessage;
import com.example.file_converter.outbox.OutboxMessageRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
public class IdempotencyIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private InboxMessageRepository inboxRepository;

    @Autowired
    private OutboxMessageRepository outboxRepository;

    @BeforeEach
    void setUp() {
        outboxRepository.deleteAll();
        inboxRepository.deleteAll();
    }

    @Test
    void shouldSKipDuplicateMessage() throws Exception {
        InboxMessage msg = new InboxMessage();
        msg.setMessageId("duplicate-001");
        msg.setStatus(InboxStatus.PROCESSED);
        msg.setCreatedAt(LocalDateTime.now());
        inboxRepository.save(msg);

        String message = """
                {
                    "messageId": "duplicate-001",
                    "bucket": "source-files",
                    "filePath": "test.txt"
                }
                """;

        kafkaTemplate.send("files.input", message);

        Thread.sleep(5000);

        List<OutboxMessage> outboxMessages = outboxRepository.findAll();
        assertTrue(outboxMessages.isEmpty());

        InboxMessage inboxMessage = inboxRepository.findById("duplicate-001").orElseThrow();
        assertEquals(InboxStatus.PROCESSED, inboxMessage.getStatus());
    }
}
