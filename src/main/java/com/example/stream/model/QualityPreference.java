package com.example.stream.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "quality_preferences", indexes = {
    @Index(name = "idx_user_ip", columnList = "user_ip"),
    @Index(name = "idx_quality", columnList = "quality"),
    @Index(name = "idx_timestamp", columnList = "timestamp"),
    @Index(name = "idx_device_type", columnList = "device_type")
})
public class QualityPreference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long preferenceId;

    private String userIp;
    private String quality;
    private String userAgent;
    private String deviceType;
    private LocalDateTime timestamp;
} 