package com.example.stream.repository;

import com.example.stream.model.StreamHighlight;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StreamHighlightRepository extends JpaRepository<StreamHighlight, Long> {
    
    // Find highlights by stream ID
    List<StreamHighlight> findByStreamSession_StreamIdOrderByTimestampSecondsAsc(Long streamId);
    
    // Find highlights by stream ID and type
    List<StreamHighlight> findByStreamSession_StreamIdAndHighlightTypeOrderByTimestampSecondsAsc(Long streamId, StreamHighlight.HighlightType type);
    
    // Find public highlights by stream ID
    List<StreamHighlight> findByStreamSession_StreamIdAndIsPublicTrueOrderByTimestampSecondsAsc(Long streamId);
    
    // Find highlights by creator
    List<StreamHighlight> findByCreatedByOrderByCreatedAtDesc(String createdBy);
    
    // Find highlights by type
    List<StreamHighlight> findByHighlightTypeOrderByCreatedAtDesc(StreamHighlight.HighlightType type);
    
    // Find highlights with pagination
    Page<StreamHighlight> findByIsPublicTrueOrderByCreatedAtDesc(Pageable pageable);
    
    // Find highlights by stream and time range
    @Query("SELECT h FROM StreamHighlight h WHERE h.streamSession.streamId = :streamId AND h.timestampSeconds BETWEEN :startTime AND :endTime ORDER BY h.timestampSeconds ASC")
    List<StreamHighlight> findByStreamIdAndTimeRange(@Param("streamId") Long streamId, @Param("startTime") Integer startTime, @Param("endTime") Integer endTime);
    
    // Count highlights by stream
    @Query("SELECT COUNT(h) FROM StreamHighlight h WHERE h.streamSession.streamId = :streamId")
    Long countByStreamId(@Param("streamId") Long streamId);
    
    // Count highlights by stream and type
    @Query("SELECT COUNT(h) FROM StreamHighlight h WHERE h.streamSession.streamId = :streamId AND h.highlightType = :type")
    Long countByStreamIdAndType(@Param("streamId") Long streamId, @Param("type") StreamHighlight.HighlightType type);
    
    // Find popular highlights (with most reactions)
    @Query("SELECT h FROM StreamHighlight h WHERE h.isPublic = true ORDER BY SIZE(h.reactions) DESC, h.createdAt DESC")
    List<StreamHighlight> findPopularHighlights(Pageable pageable);
} 