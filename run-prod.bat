@echo off
REM Script untuk menjalankan backend dalam PRODUCTION mode
REM Requires production environment variables to be set

echo.
echo ============================================================
echo    Starting Accommodation Backend (PRODUCTION MODE)
echo ============================================================
echo.
echo WARNING: Running in PRODUCTION mode!
echo Make sure all production environment variables are set.
echo.

REM Check if required environment variables are set
if "%DATABASE_URL_PROD%"=="" (
    echo ERROR: DATABASE_URL_PROD is not set!
    echo Please set production environment variables first.
    echo.
    echo Example:
    echo   set DATABASE_URL_PROD=jdbc:postgresql://host:5432/accommodation_prod
    echo   set DATABASE_USERNAME=prod_user
    echo   set DATABASE_PASSWORD=prod_password
    echo   set JWT_SECRET_KEY=your-production-jwt-secret-key
    echo   set CORS_ALLOWED_ORIGINS=https://2306212083-fe.hafizmuh.site
    echo.
    pause
    exit /b 1
)

if "%JWT_SECRET_KEY%"=="" (
    echo ERROR: JWT_SECRET_KEY is not set!
    pause
    exit /b 1
)

echo Environment Check:
echo   DATABASE_URL_PROD: %DATABASE_URL_PROD%
echo   DATABASE_USERNAME: %DATABASE_USERNAME%
echo   DATABASE_PASSWORD: [MASKED]
echo   JWT_SECRET_KEY: [MASKED]
echo   CORS_ALLOWED_ORIGINS: %CORS_ALLOWED_ORIGINS%
echo.

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
echo Starting Spring Boot application in PRODUCTION mode...
echo Press Ctrl+C to stop
echo.

REM Run with prod profile
call gradlew.bat bootRun --args="--spring.profiles.active=prod"

pause
