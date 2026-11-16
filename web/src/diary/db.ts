import { Dexie, type EntityTable } from 'dexie'
import type { DiaryEntry, MealPreset } from '@/diary/types'
import { DATABASE_NAME } from '@/lib/channel'

/** A key-value row; settings are one row ("settings") holding the whole object. */
export interface StoredValue {
  key: string
  value: unknown
}

/** A meal's photo, stored apart from the entries so reading the diary doesn't load every picture. */
export interface EntryPhoto {
  entryId: number
  blob: Blob
}

/**
 * The diary on this device, in IndexedDB. Release and dev builds have their own database
 * (lib/channel.ts). Every schema change is a new `version(n)` below with an upgrade if data has to
 * move - never an edit of an old version, or installed copies stop opening their database.
 */
export class DiaryDatabase extends Dexie {
  entries!: EntityTable<DiaryEntry, 'id'>
  presets!: EntityTable<MealPreset, 'id'>
  values!: EntityTable<StoredValue, 'key'>
  photos!: EntityTable<EntryPhoto, 'entryId'>

  constructor(name: string = DATABASE_NAME) {
    super(name)
    // Indexed fields only: createdAt for days and ranges, sourceEntryId for a meal's checks.
    this.version(1).stores({
      entries: '++id, createdAt, sourceEntryId',
      presets: '++id, name',
      values: '&key',
    })
    this.version(2).stores({ photos: '&entryId' })
  }
}

export const db = new DiaryDatabase()
