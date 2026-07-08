package com.example.file_converter.inbox;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Testcontainers
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class InboxMessageRepositoryTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private InboxMessageRepository repository;

    @Test
    void existsByMessageIdAndStatus_shouldReturnTrueOnlyForMatchingStatus() {
        InboxMessage msg = new InboxMessage();
        msg.setMessageId("test-123");
        msg.setStatus("PROCESSED");
        msg.setCreatedAt(LocalDateTime.now());
        repository.save(msg);

        assertTrue(repository.existsByMessageIdAndStatus("test-123", "PROCESSED"));
        assertFalse(repository.existsByMessageIdAndStatus("test-123", "RECEIVED"));
        assertFalse(repository.existsByMessageIdAndStatus("unknown", "PROCESSED"));
    }
}