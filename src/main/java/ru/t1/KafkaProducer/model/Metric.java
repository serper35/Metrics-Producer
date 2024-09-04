package ru.t1.KafkaProducer.model;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.sql.Timestamp;


@Data
@AllArgsConstructor
public class Metric {
    private String appName;
    private String metricName;
    private double metricValue;
    private int numberOfMistakes;
    private double processTime;
    private double usingMemory;
    private Timestamp timestamp;
}
