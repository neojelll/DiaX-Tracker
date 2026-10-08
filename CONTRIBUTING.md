# Contributing

This is currently a solo, personal project, but issues and pull requests are welcome.

## Building from source

Requires Android Studio (or Gradle) and a connected device/emulator running Android 8.0
(API 26) or newer. The JDK is not something to set up by hand: the build declares a Gradle
toolchain (JDK 17), which Gradle provisions itself if none is already on the machine.

```bash
./gradlew installDev
```

**Stack**: Kotlin, Jetpack Compose, Material3, Room, WorkManager, Coil.

## The real app and the dev app

A phone with a real diary on it has two copies of the app side by side:

| | DiaX-Tracker (real) | DiaX dev |
|---|---|---|
| application id | `com.neojelll.diaxtracker` | `com.neojelll.diaxtracker.dev` |
| data | the real diary | a disposable copy |
| what runs on it | published releases only | whatever is being tried |
| installed from | [GitHub Releases](https://github.com/neojelll/DiaX-Tracker/releases), by hand | the computer, `./gradlew installDev` |

**Nothing built on a computer is ever installed over the real app.** It is updated only
with a release APK, installed by hand. A failed migration or a bug in import/backup on
the real app hits the only copy of the data; on the dev app it costs a reinstall.

The build enforces this: only the `release` build type carries the real application id
(`debug` gets `.debug`, `dev` gets `.dev`), and a local `release` build without the
release keystore is debug-signed, so Android refuses to install it over a real
installation.

The dev app is a release build under its own id ("DiaX dev", orange icon) with its own
database, settings, photos and permissions:

- It starts empty. To get realistic data in, export an archive from the real app and
  import it into the dev one - which also exercises import itself.
- Permissions (notifications, the automatic-backup folder) are granted to it separately.
- Sensor readings reach it only if the source sends them its way: in Juggluco pick
  "DiaX dev" as a Glucodata recipient; for the xDrip-style broadcast, add
  `com.neojelll.diaxtracker.dev` wherever the sender limits it to named packages. When
  both apps receive a reading, both record it.
- To start over: `adb shell pm clear com.neojelll.diaxtracker.dev`.

`debug` (`com.neojelll.diaxtracker.debug`, "DiaX debug") is for Android Studio and
emulators - previews, the debugger. On a phone, use `dev`.

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
