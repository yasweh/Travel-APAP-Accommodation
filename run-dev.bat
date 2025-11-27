@echo off
REM Script untuk menjalankan backend di localhost dengan dev profile

echo.
echo ============================================================
echo    Starting Accommodation Backend (Development Mode)
echo ============================================================
echo.
echo Backend URL: http://localhost:8080
echo Authentication: Profile Service (hafizmuh.site)
echo Database: Local PostgreSQL
echo.

REM Check if gradlew exists
if not exist "gradlew.bat" (
    echo ERROR: gradlew.bat not found!
    echo Please run this script from the project root directory.
    pause
    exit /b 1
)

REM Build project
echo Building project...
call gradlew.bat clean build -x test

if errorlevel 1 (
    echo.
    echo ERROR: Build failed!
    pause
    exit /b 1
)

echo.
echo Starting Spring Boot application...
echo Press Ctrl+C to stop
echo.

REM Run with dev profile
call gradlew.bat bootRun --args="--spring.profiles.active=dev"

pause
