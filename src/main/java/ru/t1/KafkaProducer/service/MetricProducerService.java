package ru.t1.KafkaProducer.service;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import ru.t1.KafkaProducer.model.Metric;

@Slf4j
@Service
@AllArgsConstructor
public class MetricProducerService {

    private final KafkaTemplate<String, Metric> kafkaTemplate;

    public void sendTopic(Metric metric) {
        try {
            log.info("Sending metric to Kafka: {}", metric);
            kafkaTemplate.send("metrics-topic", metric);
        } catch (Exception e) {
            log.error("Failed to send metric to Kafka: {}", metric, e);
        }
    }
}
