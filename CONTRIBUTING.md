# Contributing to NEXIQ

Thank you for your interest in contributing to NEXIQ! We welcome bug reports, feature enhancements, documentation improvements, and localization additions.

---

## Code of Conduct

Please maintain a respectful, constructive, and collaborative environment in all issues, pull requests, and project communications.

---

## Development Workflow

### 1. Prerequisites
- **Android Studio Ladybug (2024.2.1+)** or higher.
- **JDK 21** configured as your Gradle JDK.
- **Android SDK Platform 36** and Build-Tools 36.0.0.

### 2. Fork & Clone
1. Fork the repository on GitHub: [https://github.com/sang2k10/NEXIQ](https://github.com/sang2k10/NEXIQ).
2. Clone your fork locally:
   ```bash
   git clone https://github.com/<your-username>/NEXIQ.git
   cd NEXIQ
   ```
3. Create a feature branch:
   ```bash
   git checkout -b feature/my-new-feature
   ```

### 3. Code Standards & Architecture
- **Language**: Kotlin 2.0.21 with standard Kotlin style guidelines.
- **Architecture**: Clean Architecture with strict separation of layers:
  - `presentation`: Jetpack Compose and Material 3 UI.
  - `domain`: Pure Kotlin business logic, models, and interfaces (no Android framework dependencies where possible).
  - `data`: Concrete engine implementations, repositories, and hardware services.
- **Privacy First**: Any feature touching screen captures or text must guarantee memory ephemerality and user transparency. No tracking or telemetry SDKs will be accepted.

### 4. Verification Before Submitting
Before opening a pull request, verify that all automated checks pass cleanly:

```bash
# Run unit tests
./gradlew testDebugUnitTest

# Run Android Lint
./gradlew lintDebug

# Assemble Debug APK
./gradlew assembleDebug
```

Every pull request must maintain **zero lint errors** and 100% unit test success.

### 5. Pull Request Guidelines
- Provide a clear, descriptive title and description explaining *what* changed and *why*.
- Reference any related issues (e.g. `Fixes #12`).
- Never include credentials, signing keystores, or machine-specific paths in your commits.
