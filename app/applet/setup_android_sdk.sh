#!/bin/bash
set -e

echo "Setting up Android SDK environment for Codespaces..."

# Check if pre-installed Android SDK exists in standard locations
if [ -d "/usr/local/lib/android/sdk" ]; then
    SDK_PATH="/usr/local/lib/android/sdk"
elif [ -d "/android-sdk" ]; then
    SDK_PATH="/android-sdk"
elif [ -n "$ANDROID_HOME" ] && [ -d "$ANDROID_HOME" ]; then
    SDK_PATH="$ANDROID_HOME"
else
    SDK_PATH="$HOME/android-sdk"
    if [ ! -d "$SDK_PATH/platforms/android-34" ]; then
        echo "Downloading Android Command Line Tools..."
        mkdir -p "$SDK_PATH/cmdline-tools/latest"
        curl -s -o cmdline-tools.zip https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip
        unzip -q cmdline-tools.zip -d "$SDK_PATH/cmdline-tools"
        mv "$SDK_PATH/cmdline-tools/cmdline-tools/"* "$SDK_PATH/cmdline-tools/latest/" 2>/dev/null || true
        rm -f cmdline-tools.zip
        
        echo "Accepting licenses and installing SDK platforms..."
        yes | "$SDK_PATH/cmdline-tools/latest/bin/sdkmanager" --licenses > /dev/null 2>&1 || true
        "$SDK_PATH/cmdline-tools/latest/bin/sdkmanager" "platform-tools" "platforms;android-34" "build-tools;34.0.0" > /dev/null
    fi
fi

echo "sdk.dir=$SDK_PATH" > local.properties
echo "Android SDK configured at: $SDK_PATH"
echo "You can now run: ./gradlew assembleDebug"
