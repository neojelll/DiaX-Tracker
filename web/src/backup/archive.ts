import type { SqlJsStatic } from 'sql.js'
import { ANDROID_DB, ANDROID_PHOTOS_DIR, readAndroidFiles } from '@/backup/androidBackup'
import { BackupError, type DiaryArchive } from '@/backup/types'
import { normalizeSettings } from '@/diary/settings'
import type { LocalDateTime } from '@/diary/time'
import type { MealPreset, Product, SavedEntry, SugarSource } from '@/diary/types'

/**
 * The PWA's own archive: a zip with `diary.json` - entries, presets, settings - and the photos in
 * `photos/<entry id>.<ext>`. Always the whole diary: a partial archive looks like a backup but
 * isn't one (#97). Restoring reads this format and the Android app's (androidBackup.ts).
 */

const DIARY_JSON = 'diary.json'
const PHOTOS_DIR = 'photos/'
const FORMAT = 'diax-web'
const VERSION = 1

interface DiaryJson {
  format: typeof FORMAT
  version: number
  exportedAt: LocalDateTime
  entries: SavedEntry[]
  presets: MealPreset[]
  settings: unknown
}

const EXTENSIONS: Record<string, string> = { 'image/jpeg': 'jpg', 'image/png': 'png', 'image/webp': 'webp' }
const TYPES = Object.fromEntries(Object.entries(EXTENSIONS).map(([type, ext]) => [ext, type]))

/** "diax-2026-11-16.zip". */
export const archiveName = (at: LocalDateTime) => `diax-${at.slice(0, 10)}.zip`

/** The diary as a zip. Photos are stored as they are - they are compressed already. */
export async function writeArchive(
  diary: Omit<DiaryArchive, 'source'>,
  exportedAt: LocalDateTime,
): Promise<Blob> {
  const { zipSync, strToU8 } = await import('fflate')
  const json: DiaryJson = {
    format: FORMAT,
    version: VERSION,
    exportedAt,
    entries: diary.entries,
    presets: diary.presets.map(({ name, comment, products }) => ({ name, comment, products })),
    settings: diary.settings,
  }
  const files: Record<string, Uint8Array | [Uint8Array, { level: 0 }]> = {
    [DIARY_JSON]: strToU8(JSON.stringify(json)),
  }
  for (const [entryId, blob] of diary.photos)
    files[`${PHOTOS_DIR}${entryId}.${EXTENSIONS[blob.type] ?? 'jpg'}`] = [
      new Uint8Array(await blob.arrayBuffer()),
      { level: 0 },
    ]
  return new Blob([zipSync(files) as Uint8Array<ArrayBuffer>], { type: 'application/zip' })
}

/** Reads either archive: the PWA's or the Android app's. */
export async function readArchive(file: Blob, sql?: SqlJsStatic): Promise<DiaryArchive> {
  const files = await unzip(file)
  if (files[DIARY_JSON]) return readWebFiles(files)
  if (files[ANDROID_DB]) return readAndroidFiles(files, sql)
  throw new BackupError('not-backup')
}

async function readWebFiles(files: Record<string, Uint8Array>): Promise<DiaryArchive> {
  const { strFromU8 } = await import('fflate')
  let json: Partial<DiaryJson>
  try {
    json = JSON.parse(strFromU8(files[DIARY_JSON])) as Partial<DiaryJson>
  } catch {
    throw new BackupError('not-backup')
  }
  if (json.format !== FORMAT || typeof json.version !== 'number') throw new BackupError('not-backup')
  if (json.version > VERSION) throw new BackupError('newer-version')
  if (!Array.isArray(json.entries) || !Array.isArray(json.presets)) throw new BackupError('not-backup')

  const entries = json.entries.map(entryOf)
  const photos = new Map<number, Blob>()
  for (const [path, bytes] of Object.entries(files)) {
    const match = /^photos\/(\d+)\.(\w+)$/.exec(path)
    if (!match) continue
    photos.set(
      Number(match[1]),
      new Blob([bytes as Uint8Array<ArrayBuffer>], { type: TYPES[match[2]] ?? 'image/jpeg' }),
    )
  }
  return {
    source: 'web',
    entries,
    presets: json.presets.map(presetOf),
    photos,
    settings: normalizeSettings(json.settings),
  }
}

// What the file says is checked field by field: a damaged or hand-edited archive can't put
// something the screens don't expect into the diary.
const num = (v: unknown) => (typeof v === 'number' && Number.isFinite(v) ? v : null)
const text = (v: unknown) => (typeof v === 'string' ? v : null)

function productsOf(v: unknown): Product[] {
  if (!Array.isArray(v)) return []
  return v.map((p: Record<string, unknown>) => ({
    name: text(p?.name) ?? '',
    carbsGrams: num(p?.carbsGrams) ?? 0,
  }))
}

function entryOf(raw: unknown): SavedEntry {
  const e = (raw ?? {}) as Record<string, unknown>
  const id = num(e.id)
  const createdAt = text(e.createdAt)
  if (id == null || createdAt == null || !/^\d{4}-\d{2}-\d{2}T\d{2}:\d{2}:\d{2}$/.test(createdAt))
    throw new BackupError('not-backup')
  return {
    id,
    bloodSugar: num(e.bloodSugar),
    sugarSource:
      e.sugarSource === 'MANUAL' || e.sugarSource === 'SENSOR' ? (e.sugarSource as SugarSource) : null,
    carbsGrams: num(e.carbsGrams),
    mealLabel: text(e.mealLabel),
    products: productsOf(e.products),
    shortInsulinDose: num(e.shortInsulinDose),
    longInsulinDose: num(e.longInsulinDose),
    notes: text(e.notes) ?? '',
    createdAt,
    sourceEntryId: num(e.sourceEntryId),
    sourceHour: num(e.sourceHour),
  }
}

function presetOf(raw: unknown): MealPreset {
  const p = (raw ?? {}) as Record<string, unknown>
  return { name: text(p.name) ?? '', comment: text(p.comment) ?? '', products: productsOf(p.products) }
}

/** The files restoring needs; anything else in the zip is skipped. */
async function unzip(file: Blob): Promise<Record<string, Uint8Array>> {
  const { unzipSync } = await import('fflate')
  try {
    return unzipSync(new Uint8Array(await file.arrayBuffer()), {
      filter: (f) =>
        f.name === DIARY_JSON ||
        f.name === ANDROID_DB ||
        ((f.name.startsWith(PHOTOS_DIR) || f.name.startsWith(ANDROID_PHOTOS_DIR)) && !f.name.endsWith('/')),
    })
  } catch {
    throw new BackupError('not-backup')
  }
}
