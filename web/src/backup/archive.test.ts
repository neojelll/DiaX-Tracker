import { strToU8, zipSync } from 'fflate'
import { describe, expect, it } from 'vitest'
import { archiveName, readArchive, writeArchive } from '@/backup/archive'
import { BackupError } from '@/backup/types'
import { DEFAULT_SETTINGS } from '@/diary/settings'
import { newEntry, type SavedEntry } from '@/diary/types'

const meal: SavedEntry = {
  ...newEntry('2026-10-21T08:20:00', {
    carbsGrams: 48,
    mealLabel: 'Овсянка',
    products: [{ name: 'хлопья', carbsGrams: 48 }],
    notes: 'дома',
  }),
  id: 7,
}
const check: SavedEntry = {
  ...newEntry('2026-10-21T09:20:00', {
    bloodSugar: 8.4,
    sugarSource: 'SENSOR',
    sourceEntryId: 7,
    sourceHour: 1,
  }),
  id: 9,
}
const settings = { ...DEFAULT_SETTINGS, insulinDurationHours: 5, glucoseRange: { low: 4.5, high: 9 } }

const problem = (file: Blob) =>
  readArchive(file).then(
    () => 'read',
    (e: unknown) => (e instanceof BackupError ? e.problem : String(e)),
  )
const zip = (files: Record<string, Uint8Array>) => new Blob([zipSync(files) as Uint8Array<ArrayBuffer>])
const json = (value: unknown) => zip({ 'diary.json': strToU8(JSON.stringify(value)) })

describe('the PWA archive', () => {
  it('reads back everything it wrote: entries, presets, photos and settings', async () => {
    const file = await writeArchive(
      {
        entries: [meal, check],
        presets: [
          { id: 3, name: 'Овсянка', comment: 'на воде', products: [{ name: 'хлопья', carbsGrams: 30 }] },
        ],
        photos: new Map([[7, new Blob(['png bytes'], { type: 'image/png' })]]),
        settings,
      },
      '2026-11-16T12:00:00',
    )
    const archive = await readArchive(file)

    expect(archive.source).toBe('web')
    expect(archive.entries).toEqual([meal, check])
    expect(archive.presets).toEqual([
      { name: 'Овсянка', comment: 'на воде', products: [{ name: 'хлопья', carbsGrams: 30 }] },
    ])
    expect(archive.settings).toEqual(settings)
    const photo = archive.photos.get(7)!
    expect(photo.type).toBe('image/png')
    expect(await photo.text()).toBe('png bytes')
  })

  it('turns down a newer format, a foreign json and a damaged entry', async () => {
    const base = {
      format: 'diax-web',
      version: 1,
      exportedAt: '2026-11-16T12:00:00',
      presets: [],
      settings: {},
    }
    expect(await problem(json({ ...base, version: 2, entries: [] }))).toBe('newer-version')
    expect(await problem(json({ hello: 'world' }))).toBe('not-backup')
    expect(await problem(zip({ 'diary.json': strToU8('{ not json') }))).toBe('not-backup')
    expect(await problem(json({ ...base, entries: [{ id: 1, createdAt: 'yesterday' }] }))).toBe('not-backup')
    expect(await problem(json({ ...base, entries: [{ ...meal, id: 'seven' }] }))).toBe('not-backup')
  })

  it('cleans what it reads: unknown fields go, settings are brought into bounds', async () => {
    const archive = await readArchive(
      json({
        format: 'diax-web',
        version: 1,
        exportedAt: '2026-11-16T12:00:00',
        entries: [{ ...meal, sugarSource: 'GUESS', extra: true, bloodSugar: '6' }],
        presets: [{ name: 'Борщ' }],
        settings: { insulinDurationHours: 99 },
      }),
    )
    expect(archive.entries[0]).toEqual({ ...meal, sugarSource: null, bloodSugar: null })
    expect(archive.presets).toEqual([{ name: 'Борщ', comment: '', products: [] }])
    expect(archive.settings!.insulinDurationHours).toBe(5.5)
  })

  it('names the file by the day', () => {
    expect(archiveName('2026-11-16T12:00:00')).toBe('diax-2026-11-16.zip')
  })
})
