/**
 * The app's own Lucide-style icons, drawn from the same paths as the Android app (LucidePaths), so
 * both look the same. Stock icons come from lucide-react.
 */
export const ICON_PATHS = {
  home: 'M3.5 10.5 12 3.5l8.5 7M5.5 9.2V19a1.5 1.5 0 0 0 1.5 1.5h10a1.5 1.5 0 0 0 1.5-1.5V9.2',
  dish: 'M4 11.5h16a8 8 0 0 1-16 0zM2.5 20.5h19M7.5 3.5c-1.6 1.3-1.6 2.7 0 4s1.6 2.7 0 4M12 3.5c-1.6 1.3-1.6 2.7 0 4s1.6 2.7 0 4M16.5 3.5c-1.6 1.3-1.6 2.7 0 4s1.6 2.7 0 4',
  history: 'M3.5 12a8.5 8.5 0 1 0 2.6-6.1M3 4v4h4M12 8v4.2l3 1.8',
  settings:
    'M4 6h10M18 6h2M4 12h2M10 12h10M4 18h10M18 18h2M16 8a2 2 0 1 0 0-4 2 2 0 0 0 0 4zM8 14a2 2 0 1 0 0-4 2 2 0 0 0 0 4zM16 20a2 2 0 1 0 0-4 2 2 0 0 0 0 4z',
} as const

export type IconName = keyof typeof ICON_PATHS
