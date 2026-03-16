package com.example.stream.service;

import com.example.stream.model.*;
import com.example.stream.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Service
public class QualityManagementService {

    @Autowired
    private StreamQualityRepository qualityRepository;
    
    @Autowired
    private UserQualityPreferenceRepository preferenceRepository;
    
    @Autowired
    private QualitySwitchLogRepository switchLogRepository;
    
    @Autowired
    private QualityRecordingRepository recordingRepository;
    
    @Autowired
    private QualityAnalyticsRepository analyticsRepository;
    
    @Autowired
    private StreamSessionRepository streamSessionRepository;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    private final Map<String, String> userQualityPreferences = new ConcurrentHashMap<>();
    private final Map<String, Integer> userBandwidth = new ConcurrentHashMap<>();

    // Get available quality levels
    public List<Map<String, Object>> getAvailableQualities() {
        List<StreamQuality> qualities = qualityRepository.findByIsActiveTrueOrderBySortOrderAsc();
        
        return qualities.stream().map(quality -> {
            Map<String, Object> qualityData = new HashMap<>();
            qualityData.put("qualityName", quality.getQualityName());
            qualityData.put("qualityLabel", quality.getQualityLabel());
            qualityData.put("resolution", quality.getResolution());
            qualityData.put("bitrate", quality.getBitrate());
            qualityData.put("sortOrder", quality.getSortOrder());
            return qualityData;
        }).collect(Collectors.toList());
    }

    // Set user quality preference
    public UserQualityPreference setUserQualityPreference(String userIp, Long streamId, String preferredQuality, 
                                                         Boolean autoSwitch, Integer bandwidthThreshold) {
        
        Optional<StreamSession> stream = streamSessionRepository.findById(streamId);
        if (stream.isEmpty()) {
            throw new RuntimeException("Stream not found");
        }

        Optional<UserQualityPreference> existingPreference = preferenceRepository.findByUserIpAndStreamSession_StreamId(userIp, streamId);
        
        UserQualityPreference preference;
        if (existingPreference.isPresent()) {
            preference = existingPreference.get();
            preference.setPreferredQuality(preferredQuality);
            preference.setAutoSwitch(autoSwitch);
            preference.setBandwidthThreshold(bandwidthThreshold);
            preference.setUpdatedAt(LocalDateTime.now());
        } else {
            preference = new UserQualityPreference();
            preference.setUserIp(userIp);
            preference.setStreamSession(stream.get());
            preference.setPreferredQuality(preferredQuality);
            preference.setAutoSwitch(autoSwitch);
            preference.setBandwidthThreshold(bandwidthThreshold);
            preference.setCreatedAt(LocalDateTime.now());
            preference.setUpdatedAt(LocalDateTime.now());
        }

        UserQualityPreference savedPreference = preferenceRepository.save(preference);

        // Send real-time update
        sendQualityPreferenceUpdate(streamId, savedPreference);

        return savedPreference;
    }

    // Get user quality preference
    public Map<String, Object> getUserQualityPreference(String userIp, Long streamId) {
        Optional<UserQualityPreference> preference = preferenceRepository.findByUserIpAndStreamSession_StreamId(userIp, streamId);
        
        if (preference.isPresent()) {
            UserQualityPreference pref = preference.get();
            Map<String, Object> preferenceData = new HashMap<>();
            preferenceData.put("preferredQuality", pref.getPreferredQuality());
            preferenceData.put("autoSwitch", pref.getAutoSwitch());
            preferenceData.put("bandwidthThreshold", pref.getBandwidthThreshold());
            return preferenceData;
        }
        
        // Return default preference
        Map<String, Object> defaultPreference = new HashMap<>();
        defaultPreference.put("preferredQuality", "auto");
        defaultPreference.put("autoSwitch", true);
        defaultPreference.put("bandwidthThreshold", 5000);
        return defaultPreference;
    }

    // Log quality switch
    public QualitySwitchLog logQualitySwitch(String userIp, Long streamId, String fromQuality, String toQuality, 
                                            QualitySwitchLog.SwitchReason switchReason, Integer bandwidthAvailable, 
                                            Integer latencyMs, BigDecimal packetLoss) {
        
        Optional<StreamSession> stream = streamSessionRepository.findById(streamId);
        if (stream.isEmpty()) {
            throw new RuntimeException("Stream not found");
        }

        QualitySwitchLog switchLog = new QualitySwitchLog();
        switchLog.setUserIp(userIp);
        switchLog.setStreamSession(stream.get());
        switchLog.setFromQuality(fromQuality);
        switchLog.setToQuality(toQuality);
        switchLog.setSwitchReason(switchReason);
        switchLog.setBandwidthAvailable(bandwidthAvailable);
        switchLog.setLatencyMs(latencyMs);
        switchLog.setPacketLoss(packetLoss);
        switchLog.setSwitchTimestamp(LocalDateTime.now());

        QualitySwitchLog savedLog = switchLogRepository.save(switchLog);

        // Send real-time update
        sendQualitySwitchUpdate(streamId, savedLog);

        return savedLog;
    }

