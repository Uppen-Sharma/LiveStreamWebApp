package com.example.stream.model;

import lombok.Data;

@Data
public class StreamMetrics {
    private int likes = 0;
    private int dislikes = 0;
    private double likeRatio = 0.0;
    private int currentViewers = 1247;
    private int peakViewers = 1389;
    private int newFollowers = 86;
}