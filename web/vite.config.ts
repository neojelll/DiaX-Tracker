/// <reference types="vitest/config" />
import { fileURLToPath, URL } from 'node:url'
import tailwindcss from '@tailwindcss/vite'
import react from '@vitejs/plugin-react'
import { defineConfig } from 'vite'
import { VitePWA } from 'vite-plugin-pwa'

// Screen colour of the app (GlukoColors.Screen on Android) - the splash and status bar match it.
const SCREEN = '#F7F6F5'

export default defineConfig({
  // GitHub Pages serves the app at neojelll.github.io/DiaX-Tracker/. The manifest scope and the
  // service worker follow this base, so the app must never be opened from another path.
  base: '/DiaX-Tracker/',
  resolve: {
    alias: { '@': fileURLToPath(new URL('./src', import.meta.url)) },
  },
  plugins: [
    react(),
    tailwindcss(),
    VitePWA({
      // A new version waits until the app is closed and opened again - never swapped in the middle
      // of an entry being typed.
      registerType: 'prompt',
      injectRegister: 'auto',
      includeAssets: ['icons/apple-touch-icon.png', 'icons/favicon-64.png'],
      manifest: {
        name: 'DiaX',
        short_name: 'DiaX',
        description: 'Дневник диабета: сахар, инсулин, еда.',
        lang: 'ru',
        start_url: '.',
        scope: '.',
        display: 'standalone',
        orientation: 'portrait',
        background_color: SCREEN,
        theme_color: SCREEN,
        icons: [
          { src: 'icons/icon-192.png', sizes: '192x192', type: 'image/png' },
          { src: 'icons/icon-512.png', sizes: '512x512', type: 'image/png' },
          { src: 'icons/maskable-512.png', sizes: '512x512', type: 'image/png', purpose: 'maskable' },
        ],
      },
      workbox: {
        // Everything the app needs is precached: it opens and works with no network at all.
        globPatterns: ['**/*.{html,js,css,ttf,png,svg}'],
      },
    }),
  ],
  test: {
    environment: 'jsdom',
    setupFiles: ['./src/test/setup.ts'],
  },
})
