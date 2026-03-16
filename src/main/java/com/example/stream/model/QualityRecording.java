package com.example.stream.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "quality_recordings", indexes = {
    @Index(name = "idx_stream_id", columnList = "stream_id"),
    @Index(name = "idx_quality_level", columnList = "quality_level"),
    @Index(name = "idx_is_active", columnList = "is_active"),
    @Index(name = "idx_recording_start", columnList = "recording_start")
})
public class QualityRecording {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long recordingId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stream_id", nullable = false)
    private StreamSession streamSession;

    @Column(name = "quality_level")
    private String qualityLevel;

    @Column(name = "file_path")
    private String filePath;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "recording_start")
    private LocalDateTime recordingStart;

    @Column(name = "recording_end")
    private LocalDateTime recordingEnd;
    
    @Column(name = "is_active")
    private Boolean isActive;
    
    @Column(name = "created_by")
    private String createdBy;
} 