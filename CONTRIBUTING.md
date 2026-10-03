# Contributing

This is currently a solo, personal project, but issues and pull requests are welcome.

## Building from source

Requires Android Studio (or Gradle) and a connected device/emulator running Android 8.0
(API 26) or newer. The JDK is not something to set up by hand: the build declares a Gradle
toolchain (JDK 17), which Gradle provisions itself if none is already on the machine.

```bash
./gradlew installDebug
```

**Stack**: Kotlin, Jetpack Compose, Material3, Room, WorkManager, Coil.

## Trying a change on a phone with real data

Don't install a work-in-progress build over the app that holds a real diary - a failed
migration or a bug in import/backup would hit the only copy of the data. Use the `dev`
build instead: a release build under its own application id
(`com.neojelll.diaxtracker.dev`, shown as "DiaX dev" with an orange icon) that installs
**next to** the main app with its own database, settings, photos and permissions.

```bash
./gradlew installDev
```

- It starts empty. To get realistic data in, export an archive from the main app and
  import it into the dev one - which also exercises import itself.
- Permissions (notifications, the automatic-backup folder) are granted to it separately.
- Sensor readings reach it only if the source sends them its way: in Juggluco pick
  "DiaX dev" as a Glucodata recipient; for the xDrip-style broadcast, add
  `com.neojelll.diaxtracker.dev` wherever the sender limits it to named packages. When
  both apps receive a reading, both record it.

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
