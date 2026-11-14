import { describe, expect, it } from 'vitest'
import { activeInsulin, insulinOn, insulinOnBoardFraction, insulinProgress } from '@/diary/insulin'
import { plusMinutes } from '@/diary/time'
import { newEntry } from '@/diary/types'

describe('insulinOnBoardFraction', () => {
  it('is whole at and before the dose, zero at and past the duration', () => {
    expect(insulinOnBoardFraction(0, 75, 300)).toBe(1)
    expect(insulinOnBoardFraction(-5, 75, 300)).toBe(1)
    expect(insulinOnBoardFraction(300, 75, 300)).toBe(0)
    expect(insulinOnBoardFraction(400, 75, 300)).toBe(0)
  })

  it('only ever decreases and stays within 0..1', () => {
    let previous = 1
    for (let t = -50; t <= 350; t += 5) {
      const f = insulinOnBoardFraction(t, 75, 300)
      expect(f).toBeGreaterThanOrEqual(0)
      expect(f).toBeLessThanOrEqual(1)
      expect(f).toBeLessThanOrEqual(previous)
      previous = f
    }
  })

  it('is a scalable curve - the same elapsed proportion gives the same fraction', () => {
    expect(insulinOnBoardFraction(150, 75, 300)).toBeCloseTo(insulinOnBoardFraction(100, 50, 200), 4)
  })

  it('matches the hand-derived reference value at the peak (same as the Android test)', () => {
    expect(insulinOnBoardFraction(75, 75, 300)).toBeCloseTo(0.6726, 3)
  })

  it('a different peak gives a different curve', () => {
    expect(insulinOnBoardFraction(100, 50, 300)).not.toBe(insulinOnBoardFraction(100, 100, 300))
  })
})

describe('activeInsulin', () => {
  const NOW = '2026-01-01T12:00:00'
  const HOURS = 5
  const dose = (units: number | null, minutesAgo: number) =>
    newEntry(plusMinutes(NOW, -minutesAgo), { shortInsulinDose: units })

  it('is nothing with no doses, sugar only, a worn-off dose or one from the future', () => {
    expect(activeInsulin([], NOW, HOURS)).toBeNull()
    expect(activeInsulin([dose(null, 0)], NOW, HOURS)).toBeNull()
    expect(activeInsulin([dose(4, HOURS * 60)], NOW, HOURS)).toBeNull()
    expect(activeInsulin([dose(4, -10)], NOW, HOURS)).toBeNull()
  })

  it('a fresh dose is fully on board', () => {
    expect(activeInsulin([dose(4, 0)], NOW, HOURS)!.units).toBeCloseTo(4, 3)
  })

  it('overlapping doses stack', () => {
    const expected = 2 * insulinOnBoardFraction(60, 75, 300) + 3 * insulinOnBoardFraction(0, 75, 300)
    expect(activeInsulin([dose(2, 60), dose(3, 0)], NOW, HOURS)!.units).toBeCloseTo(expected, 3)
  })

  it('time left and start follow the latest active dose', () => {
    const iob = activeInsulin([dose(1, 120), dose(1, 30)], NOW, HOURS)!
    expect(iob.fromTime).toBe(plusMinutes(NOW, -30))
    expect(iob.minutesLeft).toBe(HOURS * 60 - 30)
  })

  it('mixed valid and invalid entries count only the valid ones', () => {
    const entries = [dose(null, 5), dose(9, HOURS * 60 + 10), dose(9, -5), dose(4, 0)]
    expect(activeInsulin(entries, NOW, HOURS)!.units).toBeCloseTo(4, 3)
  })

  it('progress stays within 0..1', () => {
    const base = { units: 1, fromTime: NOW, durationMinutes: 300 }
    expect(insulinProgress({ ...base, minutesLeft: 500 })).toBe(1)
    expect(insulinProgress({ ...base, minutesLeft: -10 })).toBe(0)
  })
})

describe('insulinOn', () => {
  const day = '2026-10-09'
  const at = (when: string, short: number | null, long: number | null = null) =>
    newEntry(when, { shortInsulinDose: short, longInsulinDose: long })

  it('no entries - no short insulin and no long line', () => {
    expect(insulinOn(day, [])).toEqual({ short: 0, long: null })
  })

  it('short and long are summed separately, other days ignored', () => {
    const entries = [
      at(`${day}T08:00:00`, 4, 10),
      at(`${day}T13:00:00`, 3.5),
      at(`${day}T21:00:00`, null, 10),
      at('2026-10-08T23:59:00', 6, 12),
      at('2026-10-10T00:00:00', 2),
    ]
    expect(insulinOn(day, entries)).toEqual({ short: 7.5, long: 20 })
  })

  it('short only - the long line stays hidden', () => {
    expect(insulinOn(day, [at(`${day}T12:00:00`, 5)]).long).toBeNull()
  })
})
