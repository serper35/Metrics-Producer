package ru.t1.KafkaProducer.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import ru.t1.KafkaProducer.model.Metric;

import static org.junit.jupiter.api.Assertions.assertNotNull;


@SpringBootTest
class MetricProducerConfigTest {

    @Autowired
    private MetricProducerConfig metricProducerConfig;

    @Test
    void shouldCreateKafkaTemplate() {
        KafkaTemplate<String, Metric> kafkaTemplate = metricProducerConfig.kafkaTemplate(metricProducerConfig.producerFactory());
        assertNotNull(kafkaTemplate);
    }

    @Test
    void shouldCreateProducerFactory() {
        ProducerFactory<String, Metric> producerFactory = metricProducerConfig.producerFactory();
        assertNotNull(producerFactory);
    }
}
