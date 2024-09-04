package ru.t1.KafkaProducer.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.t1.KafkaProducer.model.Metric;
import ru.t1.KafkaProducer.service.MetricProducerService;

import java.sql.Timestamp;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
@AutoConfigureMockMvc
class MetricProducerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MetricProducerService metricProducerService;

    @Test
    void shouldSendMetricToProducer() throws Exception {
        Metric metric = new Metric("my-app", "test-metric", 100.0, 0, 50.0, 1024.0, new Timestamp(System.currentTimeMillis()));
        String metricJson = objectMapper.writeValueAsString(metric);

        mockMvc.perform(post("/producer/metrics")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(metricJson))
                .andExpect(status().isOk());

        verify(metricProducerService, times(1)).sendTopic(eq(metric));
    }
}


