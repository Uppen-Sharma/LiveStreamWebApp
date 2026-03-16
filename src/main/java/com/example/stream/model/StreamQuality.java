package com.example.stream.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "stream_qualities", indexes = {
    @Index(name = "idx_quality_name", columnList = "quality_name"),
    @Index(name = "idx_is_active", columnList = "is_active"),
    @Index(name = "idx_sort_order", columnList = "sort_order")
})
public class StreamQuality {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long qualityId;

    @Column(name = "quality_name", unique = true)
    private String qualityName;

    @Column(name = "quality_label")
    private String qualityLabel;

    private String resolution;
    private String bitrate;
    
    @Column(name = "is_active")
    private Boolean isActive;
    
    @Column(name = "sort_order")
    private Integer sortOrder;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
} 