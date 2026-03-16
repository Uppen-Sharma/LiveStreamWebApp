package com.example.stream.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "stream_highlights", indexes = {
    @Index(name = "idx_stream_timestamp", columnList = "stream_id, timestamp_seconds"),
    @Index(name = "idx_created_by", columnList = "created_by"),
    @Index(name = "idx_highlight_type", columnList = "highlight_type"),
    @Index(name = "idx_is_public", columnList = "is_public")
})
public class StreamHighlight {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long highlightId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stream_id", nullable = false)
    private StreamSession streamSession;

    private String title;
    private String description;
    
    @Column(name = "timestamp_seconds")
    private Integer timestampSeconds;
    
    @Column(name = "timestamp_formatted")
    private String timestampFormatted;
    
    @Column(name = "created_by")
    private String createdBy;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @Column(name = "is_public")
    private Boolean isPublic;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "highlight_type")
    private HighlightType highlightType;

    @OneToMany(mappedBy = "highlight", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<HighlightTag> tags;

    @OneToMany(mappedBy = "highlight", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<HighlightReaction> reactions;

    @OneToMany(mappedBy = "highlight", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<HighlightComment> comments;

    public enum HighlightType {
        HIGHLIGHT, BOOKMARK, CLIP
    }
} 