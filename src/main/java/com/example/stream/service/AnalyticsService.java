package com.example.stream.service;

import com.example.stream.model.*;
import com.example.stream.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AnalyticsService {

    @Autowired
    private StreamSessionRepository streamSessionRepository;
    
    @Autowired
    private ChatMessageRepository chatMessageRepository;
    
    @Autowired
    private EngagementStatRepository engagementStatRepository;
    
    @Autowired
    private RecordingLogRepository recordingLogRepository;
    
    @Autowired
    private QualityPreferenceRepository qualityPreferenceRepository;
    
    @Autowired
    private RecordingSessionRepository recordingSessionRepository;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // Real-time Viewer Analytics
    public void updateViewerCount(int newViewerCount) {
        Optional<StreamSession> activeStream = streamSessionRepository.findFirstByStatusOrderByStartTimeDesc("LIVE");
        if (activeStream.isPresent()) {
            StreamSession stream = activeStream.get();
            stream.setCurrentViewers(newViewerCount);
            
            // Update peak viewers if necessary
            if (newViewerCount > stream.getPeakViewers()) {
                stream.setPeakViewers(newViewerCount);
            }
            
            streamSessionRepository.save(stream);
            
            // Send real-time update to frontend
            Map<String, Object> viewerData = new HashMap<>();
            viewerData.put("currentViewers", newViewerCount);
            viewerData.put("peakViewers", stream.getPeakViewers());
            viewerData.put("timestamp", LocalDateTime.now());
            
            messagingTemplate.convertAndSend("/topic/viewers", viewerData);
        }
    }

    // Real-time Engagement Analytics
    public void updateEngagementStats(Long streamId, int likes, int dislikes, int totalMessages) {
        EngagementStat engagementStat = new EngagementStat();
        engagementStat.setStreamSession(streamSessionRepository.findById(streamId).orElse(null));
        engagementStat.setLikes(likes);
        engagementStat.setDislikes(dislikes);
        engagementStat.setTotalMessages(totalMessages);
        engagementStat.setTimestamp(LocalDateTime.now());
        
        // Calculate like ratio
        int total = likes + dislikes;
        double likeRatio = total > 0 ? (double) likes / total * 100 : 0.0;
        engagementStat.setLikeRatio(Math.round(likeRatio * 10) / 10.0);
        
        engagementStatRepository.save(engagementStat);
        
        // Send real-time update to frontend
        Map<String, Object> engagementData = new HashMap<>();
        engagementData.put("likes", likes);
        engagementData.put("dislikes", dislikes);
        engagementData.put("totalMessages", totalMessages);
        engagementData.put("likeRatio", likeRatio);
        engagementData.put("timestamp", LocalDateTime.now());
        
        messagingTemplate.convertAndSend("/topic/engagement", engagementData);
    }

    // Real-time Chat Analytics
    public void addChatMessage(Long streamId, String username, String content, String userIp) {
        StreamSession stream = streamSessionRepository.findById(streamId).orElse(null);
        if (stream != null) {
            ChatMessageEntity chatMessage = new ChatMessageEntity();
            chatMessage.setStreamSession(stream);
            chatMessage.setUsername(username);
            chatMessage.setContent(content);
            chatMessage.setUserIp(userIp);
            chatMessage.setTimestamp(LocalDateTime.now());
            chatMessage.setIsDeleted(false);
            
            chatMessageRepository.save(chatMessage);
            
            // Get updated top chatters
            List<Object[]> topChatters = chatMessageRepository.findTopChattersByStreamId(streamId);
            List<Map<String, Object>> topChattersData = topChatters.stream()
                .limit(3)
                .map(row -> {
                    Map<String, Object> chatter = new HashMap<>();
                    chatter.put("username", row[0]);
                    chatter.put("messageCount", row[1]);
                    return chatter;
                })
                .collect(Collectors.toList());
            
            // Send real-time update to frontend
            messagingTemplate.convertAndSend("/topic/top-chatters", topChattersData);
        }
    }

    // Get Real-time Viewer Chart Data
    public Map<String, Object> getViewerChartData() {
        List<ViewerCount> viewerData = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        
        // Get viewer data for last 30 minutes
        for (int i = 30; i >= 0; i--) {
            LocalDateTime time = now.minusMinutes(i);
            String timeString = time.format(formatter);
            
            // Get viewer count for this time (you can implement actual historical data storage)
            Integer viewerCount = streamSessionRepository.findCurrentViewerCount();
            if (viewerCount == null) viewerCount = 1200; // Default fallback
            
            viewerData.add(new ViewerCount(timeString, viewerCount));
        }
        
        Map<String, Object> chartData = new HashMap<>();
        chartData.put("labels", viewerData.stream().map(ViewerCount::getTime).collect(Collectors.toList()));
        chartData.put("data", viewerData.stream().map(ViewerCount::getViewers).collect(Collectors.toList()));
        
        return chartData;
    }

    // Get Real-time Engagement Chart Data
    public Map<String, Object> getEngagementChartData() {
        Optional<StreamSession> activeStream = streamSessionRepository.findFirstByStatusOrderByStartTimeDesc("LIVE");
        if (activeStream.isPresent()) {
            Long streamId = activeStream.get().getStreamId();
            
            Integer totalLikes = engagementStatRepository.getTotalLikesByStreamId(streamId);
            Integer totalDislikes = engagementStatRepository.getTotalDislikesByStreamId(streamId);
            Integer totalMessages = engagementStatRepository.getTotalMessagesByStreamId(streamId);
            Long totalFollowers = 86L; // You can implement actual follower tracking
            
            Map<String, Object> chartData = new HashMap<>();
            chartData.put("labels", Arrays.asList("Likes", "Comments", "Shares", "Follows"));
            chartData.put("data", Arrays.asList(
                totalLikes != null ? totalLikes : 0,
                totalMessages != null ? totalMessages : 0,
                40, // Placeholder for shares
                totalFollowers
            ));
            
            return chartData;
        }
        
        return new HashMap<>();
    }

    // Get Real-time Performance Chart Data
    public Map<String, Object> getPerformanceChartData() {
        List<PerformanceData> performanceData = new ArrayList<>();
        
        // Get performance data for last 7 days
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(7);
        
        List<StreamSession> streams = streamSessionRepository.findByStartTimeBetweenOrderByStartTimeDesc(startDate, endDate);
        
        for (int i = 6; i >= 0; i--) {
            LocalDateTime date = endDate.minusDays(i);
            String dateString = date.format(DateTimeFormatter.ofPattern("M/dd"));
            
            // Calculate views and watch time for this date
            int views = streams.stream()
                .filter(s -> s.getStartTime().toLocalDate().equals(date.toLocalDate()))
                .mapToInt(s -> s.getCurrentViewers() != null ? s.getCurrentViewers() : 0)
                .sum();
            
            double avgWatchTime = streams.stream()
                .filter(s -> s.getStartTime().toLocalDate().equals(date.toLocalDate()))
                .mapToDouble(s -> s.getTotalWatchTime() != null ? s.getTotalWatchTime() / 60.0 : 0)
                .average()
                .orElse(0.0);
            
            performanceData.add(new PerformanceData(dateString, views, avgWatchTime));
        }
        
        Map<String, Object> chartData = new HashMap<>();
        chartData.put("labels", performanceData.stream().map(PerformanceData::getDate).collect(Collectors.toList()));
        chartData.put("data", performanceData.stream().map(PerformanceData::getViews).collect(Collectors.toList()));
        
        return chartData;
    }

    // Get Current Stream Metrics
    public Map<String, Object> getCurrentStreamMetrics() {
        Optional<StreamSession> activeStream = streamSessionRepository.findFirstByStatusOrderByStartTimeDesc("LIVE");
        if (activeStream.isPresent()) {
            StreamSession stream = activeStream.get();
            Long streamId = stream.getStreamId();
            
            Integer totalLikes = engagementStatRepository.getTotalLikesByStreamId(streamId);
            Integer totalDislikes = engagementStatRepository.getTotalDislikesByStreamId(streamId);
            Integer totalMessages = engagementStatRepository.getTotalMessagesByStreamId(streamId);
            Double avgLikeRatio = engagementStatRepository.getAverageLikeRatioByStreamId(streamId);
            
            Map<String, Object> metrics = new HashMap<>();
            metrics.put("currentViewers", stream.getCurrentViewers());
            metrics.put("peakViewers", stream.getPeakViewers());
            metrics.put("likes", totalLikes != null ? totalLikes : 0);
            metrics.put("dislikes", totalDislikes != null ? totalDislikes : 0);
            metrics.put("totalMessages", totalMessages != null ? totalMessages : 0);
            metrics.put("likeRatio", avgLikeRatio != null ? avgLikeRatio : 0.0);
            metrics.put("newFollowers", 86); // Placeholder
            metrics.put("streamStartTime", stream.getStartTime());
            
            return metrics;
        }
        
        return new HashMap<>();
    }

    // Send Real-time Updates
    public void sendRealTimeUpdates() {
        // Send viewer chart data
        messagingTemplate.convertAndSend("/topic/viewers", getViewerChartData());
        
        // Send engagement chart data
        messagingTemplate.convertAndSend("/topic/engagement", getEngagementChartData());
        
        // Send performance chart data
        messagingTemplate.convertAndSend("/topic/performance", getPerformanceChartData());
        
        // Send current metrics
        messagingTemplate.convertAndSend("/topic/metrics", getCurrentStreamMetrics());
    }
} 