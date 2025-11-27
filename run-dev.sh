#!/bin/bash
# Script untuk menjalankan backend di localhost dengan dev profile

echo "🚀 Starting Accommodation Backend (Development Mode)"
echo "=================================================="
echo ""
echo "📍 Backend URL: http://localhost:8080"
echo "🔐 Authentication: Profile Service (hafizmuh.site)"
echo "🗄️  Database: Local PostgreSQL"
echo ""

# Check if PostgreSQL is running
if ! command -v psql &> /dev/null; then
    echo "❌ PostgreSQL not found. Please install PostgreSQL first."
    exit 1
fi

# Check if database exists
if ! psql -U postgres -lqt | cut -d \| -f 1 | grep -qw accommodation_db; then
    echo "⚠️  Database 'accommodation_db' not found!"
    echo "Creating database..."
    psql -U postgres -c "CREATE DATABASE accommodation_db;"
fi

echo "✅ Database ready"
echo ""
echo "Building project..."
./gradlew clean build -x test

echo ""
echo "🚀 Starting Spring Boot application..."
echo "Press Ctrl+C to stop"
echo ""

./gradlew bootRun --args='--spring.profiles.active=dev'
