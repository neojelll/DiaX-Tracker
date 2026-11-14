# DiaX - PWA

The DiaX diary as a Progressive Web App: React, TypeScript, Vite, Tailwind CSS. It replaces the Android app (frozen in `../android`) and is installed from the browser. Plan: epic #255.

Live: <https://neojelll.github.io/DiaX-Tracker/>

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
| `pnpm build`                                   | production build into `dist/` (with the service worker)                                        |
| `pnpm preview`                                 | serves `dist/` at <http://localhost:4173/DiaX-Tracker/> - the way to check offline and install |
| `pnpm test`                                    | unit tests (Vitest)                                                                            |
| `pnpm lint` / `pnpm typecheck` / `pnpm format` | oxlint, TypeScript, Prettier                                                                   |

The service worker only runs in the production build, so check offline behaviour with `build` + `preview`, not `dev`.

## Layout

- `src/app` - the app root and tab navigation (the tab lives in the URL hash: `#/history`).
- `src/screens` - one folder/file per tab.
- `src/components` - shared components; `components/ui` is reserved for shadcn/ui components.
- `src/lib` - helpers.
- `public/icons` - app icons, made from the Android launcher icon.

Colours and type come from the Android redesign: see the `@theme` block in `src/index.css`. The shadcn/ui colour names point at the same palette.

## Release

Every merge to `main` that touches `web/` is deployed to GitHub Pages (`.github/workflows/pages.yml`). release-please keeps a separate version and changelog for `web` (tags `web-vX.Y.Z`).
