package com.example.stream.repository;

import com.example.stream.model.QualityRecording;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface QualityRecordingRepository extends JpaRepository<QualityRecording, Long> {
    
    // Find recordings by stream ID
    List<QualityRecording> findByStreamSession_StreamIdOrderByRecordingStartDesc(Long streamId);
    
    // Find recordings by quality level
    List<QualityRecording> findByQualityLevelOrderByRecordingStartDesc(String qualityLevel);
    
    // Find active recordings
    List<QualityRecording> findByIsActiveTrue();
    
    // Find active recordings for a stream
    List<QualityRecording> findByStreamSession_StreamIdAndIsActiveTrue(Long streamId);
    
    // Find recordings by stream and quality
    List<QualityRecording> findByStreamSession_StreamIdAndQualityLevelOrderByRecordingStartDesc(Long streamId, String qualityLevel);
    
    // Find recordings within date range
    List<QualityRecording> findByRecordingStartBetweenOrderByRecordingStartDesc(LocalDateTime start, LocalDateTime end);
    
    // Find recordings by creator
    List<QualityRecording> findByCreatedByOrderByRecordingStartDesc(String createdBy);
    
    // Get total file size for a stream
    @Query("SELECT SUM(qr.fileSize) FROM QualityRecording qr WHERE qr.streamSession.streamId = :streamId")
    Long getTotalFileSizeForStream(@Param("streamId") Long streamId);
    
    // Get quality distribution for recordings
    @Query("SELECT qr.qualityLevel, COUNT(qr), SUM(qr.fileSize) FROM QualityRecording qr WHERE qr.streamSession.streamId = :streamId GROUP BY qr.qualityLevel")
    List<Object[]> getQualityDistributionForStream(@Param("streamId") Long streamId);
    
    // Find recordings by file path pattern
    @Query("SELECT qr FROM QualityRecording qr WHERE qr.filePath LIKE %:pattern%")
    List<QualityRecording> findByFilePathPattern(@Param("pattern") String pattern);
} 