# 🎵 Music Player

A fully-featured Android music player built with **Kotlin**, **Media3/ExoPlayer**, **Room**, and **Material Design 3**.

---

## ✨ Features

- 🎵 **Play local music** — scans device storage via MediaStore
- 🔀 **Shuffle & Repeat** — repeat one, repeat all, shuffle modes
- ❤️ **Favorites** — mark songs as favorites, stored in Room DB
- 📂 **Playlists** — create, rename, delete playlists, add/remove songs
- 🔍 **Search** — search songs, artists, albums in real-time
- 🎨 **Themes** — Light / Dark / System default
- ⏱️ **Sleep Timer** — auto-stop playback after set duration
- 🎧 **Headset support** — pause on unplug, Bluetooth disconnect
- 🔔 **Media notification** — persistent playback controls in notification tray
- 🔊 **Mini Player** — persistent mini player on main screen
- 📋 **Sort options** — sort by Title, Artist, Album, Date Added, Duration
- 📱 **Now Playing screen** — full album art, seekbar, song info

---

## 🏗️ Architecture

```
MVVM + Repository Pattern
├── UI Layer         → Activities, Fragments, Adapters
├── ViewModel        → MusicViewModel (shared state)
├── Repository       → MusicRepository (single source of truth)
├── Data Sources
│   ├── MediaStore   → Local audio files
│   └── Room DB      → Playlists, Favorites
└── Service          → MusicService (Media3 MediaSessionService)
```

---

## 🛠️ Tech Stack

| Layer | Technology |
|-------|-----------|
| Language | Kotlin |
| Playback | Media3 / ExoPlayer |
| Database | Room |
| Image Loading | Glide |
| Navigation | Jetpack Navigation Component |
| UI | Material Design 3, ViewBinding |
| Async | Kotlin Coroutines + LiveData |
| DI | Manual (Repository pattern) |

---

## 🚀 Build & Run

### Prerequisites
- Android Studio Hedgehog or newer
- JDK 17
- Android SDK 34

### Steps
```bash
git clone https://github.com/DanielThatu47/Music_Repo.git
cd Music_Repo
./gradlew assembleDebug
```

The APK will be at: `app/build/outputs/apk/debug/app-debug.apk`

---

## 🤖 GitHub Actions CI/CD

Every push to `main` automatically:
1. Builds a **Debug APK**
2. Builds a **Release APK**
3. Uploads both as **downloadable artifacts**

To download: Go to **Actions → latest workflow run → Artifacts**

For tagged releases (`v1.0`, `v1.1`, etc.), a **GitHub Release** is created automatically with the APK attached.

---

## 📁 Project Structure

```
app/src/main/
├── java/com/danielthatu/musicplayer/
│   ├── adapters/        # RecyclerView adapters
│   ├── database/        # Room DAOs & Database
│   ├── fragments/       # UI Fragments
│   ├── models/          # Data classes
│   ├── receivers/       # BroadcastReceivers
│   ├── services/        # MusicService (Media3)
│   ├── utils/           # Helpers & Repository
│   ├── viewmodels/      # ViewModel
│   ├── MainActivity.kt
│   ├── PlayerActivity.kt
│   └── SplashActivity.kt
└── res/
    ├── drawable/        # Vector icons
    ├── layout/          # XML layouts
    ├── menu/            # Menu XMLs
    ├── navigation/      # Nav graph
    └── values/          # Strings, colors, themes
```

---

## 📸 Screens

| Splash | Songs | Now Playing |
|--------|-------|-------------|
| App logo on brand color | All songs with album art | Full-screen player with seekbar |

| Favorites | Playlists | Search |
|-----------|-----------|--------|
| Hearted songs | Create & manage playlists | Real-time search |

---

## 📄 License

```
MIT License — Daniel Thatu © 2025
```
