package com.example.stream.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "quality_analytics", indexes = {
    @Index(name = "idx_stream_id", columnList = "stream_id"),
    @Index(name = "idx_quality_level", columnList = "quality_level"),
    @Index(name = "idx_recorded_at", columnList = "recorded_at")
})
public class QualityAnalytics {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long analyticsId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stream_id", nullable = false)
    private StreamSession streamSession;

    @Column(name = "quality_level")
    private String qualityLevel;

    @Column(name = "viewer_count")
    private Integer viewerCount;

    @Column(name = "avg_bandwidth")
    private Integer avgBandwidth;

    @Column(name = "avg_latency")
    private Integer avgLatency;

    @Column(name = "avg_packet_loss")
    private BigDecimal avgPacketLoss;

    @Column(name = "switch_count")
    private Integer switchCount;

    @Column(name = "error_count")
    private Integer errorCount;

    @Column(name = "recorded_at")
    private LocalDateTime recordedAt;
} 