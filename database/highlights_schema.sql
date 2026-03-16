-- Stream Highlights & Bookmarks Database Schema

-- Stream Highlights Table
CREATE TABLE stream_highlights (
    highlight_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    stream_id BIGINT NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    timestamp_seconds INT NOT NULL, -- Time in seconds from stream start
    timestamp_formatted VARCHAR(20), -- Formatted time (e.g., "15:30")
    created_by VARCHAR(100) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_public BOOLEAN DEFAULT TRUE,
    highlight_type ENUM('HIGHLIGHT', 'BOOKMARK', 'CLIP') DEFAULT 'HIGHLIGHT',
    
    FOREIGN KEY (stream_id) REFERENCES stream_sessions(stream_id) ON DELETE CASCADE,
    INDEX idx_stream_timestamp (stream_id, timestamp_seconds),
    INDEX idx_created_by (created_by),
    INDEX idx_highlight_type (highlight_type),
    INDEX idx_is_public (is_public)
);

-- Highlight Tags Table (for categorizing highlights)
CREATE TABLE highlight_tags (
    tag_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    highlight_id BIGINT NOT NULL,
    tag_name VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (highlight_id) REFERENCES stream_highlights(highlight_id) ON DELETE CASCADE,
    INDEX idx_highlight_id (highlight_id),
    INDEX idx_tag_name (tag_name)
);

-- Highlight Reactions Table (likes, etc.)
CREATE TABLE highlight_reactions (
    reaction_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    highlight_id BIGINT NOT NULL,
    user_ip VARCHAR(45) NOT NULL,
    reaction_type ENUM('LIKE', 'LOVE', 'WOW', 'USEFUL') DEFAULT 'LIKE',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (highlight_id) REFERENCES stream_highlights(highlight_id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_highlight (highlight_id, user_ip),
    INDEX idx_highlight_id (highlight_id),
    INDEX idx_reaction_type (reaction_type)
);

-- Highlight Comments Table
CREATE TABLE highlight_comments (
    comment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    highlight_id BIGINT NOT NULL,
    username VARCHAR(100) NOT NULL,
    content TEXT NOT NULL,
    user_ip VARCHAR(45),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_deleted BOOLEAN DEFAULT FALSE,
    
    FOREIGN KEY (highlight_id) REFERENCES stream_highlights(highlight_id) ON DELETE CASCADE,
    INDEX idx_highlight_id (highlight_id),
    INDEX idx_username (username),
    INDEX idx_created_at (created_at)
);

-- Views for Analytics
CREATE VIEW highlight_analytics AS
SELECT 
    h.highlight_id,
    h.title,
    h.timestamp_seconds,
    h.highlight_type,
    h.is_public,
    COUNT(DISTINCT r.reaction_id) as reaction_count,
    COUNT(DISTINCT c.comment_id) as comment_count,
    COUNT(DISTINCT t.tag_id) as tag_count
FROM stream_highlights h
LEFT JOIN highlight_reactions r ON h.highlight_id = r.highlight_id
LEFT JOIN highlight_comments c ON h.highlight_id = c.highlight_id AND c.is_deleted = FALSE
LEFT JOIN highlight_tags t ON h.highlight_id = t.highlight_id
GROUP BY h.highlight_id, h.title, h.timestamp_seconds, h.highlight_type, h.is_public;

-- Popular Highlights View
CREATE VIEW popular_highlights AS
SELECT 
    h.*,
    COUNT(r.reaction_id) as like_count
FROM stream_highlights h
LEFT JOIN highlight_reactions r ON h.highlight_id = r.highlight_id AND r.reaction_type = 'LIKE'
WHERE h.is_public = TRUE
GROUP BY h.highlight_id
ORDER BY like_count DESC, h.created_at DESC;

-- Triggers for automatic updates
DELIMITER //

CREATE TRIGGER update_highlight_timestamp_formatted
BEFORE INSERT ON stream_highlights
FOR EACH ROW
BEGIN
    SET NEW.timestamp_formatted = CONCAT(
        LPAD(FLOOR(NEW.timestamp_seconds / 60), 2, '0'), ':',
        LPAD(MOD(NEW.timestamp_seconds, 60), 2, '0')
    );
END//

CREATE TRIGGER update_highlight_timestamp_formatted_update
BEFORE UPDATE ON stream_highlights
FOR EACH ROW
BEGIN
    IF NEW.timestamp_seconds != OLD.timestamp_seconds THEN
        SET NEW.timestamp_formatted = CONCAT(
            LPAD(FLOOR(NEW.timestamp_seconds / 60), 2, '0'), ':',
            LPAD(MOD(NEW.timestamp_seconds, 60), 2, '0')
        );
    END IF;
END//

DELIMITER ;

-- Insert sample data for testing
INSERT INTO stream_highlights (stream_id, title, description, timestamp_seconds, created_by, highlight_type) VALUES
(1, 'Amazing Play!', 'Incredible gaming moment at 15:30', 930, 'User1', 'HIGHLIGHT'),
(1, 'Technical Discussion', 'Important technical explanation', 1800, 'User2', 'BOOKMARK'),
(1, 'Funny Moment', 'Hilarious chat interaction', 2400, 'User3', 'HIGHLIGHT');

INSERT INTO highlight_tags (highlight_id, tag_name) VALUES
(1, 'gaming'),
(1, 'amazing'),
(2, 'technical'),
(2, 'educational'),
(3, 'funny'),
(3, 'chat');

COMMIT; 