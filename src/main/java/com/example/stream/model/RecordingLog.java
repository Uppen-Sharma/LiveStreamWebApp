package com.example.stream.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "recording_logs", indexes = {
    @Index(name = "idx_recording_id", columnList = "recording_id"),
    @Index(name = "idx_quality", columnList = "quality"),
    @Index(name = "idx_status", columnList = "status")
})
public class RecordingLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long logId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stream_id")
    private StreamSession streamSession;

    private String recordingId;
    private String filePath;
    private Long fileSize;
    private Integer duration;
    private String quality;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
} 