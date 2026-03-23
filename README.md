# Aura Music Streaming App (MVP)

A beautiful, functional Minimal Viable Product for a free, ad-free music streaming Android application.

Built using Jetpack Compose, ExoPlayer (Media3), Room Database, Retrofit, and Hilt. It integrates the official YouTube Data API v3 for blazing-fast search results, and uses NewPipe Extractor to stream the background audio seamlessly without requiring a premium subscription.

## Features

- **Broadcast Retro Aesthetic:** Stunning, retro-inspired UI.
- **Background Playback:** Audio continues playing even when the app is backgrounded or the screen is locked, thanks to MediaSessionService and ExoPlayer.
- **YouTube Audio Streaming:** Uses `NewPipe Extractor` to pull raw `.m4a` audio streams from YouTube Video IDs.
- **Search:** Fully featured track searching via the Google YouTube Data API v3.
- **Local Library (Room):** Save playlists, tracks, and library data locally entirely offline.
- **Spotify Playlist Import:** Paste a public Spotify playlist URL to instantly map the tracks and import them into your app!

---

## 🚀 Setup & Build Instructions

Before opening this project in Android Studio, you **must** supply your own YouTube Data API key so the search functionality works.

### 1. Generate an API Key
1. Go to the [Google Cloud Console](https://console.cloud.google.com/).
2. Create a new project, and go to **Library**.
3. Search for **YouTube Data API v3** and enable it.
4. Go to **Credentials**, click **Create Credentials -> API Key**. Copy the generated key.

### 2. Configure `local.properties`
To keep API keys completely safe and out of source control, this project uses the `local.properties` file which is Git-ignored.

Create or open the `local.properties` file in the root project folder (same folder as `settings.gradle.kts`) and add your key:

```properties
# Add this line to local.properties
YOUTUBE_API_KEY=AIzaSy...............
```

*During the build process, Gradle will automatically read this file and safely compile it into a secure `BuildConfig.YOUTUBE_API_KEY` string resource.*

### 3. Open in Android Studio
1. Open Android Studio and select **Open**.
2. Select the repository root folder.
3. Wait for the initial **Gradle Sync** to finish.
4. Select a device from the Android emulator list or plug in a physical device.
5. Click **Run** (the green ▶ play button)!

---

## Technical Stack
- **Architecture:** MVVM with Hilt Dependency Injection.
- **UI:** Jetpack Compose + Coil for image loading.
- **Persistence:** Room Database.
- **Networking:** Retrofit + OkHttp.
- **Media:** AndroidX Media3 (ExoPlayer).
