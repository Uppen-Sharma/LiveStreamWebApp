package com.example.stream.repository;

import com.example.stream.model.RecordingSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RecordingSessionRepository extends JpaRepository<RecordingSession, Long> {
    
    // Find sessions by recording ID
    Optional<RecordingSession> findByRecordingId(String recordingId);
    
    // Find sessions by user IP
    List<RecordingSession> findByUserIpOrderByStartTimeDesc(String userIp);
    
    // Find sessions by status
    List<RecordingSession> findByStatusOrderByStartTimeDesc(String status);
    
    // Find sessions by quality
    List<RecordingSession> findByQualityOrderByStartTimeDesc(String quality);
    
    // Find sessions within time range
    List<RecordingSession> findByStartTimeBetweenOrderByStartTimeDesc(LocalDateTime start, LocalDateTime end);
    
    // Find sessions by user IP and status
    List<RecordingSession> findByUserIpAndStatusOrderByStartTimeDesc(String userIp, String status);
    
    // Get total file size for a user
    @Query("SELECT SUM(r.fileSize) FROM RecordingSession r WHERE r.userIp = :userIp")
    Long getTotalFileSizeByUserIp(@Param("userIp") String userIp);
    
    // Get total duration for a user
    @Query("SELECT SUM(r.duration) FROM RecordingSession r WHERE r.userIp = :userIp")
    Long getTotalDurationByUserIp(@Param("userIp") String userIp);
    
    // Count sessions by status
    @Query("SELECT r.status, COUNT(r) FROM RecordingSession r GROUP BY r.status")
    List<Object[]> countSessionsByStatus();
    
    // Get average session duration
    @Query("SELECT AVG(r.duration) FROM RecordingSession r WHERE r.duration > 0")
    Double getAverageSessionDuration();
} 