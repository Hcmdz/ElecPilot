# Cloud Backup Removal — Rationale

## Reference
https://forum.rclone.org/t/google-drive-and-google-photos-users-action-required/54005

## Summary
Google Drive (and Google Photos) now **require every application to use its own Google Cloud project** for OAuth authentication. The legacy shared rclone project is no longer accepted.

## Impact on ElecPilot
- Users would need to create their own Google Cloud project, enable the Drive API, configure OAuth consent screen, and generate client ID/secret
- This is a significant friction barrier for non-technical users
- The same requirement applies to OneDrive (Microsoft Entra ID app registration)

## Decision
Removed the entire cloud backup feature (rclone-based Google Drive / OneDrive sync) rather than ship a broken or user-hostile experience.

## What Remains
- **Local Auto-Backup** — fully functional, no external dependencies
- **Import/Export** — CSV/Excel, local file picker
- All core app features (PLC, motors, search, filter, statistics)

## Files Removed
See git history for the complete list (13 files deleted, 8 modified).

## Future
If cloud sync returns, it will use a first-party backend (not rclone) with a dedicated OAuth project owned by the developer.