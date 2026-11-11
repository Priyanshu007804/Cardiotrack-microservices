package com.cardio.patient.producer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.cardio.patient.event.PatientRegisteredEvent;

@Component
public class PatientEventProducer {

    private static final Logger log = LoggerFactory.getLogger(PatientEventProducer.class);

    private final KafkaTemplate<String, PatientRegisteredEvent> kafkaTemplate;
    private final String topicName;

    public PatientEventProducer(
            KafkaTemplate<String, PatientRegisteredEvent> kafkaTemplate,
            @Value("${app.kafka.topics.patient-registered:patient.registered}") String topicName) {
        this.kafkaTemplate = kafkaTemplate;
        this.topicName = topicName;
    }

    /**
     * Listens for the patient registration event after the database transaction commits.
     * Ensures an event is NEVER published if the database transaction rolls back.
     */
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPatientRegisteredCommit(PatientRegisteredEvent event) {
        log.info("Transaction committed successfully for patientId: {}. Publishing event to Kafka topic: {}",
                event.getPatientId(), topicName);
        sendEvent(event);
    }

    /**
     * Sends the event to Kafka asynchronously with failure handling and acknowledgment logging.
     */
    public void sendEvent(PatientRegisteredEvent event) {
        String key = String.valueOf(event.getPatientId());

        try {
            kafkaTemplate.send(topicName, key, event)
                    .whenComplete((result, ex) -> {
                        if (ex != null) {
                            log.error("CRITICAL: Failed to publish PatientRegisteredEvent [eventId={}] for patientId {} to Kafka: {}",
                                    event.getEventId(), event.getPatientId(), ex.getMessage(), ex);
                        } else {
                            log.info("Kafka delivery SUCCESS: PatientRegisteredEvent [eventId={}] for patientId {} published to partition {} at offset {}",
                                    event.getEventId(),
                                    event.getPatientId(),
                                    result.getRecordMetadata().partition(),
                                    result.getRecordMetadata().offset());
                        }
                    });
        } catch (Exception ex) {
            log.error("CRITICAL: Synchronous failure initiating Kafka send for patientId {}: {}",
                    event.getPatientId(), ex.getMessage(), ex);
            throw new RuntimeException("Kafka publish initiation failed for patientId: " + event.getPatientId(), ex);
        }
    }
}
