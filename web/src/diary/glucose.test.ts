import { describe, expect, it } from 'vitest'
import { glucoseLevel } from '@/diary/glucose'

const range = { low: 4.0, high: 9.0 }

describe('glucoseLevel', () => {
  it('is very low below the fixed 3.0 threshold', () => {
    expect(glucoseLevel(2.9, range)).toBe('veryLow')
  })

  it('is low between 3.0 and the low bound', () => {
    expect(glucoseLevel(3.0, range)).toBe('low')
    expect(glucoseLevel(3.9, range)).toBe('low')
  })

  it('keeps the clinical 3.9 low threshold under a looser target', () => {
    expect(glucoseLevel(3.8, { low: 3.5, high: 8.0 })).toBe('low')
  })

  it('is in range on both bounds', () => {
    expect(glucoseLevel(4.0, range)).toBe('inRange')
    expect(glucoseLevel(9.0, range)).toBe('inRange')
  })

  it('is high up to 3.9 above the high bound, very high beyond', () => {
    expect(glucoseLevel(9.1, range)).toBe('high')
    expect(glucoseLevel(12.9, range)).toBe('high')
    expect(glucoseLevel(13.0, range)).toBe('veryHigh')
  })
})
