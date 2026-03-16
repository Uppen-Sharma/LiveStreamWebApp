package com.example.stream.repository;

import com.example.stream.model.QualitySwitchLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface QualitySwitchLogRepository extends JpaRepository<QualitySwitchLog, Long> {
    
    // Find switch logs by user IP
    List<QualitySwitchLog> findByUserIpOrderBySwitchTimestampDesc(String userIp);
    
    // Find switch logs by stream ID
    List<QualitySwitchLog> findByStreamSession_StreamIdOrderBySwitchTimestampDesc(Long streamId);
    
    // Find switch logs by user IP and stream ID
    List<QualitySwitchLog> findByUserIpAndStreamSession_StreamIdOrderBySwitchTimestampDesc(String userIp, Long streamId);
    
    // Find switch logs by reason
    List<QualitySwitchLog> findBySwitchReason(QualitySwitchLog.SwitchReason switchReason);
    
    // Find switch logs by quality level
    List<QualitySwitchLog> findByToQuality(String toQuality);
    
    // Find switch logs within time range
    List<QualitySwitchLog> findBySwitchTimestampBetweenOrderBySwitchTimestampDesc(LocalDateTime start, LocalDateTime end);
    
    // Count switches by reason for a stream
    @Query("SELECT qsl.switchReason, COUNT(qsl) FROM QualitySwitchLog qsl WHERE qsl.streamSession.streamId = :streamId GROUP BY qsl.switchReason")
    List<Object[]> countSwitchesByReasonForStream(@Param("streamId") Long streamId);
    
    // Get average bandwidth for quality switches
    @Query("SELECT qsl.toQuality, AVG(qsl.bandwidthAvailable) FROM QualitySwitchLog qsl WHERE qsl.streamSession.streamId = :streamId GROUP BY qsl.toQuality")
    List<Object[]> getAverageBandwidthByQuality(@Param("streamId") Long streamId);
    
    // Get recent switches for a user
    @Query("SELECT qsl FROM QualitySwitchLog qsl WHERE qsl.userIp = :userIp ORDER BY qsl.switchTimestamp DESC LIMIT :limit")
    List<QualitySwitchLog> getRecentSwitchesForUser(@Param("userIp") String userIp, @Param("limit") int limit);
} 