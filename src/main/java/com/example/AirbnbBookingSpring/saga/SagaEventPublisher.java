package com.example.AirbnbBookingSpring.saga;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SagaEventPublisher {
//    1. The Critical Issue: Static Injection (Spring Anti-Pattern)
//    D - Dependency Inversion Principle (DIP)
//    The Violation: Your SagaEventPublisher class depends directly on RedisTemplate (a low-level implementation detail).
//
//    Why it matters: Your code comment says: // forcefully using redis... not over burden with kafka. This implies you might want to switch to Kafka later. Because you hardcoded RedisTemplate inside this class, you cannot switch to Kafka without rewriting this class.
//
//    The Fix: You should depend on an abstraction (Interface), not a concrete implementation.
//
//    O - Open/Closed Principle (OCP)
//    The Violation: The class is not "Closed for modification." If you want to change the underlying storage from Redis to Kafka, or maybe a database table, you have to modify the source code of this class.
//
//    The Fix: Create an interface EventPublisher. You can then have a RedisEventPublisher and a KafkaEventPublisher. You can switch between them using Spring profiles without touching the core Saga logic.
    @Value("${SAGA_QUEUE_VALUE}")
    private String SAGA_QUEUE;

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public void publishEvent(String eventType, String step, Map<String, Object> payload) {
        SagaEvent sagaEvent = SagaEvent.builder()
                .sagaId(UUID.randomUUID().toString())
                .eventType(eventType)
                .step(step)
                .payload(payload)
                .timestamp(LocalDateTime.now())
                .status(SagaEvent.SagaStatus.PENDING)
                .build();

        try {
            String eventJson = objectMapper.writeValueAsString(sagaEvent);
            System.out.println("PUBLISHER: Attempting to push to queue: " + SAGA_QUEUE + " | Payload: " + eventJson);
            redisTemplate.opsForList().rightPush(SAGA_QUEUE, eventJson); // we are forcefully using redis pull method here since to not over burden with kafka
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

}
