package com.example.stream.repository;

import com.example.stream.model.HighlightTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HighlightTagRepository extends JpaRepository<HighlightTag, Long> {
    
    List<HighlightTag> findByHighlight_HighlightId(Long highlightId);
    
    List<HighlightTag> findByTagName(String tagName);
    
    @Query("SELECT DISTINCT t.tagName FROM HighlightTag t WHERE t.highlight.isPublic = true")
    List<String> findAllDistinctTagNames();
    
    @Query("SELECT t FROM HighlightTag t WHERE t.tagName IN :tagNames AND t.highlight.isPublic = true")
    List<HighlightTag> findByTagNamesAndPublic(@Param("tagNames") List<String> tagNames);
} 