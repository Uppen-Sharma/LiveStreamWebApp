package com.example.stream.repository;

import com.example.stream.model.ChatMessageEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessageEntity, Long> {
    
    // Find messages by stream ID
    List<ChatMessageEntity> findByStreamSession_StreamIdOrderByTimestampDesc(Long streamId);
    
    // Find messages by stream ID with pagination
    Page<ChatMessageEntity> findByStreamSession_StreamIdAndIsDeletedFalseOrderByTimestampDesc(Long streamId, Pageable pageable);
    
    // Find messages by username
    List<ChatMessageEntity> findByUsernameOrderByTimestampDesc(String username);
    
    // Find messages by user IP
    List<ChatMessageEntity> findByUserIpOrderByTimestampDesc(String userIp);
    
    // Find messages within time range
    List<ChatMessageEntity> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime start, LocalDateTime end);
    
    // Find messages by stream and time range
    List<ChatMessageEntity> findByStreamSession_StreamIdAndTimestampBetweenOrderByTimestampDesc(Long streamId, LocalDateTime start, LocalDateTime end);
    
    // Count messages by stream
    @Query("SELECT COUNT(c) FROM ChatMessageEntity c WHERE c.streamSession.streamId = :streamId AND c.isDeleted = false")
    Long countMessagesByStreamId(@Param("streamId") Long streamId);
    
    // Get top chatters by message count
    @Query("SELECT c.username, COUNT(c) as messageCount FROM ChatMessageEntity c WHERE c.streamSession.streamId = :streamId AND c.isDeleted = false GROUP BY c.username ORDER BY messageCount DESC")
    List<Object[]> findTopChattersByStreamId(@Param("streamId") Long streamId);
} 