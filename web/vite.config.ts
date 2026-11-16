/// <reference types="vitest/config" />
import { fileURLToPath, URL } from 'node:url'
import tailwindcss from '@tailwindcss/vite'
import react from '@vitejs/plugin-react'
import { defineConfig, type Plugin } from 'vite'
import { VitePWA } from 'vite-plugin-pwa'

// Screen colour of the app (GlukoColors.Screen on Android) - the splash and status bar match it.
const SCREEN = '#F7F6F5'

// GitHub Pages serves the release build at neojelll.github.io/DiaX-Tracker/ and the dev build
// (VITE_CHANNEL=dev, follows main) at .../DiaX-Tracker/dev/ - see src/lib/channel.ts.
const RELEASE_BASE = '/DiaX-Tracker/'
const dev = process.env.VITE_CHANNEL === 'dev'
const base = dev ? `${RELEASE_BASE}dev/` : RELEASE_BASE
const appName = dev ? 'DiaX Dev' : 'DiaX'
const icons = dev ? 'icons/dev-' : 'icons/'

/** The app name and icons in index.html, per channel. */
function channelHtml(): Plugin {
  return {
    name: 'diax-channel-html',
    // Before Vite's own %ENV% replacement, which would warn about these unknown names.
    transformIndexHtml: {
      order: 'pre',
      handler: (html) => html.replaceAll('%APP_NAME%', appName).replaceAll('%ICONS%', icons),
    },
  }
}

export default defineConfig({
  // The manifest scope and the service worker follow the base, so each build stays in its own path.
  base,
  resolve: {
    alias: { '@': fileURLToPath(new URL('./src', import.meta.url)) },
  },
  plugins: [
    react(),
    tailwindcss(),
    channelHtml(),
    VitePWA({
      // A new version waits until the app is closed and opened again - never swapped in the middle
      // of an entry being typed.
      registerType: 'prompt',
      injectRegister: 'auto',
      includeAssets: [`${icons}apple-touch-icon.png`, `${icons}favicon-64.png`],
      manifest: {
        id: base,
        name: appName,
        short_name: appName,
        description: 'Дневник диабета: сахар, инсулин, еда.',
        lang: 'ru',
        start_url: '.',
        scope: '.',
        display: 'standalone',
        orientation: 'portrait',
        background_color: SCREEN,
        theme_color: SCREEN,
        icons: [
          { src: `${icons}icon-192.png`, sizes: '192x192', type: 'image/png' },
          { src: `${icons}icon-512.png`, sizes: '512x512', type: 'image/png' },
          { src: `${icons}maskable-512.png`, sizes: '512x512', type: 'image/png', purpose: 'maskable' },
        ],
      },
      workbox: {
        // Everything the app needs is precached: it opens and works with no network at all. Each
        // build carries only its own icons. The wasm is sql.js, for the Android backup import.
        globPatterns: ['**/*.{html,js,css,ttf,svg,wasm}', `${icons}*.png`],
        globIgnores: dev ? [] : ['icons/dev-*'],
        // The release build's scope contains /dev/: its service worker must leave the dev build alone.
        navigateFallbackDenylist: dev ? [] : [new RegExp(`^${RELEASE_BASE}dev/`)],
      },
    }),
  ],
  test: {
    environment: 'jsdom',
    setupFiles: ['./src/test/setup.ts'],
  },
})
