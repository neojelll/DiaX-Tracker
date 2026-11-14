# DiaX-Tracker

English | [Русский](README.md)

[![CI](https://img.shields.io/github/actions/workflow/status/neojelll/DiaX-Tracker/ci.yml?branch=main&style=flat-square)](https://github.com/neojelll/DiaX-Tracker/actions/workflows/ci.yml)
[![Latest release](https://img.shields.io/github/v/release/neojelll/DiaX-Tracker?include_prereleases&style=flat-square)](https://github.com/neojelll/DiaX-Tracker/releases/latest)

DiaX-Tracker is a diary for people with diabetes, built mainly to spare you manual data
entry and save your time. Blood sugar, insulin, meals, and notes — all in one place.

**Status: moving to a web app.** DiaX is becoming a PWA — an app that opens from the browser,
is added to the home screen and works offline, on iPhone and Android. The Android app is
frozen at [v0.4.0-beta.1](https://github.com/neojelll/DiaX-Tracker/releases/tag/v0.4.0-beta.1):
it keeps working, but gets no new features. The plan is
[epic #255](https://github.com/neojelll/DiaX-Tracker/issues/255).

## Contents

- [Web app (PWA)](#web-app-pwa)
- [Android app (frozen)](#android-app-frozen)
- [Before you start](#before-you-start)
- [License](#license)

## Web app (PWA)

| | Address | What it is |
| --- | --- | --- |
| **DiaX** | <https://neojelll.github.io/DiaX-Tracker/> | the main version, changes only with a release |
| **DiaX Dev** | <https://neojelll.github.io/DiaX-Tracker/dev/> | the latest build for trying new features, with its own separate data |

**How to install:**

- **iPhone:** open the address in Safari → Share → "Add to Home Screen".
- **Android:** open the address in Chrome → menu → "Install app".

Once installed it opens full screen and works offline. Entries stay on the phone only — no
server, no account, no analytics. A new version downloads in the background and starts on the
next launch.

The web version is still in development: the first release comes once it has the whole diary
and the import from the Android app. Until then the main address links to DiaX Dev.
Developer notes are in [web/README.md](web/README.md).

## Android app (frozen)

Everything below is about the Android app,
[v0.4.0-beta.1](https://github.com/neojelll/DiaX-Tracker/releases/tag/v0.4.0-beta.1) — a beta;
an archive from a newer version may not import into an older one (see [Limitations](#limitations)).
Its code is in [`android/`](android/).

| Entry | History | Meal presets |
| --- | --- | --- |
| ![Entry screen](docs/screenshots/entry.jpg) | ![Entry history](docs/screenshots/history.jpg) | ![Meal presets](docs/screenshots/meal-presets.jpg) |

### Features

#### Logging

- Quick entry: blood sugar, carb units (XE), short/long insulin, meal, a photo (camera or
  gallery), and a comment
- Meal presets with ingredients and automatic carb-unit calculation
- Automatic blood sugar fill-in from **xDrip+** and **Juggluco** — no need to retype sensor
  readings by hand

#### On the home screen

- Active insulin: calculated with a pharmacokinetic model (exponential decay, as in
  Loop/AndroidAPS/OpenAPS), with configurable action duration

#### History

- Search, date filters, and highlighting for out-of-range values
- Tap a card to edit it, tap a meal to see its composition
- "Sugar after a meal": automatic checks at +1h/+2h/+3h/+4h, shown as a pill on the meal's
  own card
- A reminder of what was eaten at this same time yesterday

#### Data

- Export to a ZIP archive (a week, a month, or everything) and import that merges instead of
  replacing, skipping duplicates
- Automatic nightly backup to a folder you choose, keeping the newest 7 copies
- Undo for deleting an entry, a meal, or everything

#### Interface

- Russian and English

### How to download

Download the latest APK from the [Releases page](https://github.com/neojelll/DiaX-Tracker/releases/latest)
and install it (you'll need to allow installs from that source once). Requires Android 8.0
(API 26) or newer.

The app isn't on Google Play yet.

### Connecting a sensor source

DiaX-Tracker doesn't talk to a sensor directly — it receives readings that another app
already broadcasts on the device:

- **xDrip+**: in xDrip+'s settings, under *Inter-app settings*, enable
  *"BroadcastReceiver mode"* (or the mmol/L equivalent your build uses to send local
  broadcasts) so it sends the standard `com.eveningoutpost.dexdrip.BgEstimate` broadcast.
- **Juggluco / JugglucoNG**: in Juggluco's settings, add DiaX-Tracker as a *Glucodata*
  recipient (or enable its xDrip-compatible broadcast mode).

Once enabled, readings appear in DiaX-Tracker automatically — nothing needs to be
configured on the DiaX-Tracker side. If a screen ever shows a stale-sensor warning, re-check
the broadcast setting in the source app first.

### Your data, backup and privacy

- Everything - entries, photos, presets, sensor readings - is stored locally in the app's
  own storage. Nothing is sent anywhere; there is no server, account, or analytics.
- **Export**: Settings → export a ZIP archive for a week, a month, or all your data.
- **Import**: adds the entries from an archive to what's already there; entries that match
  exactly are skipped as duplicates, and nothing existing is changed or deleted.
- **Automatic backup**: turn it on in Settings and pick a folder (e.g. one synced by your
  own cloud provider); every evening the app writes a new archive there and keeps the
  newest 7, deleting only its own older ones.
- **Permissions**: a foreground service to receive sensor broadcasts and run the automatic
  backup and reminder checks in the background. The camera and photo picker are opened as
  the system's own apps, so no separate storage/camera permission is requested.
- Uninstalling the app deletes its data. Export or turn on automatic backup first if you
  want to keep it.

### Limitations

- **Beta software**: expect rough edges and occasional bugs.
- **Schema changes**: a database change between versions is not undone by downgrading —
  going back to an older build after updating isn't supported. An archive exported by a
  newer version may not import into an older one; the other direction (old archive into a
  new version) is supported.
- **Background reliability**: automatic backup and the post-meal sugar checks depend on
  Android's background scheduling (WorkManager); aggressive battery optimisation on some
  phones (Xiaomi, Oppo/Realme, and similar) can delay or skip a run. Exclude the app from
  battery optimisation if you rely on these.
- Not on Google Play yet - see [How to download](#how-to-download).

## Before you start

DiaX-Tracker is a personal diary for your own records, not a medical device. The app does
not give treatment recommendations or calculate insulin doses: every figure on screen
(including the active-insulin estimate and the automatic sugar checks) is purely
informational and does not replace a doctor's advice. The user makes all treatment
decisions independently and bears full responsibility for them. The full text is in the
disclaimer shown on first launch.

Want to build it yourself or contribute? See [CONTRIBUTING.md](CONTRIBUTING.md).

## License

[MIT](LICENSE)
