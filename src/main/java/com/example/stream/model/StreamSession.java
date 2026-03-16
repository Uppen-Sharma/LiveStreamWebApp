package com.example.stream.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "stream_sessions", indexes = {
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_start_time", columnList = "start_time"),
    @Index(name = "idx_current_viewers", columnList = "current_viewers")
})
public class StreamSession {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long streamId;

    private String title;

    @Column(name = "start_time")
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    private String status;

    @Column(name = "current_viewers")
    private Integer currentViewers;

    @Column(name = "peak_viewers")
    private Integer peakViewers;

    @Column(name = "total_watch_time")
    private Long totalWatchTime;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
} 