import { db as defaultDb, type DiaryDatabase } from '@/diary/db'
import { normalizeSettings, type Settings } from '@/diary/settings'
import { plusDays, type LocalDate } from '@/diary/time'
import type { DiaryEntry, MealPreset, SavedEntry, SavedPreset } from '@/diary/types'

const SETTINGS_KEY = 'settings'

/**
 * Everything the screens read and write, over one database. Reads return plain arrays; a screen
 * that should follow changes wraps a read in dexie-react-hooks' useLiveQuery.
 */
export function diaryRepository(db: DiaryDatabase = defaultDb) {
  return {
    // ---- Entries ----

    /** Saves a new entry and returns its id. */
    addEntry: async (entry: DiaryEntry): Promise<number> => (await db.entries.add(stripId(entry)))!,

    /** Replaces a saved entry. */
    updateEntry: async (entry: SavedEntry): Promise<void> => {
      await db.entries.put(entry)
    },

    /**
     * Deletes an entry and returns it, for undo. A meal's checks stay: an orphaned check shows as
     * an ordinary reading (see afterMeal.ts).
     */
    deleteEntry: async (id: number): Promise<SavedEntry | undefined> => {
      const entry = (await db.entries.get(id)) as SavedEntry | undefined
      await db.entries.delete(id)
      return entry
    },

    /** Puts deleted entries back under their old ids (undo). */
    restoreEntries: async (entries: SavedEntry[]): Promise<void> => {
      await db.entries.bulkPut(entries)
    },

    getEntry: (id: number) => db.entries.get(id) as Promise<SavedEntry | undefined>,

    /** Every entry, newest first. */
    allEntries: async (): Promise<SavedEntry[]> =>
      (await db.entries.orderBy('createdAt').reverse().toArray()) as SavedEntry[],

    /** Entries from [from] up to and including [to], newest first. */
    entriesBetween: async (from: LocalDate, to: LocalDate): Promise<SavedEntry[]> =>
      (await db.entries
        .where('createdAt')
        .between(from, plusDays(to, 1), true, false)
        .reverse()
        .toArray()) as SavedEntry[],

    /** The automatic checks of a meal. */
    checksOf: async (mealId: number): Promise<SavedEntry[]> =>
      (await db.entries.where('sourceEntryId').equals(mealId).toArray()) as SavedEntry[],

    // ---- Presets ----

    /** Every preset, by name. */
    allPresets: async (): Promise<SavedPreset[]> =>
      ((await db.presets.toArray()) as SavedPreset[]).sort((a, b) => a.name.localeCompare(b.name, 'ru')),

    /** Saves a new preset or replaces one with an id; returns its id. */
    savePreset: async (preset: MealPreset): Promise<number> =>
      (await (preset.id == null ? db.presets.add(stripId(preset)) : db.presets.put(preset)))!,

    /** Deletes a preset and returns it, for undo. Entries made from it keep their own copy. */
    deletePreset: async (id: number): Promise<SavedPreset | undefined> => {
      const preset = (await db.presets.get(id)) as SavedPreset | undefined
      await db.presets.delete(id)
      return preset
    },

    restorePreset: async (preset: SavedPreset): Promise<void> => {
      await db.presets.put(preset)
    },

    // ---- The whole diary ----

    /** How many entries there are - is the diary empty? */
    countEntries: () => db.entries.count(),

    /**
     * Puts [contents] in place of every entry, preset and photo - in one transaction, so a failure
     * leaves the old diary as it was. Settings stay.
     */
    replaceDiary: async (contents: {
      entries: SavedEntry[]
      presets: MealPreset[]
      photos: Map<number, Blob>
    }): Promise<void> =>
      db.transaction('rw', db.entries, db.presets, db.photos, async () => {
        await Promise.all([db.entries.clear(), db.presets.clear(), db.photos.clear()])
        await db.entries.bulkAdd(contents.entries)
        await db.presets.bulkAdd(contents.presets.map(stripId))
        await db.photos.bulkAdd([...contents.photos].map(([entryId, blob]) => ({ entryId, blob })))
      }),

    /** The photo of an entry, if it has one. */
    photoOf: async (entryId: number): Promise<Blob | undefined> => (await db.photos.get(entryId))?.blob,

    // ---- Settings ----

    getSettings: async (): Promise<Settings> => normalizeSettings((await db.values.get(SETTINGS_KEY))?.value),

    /** Changes some settings and keeps the rest; returns the result as it will be read back. */
    updateSettings: async (change: Partial<Settings>): Promise<Settings> =>
      db.transaction('rw', db.values, async () => {
        const next = normalizeSettings({
          ...normalizeSettings((await db.values.get(SETTINGS_KEY))?.value),
          ...change,
        })
        await db.values.put({ key: SETTINGS_KEY, value: next })
        return next
      }),
  }
}

export type DiaryRepository = ReturnType<typeof diaryRepository>

/** The repository over this device's diary. */
export const diary = diaryRepository()

/** A record without its id, so the database assigns one. (add/put on an auto-increment table always
 * return the key - the `!` above only narrows Dexie's `number | undefined`.) */
function stripId<T extends { id?: number }>(record: T): T {
  const copy = { ...record }
  delete copy.id
  return copy
}
