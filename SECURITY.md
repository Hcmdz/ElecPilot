# Security Policy

## Reporting a Vulnerability

If you discover a security vulnerability in ElecPilot, please report it responsibly.

**Contact:** [YOUR_SECURITY_EMAIL]

Please include:
- Description of the vulnerability
- Steps to reproduce
- Potential impact assessment

## Response Timeline

- **Acknowledgment:** within 48 hours
- **Initial assessment:** within 1 week
- **Fix or mitigation:** based on severity

## Scope

This security policy applies to the latest release of ElecPilot.

## Disclosure

We follow responsible disclosure. Please do not publicly disclose
vulnerabilities until a fix is available.

## Security Best Practices

- No hardcoded secrets in source code
- Signing credentials stored outside repository (gradle.properties, gitignored)
- Network security configuration enforced (cleartext blocked)
- R8/ProGuard obfuscation enabled for release builds
- `allowBackup="false"` in manifest
