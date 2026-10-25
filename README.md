# Vrddhih

Vrddhih (Sanskrit *vṛddhiḥ*, "growth") is a native Android personal-growth app styled after the hunter "status window" and quest system of Solo Leveling. Quests, habits, goals and finances will feed a personal progression system of XP, levels and stats.

It is a private, local-only app. There are no accounts and no cloud. Each install keeps its own data on its own device.

## Technology stack

- Kotlin, Jetpack Compose, Material 3
- Navigation Compose, AndroidX ViewModel and Lifecycle
- MVVM (repositories arrive with the database in Phase 1)
- Gradle Kotlin DSL with a version catalog (`gradle/libs.versions.toml`)
- AGP 9.4.1 with built-in Kotlin (KGP 2.2.10), Gradle 9.6.0, compileSdk/targetSdk 37, minSdk 26

## Development environment

- CachyOS Linux, Fish shell
- Android Studio (the bundled JBR is used as the JDK; the Gradle daemon toolchain is JDK 25)
- Android SDK at `~/Android/Sdk`

## Project structure

```
app/src/main/java/com/vrddhih/app/
├── MainActivity.kt              # Entry point: edge-to-edge, sets VrddhihTheme + VrddhihApp
├── VrddhihApp.kt                # App shell: Scaffold, bottom bar, NavHost
├── core/
│   ├── theme/                   # Color.kt, Type.kt, Dimens.kt, Theme.kt (design tokens)
│   ├── navigation/              # TopLevelDestination, VrddhihNavHost, VrddhihBottomBar
│   └── ui/components/           # SystemPanel, headers, progress bar/ring, tags, FeaturePlaceholder
└── features/
    ├── dashboard/               # DashboardScreen, DashboardPanels, DashboardViewModel, DashboardUiState
    │   └── mock/                # DashboardMockData: the only mock-data source
    ├── quests/                  # Placeholder screen
    ├── habits/                  # Placeholder screen
    ├── goals/                   # Placeholder screen
    ├── finance/                 # Placeholder screen
    └── statistics/              # Placeholder screen
docs/
├── ARCHITECTURE.md
└── ROADMAP.md
```

## Opening in Android Studio

1. In Android Studio, choose **File › Open** and select `~/AndroidStudioProjects/Vrddhih`.
2. Let Gradle sync finish. The version catalog and wrapper are already pinned.
3. Select the `app` run configuration and a device or emulator.

## Running a debug build

From Android Studio, press **Run ▶**. From a terminal (fish):

```fish
set -x JAVA_HOME ~/Applications/android-studio/jbr
./gradlew assembleDebug      # APK: app/build/outputs/apk/debug/app-debug.apk
./gradlew test               # JVM unit tests
./gradlew installDebug       # Install on a connected device or emulator
```

## Current status: Phase 0 (Foundation and UI)

- The dark hunter-system design system: tokens, typography and reusable components.
- A dashboard with player status, daily summary, today's quests, habit progress, goals and upcoming deadlines. **All of it is sample data**, labelled "Sample" in the UI.
- Quest completion on the dashboard is a temporary preview. It lives in memory and resets when the app process ends.
- A working six-tab bottom navigation (Dashboard, Quests, Habits, Goals, Finance, Stats) that saves and restores tab state.
- Placeholder screens for Quests, Habits, Goals, Finance and Stats that describe each planned feature.
- No database, no persistence, no network permission, no login and no cloud services.

## Planned phases

| Phase | Scope |
|-------|-------|
| 0 | Foundation and UI *(current)* |
| 1 | Room database and local data architecture |
| 2 | Tasks and recurring quests |
| 3 | Goals and habits |
| 4 | XP, levels and achievements |
| 5 | Personal finance tracker |
| 6 | Reminders and notifications |
| 7 | Statistics, reliability and release preparation |

Details are in [docs/ROADMAP.md](docs/ROADMAP.md). Architecture notes are in [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md).
