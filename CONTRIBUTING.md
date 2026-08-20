# Contributing to ElecPilot

## Getting Started

1. Fork the repository
2. Clone your fork
3. Open in Android Studio (latest stable)
4. Sync Gradle and build

## Requirements

- Android Studio latest stable
- JDK 17+
- Android SDK 36+ (compileSdk 37)
- Device or emulator running Android 10+ (minSdk 29)

## Build

```bash
# Debug build
./gradlew assembleDebug

# Release build (requires signing config in gradle.properties)
./gradlew assembleRelease
```

## Code Style

- Kotlin with official conventions
- Jetpack Compose for UI
- Material 3 design system
- Follow existing patterns in the codebase

## Pull Requests

1. Create a feature branch from `main`
2. Make your changes
3. Run `./gradlew lint` and fix any issues
4. Write clear commit messages
5. Open a PR with a description of changes

## Issues

- Use the provided issue templates
- Include device model, Android version, and app version
- Steps to reproduce for bug reports
