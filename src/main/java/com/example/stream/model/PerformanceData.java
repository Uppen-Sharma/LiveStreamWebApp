package com.example.stream.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PerformanceData {
    private String date;
    private int views;
    private double avgWatchTime;
}