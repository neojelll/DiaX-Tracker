# DiaX - PWA

The DiaX diary as a Progressive Web App: React, TypeScript, Vite, Tailwind CSS. It replaces the Android app (frozen in `../android`) and is installed from the browser. Plan: epic #255.

Two builds live side by side on GitHub Pages:

|              | Address                                        | Built from                      | Changes                          |
| ------------ | ---------------------------------------------- | ------------------------------- | -------------------------------- |
| **DiaX**     | <https://neojelll.github.io/DiaX-Tracker/>     | the latest `web-v*` release tag | only when a release PR is merged |
| **DiaX Dev** | <https://neojelll.github.io/DiaX-Tracker/dev/> | `main`                          | on every merge                   |

DiaX Dev is the PWA's `.dev` app: install it as a second icon (inverted icon, "DEV" badge) and try changes there before releasing. The two keep separate data (`src/lib/channel.ts`) - the dev build never sees the real diary.

## Install on a phone

- **iPhone:** open the link in Safari → Share → "На экран «Домой»".
- **Android:** open the link in Chrome → menu → "Установить приложение".

Once installed it opens full screen and works with no network: the service worker keeps the whole app on the phone. The diary itself never leaves the phone. A new version is downloaded in the background and starts on the next launch.

## Develop

Node and pnpm are pinned in `../mise.toml`.

```bash
mise install
cd web
pnpm install
pnpm dev
```

| Command                                        | What it does                                                                                   |
| ---------------------------------------------- | ---------------------------------------------------------------------------------------------- |
| `pnpm dev`                                     | dev server with hot reload                                                                     |
| `pnpm build`                                   | release build into `dist/` (with the service worker)                                           |
| `pnpm build:dev`                               | the same as DiaX Dev (`/DiaX-Tracker/dev/`)                                                    |
| `pnpm preview`                                 | serves `dist/` at <http://localhost:4173/DiaX-Tracker/> - the way to check offline and install |
| `pnpm test`                                    | unit tests (Vitest)                                                                            |
| `pnpm lint` / `pnpm typecheck` / `pnpm format` | oxlint, TypeScript, Prettier                                                                   |

The service worker only runs in the production build, so check offline behaviour with `build` + `preview`, not `dev`.

## Layout

- `src/app` - the app root and tab navigation (the tab lives in the URL hash: `#/history`).
- `src/screens` - one folder/file per tab.
- `src/diary` - the diary itself: storage (IndexedDB via Dexie) and the calculations, no UI.
- `src/backup` - archives. `archive.ts` writes the PWA's own zip (`diary.json` with entries, presets and settings, plus `photos/`) - always the whole diary - and restores either it or the Android app's `diax-*.zip` (`androidBackup.ts`: Room database version 12 + `entry_photos/`, read with sql.js). fflate and sql.js load only when an archive is made or read. Settings → Данные saves an archive through the share sheet (a download where sharing files isn't supported) and restores one by replacing the diary; Home reminds when no archive was saved for 7 days.
- `src/components` - shared components; `components/ui` holds the design system.
- `src/i18n` - every text, Russian (`ru.ts`) and English (`en.ts`, typed against the Russian one so no key can be missing). Components read them with `t()`; switching the language in Settings remounts the app.
- `src/lib` - helpers. `photo.ts` makes a picked or taken photo small before it is stored (long side 1600 px, JPEG): photos live in their own IndexedDB table, keyed by entry, and go into every archive.
- `public/icons` - app icons, made from the Android launcher icon.

## Design system

It is the Android redesign, ported one to one:

- **Tokens** - the `@theme` block in `src/index.css`: colours (`bg-tile`, `text-ink`, `text-sugar-high`...), radii (`rounded-card`, `rounded-sheet`), the type scale (`text-value`, `text-sheet-title`... - size, line height and weight together) and the animations. The shadcn/ui colour names point at the same palette. Numbers get `tabular-nums`.
- **Components** - `src/components/ui`: card, tile and kicker; buttons (primary, secondary, round, chip); capsule; text field; stepper; bottom sheet; toast with undo. Icons are the Android app's own paths (`components/iconPaths.ts`).
- **Showcase** - every piece on one page at `#/kit`, in `pnpm dev` and DiaX Dev only (Settings → «Компоненты (dev)»). The release build doesn't contain it.

Numbers use the language's one decimal separator - a comma in Russian, a dot in English - in what's shown and what's typed (`lib/format.ts`, `lib/numeric.ts`).

On iPhone the keyboard slides over the page instead of shrinking it; `lib/keyboard.ts` measures it so sheets and toasts ride above it. Text fields are 16 px, or iOS zooms in on focus.

## Release

release-please keeps a separate version and changelog for `web`. Merging its release PR tags `web-vX.Y.Z`, and `.github/workflows/pages.yml` then rebuilds the release build from that tag. Merges to `main` only update DiaX Dev: the release build is rebuilt from the same tag each time, so installed copies see no change. Before the first release the root page links to DiaX Dev.
