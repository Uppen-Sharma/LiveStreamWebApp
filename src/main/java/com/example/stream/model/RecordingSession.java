package com.example.stream.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "recording_sessions", indexes = {
    @Index(name = "idx_recording_id", columnList = "recording_id"),
    @Index(name = "idx_user_ip", columnList = "user_ip"),
    @Index(name = "idx_quality", columnList = "quality"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_start_time", columnList = "start_time")
})
public class RecordingSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long sessionId;

    private String recordingId;
    private String userIp;
    private String quality;
    private String filePath;
    private Long fileSize;
    private Long duration;
    private String status;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
} 