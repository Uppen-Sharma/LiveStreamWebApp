package com.example.stream.repository;

import com.example.stream.model.EngagementStat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EngagementStatRepository extends JpaRepository<EngagementStat, Long> {
    
    // Find engagement stats by stream ID
    List<EngagementStat> findByStreamSession_StreamIdOrderByTimestampDesc(Long streamId);
    
    // Find latest engagement stats for a stream
    Optional<EngagementStat> findFirstByStreamSession_StreamIdOrderByTimestampDesc(Long streamId);
    
    // Find engagement stats within time range
    List<EngagementStat> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime start, LocalDateTime end);
    
    // Find engagement stats by stream and time range
    List<EngagementStat> findByStreamSession_StreamIdAndTimestampBetweenOrderByTimestampDesc(Long streamId, LocalDateTime start, LocalDateTime end);
    
    // Get total likes for a stream
    @Query("SELECT SUM(e.likes) FROM EngagementStat e WHERE e.streamSession.streamId = :streamId")
    Integer getTotalLikesByStreamId(@Param("streamId") Long streamId);
    
    // Get total dislikes for a stream
    @Query("SELECT SUM(e.dislikes) FROM EngagementStat e WHERE e.streamSession.streamId = :streamId")
    Integer getTotalDislikesByStreamId(@Param("streamId") Long streamId);
    
    // Get total messages for a stream
    @Query("SELECT SUM(e.totalMessages) FROM EngagementStat e WHERE e.streamSession.streamId = :streamId")
    Integer getTotalMessagesByStreamId(@Param("streamId") Long streamId);
    
    // Get average like ratio for a stream
    @Query("SELECT AVG(e.likeRatio) FROM EngagementStat e WHERE e.streamSession.streamId = :streamId")
    Double getAverageLikeRatioByStreamId(@Param("streamId") Long streamId);
} 