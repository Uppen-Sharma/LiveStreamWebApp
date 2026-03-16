package com.example.stream.repository;

import com.example.stream.model.UserQualityPreference;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserQualityPreferenceRepository extends JpaRepository<UserQualityPreference, Long> {
    
    // Find preference by user IP and stream ID
    Optional<UserQualityPreference> findByUserIpAndStreamSession_StreamId(String userIp, Long streamId);
    
    // Find all preferences for a user
    List<UserQualityPreference> findByUserIp(String userIp);
    
    // Find all preferences for a stream
    List<UserQualityPreference> findByStreamSession_StreamId(Long streamId);
    
    // Find preferences by quality level
    List<UserQualityPreference> findByPreferredQuality(String preferredQuality);
    
    // Count users with auto-switch enabled for a stream
    @Query("SELECT COUNT(uqp) FROM UserQualityPreference uqp WHERE uqp.streamSession.streamId = :streamId AND uqp.autoSwitch = true")
    Long countAutoSwitchUsersByStreamId(@Param("streamId") Long streamId);
    
    // Get quality distribution for a stream
    @Query("SELECT uqp.preferredQuality, COUNT(uqp) FROM UserQualityPreference uqp WHERE uqp.streamSession.streamId = :streamId GROUP BY uqp.preferredQuality")
    List<Object[]> getQualityDistributionByStreamId(@Param("streamId") Long streamId);
} 