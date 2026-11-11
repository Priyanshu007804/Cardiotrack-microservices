package com.cardio.notification.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import com.cardio.notification.event.PatientRegisteredEvent;

@Component
public class PatientEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(PatientEventConsumer.class);

    @KafkaListener(
            topics = "${app.kafka.topics.patient-registered:patient.registered}",
            groupId = "${spring.kafka.consumer.group-id:notification-service-group}",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consumePatientRegisteredEvent(@Payload PatientRegisteredEvent event) {
        log.info(">>> [NOTIFICATION-SERVICE CONSUMER] Received event successfully! eventId={}, eventType={}, patientId={}, timestamp={}",
                event.getEventId(),
                event.getEventType(),
                event.getPatientId(),
                event.getTimestamp());

        // In real-world workflow, send welcome email / SMS or audit record.
        // As per requirements: only log event ID, event type and patient ID.
        // Do not create duplicate patient records or clinical records.
        log.info(">>> [NOTIFICATION-SERVICE CONSUMER] Welcome notification queued for patient ID: {}", event.getPatientId());
    }
}
