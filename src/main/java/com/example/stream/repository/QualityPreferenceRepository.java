package com.example.stream.repository;

import com.example.stream.model.QualityPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface QualityPreferenceRepository extends JpaRepository<QualityPreference, Long> {
    
    // Find preferences by user IP
    List<QualityPreference> findByUserIpOrderByTimestampDesc(String userIp);
    
    // Find latest preference by user IP
    QualityPreference findFirstByUserIpOrderByTimestampDesc(String userIp);
    
    // Find preferences by quality
    List<QualityPreference> findByQualityOrderByTimestampDesc(String quality);
    
    // Find preferences by device type
    List<QualityPreference> findByDeviceTypeOrderByTimestampDesc(String deviceType);
    
    // Find preferences within time range
    List<QualityPreference> findByTimestampBetweenOrderByTimestampDesc(LocalDateTime start, LocalDateTime end);
    
    // Count preferences by quality
    @Query("SELECT q.quality, COUNT(q) FROM QualityPreference q GROUP BY q.quality ORDER BY COUNT(q) DESC")
    List<Object[]> countPreferencesByQuality();
    
    // Count preferences by device type
    @Query("SELECT q.deviceType, COUNT(q) FROM QualityPreference q GROUP BY q.deviceType ORDER BY COUNT(q) DESC")
    List<Object[]> countPreferencesByDeviceType();
    
    // Get most popular quality
    @Query("SELECT q.quality FROM QualityPreference q GROUP BY q.quality ORDER BY COUNT(q) DESC LIMIT 1")
    String findMostPopularQuality();
} 