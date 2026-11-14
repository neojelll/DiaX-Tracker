import { describe, expect, it } from 'vitest'
import { DEFAULT_SETTINGS, normalizeSettings } from '@/diary/settings'

describe('normalizeSettings', () => {
  it('nothing stored gives the defaults', () => {
    expect(normalizeSettings(undefined)).toEqual(DEFAULT_SETTINGS)
    expect(normalizeSettings({ carbDisplay: 'broken' })).toEqual(DEFAULT_SETTINGS)
  })

  it('keeps valid values', () => {
    const custom = {
      carbDisplay: { unit: 'GRAMS', gramsPerXe: 12 },
      carbGoal: { enabled: true, grams: 150 },
      insulinDurationHours: 4.5,
      glucoseRange: { low: 4.5, high: 9 },
    }
    expect(normalizeSettings(custom)).toEqual(custom)
  })

  it('pulls numbers into their bounds', () => {
    const s = normalizeSettings({
      carbDisplay: { unit: 'XE', gramsPerXe: 40 },
      carbGoal: { enabled: true, grams: 5000 },
      insulinDurationHours: 2,
      glucoseRange: { low: 2, high: 20 },
    })
    expect(s.carbDisplay.gramsPerXe).toBe(15)
    expect(s.carbGoal.grams).toBe(1000)
    expect(s.insulinDurationHours).toBe(3.5)
    expect(s.glucoseRange).toEqual({ low: 3.9, high: 15 })
  })
})
