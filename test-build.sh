#!/bin/bash

# Test script for Android build
echo "Testing Android build configuration..."

cd android-app

echo "1. Checking Gradle wrapper..."
./gradlew --version

echo "2. Checking project structure..."
./gradlew tasks --all

echo "3. Cleaning project..."
./gradlew clean

echo "4. Building debug APK..."
./gradlew assembleDebug --stacktrace

echo "5. Checking if APK was created..."
if [ -f "app/build/outputs/apk/debug/app-debug.apk" ]; then
    echo "✅ Debug APK created successfully!"
    ls -la app/build/outputs/apk/debug/
else
    echo "❌ Debug APK not found!"
    exit 1
fi

echo "6. Building release APK..."
./gradlew assembleRelease --stacktrace

echo "7. Checking if release APK was created..."
if [ -f "app/build/outputs/apk/release/app-release.apk" ]; then
    echo "✅ Release APK created successfully!"
    ls -la app/build/outputs/apk/release/
else
    echo "❌ Release APK not found!"
    exit 1
fi

echo "🎉 All builds completed successfully!"
