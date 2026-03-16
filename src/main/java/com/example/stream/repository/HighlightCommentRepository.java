package com.example.stream.repository;

import com.example.stream.model.HighlightComment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HighlightCommentRepository extends JpaRepository<HighlightComment, Long> {
    
    List<HighlightComment> findByHighlight_HighlightIdAndIsDeletedFalseOrderByCreatedAtAsc(Long highlightId);
    
    List<HighlightComment> findByHighlight_HighlightIdOrderByCreatedAtAsc(Long highlightId);
    
    List<HighlightComment> findByUsernameOrderByCreatedAtDesc(String username);
    
    @Query("SELECT COUNT(c) FROM HighlightComment c WHERE c.highlight.highlightId = :highlightId AND c.isDeleted = false")
    Long countActiveCommentsByHighlightId(@Param("highlightId") Long highlightId);
} 