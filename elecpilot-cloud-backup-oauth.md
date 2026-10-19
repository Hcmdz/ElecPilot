# ElecPilot — cloud backup OAuth: findings and options

Date: 2026-10-10. All claims below were read from the source, not recalled.
Repo state read from GitHub (`Hcmdz/ElecPilot`, private).

## Objective

Decide how ElecPilot obtains Google Drive / OneDrive authorization, given
that rclone's shared OAuth client is being retired during 2026.

## The defect (confirmed)

`RcloneAuthActivity.kt:156` runs:

```
librclone authorize <drive|onedrive> --auth-no-open-browser --config <tmp>
```

No client-id argument. `RcloneDriveService.saveRemoteConfig()` writes these
config keys (`contract.md` §4, `RcloneDriveService.kt:280`):

```
[name] / type / scope=drive / token = {...} / drive_id / drive_type
```

No `client_id`, no `client_secret`. **The app therefore uses rclone's shared
OAuth client.** When that client retires, `authorize` stops returning a token
and the "connect to Drive" button fails for every user. The `.so` keeps
working; only token acquisition dies.

Upstream, verbatim: *"This shared client_id is being retired and will stop
working during 2026. To avoid interruption you must create and use your own
client_id, so creating one is now required rather than merely recommended."*

## What exists today

| Asset | Detail |
|---|---|
| `app/src/main/jniLibs/arm64-v8a/librclone.so` | rclone 1.75.1, MIT, UPX, 7.1 MB |
| `app/src/main/jniLibs/x86_64/librclone.so` | debug only, not UPXed, 26 MB |
| `tools/rclone/main.go` | trimmed build: cmd/authorize, cmd/config, cmd/copyto; backends local, drive, onedrive (+ crypt, transitive) |
| `tools/rclone/build-librclone.sh` | build script |
| `tools/rclone/toolchain.lock` | GO 1.26.0, NDK 26.1.10909125, android-29 clang, minSdk 29 |
| `tools/rclone/contract.md` | CLI contract + fragile couplings + size gate |
| `tools/rclone/size-baseline.txt` | arm64 7448268 B, x86_64 27559024 B, apk ~20971520 B |
| `.github/workflows/rclone.yml` | bump gate |

The contract declares 6 fragile couplings — OAuth URL regex on stderr, token
capture between the `--- >` markers, a WebView host allowlist, config key
forward-compat, timestamp formats, progress JSON. All parse rclone's human
output.

## SAF is already in the codebase

`BackupManager.kt` (local backup) already uses the Storage Access Framework:

```kotlin
import androidx.documentfile.provider.DocumentFile
DocumentFile.fromTreeUri(context, Uri.parse(uriString))
```

plus `createFile`, `findFile`, `listFiles`, `cleanupOldBackupsSaf`, and
`takePersistableUriPermission` in `BackupSettingsDialog.kt:72`.

Drive and OneDrive are both Android DocumentsProviders, so they already appear
in the SAF picker. This path needs **no client ID, no secret, no Play
Services**.

## Options

| | 1. Own client ID into rclone | 2. AuthorizationClient + rclone | 3. SAF only |
|---|---|---|---|
| Client ID to create | yes | yes | **no** |
| Secret in APK | yes (discouraged) | no | no |
| Requires Play Services | no | yes | no |
| Google OAuth verification for distribution | yes | yes | **no** |
| OneDrive supported | yes | yes | yes |
| Work | hours | days | days |
| Removes rclone | no | no | yes (-7 MB APK) |

Reference implementations for option 2, both live as of 2026-10:
`rukamori/ArchiveTune` (6,958 stars, `Identity.getAuthorizationClient`) and
`Kototoro-app/Kototoro` (613 stars, same API). ArchiveTune keeps a `foss`
flavor because `com.google.android.gms` is absent on non-GMS devices — the
Google path dies entirely there, it does not degrade.

## Recommendation

1. **Option 1 now** — stops the dated bug in hours. Document the embedded
   secret as accepted debt.
2. **Option 3 as the target** — the app already has the write path; this
   removes the `.so`, `contract.md`, `toolchain.lock`, the bump CI gate, and
   both stdout parsers at once.
3. Keep rclone working until SAF is proven, so there is a rollback.

## Blockers to resolve before starting

- **Two diverged local clones.**
  - `~/Documents/GitHub/ElecPilot` @ `4e2a4dc` — matches remote `main`.
  - `~/My Android Projects/ElecPilot` @ `6d15bdb` — divergent, plus 6
    uncommitted changes (`SECURITY.md`, `gradle/libs.versions.toml`,
    `gradle/wrapper/gradle-wrapper.properties` modified; `.opencode/` and two
    `.bak-20260930T…` files untracked).
  - Decide which is the working copy before touching anything.
- **`~/My Android Projects/PaieDZ` has no git remote** and 3 uncommitted
  changes. Not ElecPilot, but the same gap.
- **Distribution channel unknown.** If any users sideload or use F-Droid,
  option 2 needs a fallback variant (ArchiveTune's `foss` pattern).

## Corrections made during this investigation

- Wrongly stated ElecPilot does not use Google Drive. The grep for
  `googleapis` / `google-services.json` was correct and returned nothing — but
  rclone *is* the Drive integration. No Google SDK does not mean no Drive.
- Wrongly denied that rclone can run on Android. `downloads.rclone.org`
  publishes no Android build, which is true; the app cross-compiles one to
  `.so`. Generalizing that to "impossible" was the error.

## Not verified

- Whether rclone accepts an `Android`-type client with no secret (decides if
  option 2 is feasible).
- The exact retirement date for rclone's shared client — upstream says only
  "during 2026".
- Where the SAF tree picker is launched in the app (not found in
  `BackupSettingsDialog.kt` / `BackupBrowserDialog.kt`; may live in a
  ViewModel).
- Background SAF upload constraints on the app's minSdk.

## First command

```
cd "/home/hicham/My Android Projects/ElecPilot" && git fetch origin && git status
```

Resolve the clone divergence before any code change.
