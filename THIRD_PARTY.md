# Third-Party Components

This project includes third-party software components with their respective licenses.

## Bundled Binaries

### Apache POI (Shadow JAR)

- **Project:** [centic9/poi-on-android](https://github.com/centic9/poi-on-android)
- **Library:** Apache POI 5.2.5
- **License:** Apache License 2.0
- **SPDX:** `Apache-2.0`
- **File:** `app/libs/poishadow-all.jar` (20 MB shadow JAR)
- **Usage:** Excel template export (XLSX format)
- **Required notice:** Includes software developed by The Apache Software Foundation (http://www.apache.org/)

### rclone

- **Project:** [rclone/rclone](https://github.com/rclone/rclone)
- **Version:** 1.70.3
- **License:** MIT License
- **SPDX:** `MIT`
- **File:** `app/src/main/jniLibs/arm64-v8a/librclone.so`
- **Usage:** Cloud backup to Google Drive / OneDrive via CLI

## Dependency Licenses

| Library | License | SPDX |
|---|---|---|
| AndroidX / Jetpack Compose | Apache 2.0 | `Apache-2.0` |
| OkHttp | Apache 2.0 | `Apache-2.0` |
| Room | Apache 2.0 | `Apache-2.0` |
| WorkManager | Apache 2.0 | `Apache-2.0` |
| Kotlin Coroutines | Apache 2.0 | `Apache-2.0` |

## Notes

- `poishadow-all.jar` is a fat JAR merging Apache POI and its transitive dependencies.
- `librclone.so` is a pre-built Go shared library compiled for arm64-v8a.
- All Apache 2.0 dependencies include their respective license files in the AAR/APK.
