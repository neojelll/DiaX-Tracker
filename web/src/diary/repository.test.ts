import { beforeEach, describe, expect, it } from 'vitest'
import { DiaryDatabase } from '@/diary/db'
import { diaryRepository, type DiaryRepository } from '@/diary/repository'
import { DEFAULT_SETTINGS } from '@/diary/settings'
import { newEntry, type SavedEntry } from '@/diary/types'

let repo: DiaryRepository
let n = 0

beforeEach(() => {
  repo = diaryRepository(new DiaryDatabase(`test-${n++}`))
})

describe('entries', () => {
  it('adds, reads back newest first, updates and deletes with undo', async () => {
    const breakfast = await repo.addEntry(
      newEntry('2026-10-21T08:20:00', {
        carbsGrams: 36,
        mealLabel: 'Овсянка',
        products: [
          { name: 'овсяные хлопья', carbsGrams: 30 },
          { name: 'банан', carbsGrams: 6 },
        ],
      }),
    )
    const lunch = await repo.addEntry(newEntry('2026-10-21T12:20:00', { shortInsulinDose: 5 }))

    const all = await repo.allEntries()
    expect(all.map((e) => e.id)).toEqual([lunch, breakfast])
    expect(all[1].products.map((p) => p.name)).toEqual(['овсяные хлопья', 'банан'])

    await repo.updateEntry({ ...all[0], shortInsulinDose: 6 })
    expect((await repo.getEntry(lunch))!.shortInsulinDose).toBe(6)

    const deleted = await repo.deleteEntry(breakfast)
    expect(await repo.getEntry(breakfast)).toBeUndefined()
    await repo.restoreEntries([deleted!])
    expect((await repo.getEntry(breakfast))!.mealLabel).toBe('Овсянка')
  })

  it('a new entry never takes a passed id', async () => {
    const withStaleId = { ...newEntry('2026-10-21T08:00:00'), id: 999 } as SavedEntry
    const id = await repo.addEntry(withStaleId)
    expect(id).not.toBe(999)
  })

  it('reads a range of days, both ends included', async () => {
    for (const at of [
      '2026-10-19T23:59:00',
      '2026-10-20T00:00:00',
      '2026-10-21T23:59:59',
      '2026-10-22T00:00:00',
    ]) {
      await repo.addEntry(newEntry(at))
    }
    const range = await repo.entriesBetween('2026-10-20', '2026-10-21')
    expect(range.map((e) => e.createdAt)).toEqual(['2026-10-21T23:59:59', '2026-10-20T00:00:00'])
  })

  it("finds a meal's checks", async () => {
    const meal = await repo.addEntry(newEntry('2026-10-21T12:00:00', { carbsGrams: 40 }))
    await repo.addEntry(
      newEntry('2026-10-21T13:00:00', { bloodSugar: 8.4, sourceEntryId: meal, sourceHour: 1 }),
    )
    await repo.addEntry(newEntry('2026-10-21T13:05:00', { bloodSugar: 7 }))
    expect((await repo.checksOf(meal)).map((e) => e.bloodSugar)).toEqual([8.4])
  })
})

describe('presets', () => {
  it('saves, sorts by name, edits and deletes with undo', async () => {
    const yogurt = await repo.savePreset({
      name: 'Йогурт',
      comment: 'перекус',
      products: [{ name: 'йогурт', carbsGrams: 15 }],
    })
    await repo.savePreset({ name: 'Гречка с курицей', comment: '', products: [] })
    expect((await repo.allPresets()).map((p) => p.name)).toEqual(['Гречка с курицей', 'Йогурт'])

    await repo.savePreset({
      id: yogurt,
      name: 'Йогурт',
      comment: '',
      products: [{ name: 'йогурт', carbsGrams: 18 }],
    })
    const deleted = await repo.deletePreset(yogurt)
    expect(deleted!.products[0].carbsGrams).toBe(18)
    expect(await repo.allPresets()).toHaveLength(1)
    await repo.restorePreset(deleted!)
    expect(await repo.allPresets()).toHaveLength(2)
  })
})

describe('settings', () => {
  it('starts at the defaults and keeps what is not changed', async () => {
    expect(await repo.getSettings()).toEqual(DEFAULT_SETTINGS)
    await repo.updateSettings({ carbDisplay: { unit: 'XE', gramsPerXe: 12 } })
    const next = await repo.updateSettings({ carbGoal: { enabled: true, grams: 150 } })
    expect(next.carbDisplay.gramsPerXe).toBe(12)
    expect(await repo.getSettings()).toEqual(next)
  })

  it('stores values within bounds', async () => {
    expect((await repo.updateSettings({ insulinDurationHours: 9 })).insulinDurationHours).toBe(5.5)
  })
})

describe('replaceDiary', () => {
  it('puts the given entries, presets and photos in place of everything, keeping the settings', async () => {
    await repo.addEntry(newEntry('2026-11-01T10:00:00', { bloodSugar: 5 }))
    await repo.savePreset({ name: 'Старый', comment: '', products: [] })
    await repo.updateSettings({ insulinDurationHours: 5 })

    const meal: SavedEntry = { ...newEntry('2026-10-21T08:20:00', { carbsGrams: 48 }), id: 7 }
    const check: SavedEntry = {
      ...newEntry('2026-10-21T09:20:00', { bloodSugar: 8.4, sourceEntryId: 7, sourceHour: 1 }),
      id: 9,
    }
    await repo.replaceDiary({
      entries: [meal, check],
      presets: [{ name: 'Овсянка', comment: '', products: [{ name: 'хлопья', carbsGrams: 30 }] }],
      photos: new Map([[7, new Blob(['jpeg'])]]),
    })

    expect((await repo.allEntries()).map((e) => e.id)).toEqual([9, 7])
    expect((await repo.checksOf(7)).map((e) => e.id)).toEqual([9])
    expect((await repo.allPresets()).map((p) => p.name)).toEqual(['Овсянка'])
    // fake-indexeddb can't clone a jsdom Blob back, so only its presence is checked here.
    expect(await repo.photoOf(7)).toBeDefined()
    expect(await repo.photoOf(9)).toBeUndefined()
    expect(await repo.countEntries()).toBe(2)
    expect((await repo.getSettings()).insulinDurationHours).toBe(5)
    // New entries carry on after the imported ids.
    expect(await repo.addEntry(newEntry('2026-11-02T10:00:00'))).toBeGreaterThan(9)
  })
})

describe('the whole diary', () => {
  it('reads everything for an archive and takes the settings of a restored one', async () => {
    await repo.addEntry(newEntry('2026-11-02T10:00:00', { bloodSugar: 6 }))
    await repo.addEntry(newEntry('2026-11-01T10:00:00', { bloodSugar: 5 }))
    await repo.updateSettings({ insulinDurationHours: 5 })
    const whole = await repo.wholeDiary()
    expect(whole.entries.map((e) => e.createdAt)).toEqual(['2026-11-01T10:00:00', '2026-11-02T10:00:00'])
    expect(whole.settings.insulinDurationHours).toBe(5)

    await repo.replaceDiary({
      entries: [],
      presets: [],
      photos: new Map(),
      settings: { ...DEFAULT_SETTINGS, insulinDurationHours: 3.5 },
    })
    expect((await repo.getSettings()).insulinDurationHours).toBe(3.5)
  })

  it('remembers when an archive was saved', async () => {
    expect(await repo.lastBackup()).toBeNull()
    await repo.markBackedUp('2026-11-16T12:00:00')
    expect(await repo.lastBackup()).toBe('2026-11-16T12:00:00')
  })
})
