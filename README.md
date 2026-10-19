# ElecPilot
<a id="readme-top"></a>

[![ElecPilot](docs/assets/feature-graphic.png)](https://github.com/Hcmdz/ElecPilot/releases/latest)

**Read this in other languages**

🇺🇸 [English](README.md) | 🇫🇷 [Français](docs/translations/README.fr-FR.md) | 🇸🇦 [العربية](docs/translations/README.ar-SA.md)
<!-- Localized app: values-fr/values-ar (+ldrtl) mirror README langs; supportsRtl verified. -->

<div align="center">
   <a href="https://github.com/Hcmdz/ElecPilot/releases">
      <img src="get-it-on-github-v2.png" width="168" height="50" alt="Get it on GitHub">
   </a>
   <a href="https://apps.obtainium.imranr.dev/redirect?r=obtainium://add/https://github.com/Hcmdz/ElecPilot/">
      <img src="get-it-on-obtainium-v2.png" width="168" height="50" alt="Get it on Obtainium">
   </a>
</div>

<br />

[![Android](https://img.shields.io/badge/Platform-Android-green.svg?logo=android)](https://www.android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple.svg?logo=kotlin)](https://kotlinlang.org)
[![MinSDK](https://img.shields.io/badge/MinSDK-29-orange.svg)](#)
[![TargetSDK](https://img.shields.io/badge/TargetSDK-36-blue.svg)](#)
[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)
[![Release](https://img.shields.io/github/v/release/Hcmdz/ElecPilot)](https://github.com/Hcmdz/ElecPilot/releases/latest)
[![Downloads](https://img.shields.io/github/downloads/Hcmdz/ElecPilot/total)](https://github.com/Hcmdz/ElecPilot/releases)
[![Stars](https://img.shields.io/github/stars/Hcmdz/ElecPilot)](https://github.com/Hcmdz/ElecPilot/stargazers)
[![Forks](https://img.shields.io/github/forks/Hcmdz/ElecPilot)](https://github.com/Hcmdz/ElecPilot/network/members)
[![Compose M3](https://img.shields.io/badge/Jetpack_Compose-Material_3-4285F4.svg?logo=jetpackcompose)](https://developer.android.com/jetpack/compose)

Android application for managing electrical motor starters and PLC I/O modules in industrial environments.

In the field, motor data lives on paper or scattered spreadsheets — lost, outdated, unsearchable. ElecPilot is the offline-first field notebook: every starter and PLC module recorded once, found in seconds, exportable to Excel or CSV.

- **Package**: `com.HcmDz.ElecPilot`
- **Version**: 6.5.4 (versionCode 34)
- **Author**: HcmDZ &lt;HcmDz.Dev@gmail.com&gt;

---

<p align="center">
  <a href="#screenshots">View Demo</a>
  ·
  <a href="https://github.com/Hcmdz/ElecPilot/issues/new?labels=bug">Report Bug</a>
  ·
  <a href="https://github.com/Hcmdz/ElecPilot/issues/new?labels=enhancement">Request Feature</a>
</p>

<details>
<summary>Table of Contents</summary>

- [📦 Downloads](#-downloads)
- [Screenshots](#screenshots)
- [Key Features](#key-features)
- [Tech Stack & Architecture](#tech-stack--architecture)
- [Getting Started](#getting-started)
- [Testing](#testing)
- [Project Structure](#project-structure)
- [Apache POI Shadow Jar](#apache-poi-shadow-jar)
- [Signing](#signing)
- [APK Size](#apk-size)
- [Changelog](#changelog-v57--v654)
- [Legal](#legal)
- [License](#license)
- [Roadmap](#️-roadmap)
- [Related Docs](#related-docs)
- [Contact](#-contact)
</details>

---

## 📦 Downloads

Get ElecPilot on GitHub: **[Latest release](https://github.com/Hcmdz/ElecPilot/releases/latest)** (`ElecPilot-release.apk`, ~13.3 MB, `arm64-v8a`).

[![Download](https://img.shields.io/badge/Download-Latest_Release-green.svg?logo=android)](https://github.com/Hcmdz/ElecPilot/releases/latest)

- Requires Android 9+ (API 29) with **arm64-v8a**; allow *Install unknown apps* for your browser when prompted.
- Verify integrity: `sha256sum -c ElecPilot-release.apk.sha256` (sidecar next to the APK).
- ⚠️ Coming from ≤ 6.5: manual reinstall required (new signing key since 6.5.1 — back up, uninstall, install, restore).

<p align="right">(<a href="#readme-top">back to top</a>)</p>

---

## Screenshots

Find any starter in seconds, light or dark — lists, details, and settings at a glance.

### Motor Starters

<div align="center">

| Motor list — table view (light mode) | Motor list — table view (dark mode) |
|---|---|
| <a href="screenshots/motor-list-table-view-light.png"><img src="screenshots/motor-list-table-view-light.png" width="270" alt="Motor list — table view, light mode"></a> | <a href="screenshots/motor-list-table-view-dark.png"><img src="screenshots/motor-list-table-view-dark.png" width="270" alt="Motor list — table view, dark mode"></a> |
| Motor list — card view (light mode) | Motor detail (dark mode) |
| <a href="screenshots/motor-list-card-view-light.png"><img src="screenshots/motor-list-card-view-light.png" width="270" alt="Motor list — card view, light mode"></a> | <a href="screenshots/motor-detail-dark.png"><img src="screenshots/motor-detail-dark.png" width="270" alt="Motor detail, dark mode"></a> |

</div>

### PLC I/O Modules

Every module on record — table or card view, with full detail per module.

<div align="center">

| PLC list — card view (light mode) | PLC list — card view (dark mode) |
|---|---|
| <a href="screenshots/plc-list-card-view-light.png"><img src="screenshots/plc-list-card-view-light.png" width="270" alt="PLC list — card view, light mode"></a> | <a href="screenshots/plc-list-card-view-dark.png"><img src="screenshots/plc-list-card-view-dark.png" width="270" alt="PLC list — card view, dark mode"></a> |
| PLC list — table view (light mode) | PLC detail (dark mode) |
| <a href="screenshots/plc-list-table-view-light.png"><img src="screenshots/plc-list-table-view-light.png" width="270" alt="PLC list — table view, light mode"></a> | <a href="screenshots/plc-detail-dark.png"><img src="screenshots/plc-detail-dark.png" width="270" alt="PLC detail, dark mode"></a> |

</div>

### Settings

Backup, language, and updates — everything configurable in one place.

<div align="center">

| Settings (light mode) | Settings (dark mode) |
|---|---|
| <a href="screenshots/settings-light.png"><img src="screenshots/settings-light.png" width="270" alt="Settings, light mode"></a> | <a href="screenshots/settings-dark.png"><img src="screenshots/settings-dark.png" width="270" alt="Settings, dark mode"></a> |

</div>

<p align="right">(<a href="#readme-top">back to top</a>)</p>

---

## Key Features

### Motor Starters
- **Every starter on record.** Full CRUD, search, batch edit, statistics.
- **Hands-free lookup.** Speak to filter motor starters with voice search.
- **Start from a clean sheet.** Ready-to-fill Excel templates for motor starters and PLC I/O.

### PLC I/O Modules
- **Every module on record.** Dedicated database and views for PLC I/O modules.

### Backup & Export
- **Your data survives the phone.** Scheduled local backup to a folder you choose, plus manual backup at any time.
- **Paper trail on demand.** Excel & CSV export/import, and ready-to-fill Excel templates for motor starters and PLC I/O.

### App
- **Always up to date.** In-app update with auto-check & download from GitHub Releases.
- **Looks at home.** Material You theming with dynamic color, edge-to-edge.
- **Speaks your language.** System/EN/FR/AR localization with auto-detect of the device language.
- **Hardened by default.** FLAG_SECURE, cleartext blocked, app backups disabled, ProGuard log stripping.

<p align="right">(<a href="#readme-top">back to top</a>)</p>

---

## Tech Stack & Architecture

### Architecture

- **Single-module** app with `MainActivity` + Jetpack Compose navigation
- Data layer: Room databases + Repository pattern
- ViewModels with StateFlow
- WorkManager for scheduled local backups

### Core Libraries & Tools

| Category | Library | Version |
|---|---|---|
| **UI** | Jetpack Compose + Material 3 | BOM 2026.09.00 |
| **Activity** | Activity Compose | 1.13.0 |
| **Lifecycle** | Lifecycle Runtime Compose | 2.11.0 |
| **Async** | Kotlin Coroutines & Flow | 1.11.0 |
| **Database** | Room | 2.8.5 |
| **Networking** | OkHttp | 5.5.0 |
| **Excel** | Apache POI (shadow jar from centic9/poi-on-android) | 5.2.5 |
| **Scheduling** | WorkManager | 2.12.0 |
| **File Access** | DocumentFile (SAF) | 1.1.0 |
| **Security** | ProGuard, network security config | — |
| **Build** | AGP 9.4.1, Kotlin 2.4.21, KSP 2.3.12 | — |
| **Lint** | Android Security Lint | 1.0.4 |
| **Quality** | Detekt CLI | 1.23.8 |
| **Testing** | JUnit4, Room Testing, Coroutines Test | 4.13.2 / 2.8.5 / 1.11.0 |

### Security Features

| Control | Implementation |
|---|---|
| **Screenshot protection** | `FLAG_SECURE` on MainActivity |
| **Log stripping** | ProGuard strips `Log.d/v/i/e/w` (including exception stack traces) in release builds |
| **Network security** | Cleartext blocked (`network_security_config.xml`), system CAs only |
| **Backup disabled** | `allowBackup="false"` |
| **MTE** | `memtagMode="sync"` enabled in manifest |

### Permissions

- `INTERNET`, `ACCESS_NETWORK_STATE` — in-app update check and download
- `POST_NOTIFICATIONS` — reserved for backup progress and update status
- `REQUEST_INSTALL_PACKAGES` — in-app update install

Entry points: `MainActivity` (launcher), `FileProvider` (`${applicationId}.fileprovider`).

### CI & Quality

- GitHub Actions: `.github/workflows/ci.yml` (`testDebugUnitTest` + `lintDebug`), `codeql.yml`, Dependabot
- Detekt: `./gradlew detekt` (config `config/detekt/detekt.yml`)
- Required CI variable **names** (values stay in repo/environment secrets, never committed): release keystore credentials (`RELEASE_STORE_FILE`, `RELEASE_STORE_PASSWORD`, `RELEASE_KEY_ALIAS`, `RELEASE_KEY_PASSWORD`)

<p align="right">(<a href="#readme-top">back to top</a>)</p>

---

## Getting Started

### Prerequisites

- **Android Studio**: Latest stable (Meerkat or newer) — IDE ([doc](https://developer.android.com/studio))
- **JDK**: 17 — Gradle toolchain ([doc](https://docs.gradle.org/current/userguide/build_java_projects.html))
- **Android SDK**: compileSdk 37 ([install](https://developer.android.com/studio#downloads))

### Build APK

```bash
cd "ElecPilot"
./gradlew assembleRelease
```

Output: `app/build/outputs/apk/release/ElecPilot-release.apk`

### Everyday Use

```bash
./gradlew installDebug   # run on device
./gradlew test           # unit tests
./gradlew lintDebug      # static analysis
```

### Run on Device

1. Open the project in **Android Studio**.
2. Wait for Gradle sync to complete.
3. Select a device or emulator running **API 29+**.
4. Press **Run** (Shift + F10) or:
   ```bash
   ./gradlew installDebug
   ```

<p align="right">(<a href="#readme-top">back to top</a>)</p>

---

## Testing

```bash
./gradlew test
./gradlew connectedAndroidTest
```

---

## Project Structure

```
app/src/main/java/com/HcmDz/ElecPilot/
├── MainActivity.kt              # Single activity, edge-to-edge, FLAG_SECURE
├── data/
│   ├── db/                      # Room databases (Motor + PLC) and DAOs
│   ├── repository/              # Data repositories
│   └── BackupPreferences.kt     # Local backup settings models
├── ui/
│   ├── screens/                 # Compose screens (Main, MotorDetail, dialogs)
│   ├── viewmodel/               # ViewModels
│   ├── components/              # Reusable UI components
│   ├── theme/                   # Material 3 theming
│   └── views/plc/               # PLC-specific views
├── util/
│   ├── BackupManager.kt         # Local backup (Excel/CSV, SAF)
│   ├── ExcelUtil.kt             # Apache POI export/import + templates
│   ├── UpdateManager.kt         # In-app update (GitHub Releases)
│   ├── VoiceSearchEngine.kt     # Voice search for the motor list
│   ├── ContextUtils.kt          # Locale-aware context helpers
│   ├── NotificationHelper.kt    # Backup notifications
│   └── BackupScheduler.kt       # WorkManager: local backup schedule
└── worker/
    └── BackupWorker.kt          # WorkManager: local backup
```

---

## Apache POI Shadow Jar

Apache POI is bundled as a pre-built shadow jar from [centic9/poi-on-android](https://github.com/centic9/poi-on-android) rather than as a standard Maven dependency.

### Why a shadow jar?

Raw Maven POI + R8 (the Android minifier) is fundamentally broken. R8's obfuscation breaks xmlbeans' `SchemaTypeSystemImpl` class name parsing, and R8's shrinking generates broken synthetic bridge classes. The shadow jar solves this by:

- Relocating `javax.xml.stream` → `org.apache.poi.javax.xml.stream` (Android lacks the full StAX API)
- Relocating `javax.xml.namespace` → `org.apache.poi.javax.xml.namespace`
- Replacing missing `java.awt.*` classes with stubs
- Bundling the [aalto-xml](https://github.com/FasterXML/aalto-xml) StAX implementation
- Merging all POI dependencies into a single jar

The shadow jar is located at `app/libs/poishadow-all.jar` (~19.5 MB raw). R8 strips unused POI classes during release builds.

### System properties

The relocated StAX parsers require system properties to be set before any POI code runs. This is handled in `ExcelUtil.kt`:

```kotlin
System.setProperty("org.apache.poi.javax.xml.stream.XMLInputFactory", "com.fasterxml.aalto.stax.InputFactoryImpl")
System.setProperty("org.apache.poi.javax.xml.stream.XMLOutputFactory", "com.fasterxml.aalto.stax.OutputFactoryImpl")
System.setProperty("org.apache.poi.javax.xml.stream.XMLEventFactory", "com.fasterxml.aalto.stax.EventFactoryImpl")
System.setProperty("org.apache.poi.ss.ignoreMissingFontSystem", "true")
```

### Updating the shadow jar

To build a newer version:

```bash
git clone https://github.com/centic9/poi-on-android.git
cd poi-on-android
./gradlew :poishadow:shadowJar
cp poishadow/build/libs/poishadow-all.jar <path-to-ElecPilot>/app/libs/
```

The master branch of centic9/poi-on-android uses POI 5.5.1. The pre-built release (5.2.5-4) uses POI 5.2.5. Both work for basic xlsx read/write.

---

## Signing

The release APK is signed with a keystore. To build a release APK:

1. Create a `gradle.properties` file in the project root (already gitignored)
2. Add the two path properties below, plus the two password properties
   named after them:

```properties
RELEASE_STORE_FILE=/path/to/your/release.keystore
RELEASE_KEY_ALIAS=ALIAS
```

The password properties are `RELEASE_STORE_PASSWORD` and
`RELEASE_KEY_PASSWORD`. Give them their value locally, or supply them from
the environment. Never commit a value for them here.

3. Run `./gradlew assembleRelease`

---

## APK Size

Current release APK size: **13.3 MB** (13 948 290 bytes, `arm64-v8a` only, R8 enabled) — measured on the published v6.5.3 asset.

| Content | Uncompressed | Share |
|---|---|---|
| App code (dex, R8 + Compose + Room + OkHttp) | ~11.6 MB | 76 % |
| Apache POI / xmlbeans resources | ~3.0 MB | 20 % |
| Other resources and assets | ~0.6 MB | 4 % |
| Native libraries | ~10 KB | <1 % |

Size reduction techniques applied:
- POI shadow jar + R8 to strip unused Apache POI/xmlbeans classes
- `abiFilters` restricted to `arm64-v8a` for release builds
- `isMinifyEnabled = true` + `isShrinkResources = true` with ProGuard rules from centic9/poi-on-android

The APK was 20.3 MB before v6.5.3; dropping the bundled rclone native binary removed ~7 MB of it.

---

## Changelog (v5.7 → v6.5.4)

Versions follow [semver](https://semver.org/); full history lives in [GitHub Releases](https://github.com/Hcmdz/ElecPilot/releases).

### v6.5.4

- **Cloud backup removal completed** — the leftover files, the OAuth entry point and the local-network exception in the network security config are gone
- **Documentation corrected** — the feature list, the security table, the permissions and the APK size now describe what the app actually does
- Signed with the same certificate as v6.5.3, so this installs as a normal in-place update

### v6.5.3

- **Cloud backup removed** — Google now requires each app to use its own Cloud project for OAuth, so the shared project rclone relied on is no longer accepted
- **APK size 20.3 MB → 13.3 MB** — the bundled rclone native binary is gone
- Unchanged: local auto-backup, CSV/Excel import-export, search, filters, statistics, voice search, in-app update, EN/FR/AR

### v6.5.2

- **System language applies without restart** — switching the app language back to *System* now follows the current system language instead of the previously selected one

### v6.5.1

- **Trimmed rclone 1.75.1** — local/Drive/OneDrive backends only
- **New signing key** — manual reinstall required (back up, uninstall, install, restore)

### v6.5

- **Cancellation-safe background work** — `CancellationException` rethrown in cloud/local backup, update check and import paths (no more false failure notification or retry after cancel)
- **Snapshot writes on Main** — backup dialogs assign UI state back on the main thread
- **ASCII backup filenames** — export/cloud timestamps use `Locale.US` (no more non-Latin digits under Arabic locale)
- **PLC card polish** — favorite star size aligned with motor cards (18.dp)
- **Build** — AGP 9.4.0, OkHttp 5.5.0, WorkManager 2.11.2

### v6.3

- **No blank screen flash on cold start** — the full screen is gated behind `hasLoadedOnce` so the empty shell (header with "0" counter) is never drawn before the first data load
- **Persistent last module** — the app reopens on the last active module (Départs / PLC I/O) after a cold restart

### v6.2

- **In-app update** — auto-check GitHub Releases, download & install with progress
- **Blank template generation** — ready-to-fill Excel templates (EN/FR headers)
- **System language option** — auto-detect device language on first install
- **Bug fixes** — CancellationException handling, filename sanitization, header validation thresholds, export queue guard
- **Security audit** — update & template feature hardened (SHA-256 recommendation, path traversal prevention)

### v6.0 → v6.1

Security audit fixes (OWASP MASVS 2.1):

| # | Severity | Fix |
|---|---|---|
| 1 | CRITICAL | Rclone temp config file: owner-only readable permissions |
| 2 | HIGH | WebView OAuth URL allowlist to prevent phishing |
| 4 | MEDIUM | Cloud backup disk cache encrypted (AES-256-GCM) |
| 5 | MEDIUM | `FLAG_SECURE` on MainActivity (screenshot/recording protection) |
| 7 | LOW | HOME env fallback uses `filesDir` instead of hardcoded path |
| 8 | LOW | SharedPreferences `MODE_PRIVATE` explicit usage |
| 9 | LOW | Error logs stripped of file paths and operation details |
| 10 | LOW | ProGuard strips `Log.e/w` with exception objects in release |

<p align="right">(<a href="#readme-top">back to top</a>)</p>

---

## Legal

- [Terms of Service](https://hcmdz.github.io/ElecPilot/terms/)
- [Privacy Policy](https://hcmdz.github.io/ElecPilot/privacy/)

---

## License

[![License: GPL v3](https://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0)

This project is licensed under the **GNU General Public License v3.0** — see the [LICENSE](LICENSE) file for details.

## 🗺️ Roadmap

Planned work is tracked in the [open issues](https://github.com/Hcmdz/ElecPilot/issues) — propose features there.

## Related Docs

- [Contributing](CONTRIBUTING.md) · [Third-Party Components](THIRD_PARTY.md) · [Security](SECURITY.md)
- [Privacy Policy](docs/privacy/) · [Terms](docs/terms/)

## 📬 Contact

HcmDZ — [@Hcmdz](https://github.com/Hcmdz)

Project link: [https://github.com/Hcmdz/ElecPilot](https://github.com/Hcmdz/ElecPilot)

---

Made with ❤️ by HcmDZ
