package com.example.file_converter;

import com.example.file_converter.inbox.InboxMessage;
import com.example.file_converter.inbox.InboxMessageRepository;
import com.example.file_converter.inbox.InboxStatus;
import com.example.file_converter.outbox.OutboxMessage;
import com.example.file_converter.outbox.OutboxMessageRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.jdbc.Sql;

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

    @Test
    @Sql(scripts = "/test-data/idempotency-setup.sql",
            executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
    @Sql(scripts = "/test-data/cleanup.sql",
            executionPhase = Sql.ExecutionPhase.AFTER_TEST_CLASS)
    void shouldSKipDuplicateMessage() throws Exception {
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
