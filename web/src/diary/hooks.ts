import { useLiveQuery } from 'dexie-react-hooks'
import { diary } from '@/diary/repository'
import { DEFAULT_SETTINGS, type Settings } from '@/diary/settings'
import type { SavedEntry, SavedPreset } from '@/diary/types'

/**
 * The diary as screens see it: live - any change, from this screen or another, re-renders. Until
 * the first read lands they give empty lists and the default settings.
 */
export function useEntries(): SavedEntry[] {
  return useLiveQuery(() => diary.allEntries(), [], [])
}

export function usePresets(): SavedPreset[] {
  return useLiveQuery(() => diary.allPresets(), [], [])
}

export function useSettings(): Settings {
  return useLiveQuery(() => diary.getSettings(), [], DEFAULT_SETTINGS)
}

/** The settings once read - undefined until then, for a screen that edits them and must start from the real values. */
export function useLoadedSettings(): Settings | undefined {
  return useLiveQuery(() => diary.getSettings(), [])
}
