package com.example.stream.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "highlight_tags", indexes = {
    @Index(name = "idx_highlight_id", columnList = "highlight_id"),
    @Index(name = "idx_tag_name", columnList = "tag_name")
})
public class HighlightTag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long tagId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "highlight_id", nullable = false)
    private StreamHighlight highlight;

    @Column(name = "tag_name")
    private String tagName;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
} 