# Contributing

This is currently a solo, personal project, but issues and pull requests are welcome.

## Building from source

Requires Android Studio (or Gradle + JDK 17) and a connected device/emulator running
Android 8.0 (API 26) or newer.

```bash
./gradlew installDebug
```

**Stack**: Kotlin, Jetpack Compose, Material3, Room, WorkManager, Coil.

## Making a change

- Branches: `[issue-number/]type/slug` (e.g. `104/feat/after-meal-sugar`).
- Commits and PR titles follow [Conventional Commits](https://www.conventionalcommits.org/)
  (`feat: …`, `fix: …`, …) - a squash-merged PR's title becomes the commit on `main`, and
  the changelog is generated from it.
- `./gradlew testDebugUnitTest lintDebug` should pass before opening a PR; CI runs the same
  checks.
- See the [open issues](https://github.com/neojelll/DiaX-Tracker/issues) for what's planned
  or being considered - most start as a loosely defined idea and get refined before anyone
  picks them up.
