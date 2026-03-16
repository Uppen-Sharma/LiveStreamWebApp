package com.example.stream.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "quality_switch_logs", indexes = {
    @Index(name = "idx_user_ip", columnList = "user_ip"),
    @Index(name = "idx_stream_id", columnList = "stream_id"),
    @Index(name = "idx_switch_timestamp", columnList = "switch_timestamp"),
    @Index(name = "idx_switch_reason", columnList = "switch_reason")
})
public class QualitySwitchLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logId;

    @Column(name = "user_ip")
    private String userIp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stream_id")
    private StreamSession streamSession;

    @Column(name = "from_quality")
    private String fromQuality;

    @Column(name = "to_quality")
    private String toQuality;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "switch_reason")
    private SwitchReason switchReason;
    
    @Column(name = "bandwidth_available")
    private Integer bandwidthAvailable;
    
    @Column(name = "latency_ms")
    private Integer latencyMs;
    
    @Column(name = "packet_loss")
    private BigDecimal packetLoss;
    
    @Column(name = "switch_timestamp")
    private LocalDateTime switchTimestamp;

    public enum SwitchReason {
        MANUAL, AUTO_BANDWIDTH, AUTO_ERROR, AUTO_QUALITY
    }
} 