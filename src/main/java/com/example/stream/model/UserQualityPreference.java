package com.example.stream.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "user_quality_preferences", indexes = {
    @Index(name = "idx_user_ip", columnList = "user_ip"),
    @Index(name = "idx_preferred_quality", columnList = "preferred_quality")
})
public class UserQualityPreference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long preferenceId;

    @Column(name = "user_ip")
    private String userIp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stream_id")
    private StreamSession streamSession;

    @Column(name = "preferred_quality")
    private String preferredQuality;
    
    @Column(name = "auto_switch")
    private Boolean autoSwitch;
    
    @Column(name = "bandwidth_threshold")
    private Integer bandwidthThreshold;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
} 