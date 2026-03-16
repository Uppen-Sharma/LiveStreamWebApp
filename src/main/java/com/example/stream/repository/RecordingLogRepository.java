package com.example.stream.repository;

import com.example.stream.model.RecordingLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RecordingLogRepository extends JpaRepository<RecordingLog, Long> {
    
    // Find recordings by stream ID
    List<RecordingLog> findByStreamSession_StreamIdOrderByStartTimeDesc(Long streamId);
    
    // Find recording by recording ID
    Optional<RecordingLog> findByRecordingId(String recordingId);
    
    // Find recordings by status
    List<RecordingLog> findByStatusOrderByStartTimeDesc(String status);
    
    // Find recordings by quality
    List<RecordingLog> findByQualityOrderByStartTimeDesc(String quality);
    
    // Find recordings within time range
    List<RecordingLog> findByStartTimeBetweenOrderByStartTimeDesc(LocalDateTime start, LocalDateTime end);
    
    // Find recordings by stream and status
    List<RecordingLog> findByStreamSession_StreamIdAndStatusOrderByStartTimeDesc(Long streamId, String status);
    
    // Get total file size for a stream
    @Query("SELECT SUM(r.fileSize) FROM RecordingLog r WHERE r.streamSession.streamId = :streamId")
    Long getTotalFileSizeByStreamId(@Param("streamId") Long streamId);
    
    // Get total duration for a stream
    @Query("SELECT SUM(r.duration) FROM RecordingLog r WHERE r.streamSession.streamId = :streamId")
    Integer getTotalDurationByStreamId(@Param("streamId") Long streamId);
    
    // Count recordings by quality
    @Query("SELECT r.quality, COUNT(r) FROM RecordingLog r GROUP BY r.quality")
    List<Object[]> countRecordingsByQuality();
} 