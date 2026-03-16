package com.example.stream.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "engagement_stats", indexes = {
    @Index(name = "idx_stream_timestamp", columnList = "stream_id, timestamp")
})
public class EngagementStat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long statId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stream_id")
    private StreamSession streamSession;

    private Integer likes;
    private Integer dislikes;
    private Integer totalMessages;
    private Double likeRatio;
    private LocalDateTime timestamp;
} 