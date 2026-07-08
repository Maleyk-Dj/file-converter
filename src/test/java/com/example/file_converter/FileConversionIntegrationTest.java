package com.example.file_converter;

import com.example.file_converter.inbox.InboxMessageRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.io.ByteArrayInputStream;
import java.util.concurrent.TimeUnit;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
public class FileConversionIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:15");

    @Container
    static KafkaContainer kafka =
            new KafkaContainer(DockerImageName.parse("confluentinc/cp-kafka:7.6.0"));

    @Container
    static GenericContainer<?> minio =
            new GenericContainer<>("minio/minio")
                    .withCommand("server /data")
                    .withExposedPorts(9000)
                    .withEnv("MINIO_ROOT_USER", "minioadmin")
                    .withEnv("MINIO_ROOT_PASSWORD", "minioadmin");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
        registry.add("minio.url", () ->
                "http://" + minio.getHost() + ":" + minio.getMappedPort(9000));
    }

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private InboxMessageRepository inboxMessageRepository;

    private MinioClient minioClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() throws Exception {
        minioClient = MinioClient.builder()
                .endpoint("http://" + minio.getHost() + ":" + minio.getMappedPort(9000))
                .credentials("minioadmin", "minioadmin")
                .build();

        // Создаём бакеты если их нет
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
                    .existsByMessageIdAndStatus("integration-001", "PROCESSED"));
        });
    }
}
