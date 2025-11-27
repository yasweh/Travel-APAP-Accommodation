#!/bin/bash
# Script untuk menjalankan backend dalam PRODUCTION mode
# Requires production environment variables to be set

echo ""
echo "============================================================"
echo "   Starting Accommodation Backend (PRODUCTION MODE)"
echo "============================================================"
echo ""
echo "⚠️  WARNING: Running in PRODUCTION mode!"
echo "Make sure all production environment variables are set."
echo ""

# Check if required environment variables are set
if [ -z "$DATABASE_URL_PROD" ]; then
    echo "❌ ERROR: DATABASE_URL_PROD is not set!"
    echo ""
    echo "Please set production environment variables first:"
    echo ""
    echo "  export DATABASE_URL_PROD='jdbc:postgresql://host:5432/accommodation_prod'"
    echo "  export DATABASE_USERNAME='prod_user'"
    echo "  export DATABASE_PASSWORD='prod_password'"
    echo "  export JWT_SECRET_KEY='your-production-jwt-secret-key'"
    echo "  export CORS_ALLOWED_ORIGINS='https://2306212083-fe.hafizmuh.site'"
    echo ""
    exit 1
fi

if [ -z "$JWT_SECRET_KEY" ]; then
    echo "❌ ERROR: JWT_SECRET_KEY is not set!"
    exit 1
fi

echo "✅ Environment Check:"
echo "   DATABASE_URL_PROD: $DATABASE_URL_PROD"
echo "   DATABASE_USERNAME: $DATABASE_USERNAME"
echo "   DATABASE_PASSWORD: [MASKED]"
echo "   JWT_SECRET_KEY: [MASKED]"
echo "   CORS_ALLOWED_ORIGINS: ${CORS_ALLOWED_ORIGINS:-[NOT SET]}"
echo ""

echo "Building project..."
./gradlew clean build -x test

if [ $? -ne 0 ]; then
    echo ""
    echo "❌ ERROR: Build failed!"
    exit 1
fi

echo ""
echo "🚀 Starting Spring Boot application in PRODUCTION mode..."
echo "Press Ctrl+C to stop"
echo ""

./gradlew bootRun --args='--spring.profiles.active=prod'
