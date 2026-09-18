# rclone CLI contract — ElecPilot cloud backup

Single source of truth for every rclone CLI behavior the app depends on.
Any rclone bump PR must keep every row below green (smoke test + device
OAuth gate). A row that changes upstream is a breaking change: update this
file and the app code in the same PR.

## Commands (trimmed binary must provide exactly these)

| Command | Used as | Code ref |
|---|---|---|
| `authorize` | `authorize <drive\|onedrive> --auth-no-open-browser --config <tmp>` | `RcloneAuthActivity.kt:156` |
| `listremotes` | `listremotes` → lines `<name>:` | `RcloneDriveService.kt:132` |
| `lsf` | `lsf <remote>:/<folder>/ --format tsp --separator \t` → `time \t size \t path` | `RcloneDriveService.kt:140` |
| `copyto` | `copyto <src> <dst>` (+ `--stats=1s --use-json-log --stats-log-level NOTICE` when reporting progress) | `RcloneDriveService.kt:186,232,254,337` |
| `deletefile` | `deletefile <remote>:/<id>`, fallback `delete <remote>:/<id>/` | `RcloneDriveService.kt:216` |
| `mkdir` | `mkdir <remote>:/<folder>/` | `RcloneDriveService.kt:271` |
| `config` | kept for operator debugging (`openRcloneConfig` links docs) | `RcloneDriveService.kt:327` |
| `version` | flag only: `rclone --version` (no `version` subcommand upstream) | CI |

Backends: `local`, `drive`, `onedrive` — plus `crypt`, which upstream pulls in
transitively via `fs/operations` and cannot be excluded without forking
rclone. `crypt` is an overlay the app never references; the smoke test
asserts no other backend is present.
Verify with: `librclone --config /dev/null help backends`.

## Fragile couplings (watch on every bump)

1. **Auth URL**: stderr line `http://127.0.0.1:<port>/auth…` parsed by regex
   (`RcloneAuthActivity.kt:255`). If upstream changes the log format, OAuth breaks.
2. **Token capture**: stdout block between `--->` and `<---`, must start with
   `{` (`RcloneAuthActivity.kt:221`). Same risk.
3. **WebView allowlist**: `127.0.0.1, localhost, accounts.google.com,
   oauth2.googleapis.com, login.microsoftonline.com, login.live.com`
   (`RcloneAuthActivity.kt:121`). New OAuth hosts upstream → update list.
4. **Config keys written by the app**: `[name] / type / scope=drive /
   token = {...} / drive_id / drive_type` (`RcloneDriveService.kt:280`).
   Old encrypted configs must still load in the new binary (forward-compat
   test on every bump); rollback must not corrupt them.
5. **Timestamps**: `parseTime` accepts `yyyy-MM-dd HH:mm:ss` (lsf `tsp`) + 2
   ISO-UTC variants. Golden samples live in the unit tests.
6. **Progress JSON**: `stats{bytes,totalBytes,speed,eta}` lines starting with
   `{` on stderr. Golden samples live in the unit tests.

## Size baseline

Release ships `arm64-v8a` only (`abiFilters`). Reference (v1.75.1):

- `app/src/main/jniLibs/arm64-v8a/librclone.so`: 7.1 MB (UPX; was 6.5 MB at
  v1.70.3 — genuine upstream growth, +10%)
- `app/src/main/jniLibs/x86_64/librclone.so`: 26 MB (debug only, NOT UPXed —
  intentional, keep the asymmetry; release never ships it)
- APK release: ~20 MB

Gate: fail the bump PR if arm64 `.so` > baseline +10% or release APK >
baseline +2 MB. Recorded in `size-baseline.txt`.
