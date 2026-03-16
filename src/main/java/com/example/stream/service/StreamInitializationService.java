package com.example.stream.service;

import com.example.stream.model.StreamSession;
import com.example.stream.model.StreamQuality;
import com.example.stream.repository.StreamSessionRepository;
import com.example.stream.repository.StreamQualityRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class StreamInitializationService implements CommandLineRunner {

    @Autowired
    private StreamSessionRepository streamSessionRepository;
    
    @Autowired
    private AnalyticsService analyticsService;
    
    @Autowired
    private QualityManagementService qualityManagementService;
    
    @Autowired
    private StreamQualityRepository streamQualityRepository;

    @Override
    public void run(String... args) throws Exception {
        // Initialize database with a default stream session if none exists
        initializeDefaultStream();
        
        // Initialize quality levels
        initializeQualityLevels();
    }

    private void initializeDefaultStream() {
        Optional<StreamSession> existingStream = streamSessionRepository.findFirstByStatusOrderByStartTimeDesc("LIVE");
        
        if (existingStream.isEmpty()) {
            StreamSession defaultStream = new StreamSession();
            defaultStream.setTitle("Live Streaming Session");
            defaultStream.setStartTime(LocalDateTime.now());
            defaultStream.setStatus("LIVE");
            defaultStream.setCurrentViewers(1247);
            defaultStream.setPeakViewers(1389);
            defaultStream.setTotalWatchTime(0L);
            defaultStream.setCreatedAt(LocalDateTime.now());
            defaultStream.setUpdatedAt(LocalDateTime.now());
            
            streamSessionRepository.save(defaultStream);
            System.out.println("Default stream session initialized");
        }
    }

    private void initializeQualityLevels() {
        // Check if quality levels already exist
        if (streamQualityRepository.count() == 0) {
            // Create default quality levels
            StreamQuality highQuality = new StreamQuality();
            highQuality.setQualityName("high");
            highQuality.setQualityLabel("High Quality");
            highQuality.setResolution("1920x1080");
            highQuality.setBitrate("5000");
            highQuality.setIsActive(true);
            highQuality.setSortOrder(1);
            highQuality.setCreatedAt(LocalDateTime.now());
            streamQualityRepository.save(highQuality);
            
            StreamQuality mediumQuality = new StreamQuality();
            mediumQuality.setQualityName("medium");
            mediumQuality.setQualityLabel("Medium Quality");
            mediumQuality.setResolution("1280x720");
            mediumQuality.setBitrate("2500");
            mediumQuality.setIsActive(true);
            mediumQuality.setSortOrder(2);
            mediumQuality.setCreatedAt(LocalDateTime.now());
            streamQualityRepository.save(mediumQuality);
            
            StreamQuality lowQuality = new StreamQuality();
            lowQuality.setQualityName("low");
            lowQuality.setQualityLabel("Low Quality");
            lowQuality.setResolution("854x480");
            lowQuality.setBitrate("1000");
            lowQuality.setIsActive(true);
            lowQuality.setSortOrder(3);
            lowQuality.setCreatedAt(LocalDateTime.now());
            streamQualityRepository.save(lowQuality);
            
            StreamQuality autoQuality = new StreamQuality();
            autoQuality.setQualityName("auto");
            autoQuality.setQualityLabel("Auto Quality");
            autoQuality.setResolution("Adaptive");
            autoQuality.setBitrate("0");
            autoQuality.setIsActive(true);
            autoQuality.setSortOrder(0);
            autoQuality.setCreatedAt(LocalDateTime.now());
            streamQualityRepository.save(autoQuality);
            
            System.out.println("Quality levels initialized");
        }
    }

    // Send real-time updates every 5 seconds
    @Scheduled(fixedRate = 5000)
    public void sendRealTimeUpdates() {
        try {
            analyticsService.sendRealTimeUpdates();
        } catch (Exception e) {
            System.err.println("Error sending real-time updates: " + e.getMessage());
        }
    }

    // Update viewer count periodically (simulate real viewer changes)
    @Scheduled(fixedRate = 10000)
    public void simulateViewerChanges() {
        try {
            Optional<StreamSession> activeStream = streamSessionRepository.findFirstByStatusOrderByStartTimeDesc("LIVE");
            if (activeStream.isPresent()) {
                StreamSession stream = activeStream.get();
                int currentViewers = stream.getCurrentViewers() != null ? stream.getCurrentViewers() : 1247;
                
                // Simulate small viewer count changes
                int change = (int) (Math.random() * 20) - 10; // -10 to +10
                int newViewerCount = Math.max(1000, currentViewers + change);
                
                analyticsService.updateViewerCount(newViewerCount);
            }
        } catch (Exception e) {
            System.err.println("Error simulating viewer changes: " + e.getMessage());
        }
    }
    
    // Send quality analytics updates every 10 seconds
    @Scheduled(fixedRate = 10000)
    public void sendQualityAnalyticsUpdates() {
        try {
            qualityManagementService.sendQualityAnalyticsUpdates();
        } catch (Exception e) {
            System.err.println("Error sending quality analytics updates: " + e.getMessage());
        }
    }
} 