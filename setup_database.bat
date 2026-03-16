@echo off
echo ========================================
echo Live Streaming Application Database Setup
echo ========================================
echo.

REM Check if MySQL is installed
echo Checking MySQL installation...
mysql --version >nul 2>&1
if %errorlevel% neq 0 (
    echo ERROR: MySQL is not installed or not in PATH
    echo.
    echo Please install MySQL first:
    echo 1. Download from: https://dev.mysql.com/downloads/installer/
    echo 2. Or use XAMPP: https://www.apachefriends.org/download.html
    echo 3. Add MySQL to your system PATH
    echo.
    pause
    exit /b 1
)

echo MySQL found! Version:
mysql --version
echo.

REM Prompt for MySQL password
set /p mysql_password="Enter MySQL root password (leave empty if no password): "

REM Create database
echo.
echo Creating database 'stream'...
if "%mysql_password%"=="" (
    mysql -u root -e "CREATE DATABASE IF NOT EXISTS stream CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
) else (
    mysql -u root -p%mysql_password% -e "CREATE DATABASE IF NOT EXISTS stream CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"
)

if %errorlevel% neq 0 (
    echo ERROR: Failed to create database
    echo Please check your MySQL password and try again
    pause
    exit /b 1
)

echo Database 'stream' created successfully!
echo.

REM Run schema scripts
echo Running schema scripts...

REM Check if schema files exist
if not exist "database\highlights_schema.sql" (
    echo ERROR: highlights_schema.sql not found
    echo Please ensure you're running this from the livestream directory
    pause
    exit /b 1
)

if not exist "database\quality_recording_schema.sql" (
    echo ERROR: quality_recording_schema.sql not found
    echo Please ensure you're running this from the livestream directory
    pause
    exit /b 1
)

REM Run highlights schema
echo Running highlights schema...
if "%mysql_password%"=="" (
    mysql -u root stream < database\highlights_schema.sql
) else (
    mysql -u root -p%mysql_password% stream < database\highlights_schema.sql
)

if %errorlevel% neq 0 (
    echo ERROR: Failed to run highlights schema
    pause
    exit /b 1
)

REM Run quality recording schema
echo Running quality recording schema...
if "%mysql_password%"=="" (
    mysql -u root stream < database\quality_recording_schema.sql
) else (
    mysql -u root -p%mysql_password% stream < database\quality_recording_schema.sql
)

if %errorlevel% neq 0 (
    echo ERROR: Failed to run quality recording schema
    pause
    exit /b 1
)

echo.
echo ========================================
echo Database setup completed successfully!
echo ========================================
echo.
echo Next steps:
echo 1. Update application.properties with your MySQL password
echo 2. Run: mvn spring-boot:run
echo 3. Open browser to: http://localhost:8080
echo.
pause 