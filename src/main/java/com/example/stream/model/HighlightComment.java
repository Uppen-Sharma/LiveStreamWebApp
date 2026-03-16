package com.example.stream.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "highlight_comments", indexes = {
    @Index(name = "idx_highlight_id", columnList = "highlight_id"),
    @Index(name = "idx_username", columnList = "username"),
    @Index(name = "idx_created_at", columnList = "created_at")
})
public class HighlightComment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "highlight_id", nullable = false)
    private StreamHighlight highlight;

    private String username;
    private String content;
    
    @Column(name = "user_ip")
    private String userIp;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "is_deleted")
    private Boolean isDeleted;
} 