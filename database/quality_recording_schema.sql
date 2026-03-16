-- Quality-Based Recording & Real-Time Quality Switching Database Schema

-- Stream Quality Levels Table
CREATE TABLE stream_qualities (
    quality_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    quality_name VARCHAR(50) NOT NULL UNIQUE, -- 'auto', 'high', 'medium', 'low'
    quality_label VARCHAR(100) NOT NULL, -- 'Auto', 'High (1080p)', 'Medium (720p)', 'Low (480p)'
    resolution VARCHAR(20), -- '1920x1080', '1280x720', '854x480'
    bitrate VARCHAR(20), -- '5000k', '2500k', '1000k'
    is_active BOOLEAN DEFAULT TRUE,
    sort_order INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    INDEX idx_quality_name (quality_name),
    INDEX idx_is_active (is_active),
    INDEX idx_sort_order (sort_order)
);

-- User Quality Preferences Table
CREATE TABLE user_quality_preferences (
    preference_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_ip VARCHAR(45) NOT NULL,
    stream_id BIGINT,
    preferred_quality VARCHAR(50) NOT NULL,
    auto_switch BOOLEAN DEFAULT TRUE,
    bandwidth_threshold INT DEFAULT 5000, -- kbps
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    
    FOREIGN KEY (stream_id) REFERENCES stream_sessions(stream_id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_stream (user_ip, stream_id),
    INDEX idx_user_ip (user_ip),
    INDEX idx_preferred_quality (preferred_quality)
);

-- Quality Switching Log Table
CREATE TABLE quality_switch_logs (
    log_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_ip VARCHAR(45) NOT NULL,
    stream_id BIGINT,
    from_quality VARCHAR(50),
    to_quality VARCHAR(50) NOT NULL,
    switch_reason ENUM('MANUAL', 'AUTO_BANDWIDTH', 'AUTO_ERROR', 'AUTO_QUALITY') NOT NULL,
    bandwidth_available INT, -- kbps
    latency_ms INT,
    packet_loss DECIMAL(5,2), -- percentage
    switch_timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (stream_id) REFERENCES stream_sessions(stream_id) ON DELETE CASCADE,
    INDEX idx_user_ip (user_ip),
    INDEX idx_stream_id (stream_id),
    INDEX idx_switch_timestamp (switch_timestamp),
    INDEX idx_switch_reason (switch_reason)
);

-- Quality-Based Recording Sessions Table
CREATE TABLE quality_recordings (
    recording_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    stream_id BIGINT NOT NULL,
    quality_level VARCHAR(50) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT, -- bytes
    duration_seconds INT,
    recording_start TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    recording_end TIMESTAMP NULL,
    is_active BOOLEAN DEFAULT TRUE,
    created_by VARCHAR(100) DEFAULT 'system',
    
    FOREIGN KEY (stream_id) REFERENCES stream_sessions(stream_id) ON DELETE CASCADE,
    INDEX idx_stream_id (stream_id),
    INDEX idx_quality_level (quality_level),
    INDEX idx_is_active (is_active),
    INDEX idx_recording_start (recording_start)
);

-- Quality Analytics Table
CREATE TABLE quality_analytics (
    analytics_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    stream_id BIGINT NOT NULL,
    quality_level VARCHAR(50) NOT NULL,
    viewer_count INT DEFAULT 0,
    avg_bandwidth INT, -- kbps
    avg_latency INT, -- ms
    avg_packet_loss DECIMAL(5,2), -- percentage
    switch_count INT DEFAULT 0,
    error_count INT DEFAULT 0,
    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (stream_id) REFERENCES stream_sessions(stream_id) ON DELETE CASCADE,
    INDEX idx_stream_id (stream_id),
    INDEX idx_quality_level (quality_level),
    INDEX idx_recorded_at (recorded_at)
);

-- Quality Performance Metrics Table
CREATE TABLE quality_performance (
    performance_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    stream_id BIGINT NOT NULL,
    quality_level VARCHAR(50) NOT NULL,
    metric_type ENUM('BANDWIDTH', 'LATENCY', 'PACKET_LOSS', 'ERROR_RATE') NOT NULL,
    metric_value DECIMAL(10,4) NOT NULL,
    sample_count INT DEFAULT 1,
    recorded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (stream_id) REFERENCES stream_sessions(stream_id) ON DELETE CASCADE,
    INDEX idx_stream_id (stream_id),
    INDEX idx_quality_level (quality_level),
    INDEX idx_metric_type (metric_type),
    INDEX idx_recorded_at (recorded_at)
);

-- Views for Analytics
CREATE VIEW quality_summary AS
SELECT 
    qa.stream_id,
    qa.quality_level,
    qa.viewer_count,
    qa.avg_bandwidth,
    qa.avg_latency,
    qa.avg_packet_loss,
    qa.switch_count,
    qa.error_count,
    COUNT(qsl.log_id) as total_switches,
    COUNT(CASE WHEN qsl.switch_reason = 'MANUAL' THEN 1 END) as manual_switches,
    COUNT(CASE WHEN qsl.switch_reason = 'AUTO_BANDWIDTH' THEN 1 END) as auto_bandwidth_switches,
    COUNT(CASE WHEN qsl.switch_reason = 'AUTO_ERROR' THEN 1 END) as auto_error_switches
FROM quality_analytics qa
LEFT JOIN quality_switch_logs qsl ON qa.stream_id = qsl.stream_id AND qa.quality_level = qsl.to_quality
GROUP BY qa.analytics_id, qa.stream_id, qa.quality_level, qa.viewer_count, qa.avg_bandwidth, qa.avg_latency, qa.avg_packet_loss, qa.switch_count, qa.error_count;

CREATE VIEW popular_qualities AS
SELECT 
    quality_level,
    COUNT(DISTINCT stream_id) as stream_count,
    SUM(viewer_count) as total_viewers,
    AVG(avg_bandwidth) as avg_bandwidth,
    AVG(avg_latency) as avg_latency
FROM quality_analytics
GROUP BY quality_level
ORDER BY total_viewers DESC;

-- Triggers for automatic updates
DELIMITER //

CREATE TRIGGER update_quality_preference_timestamp
BEFORE UPDATE ON user_quality_preferences
FOR EACH ROW
BEGIN
    SET NEW.updated_at = CURRENT_TIMESTAMP;
END//

CREATE TRIGGER log_quality_switch
AFTER INSERT ON quality_switch_logs
FOR EACH ROW
BEGIN
    -- Update analytics with switch count
    INSERT INTO quality_analytics (stream_id, quality_level, switch_count, recorded_at)
    VALUES (NEW.stream_id, NEW.to_quality, 1, CURRENT_TIMESTAMP)
    ON DUPLICATE KEY UPDATE 
        switch_count = switch_count + 1,
        recorded_at = CURRENT_TIMESTAMP;
END//

DELIMITER ;

-- Insert default quality levels
INSERT INTO stream_qualities (quality_name, quality_label, resolution, bitrate, sort_order) VALUES
('auto', 'Auto', NULL, NULL, 0),
('high', 'High (1080p)', '1920x1080', '5000k', 1),
('medium', 'Medium (720p)', '1280x720', '2500k', 2),
('low', 'Low (480p)', '854x480', '1000k', 3);

-- Insert sample quality preferences
INSERT INTO user_quality_preferences (user_ip, stream_id, preferred_quality, auto_switch, bandwidth_threshold) VALUES
('192.168.1.100', 1, 'auto', TRUE, 5000),
('192.168.1.101', 1, 'high', FALSE, 8000),
('192.168.1.102', 1, 'medium', TRUE, 3000);

-- Insert sample quality switch logs
INSERT INTO quality_switch_logs (user_ip, stream_id, from_quality, to_quality, switch_reason, bandwidth_available, latency_ms, packet_loss) VALUES
('192.168.1.100', 1, 'high', 'medium', 'AUTO_BANDWIDTH', 2500, 45, 0.5),
('192.168.1.101', 1, 'high', 'medium', 'MANUAL', 3500, 50, 0.2),
('192.168.1.102', 1, 'medium', 'low', 'AUTO_ERROR', 800, 120, 5.0);

-- Insert sample quality recordings
INSERT INTO quality_recordings (stream_id, quality_level, file_path, file_size, duration_seconds, recording_start, recording_end, is_active) VALUES
(1, 'high', '/recordings/stream_1_high_20241201_143000.mp4', 1024000000, 3600, '2024-12-01 14:30:00', '2024-12-01 15:30:00', FALSE),
(1, 'medium', '/recordings/stream_1_medium_20241201_143000.mp4', 512000000, 3600, '2024-12-01 14:30:00', '2024-12-01 15:30:00', FALSE),
(1, 'low', '/recordings/stream_1_low_20241201_143000.mp4', 256000000, 3600, '2024-12-01 14:30:00', '2024-12-01 15:30:00', FALSE);

-- Insert sample quality analytics
INSERT INTO quality_analytics (stream_id, quality_level, viewer_count, avg_bandwidth, avg_latency, avg_packet_loss, switch_count, error_count) VALUES
(1, 'high', 150, 4800, 35, 0.1, 5, 2),
(1, 'medium', 300, 2400, 45, 0.5, 12, 8),
(1, 'low', 100, 950, 80, 2.0, 3, 15);

COMMIT; 