    // Start quality-based recording
    public QualityRecording startQualityRecording(Long streamId, String qualityLevel, String createdBy) {
        Optional<StreamSession> stream = streamSessionRepository.findById(streamId);
        if (stream.isEmpty()) {
            throw new RuntimeException("Stream not found");
        }

        // Generate file path
        String timestamp = LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String filePath = String.format("/recordings/stream_%d_%s_%s.mp4", streamId, qualityLevel, timestamp);

        QualityRecording recording = new QualityRecording();
        recording.setStreamSession(stream.get());
        recording.setQualityLevel(qualityLevel);
        recording.setFilePath(filePath);
        recording.setRecordingStart(LocalDateTime.now());
        recording.setIsActive(true);
        recording.setCreatedBy(createdBy);

        QualityRecording savedRecording = recordingRepository.save(recording);

        // Send real-time update
        sendRecordingStartUpdate(streamId, savedRecording);

        return savedRecording;
    }

    // Stop quality recording
    public QualityRecording stopQualityRecording(Long recordingId, Long fileSize, Integer durationSeconds) {
        Optional<QualityRecording> recording = recordingRepository.findById(recordingId);
        if (recording.isEmpty()) {
            throw new RuntimeException("Recording not found");
        }

        QualityRecording rec = recording.get();
        rec.setRecordingEnd(LocalDateTime.now());
        rec.setFileSize(fileSize);
        rec.setDurationSeconds(durationSeconds);
        rec.setIsActive(false);

        QualityRecording savedRecording = recordingRepository.save(rec);

        // Send real-time update
        sendRecordingStopUpdate(rec.getStreamSession().getStreamId(), savedRecording);

        return savedRecording;
    }

    // Get quality recordings for a stream
    public List<Map<String, Object>> getQualityRecordings(Long streamId) {
        List<QualityRecording> recordings = recordingRepository.findByStreamSession_StreamIdOrderByRecordingStartDesc(streamId);
        
        return recordings.stream().map(recording -> {
            Map<String, Object> recordingData = new HashMap<>();
            recordingData.put("recordingId", recording.getRecordingId());
            recordingData.put("qualityLevel", recording.getQualityLevel());
            recordingData.put("filePath", recording.getFilePath());
            recordingData.put("fileSize", recording.getFileSize());
            recordingData.put("durationSeconds", recording.getDurationSeconds());
            recordingData.put("recordingStart", recording.getRecordingStart());
            recordingData.put("recordingEnd", recording.getRecordingEnd());
            recordingData.put("isActive", recording.getIsActive());
            recordingData.put("createdBy", recording.getCreatedBy());
            return recordingData;
        }).collect(Collectors.toList());
    }

    // Get quality analytics for a stream
    public List<Map<String, Object>> getQualityAnalytics(Long streamId) {
        List<QualityAnalytics> analytics = analyticsRepository.findByStreamSession_StreamIdOrderByRecordedAtDesc(streamId);
        
        return analytics.stream().map(analytic -> {
            Map<String, Object> analyticData = new HashMap<>();
            analyticData.put("qualityLevel", analytic.getQualityLevel());
            analyticData.put("viewerCount", analytic.getViewerCount());
            analyticData.put("avgBandwidth", analytic.getAvgBandwidth());
            analyticData.put("avgLatency", analytic.getAvgLatency());
            analyticData.put("avgPacketLoss", analytic.getAvgPacketLoss());
            analyticData.put("switchCount", analytic.getSwitchCount());
            analyticData.put("errorCount", analytic.getErrorCount());
            analyticData.put("recordedAt", analytic.getRecordedAt());
            return analyticData;
        }).collect(Collectors.toList());
    }

