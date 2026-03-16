package com.example.stream.repository;

import com.example.stream.model.QualityAnalytics;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface QualityAnalyticsRepository extends JpaRepository<QualityAnalytics, Long> {
    
    // Find analytics by stream ID
    List<QualityAnalytics> findByStreamSession_StreamIdOrderByRecordedAtDesc(Long streamId);
    
    // Find analytics by quality level
    List<QualityAnalytics> findByQualityLevelOrderByRecordedAtDesc(String qualityLevel);
    
    // Find analytics by stream and quality
    List<QualityAnalytics> findByStreamSession_StreamIdAndQualityLevelOrderByRecordedAtDesc(Long streamId, String qualityLevel);
    
    // Find analytics within time range
    List<QualityAnalytics> findByRecordedAtBetweenOrderByRecordedAtDesc(LocalDateTime start, LocalDateTime end);
    
    // Get latest analytics for each quality level in a stream
    @Query("SELECT qa FROM QualityAnalytics qa WHERE qa.streamSession.streamId = :streamId AND qa.recordedAt = (SELECT MAX(qa2.recordedAt) FROM QualityAnalytics qa2 WHERE qa2.streamSession.streamId = :streamId AND qa2.qualityLevel = qa.qualityLevel)")
    List<QualityAnalytics> getLatestAnalyticsByQualityForStream(@Param("streamId") Long streamId);
    
    // Get average metrics for a quality level
    @Query("SELECT AVG(qa.avgBandwidth), AVG(qa.avgLatency), AVG(qa.avgPacketLoss) FROM QualityAnalytics qa WHERE qa.qualityLevel = :qualityLevel")
    List<Object[]> getAverageMetricsForQuality(@Param("qualityLevel") String qualityLevel);
    
    // Get quality performance summary
    @Query("SELECT qa.qualityLevel, AVG(qa.viewerCount), AVG(qa.avgBandwidth), AVG(qa.avgLatency), SUM(qa.switchCount), SUM(qa.errorCount) FROM QualityAnalytics qa WHERE qa.streamSession.streamId = :streamId GROUP BY qa.qualityLevel")
    List<Object[]> getQualityPerformanceSummary(@Param("streamId") Long streamId);
} 