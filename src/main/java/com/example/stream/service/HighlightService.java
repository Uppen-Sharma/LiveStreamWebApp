package com.example.stream.service;

import com.example.stream.model.*;
import com.example.stream.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class HighlightService {

    @Autowired
    private StreamHighlightRepository highlightRepository;
    
    @Autowired
    private HighlightTagRepository tagRepository;
    
    @Autowired
    private HighlightReactionRepository reactionRepository;
    
    @Autowired
    private HighlightCommentRepository commentRepository;
    
    @Autowired
    private StreamSessionRepository streamSessionRepository;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    // Create a new highlight
    public StreamHighlight createHighlight(Long streamId, String title, String description, 
                                         Integer timestampSeconds, String createdBy, 
                                         StreamHighlight.HighlightType type, List<String> tags) {
        
        Optional<StreamSession> stream = streamSessionRepository.findById(streamId);
        if (stream.isEmpty()) {
            throw new RuntimeException("Stream not found");
        }

        StreamHighlight highlight = new StreamHighlight();
        highlight.setStreamSession(stream.get());
        highlight.setTitle(title);
        highlight.setDescription(description);
        highlight.setTimestampSeconds(timestampSeconds);
        highlight.setCreatedBy(createdBy);
        highlight.setHighlightType(type);
        highlight.setIsPublic(true);
        highlight.setCreatedAt(LocalDateTime.now());
        highlight.setUpdatedAt(LocalDateTime.now());

        // Format timestamp
        int minutes = timestampSeconds / 60;
        int seconds = timestampSeconds % 60;
        highlight.setTimestampFormatted(String.format("%02d:%02d", minutes, seconds));

        StreamHighlight savedHighlight = highlightRepository.save(highlight);

        // Add tags if provided
        if (tags != null && !tags.isEmpty()) {
            for (String tagName : tags) {
                HighlightTag tag = new HighlightTag();
                tag.setHighlight(savedHighlight);
                tag.setTagName(tagName.toLowerCase());
                tag.setCreatedAt(LocalDateTime.now());
                tagRepository.save(tag);
            }
        }

        // Send real-time update
        sendHighlightUpdate(streamId, savedHighlight);

        return savedHighlight;
    }

    // Get highlights for a stream
    public List<Map<String, Object>> getHighlightsForStream(Long streamId) {
        List<StreamHighlight> highlights = highlightRepository.findByStreamSession_StreamIdAndIsPublicTrueOrderByTimestampSecondsAsc(streamId);
        
        return highlights.stream().map(highlight -> {
            Map<String, Object> highlightData = new HashMap<>();
            highlightData.put("highlightId", highlight.getHighlightId());
            highlightData.put("title", highlight.getTitle());
            highlightData.put("description", highlight.getDescription());
            highlightData.put("timestampSeconds", highlight.getTimestampSeconds());
            highlightData.put("timestampFormatted", highlight.getTimestampFormatted());
            highlightData.put("createdBy", highlight.getCreatedBy());
            highlightData.put("highlightType", highlight.getHighlightType());
            highlightData.put("createdAt", highlight.getCreatedAt());
            
            // Get tags
            List<String> tags = highlight.getTags().stream()
                .map(HighlightTag::getTagName)
                .collect(Collectors.toList());
            highlightData.put("tags", tags);
            
            // Get reaction count
            long reactionCount = highlight.getReactions().stream()
                .filter(r -> r.getReactionType() == HighlightReaction.ReactionType.LIKE)
                .count();
            highlightData.put("likeCount", reactionCount);
            
            // Get comment count
            long commentCount = highlight.getComments().stream()
                .filter(c -> !c.getIsDeleted())
                .count();
            highlightData.put("commentCount", commentCount);
            
            return highlightData;
        }).collect(Collectors.toList());
    }

    // Add reaction to highlight
    public void addReaction(Long highlightId, String userIp, HighlightReaction.ReactionType reactionType) {
        Optional<StreamHighlight> highlight = highlightRepository.findById(highlightId);
        if (highlight.isEmpty()) {
            throw new RuntimeException("Highlight not found");
        }

        // Check if user already reacted
        Optional<HighlightReaction> existingReaction = reactionRepository.findByHighlight_HighlightIdAndUserIp(highlightId, userIp);
        
        if (existingReaction.isPresent()) {
            // Update existing reaction
            HighlightReaction reaction = existingReaction.get();
            reaction.setReactionType(reactionType);
            reaction.setCreatedAt(LocalDateTime.now());
            reactionRepository.save(reaction);
        } else {
            // Create new reaction
            HighlightReaction reaction = new HighlightReaction();
            reaction.setHighlight(highlight.get());
            reaction.setUserIp(userIp);
            reaction.setReactionType(reactionType);
            reaction.setCreatedAt(LocalDateTime.now());
            reactionRepository.save(reaction);
        }

        // Send real-time update
        sendHighlightReactionUpdate(highlightId);
    }

    // Add comment to highlight
    public HighlightComment addComment(Long highlightId, String username, String content, String userIp) {
        Optional<StreamHighlight> highlight = highlightRepository.findById(highlightId);
        if (highlight.isEmpty()) {
            throw new RuntimeException("Highlight not found");
        }

        HighlightComment comment = new HighlightComment();
        comment.setHighlight(highlight.get());
        comment.setUsername(username);
        comment.setContent(content);
        comment.setUserIp(userIp);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setIsDeleted(false);

        HighlightComment savedComment = commentRepository.save(comment);

        // Send real-time update
        sendHighlightCommentUpdate(highlightId, savedComment);

        return savedComment;
    }

    // Get comments for highlight
    public List<Map<String, Object>> getCommentsForHighlight(Long highlightId) {
        List<HighlightComment> comments = commentRepository.findByHighlight_HighlightIdAndIsDeletedFalseOrderByCreatedAtAsc(highlightId);
        
        return comments.stream().map(comment -> {
            Map<String, Object> commentData = new HashMap<>();
            commentData.put("commentId", comment.getCommentId());
            commentData.put("username", comment.getUsername());
            commentData.put("content", comment.getContent());
            commentData.put("createdAt", comment.getCreatedAt());
            return commentData;
        }).collect(Collectors.toList());
    }

    // Delete highlight
    public void deleteHighlight(Long highlightId, String createdBy) {
        Optional<StreamHighlight> highlight = highlightRepository.findById(highlightId);
        if (highlight.isEmpty()) {
            throw new RuntimeException("Highlight not found");
        }

        if (!highlight.get().getCreatedBy().equals(createdBy)) {
            throw new RuntimeException("Not authorized to delete this highlight");
        }

        Long streamId = highlight.get().getStreamSession().getStreamId();
        highlightRepository.delete(highlight.get());

        // Send real-time update
        sendHighlightDeletedUpdate(streamId, highlightId);
    }

    // Jump to highlight timestamp
    public Map<String, Object> getHighlightTimestamp(Long highlightId) {
        Optional<StreamHighlight> highlight = highlightRepository.findById(highlightId);
        if (highlight.isEmpty()) {
            throw new RuntimeException("Highlight not found");
        }

        Map<String, Object> timestampData = new HashMap<>();
        timestampData.put("highlightId", highlight.get().getHighlightId());
        timestampData.put("timestampSeconds", highlight.get().getTimestampSeconds());
        timestampData.put("timestampFormatted", highlight.get().getTimestampFormatted());
        timestampData.put("title", highlight.get().getTitle());

        return timestampData;
    }

    // Search highlights by tags
    public List<StreamHighlight> searchHighlightsByTags(List<String> tags) {
        List<HighlightTag> highlightTags = tagRepository.findByTagNamesAndPublic(tags);
        return highlightTags.stream()
            .map(HighlightTag::getHighlight)
            .distinct()
            .collect(Collectors.toList());
    }

    // Get popular highlights
    public List<Map<String, Object>> getPopularHighlights(int limit) {
        List<StreamHighlight> highlights = highlightRepository.findPopularHighlights(Pageable.ofSize(limit));
        
        return highlights.stream().map(highlight -> {
            Map<String, Object> highlightData = new HashMap<>();
            highlightData.put("highlightId", highlight.getHighlightId());
            highlightData.put("title", highlight.getTitle());
            highlightData.put("description", highlight.getDescription());
            highlightData.put("timestampFormatted", highlight.getTimestampFormatted());
            highlightData.put("createdBy", highlight.getCreatedBy());
            highlightData.put("highlightType", highlight.getHighlightType());
            
            // Get reaction count
            long reactionCount = highlight.getReactions().stream()
                .filter(r -> r.getReactionType() == HighlightReaction.ReactionType.LIKE)
                .count();
            highlightData.put("likeCount", reactionCount);
            
            return highlightData;
        }).collect(Collectors.toList());
    }

    // Real-time update methods
    private void sendHighlightUpdate(Long streamId, StreamHighlight highlight) {
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("type", "HIGHLIGHT_CREATED");
        updateData.put("highlight", highlight);
        updateData.put("streamId", streamId);
        
        messagingTemplate.convertAndSend("/topic/highlights", updateData);
    }

    private void sendHighlightReactionUpdate(Long highlightId) {
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("type", "HIGHLIGHT_REACTION");
        updateData.put("highlightId", highlightId);
        
        messagingTemplate.convertAndSend("/topic/highlights", updateData);
    }

    private void sendHighlightCommentUpdate(Long highlightId, HighlightComment comment) {
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("type", "HIGHLIGHT_COMMENT");
        updateData.put("highlightId", highlightId);
        updateData.put("comment", comment);
        
        messagingTemplate.convertAndSend("/topic/highlights", updateData);
    }

    private void sendHighlightDeletedUpdate(Long streamId, Long highlightId) {
        Map<String, Object> updateData = new HashMap<>();
        updateData.put("type", "HIGHLIGHT_DELETED");
        updateData.put("highlightId", highlightId);
        updateData.put("streamId", streamId);
        
        messagingTemplate.convertAndSend("/topic/highlights", updateData);
    }
} 