    // Get quality switch history for a user
    public List<Map<String, Object>> getQualitySwitchHistory(String userIp, Long streamId, int limit) {
        List<QualitySwitchLog> switchLogs = switchLogRepository.findByUserIpAndStreamSession_StreamIdOrderBySwitchTimestampDesc(userIp, streamId);
        
        return switchLogs.stream().limit(limit).map(switchLog -> {
            Map<String, Object> switchData = new HashMap<>();
            switchData.put("fromQuality", switchLog.getFromQuality());
            switchData.put("toQuality", switchLog.getToQuality());
            switchData.put("switchReason", switchLog.getSwitchReason());
            switchData.put("bandwidthAvailable", switchLog.getBandwidthAvailable());
            switchData.put("latencyMs", switchLog.getLatencyMs());
            switchData.put("packetLoss", switchLog.getPacketLoss());
            switchData.put("switchTimestamp", switchLog.getSwitchTimestamp());
            return switchData;
        }).collect(Collectors.toList());
    }

    // Get quality distribution for a stream
    public Map<String, Object> getQualityDistribution(Long streamId) {
        List<Object[]> distribution = preferenceRepository.getQualityDistributionByStreamId(streamId);
        
        Map<String, Object> distributionData = new HashMap<>();
        for (Object[] row : distribution) {
            String quality = (String) row[0];
            Long count = (Long) row[1];
            distributionData.put(quality, count);
        }
        
        return distributionData;
    }

    // Auto-switch quality based on bandwidth
    public String determineOptimalQuality(Integer bandwidthAvailable, String currentQuality) {
        List<StreamQuality> qualities = qualityRepository.findByIsActiveTrueOrderBySortOrderAsc();
        
        // Remove 'auto' from consideration
        qualities = qualities.stream()
            .filter(q -> !"auto".equals(q.getQualityName()))
            .collect(Collectors.toList());
        
        // Find the best quality that fits the bandwidth
        for (StreamQuality quality : qualities) {
            if (quality.getBitrate() != null) {
                int bitrateKbps = Integer.parseInt(quality.getBitrate().replace("k", ""));
                if (bandwidthAvailable >= bitrateKbps * 1.2) { // 20% buffer
                    return quality.getQualityName();
                }
            }
        }
        
        // If no quality fits, return the lowest quality
        return qualities.get(qualities.size() - 1).getQualityName();
    }

    // Update quality analytics
    public void updateQualityAnalytics(Long streamId, String qualityLevel, Integer viewerCount, 
                                     Integer avgBandwidth, Integer avgLatency, BigDecimal avgPacketLoss, 
                                     Integer switchCount, Integer errorCount) {
        
        Optional<StreamSession> stream = streamSessionRepository.findById(streamId);
        if (stream.isEmpty()) {
            return;
        }

        QualityAnalytics analytics = new QualityAnalytics();
        analytics.setStreamSession(stream.get());
        analytics.setQualityLevel(qualityLevel);
        analytics.setViewerCount(viewerCount);
        analytics.setAvgBandwidth(avgBandwidth);
        analytics.setAvgLatency(avgLatency);
        analytics.setAvgPacketLoss(avgPacketLoss);
        analytics.setSwitchCount(switchCount);
        analytics.setErrorCount(errorCount);
        analytics.setRecordedAt(LocalDateTime.now());

        analyticsRepository.save(analytics);

        // Send real-time update
        sendQualityAnalyticsUpdate(streamId, analytics);
    }

    // Real-time update methods
    private void sendQualityPreferenceUpdate(Long streamId, UserQualityPreference preference) {
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("type", "QUALITY_PREFERENCE_UPDATE");
        updateData.put("streamId", streamId);
        updateData.put("userIp", preference.getUserIp());
        updateData.put("preferredQuality", preference.getPreferredQuality());
        updateData.put("autoSwitch", preference.getAutoSwitch());
        
        messagingTemplate.convertAndSend("/topic/quality", updateData);
    }

    private void sendQualitySwitchUpdate(Long streamId, QualitySwitchLog switchLog) {
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("type", "QUALITY_SWITCH");
        updateData.put("streamId", streamId);
        updateData.put("userIp", switchLog.getUserIp());
        updateData.put("fromQuality", switchLog.getFromQuality());
        updateData.put("toQuality", switchLog.getToQuality());
        updateData.put("switchReason", switchLog.getSwitchReason());
        updateData.put("switchTimestamp", switchLog.getSwitchTimestamp());
        
        messagingTemplate.convertAndSend("/topic/quality", updateData);
    }

