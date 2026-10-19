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

## Dependency Licenses

Shipped dependencies (declared in `gradle/libs.versions.toml`):

| Library | Version | License | SPDX |
|---|---|---|---|
| Kotlin stdlib | 2.4.21 | Apache 2.0 | `Apache-2.0` |
| Jetpack Compose (BOM) | 2026.09.00 | Apache 2.0 | `Apache-2.0` |
| Activity-Compose | 1.13.0 | Apache 2.0 | `Apache-2.0` |
| Lifecycle-runtime | 2.11.0 | Apache 2.0 | `Apache-2.0` |
| Room | 2.8.5 | Apache 2.0 | `Apache-2.0` |
| WorkManager | 2.12.0 | Apache 2.0 | `Apache-2.0` |
| OkHttp | 5.5.0 | Apache 2.0 | `Apache-2.0` |
| DocumentFile | 1.1.0 | Apache 2.0 | `Apache-2.0` |
| Kotlin Coroutines (transitive) | 1.11.0 | Apache 2.0 | `Apache-2.0` |

Test-only dependencies (not shipped in the APK):

| Library | Version | License | SPDX |
|---|---|---|---|
| JUnit 4 | 4.13.2 | EPL-1.0 | `EPL-1.0` |
| androidx.test (ext-junit / runner) | 1.3.0 / 1.7.0 | Apache 2.0 | `Apache-2.0` |
| core-testing | 2.2.0 | Apache 2.0 | `Apache-2.0` |
| room-testing | 2.8.5 | Apache 2.0 | `Apache-2.0` |
| coroutines-test | 1.11.0 | Apache 2.0 | `Apache-2.0` |

## Notes

- `poishadow-all.jar` is a fat JAR merging Apache POI and its transitive dependencies.
- All Apache 2.0 dependencies include their respective license files in the AAR/APK.
- rclone was bundled as a native library until v6.5.3, when cloud backup was
  removed. It is no longer shipped in the APK. The build script that produced
  it remains under `tools/rclone/` for anyone who needs the binary.
