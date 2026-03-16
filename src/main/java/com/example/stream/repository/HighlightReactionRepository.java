package com.example.stream.repository;

import com.example.stream.model.HighlightReaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HighlightReactionRepository extends JpaRepository<HighlightReaction, Long> {
    
    Optional<HighlightReaction> findByHighlight_HighlightIdAndUserIp(Long highlightId, String userIp);
    
    List<HighlightReaction> findByHighlight_HighlightId(Long highlightId);
    
    List<HighlightReaction> findByHighlight_HighlightIdAndReactionType(Long highlightId, HighlightReaction.ReactionType reactionType);
    
    @Query("SELECT COUNT(r) FROM HighlightReaction r WHERE r.highlight.highlightId = :highlightId AND r.reactionType = :reactionType")
    Long countByHighlightIdAndReactionType(@Param("highlightId") Long highlightId, @Param("reactionType") HighlightReaction.ReactionType reactionType);
} 