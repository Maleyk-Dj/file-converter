package com.example.file_converter.outbox;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OutboxPoller {

    private final OutboxService outboxService;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Scheduled(fixedDelay = 5000)
    public void poll() {
        outboxService.findAll().forEach(msg -> {
            try {
                // отправляем в Kafka и ждём подтверждения
                kafkaTemplate.send(msg.getTopic(), msg.getPayload()).get();

                //  только после успешной отправки помечаем SENT
                outboxService.markSent(msg.getId());

                log.info("Outbox сообщение отправлено: {}", msg.getId());
            } catch (Exception e) {
                // если не удалось отправить оставляем NEW
                // поллер попробует снова через 5 секунд
                log.error("Не удалось отправить outbox сообщение: {}", msg.getId(), e);
            }
        });
    }
}
