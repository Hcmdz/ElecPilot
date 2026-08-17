# Contributing

Thank you for your interest in contributing to ElecPilot!

## Getting Started

1. Fork the repository
2. Clone your fork
3. Create a feature branch: `git checkout -b feature/your-feature`
4. Make your changes
5. Run tests: `./gradlew test`
6. Run lint: `./gradlew lint`
7. Commit your changes
8. Push to your fork
9. Submit a pull request

## Development Setup

### Prerequisites

- Android Studio latest stable
- JDK 17+
- Android SDK (compileSdk 37+)

### Building

```bash
# Debug build
./gradlew assembleDebug

# Release build (requires signing config)
./gradlew assembleRelease

# Run tests
./gradlew test

# Run lint
./gradlew lint
```

## Code Style

- Follow Kotlin coding conventions
- Compose @Composable functions: PascalCase
- Follow existing patterns in the codebase
- Keep functions focused and small

## Commit Messages

Use [Conventional Commits](https://www.conventionalcommits.org/):

- `feat:` new feature
- `fix:` bug fix
- `docs:` documentation changes
- `refactor:` code refactoring
- `test:` adding tests
- `chore:` maintenance tasks

## Pull Request Process

1. Update documentation if needed
2. Add tests for new functionality
3. Ensure all tests pass
4. Ensure lint passes with no errors
5. Request review from maintainers

## License

By contributing, you agree that your contributions will be licensed under
the GNU General Public License v3.0.
