package com.example.stream.service;

import com.example.stream.model.*;
import com.example.stream.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

@Service
public class StreamService {

    @Autowired
    private SimpMessagingTemplate messagingTemplate;
    
    @Autowired
    private AnalyticsService analyticsService;
    
    @Autowired
    private StreamSessionRepository streamSessionRepository;
    
    @Autowired
    private ChatMessageRepository chatMessageRepository;
    
    @Autowired
    private EngagementStatRepository engagementStatRepository;

    private final LocalDateTime streamStartTime;
    private final StreamMetrics metrics;
    private final List<ChatMessage> chatMessages;
    private final Map<String, Integer> userMessageCount;
    private final List<ViewerCount> viewerData;
    private final List<PerformanceData> performanceData;

    public StreamService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
        this.streamStartTime = LocalDateTime.now();
        this.metrics = new StreamMetrics();
        this.chatMessages = new CopyOnWriteArrayList<>();
        this.userMessageCount = new ConcurrentHashMap<>();
        
        initializeSampleChatMessages();
        
        this.viewerData = generateViewerData();
        
        this.performanceData = generatePerformanceData();
    }

    private void initializeSampleChatMessages() {
        chatMessages.add(new ChatMessage("User1", "Great stream today!", LocalDateTime.now().minusMinutes(5)));
        chatMessages.add(new ChatMessage("User2", "The content is awesome!", LocalDateTime.now().minusMinutes(4)));
        chatMessages.add(new ChatMessage("User3", "How often do you stream?", LocalDateTime.now().minusMinutes(3)));
        chatMessages.add(new ChatMessage("User4", "👍 👍 👍", LocalDateTime.now().minusMinutes(2)));
        chatMessages.add(new ChatMessage("User5", "Can you explain that last part again?", LocalDateTime.now().minusMinutes(1)));

        userMessageCount.put("User1", 1);
        userMessageCount.put("User2", 1);
        userMessageCount.put("User3", 1);
        userMessageCount.put("User4", 1);
        userMessageCount.put("User5", 1);
    }

    private List<ViewerCount> generateViewerData() {
        List<ViewerCount> data = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        
        Random random = new Random();
        int baseViewers = 1200;
        
        for (int i = 30; i >= 0; i--) {
            LocalDateTime time = now.minusMinutes(i);
            String timeString = time.format(formatter);

            int randomFactor = random.nextInt(200) - 100;
            int trendFactor = i < 15 ? (15 - i) * 10 : 0;
            int viewers = Math.max(0, Math.round(baseViewers + randomFactor + trendFactor));
            
            data.add(new ViewerCount(timeString, viewers));
        }
        
        return data;
    }

    private List<PerformanceData> generatePerformanceData() {
        List<PerformanceData> data = new ArrayList<>();
        
        data.add(new PerformanceData("3/12", 18452, 17.2));
        data.add(new PerformanceData("3/13", 19873, 18.5));
        data.add(new PerformanceData("3/14", 17645, 16.8));
        data.add(new PerformanceData("3/15", 21037, 19.3));
        data.add(new PerformanceData("3/16", 22185, 20.1));
        data.add(new PerformanceData("3/17", 20973, 19.2));
        data.add(new PerformanceData("3/18", 24389, 18.4));
        
        return data;
    }

    public StreamMetrics incrementLike() {
        // Get current active stream
        Optional<StreamSession> activeStream = streamSessionRepository.findFirstByStatusOrderByStartTimeDesc("LIVE");
        if (activeStream.isPresent()) {
            Long streamId = activeStream.get().getStreamId();
            
            // Get current engagement stats
            Optional<EngagementStat> currentStats = engagementStatRepository.findFirstByStreamSession_StreamIdOrderByTimestampDesc(streamId);
            int currentLikes = currentStats.map(EngagementStat::getLikes).orElse(0);
            int currentDislikes = currentStats.map(EngagementStat::getDislikes).orElse(0);
            int currentMessages = currentStats.map(EngagementStat::getTotalMessages).orElse(0);
            
            // Update engagement stats via analytics service
            analyticsService.updateEngagementStats(streamId, currentLikes + 1, currentDislikes, currentMessages);
            
            // Update local metrics for backward compatibility
            metrics.setLikes(currentLikes + 1);
            metrics.setDislikes(currentDislikes);
            updateLikeRatio();
        }
        
        return metrics;
    }

    public StreamMetrics incrementDislike() {
        // Get current active stream
        Optional<StreamSession> activeStream = streamSessionRepository.findFirstByStatusOrderByStartTimeDesc("LIVE");
        if (activeStream.isPresent()) {
            Long streamId = activeStream.get().getStreamId();
            
            // Get current engagement stats
            Optional<EngagementStat> currentStats = engagementStatRepository.findFirstByStreamSession_StreamIdOrderByTimestampDesc(streamId);
            int currentLikes = currentStats.map(EngagementStat::getLikes).orElse(0);
            int currentDislikes = currentStats.map(EngagementStat::getDislikes).orElse(0);
            int currentMessages = currentStats.map(EngagementStat::getTotalMessages).orElse(0);
            
            // Update engagement stats via analytics service
            analyticsService.updateEngagementStats(streamId, currentLikes, currentDislikes + 1, currentMessages);
            
            // Update local metrics for backward compatibility
            metrics.setLikes(currentLikes);
            metrics.setDislikes(currentDislikes + 1);
            updateLikeRatio();
        }
        
        return metrics;
    }

    private void updateLikeRatio() {
        int total = metrics.getLikes() + metrics.getDislikes();
        if (total > 0) {
            double ratio = (double) metrics.getLikes() / total * 100;
            metrics.setLikeRatio(Math.round(ratio * 10) / 10.0);
        } else {
            metrics.setLikeRatio(0.0);
        }
    }

    public ChatMessage addChatMessage(ChatMessage message) {
        // Get current active stream
        Optional<StreamSession> activeStream = streamSessionRepository.findFirstByStatusOrderByStartTimeDesc("LIVE");
        if (activeStream.isPresent()) {
            Long streamId = activeStream.get().getStreamId();
            
            // Add chat message via analytics service
            analyticsService.addChatMessage(streamId, message.getUsername(), message.getContent(), "127.0.0.1");
        }
        
        // Update local data for backward compatibility
        chatMessages.add(message);
        userMessageCount.merge(message.getUsername(), 1, Integer::sum);
        
        return message;
    }

    public List<TopChatter> getTopChatters() {
        // Get current active stream
        Optional<StreamSession> activeStream = streamSessionRepository.findFirstByStatusOrderByStartTimeDesc("LIVE");
        if (activeStream.isPresent()) {
            Long streamId = activeStream.get().getStreamId();
            
            // Get top chatters from database
            List<Object[]> topChattersData = chatMessageRepository.findTopChattersByStreamId(streamId);
            return topChattersData.stream()
                .limit(3)
                .map(row -> new TopChatter((String) row[0], ((Number) row[1]).intValue()))
                .collect(Collectors.toList());
        }
        
        // Fallback to local data
        return userMessageCount.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(3)
                .map(entry -> new TopChatter(entry.getKey(), entry.getValue()))
                .collect(Collectors.toList());
    }

    public StreamMetrics getStreamMetrics() {
        // Get current active stream
        Optional<StreamSession> activeStream = streamSessionRepository.findFirstByStatusOrderByStartTimeDesc("LIVE");
        if (activeStream.isPresent()) {
            StreamSession stream = activeStream.get();
            
            // Update metrics from database
            metrics.setCurrentViewers(stream.getCurrentViewers() != null ? stream.getCurrentViewers() : 1247);
            metrics.setPeakViewers(stream.getPeakViewers() != null ? stream.getPeakViewers() : 1389);
            
            // Get engagement stats
            Optional<EngagementStat> engagementStat = engagementStatRepository.findFirstByStreamSession_StreamIdOrderByTimestampDesc(stream.getStreamId());
            if (engagementStat.isPresent()) {
                EngagementStat stat = engagementStat.get();
                metrics.setLikes(stat.getLikes() != null ? stat.getLikes() : 0);
                metrics.setDislikes(stat.getDislikes() != null ? stat.getDislikes() : 0);
                metrics.setLikeRatio(stat.getLikeRatio() != null ? stat.getLikeRatio() : 0.0);
            }
        }
        
        return metrics;
    }

    public List<ChatMessage> getChatMessages() {
        // Get current active stream
        Optional<StreamSession> activeStream = streamSessionRepository.findFirstByStatusOrderByStartTimeDesc("LIVE");
        if (activeStream.isPresent()) {
            Long streamId = activeStream.get().getStreamId();
            
            // Get chat messages from database
            List<ChatMessageEntity> dbMessages = chatMessageRepository.findByStreamSession_StreamIdOrderByTimestampDesc(streamId);
            return dbMessages.stream()
                .map(msg -> new ChatMessage(msg.getUsername(), msg.getContent(), msg.getTimestamp()))
                .collect(Collectors.toList());
        }
        
        return chatMessages;
    }

    public List<ViewerCount> getViewerData() {
        // Get real-time viewer data from analytics service
        Map<String, Object> chartData = analyticsService.getViewerChartData();
        
        List<String> labels = (List<String>) chartData.get("labels");
        List<Integer> data = (List<Integer>) chartData.get("data");
        
        List<ViewerCount> viewerData = new ArrayList<>();
        for (int i = 0; i < labels.size(); i++) {
            viewerData.add(new ViewerCount(labels.get(i), data.get(i)));
        }
        
        return viewerData;
    }

    public List<PerformanceData> getPerformanceData() {
        // Get real-time performance data from analytics service
        Map<String, Object> chartData = analyticsService.getPerformanceChartData();
        
        List<String> labels = (List<String>) chartData.get("labels");
        List<Integer> data = (List<Integer>) chartData.get("data");
        
        List<PerformanceData> performanceData = new ArrayList<>();
        for (int i = 0; i < labels.size(); i++) {
            performanceData.add(new PerformanceData(labels.get(i), data.get(i), 18.0)); // Default watch time
        }
        
        return performanceData;
    }

    public LocalDateTime getStreamStartTime() {
        // Get current active stream start time
        Optional<StreamSession> activeStream = streamSessionRepository.findFirstByStatusOrderByStartTimeDesc("LIVE");
        return activeStream.map(StreamSession::getStartTime).orElse(streamStartTime);
    }
    
    public StreamData getStreamData() {
        StreamData data = new StreamData();
        data.setMetrics(getStreamMetrics());
        data.setTopChatters(getTopChatters());
        data.setViewerData(getViewerData());
        data.setPerformanceData(getPerformanceData());
        data.setStreamStartTime(getStreamStartTime());
        return data;
    }
}