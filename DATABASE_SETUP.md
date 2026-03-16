# Database Setup Guide for Live Streaming Application

## Overview

This application requires MySQL database to store stream data, quality settings, highlights, and analytics. Follow this guide to set up the database properly.

## Option 1: Install MySQL Server (Recommended)

### Windows Installation

1. **Download MySQL Installer**
   - Go to: https://dev.mysql.com/downloads/installer/
   - Download "MySQL Installer for Windows"
   - Choose the larger download (includes all MySQL products)

2. **Run the Installer**
   ```bash
   # Run the downloaded .msi file
   # Choose "Developer Default" or "Server only"
   # Follow the installation wizard
   ```

3. **Configure MySQL**
   - Set root password (remember this!)
   - Choose port 3306 (default)
   - Complete the installation

4. **Add MySQL to PATH**
   - Open System Properties → Environment Variables
   - Add MySQL bin directory to PATH (usually `C:\Program Files\MySQL\MySQL Server 8.0\bin`)

### Alternative: XAMPP (Easier for Development)

1. **Download XAMPP**
   - Go to: https://www.apachefriends.org/download.html
   - Download XAMPP for Windows

2. **Install XAMPP**
   ```bash
   # Run the installer
   # Choose components: Apache, MySQL, phpMyAdmin
   # Complete installation
   ```

3. **Start MySQL**
   - Open XAMPP Control Panel
   - Click "Start" next to MySQL
   - MySQL will run on port 3306

## Option 2: Use Docker (Advanced)

If you have Docker installed:

```bash
# Pull MySQL image
docker pull mysql:8.0

# Run MySQL container
docker run --name mysql-stream -e MYSQL_ROOT_PASSWORD=yourpassword -e MYSQL_DATABASE=stream -p 3306:3306 -d mysql:8.0
```

## Database Setup

### 1. Create Database

Once MySQL is running, create the database:

```bash
# Connect to MySQL
mysql -u root -p

# Create database
CREATE DATABASE stream CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

# Verify database creation
SHOW DATABASES;

# Exit MySQL
EXIT;
```

### 2. Run Schema Scripts

Execute the database schema files:

```bash
# Navigate to project directory
cd livestream

# Run highlights schema
mysql -u root -p stream < database/highlights_schema.sql

# Run quality recording schema
mysql -u root -p stream < database/quality_recording_schema.sql
```

### 3. Verify Tables

Check that all tables were created:

```bash
mysql -u root -p stream

# Show all tables
SHOW TABLES;

# Expected tables:
# - stream_sessions
# - stream_metrics
# - chat_messages
# - engagement_stats
# - stream_highlights
# - highlight_comments
# - highlight_reactions
# - highlight_tags
# - stream_qualities
# - user_quality_preferences
# - quality_switch_logs
# - quality_recordings
# - quality_analytics
# - top_chatters
# - viewer_counts

EXIT;
```

## Application Configuration

### Update application.properties

Ensure your `application.properties` has the correct database settings:

```properties
# Database configuration
spring.datasource.url=jdbc:mysql://localhost:3306/stream?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=your_mysql_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# Hibernate settings
spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

**Important:** Replace `your_mysql_password` with the password you set during MySQL installation.

## Quick Setup Script

Create a batch file for Windows to automate the setup:

```batch
@echo off
echo Setting up MySQL database for Live Streaming Application...

REM Check if MySQL is installed
mysql --version >nul 2>&1
if %errorlevel% neq 0 (
    echo MySQL is not installed or not in PATH
    echo Please install MySQL first
    pause
    exit /b 1
)

REM Create database
echo Creating database...
mysql -u root -p -e "CREATE DATABASE IF NOT EXISTS stream CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

REM Run schema scripts
echo Running schema scripts...
mysql -u root -p stream < database/highlights_schema.sql
mysql -u root -p stream < database/quality_recording_schema.sql

echo Database setup complete!
pause
```

Save this as `setup_database.bat` in your project root and run it.

## Troubleshooting

### Common Issues

1. **"Access denied for user 'root'@'localhost'"**
   - Solution: Check your MySQL root password
   - Reset password if needed: `mysqladmin -u root password "newpassword"`

2. **"Can't connect to MySQL server"**
   - Solution: Ensure MySQL service is running
   - Windows: Check Services app for "MySQL80" service
   - XAMPP: Check XAMPP Control Panel

3. **"Unknown database 'stream'"**
   - Solution: Create the database first
   - Run: `CREATE DATABASE stream;`

4. **Port 3306 already in use**
   - Solution: Check if another MySQL instance is running
   - Stop conflicting services or change port

### Testing Database Connection

Test the connection manually:

```bash
# Test connection
mysql -u root -p -h localhost -P 3306

# If successful, you should see MySQL prompt
mysql>
```

### Alternative: Use H2 Database (Development Only)

For quick testing without MySQL, you can temporarily use H2 in-memory database:

```properties
# Add to application.properties for H2
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.h2.console.enabled=true
```

Add H2 dependency to `pom.xml`:

```xml
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>
```

## Next Steps

After setting up the database:

1. **Start the application:**
   ```bash
   mvn spring-boot:run
   ```

2. **Access the application:**
   - Open browser to: http://localhost:8080

3. **Verify functionality:**
   - Check if quality controls appear
   - Test quality switching
   - Verify highlights feature
   - Check real-time analytics

## Support

If you encounter issues:

1. Check MySQL error logs
2. Verify database connection settings
3. Ensure all schema scripts ran successfully
4. Check application logs for specific error messages

The application will automatically create missing tables if `spring.jpa.hibernate.ddl-auto=update` is set, but it's better to run the schema scripts for proper initialization. 