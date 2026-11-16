import type { Database, SqlJsStatic, SqlValue } from 'sql.js'
import type { LocalDateTime } from '@/diary/time'
import type { MealPreset, Product, SavedEntry, SugarSource } from '@/diary/types'
import { BackupError, type DiaryArchive } from '@/backup/types'

/**
 * Reading the Android app's backup zip (BackupExporter.kt), once archive.ts has unzipped it: the Room database file
 * `diary_database` plus the photos in `entry_photos/`. The database is opened in the browser with
 * sql.js; only the last schema (Room version 12) is read - the Android app is frozen there, and an
 * older backup can be brought up to it by restoring it on the phone and saving it again.
 */

export const ANDROID_DB = 'diary_database'
export const ANDROID_PHOTOS_DIR = 'entry_photos/'
const SCHEMA_VERSION = 12
const SQLITE_HEADER = 'SQLite format 3\0'

/**
 * Reads the database and photos of an unzipped Android archive. [sql] is the sql.js module -
 * loaded on demand (with its wasm) when not given, so the app doesn't carry it until someone imports.
 */
export async function readAndroidFiles(
  files: Record<string, Uint8Array>,
  sql?: SqlJsStatic,
): Promise<DiaryArchive> {
  const dbFile = files[ANDROID_DB]
  if (!dbFile || !hasSqliteHeader(dbFile)) throw new BackupError('not-backup')

  const SQL = sql ?? (await loadSqlJs())
  let database: Database
  try {
    database = new SQL.Database(dbFile)
  } catch {
    throw new BackupError('not-backup')
  }
  try {
    let version: number
    try {
      version = Number(rows(database, 'PRAGMA user_version')[0]?.user_version)
      rows(database, 'SELECT id FROM diary_entries LIMIT 1')
    } catch {
      throw new BackupError('not-backup')
    }
    if (version !== SCHEMA_VERSION)
      throw new BackupError(version < SCHEMA_VERSION ? 'old-version' : 'not-backup')
    return read(database, files)
  } finally {
    database.close()
  }
}

function read(database: Database, files: Record<string, Uint8Array>): DiaryArchive {
  const entryProducts = productsBy(
    rows(
      database,
      'SELECT diaryEntryId AS owner, name, carbsGrams FROM diary_entry_products ORDER BY sortOrder, id',
    ),
  )
  const presetProducts = productsBy(
    rows(
      database,
      'SELECT mealPresetId AS owner, name, carbsGrams FROM meal_preset_products ORDER BY sortOrder, id',
    ),
  )

  const photos = new Map<number, Blob>()
  const entries = rows(database, 'SELECT * FROM diary_entries ORDER BY createdAt, id').map(
    (row): SavedEntry => {
      const id = Number(row.id)
      const photo = photoName(row.photoPath)
      const bytes = photo == null ? undefined : files[ANDROID_PHOTOS_DIR + photo]
      if (bytes) photos.set(id, new Blob([bytes as Uint8Array<ArrayBuffer>], { type: 'image/jpeg' }))
      return {
        id,
        bloodSugar: numberOrNull(row.bloodSugar),
        sugarSource: sugarSource(row.sugarSource),
        carbsGrams: numberOrNull(row.carbsGrams),
        mealLabel: textOrNull(row.mealLabel),
        products: entryProducts.get(id) ?? [],
        shortInsulinDose: numberOrNull(row.shortInsulinDose),
        longInsulinDose: numberOrNull(row.longInsulinDose),
        notes: typeof row.notes === 'string' ? row.notes : '',
        createdAt: localDateTime(String(row.createdAt)),
        sourceEntryId: numberOrNull(row.sourceEntryId),
        sourceHour: numberOrNull(row.sourceHour),
      }
    },
  )

  const presets = rows(database, 'SELECT id, name, comment FROM meal_presets ORDER BY id').map(
    (row): MealPreset => ({
      name: String(row.name),
      comment: typeof row.comment === 'string' ? row.comment : '',
      products: presetProducts.get(Number(row.id)) ?? [],
    }),
  )

  return { source: 'android', entries, presets, photos, settings: null }
}

/**
 * Android stores `LocalDateTime.toString()`: seconds are left out when zero and fractions are kept
 * ("2026-10-21T14:30", "...T14:30:05.123"). The PWA keeps every time to the second.
 */
export function localDateTime(text: string): LocalDateTime {
  const match = /^(\d{4}-\d{2}-\d{2}T\d{2}:\d{2})(:\d{2})?/.exec(text)
  if (!match) throw new BackupError('not-backup')
  return match[1] + (match[2] ?? ':00')
}

type Row = Record<string, SqlValue>

function rows(database: Database, query: string): Row[] {
  const result = database.exec(query)[0]
  if (!result) return []
  return result.values.map((values) => Object.fromEntries(result.columns.map((c, i) => [c, values[i]])))
}

function productsBy(list: Row[]): Map<number, Product[]> {
  const byOwner = new Map<number, Product[]>()
  for (const row of list) {
    const owner = Number(row.owner)
    const products = byOwner.get(owner) ?? []
    products.push({ name: String(row.name), carbsGrams: Number(row.carbsGrams) })
    byOwner.set(owner, products)
  }
  return byOwner
}

const numberOrNull = (value: SqlValue) => (typeof value === 'number' && Number.isFinite(value) ? value : null)
const textOrNull = (value: SqlValue) => (typeof value === 'string' ? value : null)
const sugarSource = (value: SqlValue): SugarSource | null =>
  value === 'MANUAL' || value === 'SENSOR' ? value : null

/** Only the file name: the path points into the phone's app folder. */
function photoName(path: SqlValue): string | null {
  if (typeof path !== 'string' || path === '') return null
  return path.slice(path.lastIndexOf('/') + 1)
}

function hasSqliteHeader(bytes: Uint8Array): boolean {
  if (bytes.length < SQLITE_HEADER.length) return false
  for (let i = 0; i < SQLITE_HEADER.length; i++) if (bytes[i] !== SQLITE_HEADER.charCodeAt(i)) return false
  return true
}

async function loadSqlJs(): Promise<SqlJsStatic> {
  const [{ default: initSqlJs }, { default: wasmUrl }] = await Promise.all([
    import('sql.js'),
    import('sql.js/dist/sql-wasm-browser.wasm?url'),
  ])
  return initSqlJs({ locateFile: () => wasmUrl })
}
