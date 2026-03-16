package com.example.stream.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "chat_messages", indexes = {
    @Index(name = "idx_stream_timestamp", columnList = "stream_id, timestamp"),
    @Index(name = "idx_username", columnList = "username"),
    @Index(name = "idx_user_ip", columnList = "user_ip")
})
public class ChatMessageEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long messageId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stream_id")
    private StreamSession streamSession;

    private String username;
    private String content;
    private String userIp;
    private LocalDateTime timestamp;
    private Boolean isDeleted;
} 