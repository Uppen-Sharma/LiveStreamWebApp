package com.example.stream.repository;

import com.example.stream.model.StreamSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface StreamSessionRepository extends JpaRepository<StreamSession, Long> {
    
    // Find active streams
    List<StreamSession> findByStatusOrderByStartTimeDesc(String status);
    
    // Find the current active stream
    Optional<StreamSession> findFirstByStatusOrderByStartTimeDesc(String status);
    
    // Get streams within a date range
    List<StreamSession> findByStartTimeBetweenOrderByStartTimeDesc(LocalDateTime start, LocalDateTime end);
    
    // Get peak viewers for a stream
    @Query("SELECT MAX(s.peakViewers) FROM StreamSession s WHERE s.streamId = :streamId")
    Integer findPeakViewersByStreamId(@Param("streamId") Long streamId);
    
    // Get total watch time for a stream
    @Query("SELECT SUM(s.totalWatchTime) FROM StreamSession s WHERE s.streamId = :streamId")
    Long findTotalWatchTimeByStreamId(@Param("streamId") Long streamId);
    
    // Get current viewer count for active stream
    @Query("SELECT s.currentViewers FROM StreamSession s WHERE s.status = 'LIVE' ORDER BY s.startTime DESC LIMIT 1")
    Integer findCurrentViewerCount();
} 