package com.example.file_converter;

import com.example.file_converter.inbox.InboxMessageRepository;
import com.example.file_converter.inbox.InboxStatus;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.ByteArrayInputStream;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
class FileConversionIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private InboxMessageRepository inboxMessageRepository;

    private MinioClient minioClient;

    @BeforeEach
    void setUp() throws Exception {
        minioClient = MinioClient.builder()
                .endpoint("http://" + minio.getHost() + ":" + minio.getMappedPort(9000))
                .credentials("minioadmin", "minioadmin")
                .build();

        if (!minioClient.bucketExists(BucketExistsArgs.builder()
                .bucket("source-files").build())) {
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket("source-files").build());
        }
        if (!minioClient.bucketExists(BucketExistsArgs.builder()
                .bucket("converted-files").build())) {
            minioClient.makeBucket(MakeBucketArgs.builder()
                    .bucket("converted-files").build());
        }
    }

    @Test
    void happyPath_shouldConvertFileAndUpdateInbox() throws Exception {
        byte[] content = "Hello Integration Test".getBytes();
        minioClient.putObject(PutObjectArgs.builder()
                .bucket("source-files")
                .object("integration-test.txt")
                .stream(new ByteArrayInputStream(content), content.length, -1)
                .contentType("text/plain")
                .build());

        String message = """
                {
                    "messageId": "integration-001",
                    "bucket": "source-files",
                    "filePath": "integration-test.txt"
                }
                """;
        kafkaTemplate.send("files.input", message);

        await().atMost(30, TimeUnit.SECONDS).untilAsserted(() -> {
            assertTrue(inboxMessageRepository
                    .existsByMessageIdAndStatus("integration-001", InboxStatus.PROCESSED));
        });
    }
}
