package ru.t1.KafkaProducer.controller;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.t1.KafkaProducer.model.Metric;
import ru.t1.KafkaProducer.service.MetricProducerService;

@RestController
@RequestMapping("/producer")
@Api(tags = "Metric Management")
@Slf4j
public class MetricProducerController {
    private final MetricProducerService metricProducerService;

    public MetricProducerController(MetricProducerService metricProducerService) {
        this.metricProducerService = metricProducerService;
    }

    @PostMapping("/metrics")
    @ApiOperation(value = "Send metrics to the topic", notes = "This endpoint sends a metric to the specified topic.")
    public ResponseEntity<Void> sendMetrics(@RequestBody Metric metric) {
        log.info("Received request to send metric: {}", metric);

        try {
            metricProducerService.sendTopic(metric);
            log.info("Metric sent successfully");
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            log.error("Error sending metric: {}", e.getMessage());
            return ResponseEntity.badRequest().build();
        }
    }
}
