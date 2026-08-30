# MusicWave

A modern music streaming app inspired by ArchiveTune, built with Kotlin and Jetpack Compose.

## Features

- Stream music from YouTube Music
- Search songs, artists, albums, and playlists
- Browse home page with recommendations
- View artist and album details
- Create and manage playlists
- View lyrics
- Background playback with media notifications
- Mini player for quick controls
- Now Playing screen with full controls

## Architecture

- **UI Layer**: Jetpack Compose with Material 3
- **Data Layer**: InnerTube API (YouTube Music) + MoriExtractor
- **Playback**: Media3 (ExoPlayer) with MediaSession
- **DI**: Hilt
- **Networking**: Ktor + OkHttp

## Tech Stack

- Kotlin 2.0
- Jetpack Compose
- Material 3
- Hilt
- Ktor
- Media3/ExoPlayer
- Coil
- Room
- DataStore
- Coroutines

## Building

1. Clone the repository
2. Open in Android Studio
3. Sync Gradle
4. Build and run on device or emulator

## License

GPL-3.0