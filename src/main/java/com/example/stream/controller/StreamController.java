package com.example.stream.controller;

import com.example.stream.model.ChatMessage;
import com.example.stream.model.StreamData;
import com.example.stream.model.StreamMetrics;
import com.example.stream.service.StreamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/live")
public class StreamController {

    @Autowired
    private StreamService streamService;

    private static final String RECORDINGS_DIR = "C:/nginx/html/recordings";

    @GetMapping
    public String liveStream(Model model) {
        model.addAttribute("metrics", streamService.getStreamMetrics());
        model.addAttribute("chatMessages", streamService.getChatMessages());
        model.addAttribute("viewerData", streamService.getViewerData());
        model.addAttribute("performanceData", streamService.getPerformanceData());
        model.addAttribute("topChatters", streamService.getTopChatters());
        model.addAttribute("streamStartTime", streamService.getStreamStartTime());
        
        model.addAttribute("streamUrl", "http://localhost:90/hls/stream.m3u8");// Update with your actual stream URL

        
        return "livestream";
    }

    @GetMapping("/stream.m3u8")
    @ResponseBody
    public FileSystemResource getLiveStream() {

        return new FileSystemResource(new File("/var/www/html/live/test.m3u8"));
    }

    @PostMapping("/like")
    @ResponseBody
    public StreamMetrics incrementLike() {
        try {
            System.out.println("Like endpoint called");
            StreamMetrics result = streamService.incrementLike();
            System.out.println("Like result: " + result);
            return result;
        } catch (Exception e) {
            System.err.println("Error incrementing likes: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error incrementing likes", e);
        }
    }

    @PostMapping("/dislike")
    @ResponseBody
    public StreamMetrics incrementDislike() {
        try {
            System.out.println("Dislike endpoint called");
            StreamMetrics result = streamService.incrementDislike();
            System.out.println("Dislike result: " + result);
            return result;
        } catch (Exception e) {
            System.err.println("Error incrementing dislikes: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Error incrementing dislikes", e);
        }
    }

    @MessageMapping("/chat")
    @SendTo("/topic/messages")
    public ChatMessage sendMessage(ChatMessage message) {
        if (message == null || message.getContent() == null || message.getContent().trim().isEmpty()) {
            throw new IllegalArgumentException("Invalid chat message");
        }
        message.setTimestamp(LocalDateTime.now());
        return streamService.addChatMessage(message);
    }

    @GetMapping("/data")
    @ResponseBody
    public StreamData getStreamData() {
        try {
            return streamService.getStreamData();
        } catch (Exception e) {
            throw new RuntimeException("Error fetching stream data", e);
        }
    }

    @GetMapping("/recordings")
    @ResponseBody
    public ResponseEntity<?> listRecordings() {
        try {
            Path dir = Paths.get(RECORDINGS_DIR);
            if (!Files.exists(dir)) {
                return ResponseEntity.ok().body(new String[]{});
            }
            var files = Files.list(dir)
                    .filter(Files::isRegularFile)
                    .map(path -> path.getFileName().toString())
                    .collect(Collectors.toList());
            return ResponseEntity.ok(files);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error listing recordings: " + e.getMessage());
        }
    }

    @GetMapping("/recordings/{filename}")
    public ResponseEntity<Resource> getRecording(@PathVariable String filename) {
        try {
            Path file = Paths.get(RECORDINGS_DIR).resolve(filename).normalize();
            Resource resource = new UrlResource(file.toUri());
            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }
            String contentType = Files.probeContentType(file);
            if (contentType == null) {
                contentType = "application/octet-stream";
            }
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
