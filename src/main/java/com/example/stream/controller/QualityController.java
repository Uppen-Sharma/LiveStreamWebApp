package com.example.stream.controller;

import com.example.stream.model.QualitySwitchLog;
import com.example.stream.service.QualityManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/quality")
@CrossOrigin(origins = "*")
public class QualityController {

    @Autowired
    private QualityManagementService qualityService;

    // Get available quality levels
    @GetMapping("/qualities")
    public ResponseEntity<?> getAvailableQualities() {
        try {
            List<Map<String, Object>> qualities = qualityService.getAvailableQualities();
            return ResponseEntity.ok(Map.of(
                "success", true,
                "qualities", qualities
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Error fetching qualities: " + e.getMessage()
            ));
        }
    }

    // Set user quality preference
    @PostMapping("/preference")
    public ResponseEntity<?> setQualityPreference(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        try {
            String userIp = getClientIpAddress(httpRequest);
            Long streamId = Long.valueOf(request.get("streamId").toString());
            String preferredQuality = (String) request.get("preferredQuality");
            Boolean autoSwitch = (Boolean) request.get("autoSwitch");
            Integer bandwidthThreshold = Integer.valueOf(request.get("bandwidthThreshold").toString());

            var preference = qualityService.setUserQualityPreference(userIp, streamId, preferredQuality, autoSwitch, bandwidthThreshold);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Quality preference updated successfully",
                "preference", preference
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Error setting quality preference: " + e.getMessage()
            ));
        }
    }

    // Get user quality preference
    @GetMapping("/preference/{streamId}")
    public ResponseEntity<?> getQualityPreference(@PathVariable Long streamId, HttpServletRequest httpRequest) {
        try {
            String userIp = getClientIpAddress(httpRequest);
            Map<String, Object> preference = qualityService.getUserQualityPreference(userIp, streamId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "preference", preference
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Error fetching quality preference: " + e.getMessage()
            ));
        }
    }

    // Log quality switch
    @PostMapping("/switch")
    public ResponseEntity<?> logQualitySwitch(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        try {
            String userIp = getClientIpAddress(httpRequest);
            Long streamId = Long.valueOf(request.get("streamId").toString());
            String fromQuality = (String) request.get("fromQuality");
            String toQuality = (String) request.get("toQuality");
            String switchReasonStr = (String) request.get("switchReason");
            Integer bandwidthAvailable = (Integer) request.get("bandwidthAvailable");
            Integer latencyMs = (Integer) request.get("latencyMs");
            BigDecimal packetLoss = new BigDecimal(request.get("packetLoss").toString());

            QualitySwitchLog.SwitchReason switchReason = QualitySwitchLog.SwitchReason.valueOf(switchReasonStr.toUpperCase());

            var switchLog = qualityService.logQualitySwitch(userIp, streamId, fromQuality, toQuality, 
                                                          switchReason, bandwidthAvailable, latencyMs, packetLoss);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Quality switch logged successfully",
                "switchLog", switchLog
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Error logging quality switch: " + e.getMessage()
            ));
        }
    }

    // Start quality recording
    @PostMapping("/recording/start")
    public ResponseEntity<?> startQualityRecording(@RequestBody Map<String, Object> request) {
        try {
            Long streamId = Long.valueOf(request.get("streamId").toString());
            String qualityLevel = (String) request.get("qualityLevel");
            String createdBy = (String) request.get("createdBy");

            var recording = qualityService.startQualityRecording(streamId, qualityLevel, createdBy);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Quality recording started successfully",
                "recording", recording
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Error starting quality recording: " + e.getMessage()
            ));
        }
    }

    // Stop quality recording
    @PostMapping("/recording/{recordingId}/stop")
    public ResponseEntity<?> stopQualityRecording(@PathVariable Long recordingId, @RequestBody Map<String, Object> request) {
        try {
            Long fileSize = Long.valueOf(request.get("fileSize").toString());
            Integer durationSeconds = Integer.valueOf(request.get("durationSeconds").toString());

            var recording = qualityService.stopQualityRecording(recordingId, fileSize, durationSeconds);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Quality recording stopped successfully",
                "recording", recording
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Error stopping quality recording: " + e.getMessage()
            ));
        }
    }

    // Get quality recordings for a stream
    @GetMapping("/recordings/{streamId}")
    public ResponseEntity<?> getQualityRecordings(@PathVariable Long streamId) {
        try {
            List<Map<String, Object>> recordings = qualityService.getQualityRecordings(streamId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "recordings", recordings
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Error fetching quality recordings: " + e.getMessage()
            ));
        }
    }

    // Get quality analytics for a stream
    @GetMapping("/analytics/{streamId}")
    public ResponseEntity<?> getQualityAnalytics(@PathVariable Long streamId) {
        try {
            List<Map<String, Object>> analytics = qualityService.getQualityAnalytics(streamId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "analytics", analytics
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Error fetching quality analytics: " + e.getMessage()
            ));
        }
    }

    // Get quality switch history for a user
    @GetMapping("/switches/{streamId}")
    public ResponseEntity<?> getQualitySwitchHistory(@PathVariable Long streamId, 
                                                   @RequestParam(defaultValue = "10") int limit,
                                                   HttpServletRequest httpRequest) {
        try {
            String userIp = getClientIpAddress(httpRequest);
            List<Map<String, Object>> switchHistory = qualityService.getQualitySwitchHistory(userIp, streamId, limit);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "switchHistory", switchHistory
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Error fetching quality switch history: " + e.getMessage()
            ));
        }
    }

    // Get quality distribution for a stream
    @GetMapping("/distribution/{streamId}")
    public ResponseEntity<?> getQualityDistribution(@PathVariable Long streamId) {
        try {
            Map<String, Object> distribution = qualityService.getQualityDistribution(streamId);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "distribution", distribution
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Error fetching quality distribution: " + e.getMessage()
            ));
        }
    }

    // Determine optimal quality based on bandwidth
    @PostMapping("/optimal")
    public ResponseEntity<?> determineOptimalQuality(@RequestBody Map<String, Object> request) {
        try {
            Integer bandwidthAvailable = Integer.valueOf(request.get("bandwidthAvailable").toString());
            String currentQuality = (String) request.get("currentQuality");

            String optimalQuality = qualityService.determineOptimalQuality(bandwidthAvailable, currentQuality);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "optimalQuality", optimalQuality,
                "bandwidthAvailable", bandwidthAvailable
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Error determining optimal quality: " + e.getMessage()
            ));
        }
    }

    // Update quality analytics
    @PostMapping("/analytics/update")
    public ResponseEntity<?> updateQualityAnalytics(@RequestBody Map<String, Object> request) {
        try {
            Long streamId = Long.valueOf(request.get("streamId").toString());
            String qualityLevel = (String) request.get("qualityLevel");
            Integer viewerCount = Integer.valueOf(request.get("viewerCount").toString());
            Integer avgBandwidth = Integer.valueOf(request.get("avgBandwidth").toString());
            Integer avgLatency = Integer.valueOf(request.get("avgLatency").toString());
            BigDecimal avgPacketLoss = new BigDecimal(request.get("avgPacketLoss").toString());
            Integer switchCount = Integer.valueOf(request.get("switchCount").toString());
            Integer errorCount = Integer.valueOf(request.get("errorCount").toString());

            qualityService.updateQualityAnalytics(streamId, qualityLevel, viewerCount, avgBandwidth, 
                                                avgLatency, avgPacketLoss, switchCount, errorCount);

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Quality analytics updated successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Error updating quality analytics: " + e.getMessage()
            ));
        }
    }

    // Get client IP address
    private String getClientIpAddress(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty() && !"unknown".equalsIgnoreCase(xForwardedFor)) {
            return xForwardedFor.split(",")[0];
        }
        
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty() && !"unknown".equalsIgnoreCase(xRealIp)) {
            return xRealIp;
        }
        
        return request.getRemoteAddr();
    }
} 