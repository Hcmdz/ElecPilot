# Security Policy

## Supported Versions

| Version | Supported |
|---|---|
| 6.2 | Yes |
| < 6.0 | No |

## Reporting a Vulnerability

If you discover a security vulnerability in ElecPilot, please report it responsibly:

1. **Do NOT open a public GitHub issue** for security vulnerabilities
2. **Email:** [REDACTED] (replace with your contact)
3. **Include:**
   - Description of the vulnerability
   - Steps to reproduce
   - Potential impact
   - Suggested fix (if any)

## Response Timeline

- **Acknowledgment:** within 48 hours
- **Initial assessment:** within 1 week
- **Fix or mitigation:** within 30 days for critical issues

## Security Measures

- All network traffic encrypted (TLS enforced via Network Security Config)
- Cloud backup tokens encrypted at rest (AES-256-GCM, Android KeyStore)
- Rclone config files encrypted before disk write
- `FLAG_SECURE` enabled to prevent screenshots of sensitive data
- R8/ProGuard strips debug logs in release builds
- `allowBackup` disabled to prevent ADB data extraction
