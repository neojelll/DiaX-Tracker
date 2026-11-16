import initSqlJs, { type SqlJsStatic } from 'sql.js'
import { strToU8, zipSync } from 'fflate'
import { beforeAll, describe, expect, it } from 'vitest'
import { BackupError, localDateTime, readAndroidBackup } from '@/backup/androidBackup'

// The Android schema at Room version 12 (android/app/schemas/.../12.json).
const SCHEMA = `
CREATE TABLE diary_entries (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, bloodSugar REAL, sugarSource TEXT,
  carbsGrams REAL, mealLabel TEXT, shortInsulinDose REAL, longInsulinDose REAL, notes TEXT NOT NULL,
  photoPath TEXT, createdAt TEXT NOT NULL, sourceEntryId INTEGER, sourceHour INTEGER);
CREATE TABLE meal_presets (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, comment TEXT NOT NULL);
CREATE TABLE meal_preset_products (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, mealPresetId INTEGER NOT NULL,
  name TEXT NOT NULL, carbsGrams REAL NOT NULL, sortOrder INTEGER NOT NULL);
CREATE TABLE diary_entry_products (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, diaryEntryId INTEGER NOT NULL,
  name TEXT NOT NULL, carbsGrams REAL NOT NULL, sortOrder INTEGER NOT NULL);
CREATE TABLE sensor_readings_log (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, timestamp TEXT NOT NULL, bloodSugar REAL NOT NULL);
`

const DIARY = `
INSERT INTO diary_entries VALUES
  (7, 6.2, 'MANUAL', 48, 'Овсянка', 6, NULL, 'перед работой', '/data/user/0/com.neojelll.diaxtracker/files/entry_photos/a1.jpg', '2026-10-21T08:20', NULL, NULL),
  (9, 8.4, 'SENSOR', NULL, NULL, NULL, NULL, '', NULL, '2026-10-21T09:20:05.123456', 7, 1),
  (3, NULL, NULL, NULL, NULL, NULL, 18, '', NULL, '2026-10-20T22:00:30', NULL, NULL);
INSERT INTO diary_entry_products VALUES (1, 7, 'банан', 18, 1), (2, 7, 'овсяные хлопья', 30, 0);
INSERT INTO meal_presets VALUES (4, 'Овсянка', 'на воде');
INSERT INTO meal_preset_products VALUES (1, 4, 'банан', 18, 1), (2, 4, 'овсяные хлопья', 30, 0);
`

let SQL: SqlJsStatic

beforeAll(async () => {
  SQL = await initSqlJs()
})

function database(version: number, script = SCHEMA + DIARY): Uint8Array {
  const db = new SQL.Database()
  db.run(script)
  db.run(`PRAGMA user_version = ${version}`)
  const bytes = db.export()
  db.close()
  return bytes
}

const zip = (files: Record<string, Uint8Array>) => new Blob([zipSync(files) as Uint8Array<ArrayBuffer>])
const photo = strToU8('jpeg bytes')

describe('readAndroidBackup', () => {
  it('reads entries, their products, presets and photos', async () => {
    const backup = await readAndroidBackup(
      zip({ diary_database: database(12), 'entry_photos/a1.jpg': photo, 'entry_photos/orphan.jpg': photo }),
      SQL,
    )

    expect(backup.entries.map((e) => [e.id, e.createdAt])).toEqual([
      [3, '2026-10-20T22:00:30'],
      [7, '2026-10-21T08:20:00'],
      [9, '2026-10-21T09:20:05'],
    ])
    const [shot, meal, check] = backup.entries
    expect(meal).toMatchObject({
      bloodSugar: 6.2,
      sugarSource: 'MANUAL',
      carbsGrams: 48,
      mealLabel: 'Овсянка',
      shortInsulinDose: 6,
      longInsulinDose: null,
      notes: 'перед работой',
      products: [
        { name: 'овсяные хлопья', carbsGrams: 30 },
        { name: 'банан', carbsGrams: 18 },
      ],
    })
    expect(check).toMatchObject({ sugarSource: 'SENSOR', sourceEntryId: 7, sourceHour: 1, products: [] })
    expect(shot).toMatchObject({ longInsulinDose: 18, bloodSugar: null, sugarSource: null })

    expect(backup.presets).toEqual([
      {
        name: 'Овсянка',
        comment: 'на воде',
        products: [
          { name: 'овсяные хлопья', carbsGrams: 30 },
          { name: 'банан', carbsGrams: 18 },
        ],
      },
    ])
    expect([...backup.photos.keys()]).toEqual([7])
    expect(await backup.photos.get(7)!.text()).toBe('jpeg bytes')
  })

  it('reads a backup whose photo is missing from the zip', async () => {
    const backup = await readAndroidBackup(zip({ diary_database: database(12) }), SQL)
    expect(backup.entries).toHaveLength(3)
    expect(backup.photos.size).toBe(0)
  })

  it('turns down an older schema and anything that is not a backup', async () => {
    const problem = (file: Blob) =>
      readAndroidBackup(file, SQL).then(
        () => 'read',
        (e: unknown) => (e instanceof BackupError ? e.problem : String(e)),
      )

    expect(await problem(zip({ diary_database: database(11) }))).toBe('old-version')
    expect(await problem(zip({ 'notes.txt': strToU8('hello') }))).toBe('not-backup')
    expect(await problem(zip({ diary_database: strToU8('not a database at all') }))).toBe('not-backup')
    expect(await problem(zip({ diary_database: database(12, 'CREATE TABLE other (x);') }))).toBe('not-backup')
    expect(await problem(new Blob(['plain text']))).toBe('not-backup')
  })
})

describe('localDateTime', () => {
  it('keeps every time to the second', () => {
    expect(localDateTime('2026-10-21T14:30')).toBe('2026-10-21T14:30:00')
    expect(localDateTime('2026-10-21T14:30:05')).toBe('2026-10-21T14:30:05')
    expect(localDateTime('2026-10-21T14:30:05.123456789')).toBe('2026-10-21T14:30:05')
  })
})
