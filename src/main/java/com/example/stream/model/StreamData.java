package com.example.stream.model;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
public class StreamData {
    private StreamMetrics metrics;
    private List<TopChatter> topChatters;
    private List<ViewerCount> viewerData;
    private List<PerformanceData> performanceData;
    private LocalDateTime streamStartTime;
}