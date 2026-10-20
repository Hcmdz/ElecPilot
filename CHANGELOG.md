# Changelog

All notable changes to ElecPilot.

Versions follow [semver](https://semver.org/); full history lives in [GitHub Releases](https://github.com/Hcmdz/ElecPilot/releases).

## Unreleased

## v6.5.4

- **Cloud backup removal completed** — the leftover files, the OAuth entry point and the local-network exception in the network security config are gone
- **Documentation corrected** — the feature list, the security table, the permissions and the APK size now describe what the app actually does
- Signed with the same certificate as v6.5.3, so this installs as a normal in-place update

## v6.5.3

- **Cloud backup removed** — Google now requires each app to use its own Cloud project for OAuth, so the shared project rclone relied on is no longer accepted
- **APK size 20.3 MB → 13.3 MB** — the bundled rclone native binary is gone
- Unchanged: local auto-backup, CSV/Excel import-export, search, filters, statistics, voice search, in-app update, EN/FR/AR

## v6.5.2

- **System language applies without restart** — switching the app language back to *System* now follows the current system language instead of the previously selected one

## v6.5.1

- **Trimmed rclone 1.75.1** — local/Drive/OneDrive backends only
- **New signing key** — manual reinstall required (back up, uninstall, install, restore)

## v6.5

- **Cancellation-safe background work** — `CancellationException` rethrown in cloud/local backup, update check and import paths (no more false failure notification or retry after cancel)
- **Snapshot writes on Main** — backup dialogs assign UI state back on the main thread
- **ASCII backup filenames** — export/cloud timestamps use `Locale.US` (no more non-Latin digits under Arabic locale)
- **PLC card polish** — favorite star size aligned with motor cards (18.dp)
- **Build** — AGP 9.4.0, OkHttp 5.5.0, WorkManager 2.11.2

## v6.3

- **No blank screen flash on cold start** — the full screen is gated behind `hasLoadedOnce` so the empty shell (header with "0" counter) is never drawn before the first data load
- **Persistent last module** — the app reopens on the last active module (Départs / PLC I/O) after a cold restart

## v6.2

- **In-app update** — auto-check GitHub Releases, download & install with progress
- **Blank template generation** — ready-to-fill Excel templates (EN/FR headers)
- **System language option** — auto-detect device language on first install
- **Bug fixes** — CancellationException handling, filename sanitization, header validation thresholds, export queue guard
- **Security audit** — update & template feature hardened (SHA-256 recommendation, path traversal prevention)

## v6.0 → v6.1

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
