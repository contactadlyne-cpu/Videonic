# Videonic Lite Video Player

A high-performance, lightweight, and modern native Android video player inspired by the intuitive ergonomics of MX Player, built with Jetpack Compose, Material 3, and AndroidX Media3 ExoPlayer.

## Key Features

- **AndroidX Media3 ExoPlayer Engine**: Native hardware-accelerated playback for MP4, MKV, WebM, MOV, and device-supported formats.
- **Intuitive Touch Gestures**:
  - Double-tap left/right to seek 10 seconds.
  - Left vertical swipe to adjust screen brightness (0%–100%) with HUD indicator.
  - Right vertical swipe to adjust media volume (0–15) with HUD indicator.
  - Horizontal swipe to scrub through video timeline with live timestamp preview.
- **22 Screens & Dialogs**:
  1. Splash Screen with original brand badge and smooth intro.
  2. Scoped Video Permissions Primer with zero-permission Storage Access Framework fallback.
  3. Videos Grid with duration overlays, resolution badges, and file sizes.
  4. Videos List view with metadata and instant toggle.
  5. Device Folder hierarchy organized via MediaStore.
  6. Folder Details screen showing videos inside any folder.
  7. Recently Watched history with watch progress bars and remaining time.
  8. Persistent Favorites with instant heart toggle.
  9. Live Search across filenames, titles, and folders.
  10. Portrait Video Player with auto-hiding controls.
  11. Landscape Cinema Player with immersive gesture overlays and orientation toggle.
  12. Player Options Bottom Sheet (Speed, Aspect, Sleep Timer, Repeat, Background Play, Lock).
  13. Audio & Subtitles selector for embedded tracks and external SRT/VTT files.
  14. Video File Information Inspector (Resolution, Codec, Audio channels, Bitrate, Location, Duration).
  15. Video Actions Bottom Sheet (Play, Favorite, Add to Playlist, Rename, Delete, Share, Info).
  16. Playlist Management with video counts and Create FAB.
  17. Create / Edit Playlist Dialog with validation.
  18. Playlist Queue with Play All, Shuffle, and Delete.
  19. Sleep Timer with presets (15m, 30m, 1h, 2h, End of Video) and countdown.
  20. Categorized Settings (Appearance, Playback, Library, Other).
  21. Theme Selection Dialog (Light, Dark, Follow System).
  22. About & Privacy Screen with version info, licenses, and sharing.
- **Local Persistence**: Powered by Room Database and Jetpack DataStore Preferences. 100% offline, zero accounts, zero analytics, zero data harvesting.
- **Picture-in-Picture (PiP)**: Seamless multitasking while playing videos.
- **Lock Mode**: Lock screen controls to prevent accidental touches during movies.

---

## Play Store Preparation & Build Guide

### 1. Generating an Upload Keystore
To sign your release APK and Google Play Store App Bundle (`.aab`) with a consistent release key, generate an upload keystore on your computer using Java's `keytool`:

```bash
keytool -genkey -v -keystore videonic-upload-key.jks -alias videonic -keyalg RSA -keysize 2048 -validity 10000
```

Store this keystore safely. All future updates to Google Play must be signed with this exact key.

### 2. GitHub Actions CI Configuration
This project includes a complete GitHub Actions workflow (`.github/workflows/android-build.yml`) that automatically compiles, tests, and packages release APKs and AABs whenever you push to `main` or trigger manually.

To enable automated release signing, encode your keystore to Base64:
```bash
# On Linux / macOS
base64 -w 0 videonic-upload-key.jks > keystore_base64.txt

# On Windows (PowerShell)
[Convert]::ToBase64String([IO.File]::ReadAllBytes("videonic-upload-key.jks")) | Out-File -Encoding utf8 keystore_base64.txt
```

In your GitHub repository, go to **Settings > Secrets and variables > Actions** and add:
- `ANDROID_KEYSTORE_BASE64`: The Base64 string from `keystore_base64.txt`.
- `ANDROID_KEYSTORE_PASSWORD`: The password chosen for the keystore.
- `ANDROID_KEY_ALIAS`: `videonic` (or your chosen alias).
- `ANDROID_KEY_PASSWORD`: The password chosen for the key alias.

When you push code or trigger the workflow, GitHub Actions will produce:
- `Videonic-Lite-Debug-APK`
- `Videonic-Lite-Release-APK`
- `Videonic-Lite-PlayStore-AAB`

### 3. Google Play Console Declarations
- **Title**: Videonic Lite Video Player (<30 characters, policy compliant).
- **Core Category**: Video Players & Editors.
- **Permissions Declaration**:
  - `READ_MEDIA_VIDEO` is declared strictly for the core purpose of browsing, organizing, and playing on-device video files.
  - The app includes zero background analytics, zero advertising SDKs, and zero internet server storage.
- **Target SDK**: API 36 (Android 16 compatible).
- **Minimum SDK**: API 26 (Android 8.0 Oreo+).

---

## Architecture
- Single Activity (`MainActivity.kt`)
- Jetpack Compose + Material 3 UI
- AndroidX Media3 ExoPlayer (`1.5.1`)
- Scoped Storage MediaStore queries + SAF file picker
- Room 2.7.0 with Kotlin Symbol Processing (KSP)
- Jetpack DataStore Preferences
- Coil 2.7.0 with VideoFrameDecoder
