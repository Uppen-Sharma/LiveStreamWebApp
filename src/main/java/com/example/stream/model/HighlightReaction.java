package com.example.stream.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "highlight_reactions", indexes = {
    @Index(name = "idx_highlight_id", columnList = "highlight_id"),
    @Index(name = "idx_reaction_type", columnList = "reaction_type")
})
public class HighlightReaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long reactionId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "highlight_id", nullable = false)
    private StreamHighlight highlight;

    @Column(name = "user_ip")
    private String userIp;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "reaction_type")
    private ReactionType reactionType;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;

    public enum ReactionType {
        LIKE, LOVE, WOW, USEFUL
    }
} 