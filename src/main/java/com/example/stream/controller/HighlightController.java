package com.example.stream.controller;

import com.example.stream.model.HighlightReaction;
import com.example.stream.model.StreamHighlight;
import com.example.stream.service.HighlightService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/highlights")
@CrossOrigin(origins = "*")
public class HighlightController {

    @Autowired
    private HighlightService highlightService;

    // Create a new highlight
    @PostMapping("/create")
    public ResponseEntity<?> createHighlight(@RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        try {
            Long streamId = Long.valueOf(request.get("streamId").toString());
            String title = (String) request.get("title");
            String description = (String) request.get("description");
            Integer timestampSeconds = Integer.valueOf(request.get("timestampSeconds").toString());
            String createdBy = (String) request.get("createdBy");
            String type = (String) request.get("type");
            @SuppressWarnings("unchecked")
            List<String> tags = (List<String>) request.get("tags");

            StreamHighlight.HighlightType highlightType = StreamHighlight.HighlightType.valueOf(type.toUpperCase());

            StreamHighlight highlight = highlightService.createHighlight(
                streamId, title, description, timestampSeconds, createdBy, highlightType, tags
            );

            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Highlight created successfully",
                "highlight", highlight
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Error creating highlight: " + e.getMessage()
            ));
        }
    }

    // Get highlights for a stream
    @GetMapping("/stream/{streamId}")
    public ResponseEntity<?> getHighlightsForStream(@PathVariable Long streamId) {
        try {
            List<Map<String, Object>> highlights = highlightService.getHighlightsForStream(streamId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "highlights", highlights
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Error fetching highlights: " + e.getMessage()
            ));
        }
    }

    // Add reaction to highlight
    @PostMapping("/{highlightId}/reaction")
    public ResponseEntity<?> addReaction(@PathVariable Long highlightId, @RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        try {
            String userIp = getClientIpAddress(httpRequest);
            String reactionType = (String) request.get("reactionType");
            
            HighlightReaction.ReactionType type = HighlightReaction.ReactionType.valueOf(reactionType.toUpperCase());
            
            highlightService.addReaction(highlightId, userIp, type);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Reaction added successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Error adding reaction: " + e.getMessage()
            ));
        }
    }

    // Add comment to highlight
    @PostMapping("/{highlightId}/comment")
    public ResponseEntity<?> addComment(@PathVariable Long highlightId, @RequestBody Map<String, Object> request, HttpServletRequest httpRequest) {
        try {
            String username = (String) request.get("username");
            String content = (String) request.get("content");
            String userIp = getClientIpAddress(httpRequest);
            
            var comment = highlightService.addComment(highlightId, username, content, userIp);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Comment added successfully",
                "comment", comment
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Error adding comment: " + e.getMessage()
            ));
        }
    }

    // Get comments for highlight
    @GetMapping("/{highlightId}/comments")
    public ResponseEntity<?> getCommentsForHighlight(@PathVariable Long highlightId) {
        try {
            List<Map<String, Object>> comments = highlightService.getCommentsForHighlight(highlightId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "comments", comments
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Error fetching comments: " + e.getMessage()
            ));
        }
    }

    // Delete highlight
    @DeleteMapping("/{highlightId}")
    public ResponseEntity<?> deleteHighlight(@PathVariable Long highlightId, @RequestBody Map<String, Object> request) {
        try {
            String createdBy = (String) request.get("createdBy");
            highlightService.deleteHighlight(highlightId, createdBy);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Highlight deleted successfully"
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Error deleting highlight: " + e.getMessage()
            ));
        }
    }

    // Jump to highlight timestamp
    @GetMapping("/{highlightId}/timestamp")
    public ResponseEntity<?> getHighlightTimestamp(@PathVariable Long highlightId) {
        try {
            Map<String, Object> timestampData = highlightService.getHighlightTimestamp(highlightId);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "timestamp", timestampData
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Error fetching timestamp: " + e.getMessage()
            ));
        }
    }

    // Search highlights by tags
    @PostMapping("/search")
    public ResponseEntity<?> searchHighlightsByTags(@RequestBody Map<String, Object> request) {
        try {
            @SuppressWarnings("unchecked")
            List<String> tags = (List<String>) request.get("tags");
            
            List<StreamHighlight> highlights = highlightService.searchHighlightsByTags(tags);
            
            return ResponseEntity.ok(Map.of(
                "success", true,
                "highlights", highlights
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Error searching highlights: " + e.getMessage()
            ));
        }
    }

    // Get popular highlights
    @GetMapping("/popular")
    public ResponseEntity<?> getPopularHighlights(@RequestParam(defaultValue = "10") int limit) {
        try {
            List<Map<String, Object>> highlights = highlightService.getPopularHighlights(limit);
            return ResponseEntity.ok(Map.of(
                "success", true,
                "highlights", highlights
            ));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "success", false,
                "message", "Error fetching popular highlights: " + e.getMessage()
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