# PodTrail

<div align="center">
  <img width="260" height="260" alt="PodTrail Logo" src="https://github.com/user-attachments/assets/9ff08f4a-5a53-46cb-b205-8d31fdbee25e" />
  <br>
  <br>

  ![Android Build](https://github.com/SV-stark/PodTrail/actions/workflows/build.yml/badge.svg)
  ![License](https://img.shields.io/badge/License-GPL%20v3-blue.svg)
  ![Kotlin](https://img.shields.io/badge/Kotlin-2.4.0-purple.svg)
  ![Compose](https://img.shields.io/badge/Material%203-Compose-green.svg)

  <p>
    <b>A minimal, privacy-focused, size-optimized Android podcast tracker.</b>
  </p>
</div>

---

## 📖 Overview

**PodTrail** is a lightweight, privacy-focused Android application designed for podcast enthusiasts who value simplicity, performance, and clean design. Built with modern Android architecture and Jetpack Compose Material 3, it offers an offline-first experience for tracking your favorite podcasts without bloat or telemetry.

---

## ✨ Key Features

- **🔍 Podcast Search & Discovery**: Search podcasts via the **iTunes API**, explore curated top charts by genre, or add custom RSS URLs.
- **📁 OPML & Backup Management**:
  - **OPML Import & Export**: Easily transfer subscriptions between podcast apps using standard OPML XML files.
  - **GZIP Database Backups**: Export and restore your complete listening history, favorites, and settings to a compressed JSON backup file.
- **⚡ Size & Resource Optimized**:
  - **Minimal APK Footprint**: Built with standalone vector icons, ABI splitting, and R8 shrinking to keep download size exceptionally small.
  - **Efficient Image Caching**: Configured Coil 3 memory and disk caching with crossfade transitions to save network bandwidth and RAM.
  - **Database Maintenance**: SQL-level storage maintenance for cleaning old episodes and compacting database size.
- **🎨 Modern Material 3 UI**:
  - Full **Dynamic Color** support (Android 12+ wallpaper colors).
  - Pure **AMOLED Dark Mode** and custom color picker palettes.
  - Grid & List view toggles with percentage progress badges.
- **📊 Episode Tracking & Stats**:
  - Track listening history and maintain daily listening streaks.
  - Sort episodes by **Date** (Newest/Oldest) or **Duration** (Shortest/Longest).
  - Multi-select batch actions (mark listened, add to playlist, delete).
- **📅 Interactive Calendar**: View episode releases organized on an interactive monthly calendar grid.
- **🔄 Background Sync**: Automatically refresh podcast feeds in the background using AndroidX WorkManager.

---

## 🛠️ Tech Stack

- **Language**: [Kotlin 2.4+](https://kotlinlang.org/)
- **UI Framework**: [Jetpack Compose Material 3](https://developer.android.com/jetpack/compose)
- **Dependency Injection**: [Hilt](https://dagger.dev/hilt/)
- **Database**: [Room](https://developer.android.com/training/data-storage/room) (with optimized SQL queries & migrations)
- **Networking**: [OkHttp 5](https://square.github.io/okhttp/) & [Gson](https://github.com/google/gson)
- **Image Loading**: [Coil 3](https://coil-kt.github.io/coil/) (Disk & Memory Cache configured)
- **Background Operations**: [WorkManager](https://developer.android.com/topic/libraries/architecture/workmanager)
- **Architecture**: MVVM with Uni-directional Data Flow (UDF)

---

## 🚀 Getting Started

### Prerequisites

- **Android Studio** (Ladybug | 2024.2.1 or newer recommended)
- **JDK 21** (Required for Gradle build compatibility)
- **Android SDK**: `minSdk 23` | `targetSdk 37`

### Installation & Build

1. **Clone the Repository**
   ```bash
   git clone https://github.com/SV-stark/PodTrail.git
   cd PodTrail
   ```

2. **Open in Android Studio**
   - Select **Open an existing project** and navigate to the `PodTrail` root directory.

3. **Build & Run**
   - Allow Gradle to sync dependencies.
   - Run the `:app` module on an emulator or physical device.

---

## 📄 License

This project is open-source and licensed under the **GPL v3 License**. See the [LICENSE](LICENSE) file for details.
