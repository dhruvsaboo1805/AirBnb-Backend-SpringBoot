package com.example.AirbnbBookingSpring.saga;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.util.concurrent.TimeUnit;

@Component
@Slf4j
@RequiredArgsConstructor
public class SagaEventConsumer {

    @Value("${SAGA_QUEUE_VALUE}")
    private String SAGA_QUEUE;

    private final ObjectMapper objectMapper;
    private final RedisTemplate<String , String> redisTemplate;
    private final SagaEventProcessor sagaEventProcessor;

    @Scheduled(fixedDelay = 500)
    public void consumeEvents() {
        try {
            System.out.println("SAGA_QUEUE IS -> " + SAGA_QUEUE);
            String eventJson = redisTemplate.opsForList().leftPop(SAGA_QUEUE , 1 , TimeUnit.SECONDS);
            System.out.println("Event json IS -> " + eventJson);
            if (eventJson != null) {
                SagaEvent sagaEvent = objectMapper.readValue(eventJson, SagaEvent.class);
                sagaEventProcessor.processEvent(sagaEvent);
                log.info("Processing saga event: {}", sagaEvent.getSagaId());
                sagaEventProcessor.processEvent(sagaEvent);
                log.info("Saga event processed successfully for saga id: {}", sagaEvent.getSagaId());
            }
        } catch (Exception e) {
            log.error("Error processing saga event: {}", e.getMessage());
            throw new RuntimeException("Failed to process saga event", e);
        }
    }
}
