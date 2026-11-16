import type { Settings } from '@/diary/settings'
import type { MealPreset, SavedEntry } from '@/diary/types'

/** Why a file can't be restored. */
export type BackupProblem = 'not-backup' | 'old-version' | 'newer-version'

export class BackupError extends Error {
  readonly problem: BackupProblem

  constructor(problem: BackupProblem) {
    super(problem)
    this.name = 'BackupError'
    this.problem = problem
  }
}

/**
 * A whole diary as an archive holds it, in the PWA's shapes. Entries keep their ids, so a meal's
 * checks still point at it.
 */
export interface DiaryArchive {
  /** Which app made it: the Android app's archive has no settings. */
  source: 'web' | 'android'
  entries: SavedEntry[]
  presets: MealPreset[]
  /** Photos by the id of their entry. */
  photos: Map<number, Blob>
  settings: Settings | null
}
