# Implementation Plan - Navigation, State, and UI Refinements

This plan addresses critical UI issues, refines the playback experience, and streamlines settings as requested.

## 1. UI State & Navigation Fixes
- **Fix "No video found" Flash:** Update `ScreenHomeVideos` and `ScreenFolders` to strictly wait for `isLoading == false` before rendering the empty state. This will eliminate the brief flicker during data fetching.
- **Restore Favorites Navigation:** Re-integrate the `ScreenFavorites` UI inside the `MainAppScaffold` content area as a bottom nav tab. This ensures the Bottom Navigation and Top Bar remain visible when viewing favorites.
- **Fix PiP Restore Flow:** Update `MainAppScaffold` and `MainActivity` to ensure that when the user exits PiP mode by tapping the window, the app remains on the `PLAYER` screen instead of defaulting to `HOME`.

## 2. Playback & Feature Refinements
- **Simplify Rotation:** 
    - Remove the "Auto Rotate" toggle from `ScreenSettings`.
    - Retain the manual rotation button in the video player to cycle through Portrait and Landscape orientations.
- **Refine Background Play:**
    - Remove the headphone icon and the explicit "Background Audio" setting.
    - Logic change: Audio will only continue when the app enters PiP mode. Normal backgrounding (without PiP) will pause playback to conserve battery and follow standard video player behavior.
- **Fix Sort Logic:**
    - Audit the `LibraryViewModel` sorting implementation to ensure results are updated instantly.
    - Confirm "Newest first" is the hardcoded default for every fresh app launch.

## 3. Visual & Stability Polish
- **Splash Screen:** Ensure the splash screen logic is robust for cold starts.
- **General Audit:** Review interaction sources and animations across all screens for consistent performance.

## Verification
- **Cold Start:** Verify Splash screen appears once.
- **Navigation:** Test switching between Videos, Folders, Recent, and Favorites while keeping the bottom bar visible.
- **PiP Mode:** Enter PiP, return to app, and verify the video player screen is still active.
- **Loading States:** Verify no "No video" text flashes when the app first loads.
