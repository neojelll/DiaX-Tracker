# DiaX-Tracker

English | [Русский](README.ru.md)

[![CI](https://img.shields.io/github/actions/workflow/status/neojelll/DiaX-Tracker/ci.yml?branch=main&style=flat-square)](https://github.com/neojelll/DiaX-Tracker/actions/workflows/ci.yml)

A personal diary for people with diabetes: blood sugar, insulin, meals, and notes — all in
one place, with no cloud and no accounts. All data stays on the device.

Blood sugar doesn't have to be entered by hand — the app receives readings directly from
**xDrip+** and **Juggluco** and fills them into the entry automatically. The home screen
always shows how much insulin is still active in the body — calculated using the same
pharmacokinetic model used by Loop/AndroidAPS/OpenAPS.

| Entry | Meals | History |
| --- | --- | --- |
| ![Entry screen](docs/screenshots/entry.jpg) | ![Meal presets](docs/screenshots/meal-presets.jpg) | ![Entry history](docs/screenshots/history.jpg) |

## Features

- Automatic blood sugar fill-in from **xDrip+** and **Juggluco** — no need to retype
  sensor readings by hand
- Active insulin on the home screen: calculated with a pharmacokinetic model (exponential
  decay, as in Loop/AndroidAPS/OpenAPS), with configurable action duration
- Quick entry: blood sugar, carb units, short/long insulin, meal, photo, and a comment
- Meal presets with ingredients and automatic carb-unit calculation
- Entry history with search, date filters, and highlighting for out-of-range values
- Import/export data as a ZIP archive, auto-backup
- Russian and English interface

## Important

DiaX-Tracker is a personal diary for your own records, not a medical device. The app does
not give treatment recommendations or calculate insulin doses: every figure on screen
(including the active-insulin estimate) is purely informational and does not replace a
doctor's advice. The user makes all treatment decisions independently and bears full
responsibility for them. The full text is in the disclaimer shown on first launch.

## Build and run

Requires Android Studio (or Gradle + JDK 17) and a connected device/emulator running
Android 8.0 (API 26) or newer.

```bash
./gradlew installDebug
```

## Stack

Kotlin, Jetpack Compose, Material3, Room, WorkManager, Coil.

## License

[MIT](LICENSE)
