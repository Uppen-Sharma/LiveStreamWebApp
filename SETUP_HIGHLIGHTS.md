# Stream Highlights & Bookmarks Setup Guide

## Overview
This guide will help you set up the Stream Highlights & Bookmarks feature for your live streaming application. This feature allows users to mark important moments during streams, create bookmarks, and add comments and reactions.

## Features Included
- ✅ **Create Highlights**: Mark important moments with timestamps
- ✅ **Bookmarks**: Save specific points in streams for later reference
- ✅ **Tags & Categories**: Organize highlights with tags
- ✅ **Comments & Reactions**: Social interaction on highlights
- ✅ **Real-time Updates**: Live updates via WebSocket
- ✅ **Search & Filter**: Find highlights by type, tags, or text
- ✅ **Jump to Time**: Instantly navigate to highlight timestamps

## Database Setup

### 1. Run the Database Schema
Execute the SQL script to create the required tables:

```bash
# Connect to your MySQL database
mysql -u your_username -p your_database_name

# Run the highlights schema
source livestream/database/highlights_schema.sql
```

### 2. Verify Tables Created
The following tables should be created:
- `stream_highlights` - Main highlights table
- `highlight_tags` - Tags for categorizing highlights
- `highlight_reactions` - User reactions (likes, etc.)
- `highlight_comments` - Comments on highlights
- `highlight_analytics` - View for analytics
- `popular_highlights` - View for popular highlights

### 3. Sample Data
The schema includes sample data for testing:
- 3 sample highlights with different types
- Sample tags for each highlight

## Application Setup

### 1. Verify Dependencies
Ensure your `pom.xml` includes:
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-websocket</artifactId>
</dependency>
```

### 2. Database Configuration
Update `application.properties`:
```properties
# Database connection
spring.datasource.url=jdbc:mysql://localhost:3306/your_database
spring.datasource.username=your_username
spring.datasource.password=your_password

# JPA settings
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# WebSocket
spring.websocket.enabled=true
```

### 3. Start the Application
```bash
cd livestream
./mvnw spring-boot:run
```

## Usage Guide

### Creating Highlights
1. **During Stream**: Click "Create Highlight" button
2. **Fill Details**:
   - Title (required)
   - Description (optional)
   - Type: Highlight, Bookmark, or Clip
   - Tags (comma-separated)
   - Current time (auto-captured or manual)
3. **Submit**: Click "Create Highlight"

### Managing Highlights
- **View All**: See all highlights in the main list
- **Filter**: Use filter buttons (All, Highlights, Bookmarks, Clips)
- **Search**: Use the search box to find specific highlights
- **Jump to Time**: Click the play button to jump to that moment
- **Like/Comment**: Interact with highlights

### Real-time Features
- **Live Updates**: New highlights appear instantly
- **Reactions**: Like highlights in real-time
- **Comments**: Add and view comments live
- **Notifications**: Get notified of new highlights

## API Endpoints

### Highlights Management
- `POST /api/highlights/create` - Create new highlight
- `GET /api/highlights/stream/{streamId}` - Get highlights for stream
- `DELETE /api/highlights/{highlightId}` - Delete highlight

### Interactions
- `POST /api/highlights/{highlightId}/reaction` - Add reaction
- `POST /api/highlights/{highlightId}/comment` - Add comment
- `GET /api/highlights/{highlightId}/comments` - Get comments

### Navigation
- `GET /api/highlights/{highlightId}/timestamp` - Get timestamp for jumping

### Search & Discovery
- `POST /api/highlights/search` - Search by tags
- `GET /api/highlights/popular` - Get popular highlights

## WebSocket Topics

### Subscriptions
- `/topic/highlights` - Real-time highlight updates

### Update Types
- `HIGHLIGHT_CREATED` - New highlight created
- `HIGHLIGHT_REACTION` - Reaction added/updated
- `HIGHLIGHT_COMMENT` - Comment added
- `HIGHLIGHT_DELETED` - Highlight deleted

## Troubleshooting

### Common Issues

1. **Database Connection Error**
   - Verify MySQL is running
   - Check database credentials in `application.properties`
   - Ensure database exists

2. **WebSocket Connection Failed**
   - Check if application is running on correct port
   - Verify WebSocket endpoint `/ws` is accessible
   - Check browser console for connection errors

3. **Highlights Not Loading**
   - Verify database tables exist
   - Check application logs for SQL errors
   - Ensure stream ID is correct

4. **Real-time Updates Not Working**
   - Verify WebSocket connection is established
   - Check browser console for subscription errors
   - Ensure highlight service is sending updates

### Debug Steps

1. **Check Application Logs**
   ```bash
   tail -f livestream/logs/application.log
   ```

2. **Verify Database Tables**
   ```sql
   SHOW TABLES LIKE 'highlight%';
   ```

3. **Test API Endpoints**
   ```bash
   curl -X GET http://localhost:8080/api/highlights/stream/1
   ```

4. **Check WebSocket Connection**
   - Open browser developer tools
   - Look for WebSocket connection in Network tab
   - Check Console for connection messages

## Customization

### Adding New Highlight Types
1. Update `StreamHighlight.HighlightType` enum
2. Add new option to HTML select
3. Update CSS for new type styling

### Custom Tags
- Tags are stored as lowercase
- Comma-separated in input field
- Automatically trimmed and filtered

### Styling
- Modify `style.css` for visual changes
- Update highlight card templates in JavaScript
- Customize modal styles

## Performance Considerations

### Database Optimization
- Indexes are created on frequently queried columns
- Use pagination for large highlight lists
- Consider caching for popular highlights

### Real-time Updates
- WebSocket messages are lightweight
- Updates are batched when possible
- Connection is automatically re-established

## Security Notes

### Input Validation
- All user inputs are validated
- SQL injection protection via JPA
- XSS protection in templates

### Access Control
- Currently uses simple user identification
- In production, implement proper authentication
- Add authorization for highlight deletion

## Next Steps

### Potential Enhancements
1. **User Authentication**: Add proper user management
2. **Highlight Sharing**: Share highlights via links
3. **Export Features**: Export highlights to various formats
4. **Advanced Search**: Full-text search with filters
5. **Highlight Clips**: Generate video clips from highlights
6. **Analytics Dashboard**: Detailed highlight analytics

### Integration Ideas
- **Social Media**: Share highlights to social platforms
- **Email Notifications**: Notify users of new highlights
- **Mobile App**: Native mobile application
- **API Integration**: Third-party integrations

## Support

For issues or questions:
1. Check the troubleshooting section above
2. Review application logs
3. Verify database setup
4. Test individual components

---

**Note**: This feature is designed to work with the existing live streaming application. Ensure all base functionality (streaming, recording, analytics) is working before implementing highlights. 