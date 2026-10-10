package com.rideshare.ride_service.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rideshare.ride_service.event.RideRequestedEvent;
import com.rideshare.ride_service.model.OutboxEvent;
import com.rideshare.ride_service.repository.OutboxEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {

    private final OutboxEventRepository outboxEventRepository;
    private final ObjectMapper objectMapper;
    private final KafkaTemplate<String, RideRequestedEvent> kafkaTemplate;

    @Scheduled(fixedDelay = 5000)
    public void publishEvents() {

        List<OutboxEvent> events =
                outboxEventRepository.findByPublishedFalse();

        for (OutboxEvent event : events) {

            try {
                RideRequestedEvent rideEvent = objectMapper.readValue(
                        event.getPayload(),
                        RideRequestedEvent.class
                );

                kafkaTemplate.send(
                        "ride.requested",
                        rideEvent.getRideId(),
                        rideEvent
                ).get();

                event.setPublished(true);
                outboxEventRepository.save(event);

            } catch (Exception e) {
                log.error("Failed to publish outbox event {}", event.getId(), e);
            }
        }
    }
}