    private void sendRecordingStartUpdate(Long streamId, QualityRecording recording) {
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("type", "RECORDING_STARTED");
        updateData.put("streamId", streamId);
        updateData.put("recordingId", recording.getRecordingId());
        updateData.put("qualityLevel", recording.getQualityLevel());
        updateData.put("filePath", recording.getFilePath());
        
        messagingTemplate.convertAndSend("/topic/quality", updateData);
    }

    private void sendRecordingStopUpdate(Long streamId, QualityRecording recording) {
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("type", "RECORDING_STOPPED");
        updateData.put("streamId", streamId);
        updateData.put("recordingId", recording.getRecordingId());
        updateData.put("fileSize", recording.getFileSize());
        updateData.put("durationSeconds", recording.getDurationSeconds());
        
        messagingTemplate.convertAndSend("/topic/quality", updateData);
    }

    private void sendQualityAnalyticsUpdate(Long streamId, QualityAnalytics analytics) {
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("type", "QUALITY_ANALYTICS_UPDATE");
        updateData.put("streamId", streamId);
        updateData.put("qualityLevel", analytics.getQualityLevel());
        updateData.put("viewerCount", analytics.getViewerCount());
        updateData.put("avgBandwidth", analytics.getAvgBandwidth());
        updateData.put("avgLatency", analytics.getAvgLatency());
        
        messagingTemplate.convertAndSend("/topic/quality", updateData);
    }

    /**
     * Send quality analytics updates via WebSocket
     */
    public void sendQualityAnalyticsUpdates() {
        try {
            // Get latest quality analytics for all streams
            List<StreamSession> activeStreams = streamSessionRepository.findByStatusOrderByStartTimeDesc("LIVE");
            
            for (StreamSession stream : activeStreams) {
                List<Map<String, Object>> analytics = getQualityAnalytics(stream.getStreamId());
                
                Map<String, Object> update = Map.of(
                    "type", "QUALITY_ANALYTICS_UPDATE",
                    "streamId", stream.getStreamId(),
                    "analytics", analytics,
                    "timestamp", LocalDateTime.now()
                );
                
                messagingTemplate.convertAndSend("/topic/quality-analytics", update);
            }
        } catch (Exception e) {
            log.error("Error sending quality analytics updates", e);
        }
    }

    public void setUserQualityPreference(String userId, String quality) {
        userQualityPreferences.put(userId, quality);
        notifyQualityChange(userId, quality);
    }

    public String getUserQualityPreference(String userId) {
        return userQualityPreferences.getOrDefault(userId, "auto");
    }

    public void updateUserBandwidth(String userId, int bandwidthKbps) {
        userBandwidth.put(userId, bandwidthKbps);
        
        // Auto-switch quality based on bandwidth
        String currentQuality = getUserQualityPreference(userId);
        if ("auto".equals(currentQuality)) {
            String optimalQuality = determineOptimalQuality(bandwidthKbps);
            if (!optimalQuality.equals(currentQuality)) {
                setUserQualityPreference(userId, optimalQuality);
            }
        }
    }

    private String determineOptimalQuality(int bandwidthKbps) {
        if (bandwidthKbps >= 5000) {
            return "high";
        } else if (bandwidthKbps >= 2500) {
            return "medium";
        } else {
            return "low";
        }
    }

    private void notifyQualityChange(String userId, String quality) {
        Map<String, Object> qualityUpdate = Map.of(
            "userId", userId,
            "quality", quality,
            "timestamp", System.currentTimeMillis()
        );
        
        messagingTemplate.convertAndSend("/topic/quality-update", qualityUpdate);
    }

    public void logQualitySwitch(String userId, String fromQuality, String toQuality) {
        // Log quality switch for analytics
        Map<String, Object> switchLog = Map.of(
            "userId", userId,
            "fromQuality", fromQuality,
            "toQuality", toQuality,
            "timestamp", System.currentTimeMillis()
        );
        
        messagingTemplate.convertAndSend("/topic/quality-switch", switchLog);
    }

    public Map<String, Object> getQualityStats() {
        Map<String, Object> stats = new ConcurrentHashMap<>();
        stats.put("totalUsers", userQualityPreferences.size());
        stats.put("highQualityUsers", userQualityPreferences.values().stream().filter(q -> "high".equals(q)).count());
        stats.put("mediumQualityUsers", userQualityPreferences.values().stream().filter(q -> "medium".equals(q)).count());
        stats.put("lowQualityUsers", userQualityPreferences.values().stream().filter(q -> "low".equals(q)).count());
        stats.put("autoQualityUsers", userQualityPreferences.values().stream().filter(q -> "auto".equals(q)).count());
        
        return stats;
    }
} 