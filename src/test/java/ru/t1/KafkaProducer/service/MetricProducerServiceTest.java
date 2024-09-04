package ru.t1.KafkaProducer.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.kafka.core.KafkaTemplate;
import ru.t1.KafkaProducer.model.Metric;
import java.sql.Timestamp;

import static org.mockito.Mockito.*;

import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

class MetricProducerServiceTest {

    @Mock
    private KafkaTemplate<String, Metric> kafkaTemplate;

    @InjectMocks
    private MetricProducerService metricProducerService;

    @Captor
    private ArgumentCaptor<Metric> metricCaptor;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testSendTopic() {
        Metric metric = new Metric("my-app", "test-metric", 100.0, 0, 50.0, 1024.0, new Timestamp(System.currentTimeMillis()));

        metricProducerService.sendTopic(metric);

        verify(kafkaTemplate).send("metrics-topic", metric);

        verify(kafkaTemplate).send(eq("metrics-topic"), metricCaptor.capture());
        Metric capturedMetric = metricCaptor.getValue();

        assertEquals(metric, capturedMetric);
    }

    @Test
    void testSendTopicFailure() {

        Metric metric = new Metric("my-app", "test-metric", 100.0, 0, 50.0, 1024.0, new Timestamp(System.currentTimeMillis()));


        doThrow(new RuntimeException("Kafka error")).when(kafkaTemplate).send(eq("metrics-topic"), any(Metric.class));

        metricProducerService.sendTopic(metric);

        verify(kafkaTemplate).send("metrics-topic", metric);
    }
}

