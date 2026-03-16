# Quality-Based Recording & Real-Time Quality Switching Setup Guide

## Overview

This feature provides advanced quality management for live streaming with:
- Multi-quality streaming (High, Medium, Low, Auto)
- Quality-based recording
- Real-time quality switching based on bandwidth
- Quality analytics and monitoring
- User quality preferences

## Database Setup

### 1. Run the Quality Recording Schema

Execute the SQL schema file to create all necessary tables:

```bash
mysql -u your_username -p your_database < database/quality_recording_schema.sql
```

This creates the following tables:
- `stream_qualities` - Available quality levels
- `user_quality_preferences` - User quality settings
- `quality_switch_logs` - Quality switch history
- `quality_recordings` - Quality-based recordings
- `quality_analytics` - Real-time quality metrics

### 2. Verify Database Tables

Check that all tables were created successfully:

```sql
SHOW TABLES LIKE 'quality%';
SHOW TABLES LIKE 'stream_qualities';
SHOW TABLES LIKE 'user_quality_preferences';
```

## Nginx Configuration

### 1. Update Nginx Configuration

The `nginx.conf` file has been updated with:
- Multi-quality streaming support
- Quality-based recording paths
- FFmpeg transcoding for different quality levels

### 2. Install FFmpeg (Required)

Install FFmpeg for video transcoding:

**Windows:**
```bash
# Download from https://ffmpeg.org/download.html
# Add to PATH environment variable
```

**Linux (Ubuntu/Debian):**
```bash
sudo apt update
sudo apt install ffmpeg
```

**macOS:**
```bash
brew install ffmpeg
```

### 3. Create Recording Directories

Create the necessary directories for quality recordings:

```bash
mkdir -p C:/nginx/html/recordings
mkdir -p C:/nginx/html/hls
```

## Application Configuration

### 1. Update Application Properties

Ensure your `application.properties` includes:

```properties
# Database configuration
spring.datasource.url=jdbc:mysql://localhost:3306/your_database
spring.datasource.username=your_username
spring.datasource.password=your_password

# JPA configuration
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true

# WebSocket configuration
spring.websocket.enabled=true

# Quality management
quality.recording.path=C:/nginx/html/recordings
quality.auto-switch.enabled=true
quality.bandwidth.threshold=5000
```

### 2. Verify Java Dependencies

Ensure your `pom.xml` includes:

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>mysql</groupId>
    <artifactId>mysql-connector-java</artifactId>
</dependency>
```

## Frontend Setup

### 1. Quality Controls

The quality controls are automatically added to the livestream page:
- Quality selector (Auto, High, Medium, Low)
- Auto-switch toggle
- Bandwidth threshold setting
- Quality recording controls
- Real-time quality analytics

### 2. Video Player Sources

The video player now includes multiple quality sources:
- Auto (adaptive)
- High (1080p, 5000 kbps)
- Medium (720p, 2500 kbps)
- Low (480p, 1000 kbps)

## Testing the Feature

### 1. Start the Application

```bash
cd livestream
mvn spring-boot:run
```

### 2. Access the Application

Navigate to `http://localhost:8080`

### 3. Test Quality Switching

1. **Manual Quality Switch:**
   - Select different quality levels using radio buttons
   - Verify video source changes
   - Check quality switch logs

2. **Auto Quality Switch:**
   - Enable auto-switch checkbox
   - Monitor bandwidth changes
   - Verify automatic quality switching

3. **Quality Recording:**
   - Click "Start Recording" in quality controls
   - Verify recording starts in current quality
   - Check recording files in `C:/nginx/html/recordings`

### 4. Test Quality Analytics

1. **Real-time Metrics:**
   - Monitor latency, packet loss, switch count
   - Check quality distribution
   - View quality switch history

2. **API Endpoints:**
   - `GET /api/quality/qualities` - Available qualities
   - `POST /api/quality/preference` - Set user preference
   - `POST /api/quality/switch` - Log quality switch
   - `GET /api/quality/analytics/{streamId}` - Quality analytics

## Troubleshooting

### Common Issues

1. **FFmpeg Not Found:**
   ```
   Error: ffmpeg command not found
   Solution: Install FFmpeg and add to PATH
   ```

2. **Recording Directory Not Found:**
   ```
   Error: Cannot create recording file
   Solution: Create C:/nginx/html/recordings directory
   ```

3. **Database Connection Issues:**
   ```
   Error: Cannot connect to database
   Solution: Verify database credentials and connection
   ```

4. **Quality Switch Not Working:**
   ```
   Error: Quality switch not logged
   Solution: Check WebSocket connection and API endpoints
   ```

### Debug Mode

Enable debug logging in `application.properties`:

```properties
logging.level.com.example.stream=DEBUG
logging.level.org.springframework.web=DEBUG
```

### Performance Monitoring

Monitor system resources:
- CPU usage during transcoding
- Disk space for recordings
- Network bandwidth usage
- Memory consumption

## API Reference

### Quality Management Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/quality/qualities` | Get available quality levels |
| POST | `/api/quality/preference` | Set user quality preference |
| GET | `/api/quality/preference/{streamId}` | Get user quality preference |
| POST | `/api/quality/switch` | Log quality switch |
| POST | `/api/quality/recording/start` | Start quality recording |
| POST | `/api/quality/recording/{id}/stop` | Stop quality recording |
| GET | `/api/quality/recordings/{streamId}` | Get quality recordings |
| GET | `/api/quality/analytics/{streamId}` | Get quality analytics |
| GET | `/api/quality/switches/{streamId}` | Get quality switch history |
| GET | `/api/quality/distribution/{streamId}` | Get quality distribution |
| POST | `/api/quality/optimal` | Determine optimal quality |
| POST | `/api/quality/analytics/update` | Update quality analytics |

### WebSocket Topics

- `/topic/quality-updates` - Real-time quality updates
- `/topic/quality-switches` - Quality switch notifications
- `/topic/quality-analytics` - Quality analytics updates

## Security Considerations

1. **File Access:**
   - Restrict access to recording directories
   - Implement proper file permissions
   - Use secure file paths

2. **API Security:**
   - Implement authentication for quality endpoints
   - Validate user permissions
   - Rate limit API calls

3. **Data Privacy:**
   - Anonymize user IP addresses
   - Implement data retention policies
   - Secure quality preference data

## Performance Optimization

1. **Transcoding:**
   - Use hardware acceleration when available
   - Optimize FFmpeg parameters
   - Monitor CPU usage

2. **Storage:**
   - Implement recording cleanup
   - Use compression for recordings
   - Monitor disk space

3. **Network:**
   - Optimize bandwidth thresholds
   - Implement quality fallbacks
   - Monitor network performance

## Future Enhancements

1. **Advanced Analytics:**
   - Quality performance trends
   - User behavior analysis
   - Predictive quality switching

2. **Quality Optimization:**
   - Machine learning for quality selection
   - Dynamic bitrate adjustment
   - Content-aware quality settings

3. **Integration:**
   - CDN integration
   - Cloud recording storage
   - Third-party analytics

## Support

For issues or questions:
1. Check the troubleshooting section
2. Review application logs
3. Verify configuration settings
4. Test with minimal setup

## Version History

- **v1.0** - Initial implementation
  - Multi-quality streaming
  - Quality-based recording
  - Real-time quality switching
  - Quality analytics
  - User preferences 