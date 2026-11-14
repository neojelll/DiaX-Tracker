import { describe, expect, it } from 'vitest'
import { carbsGramsOn, formatCarbs, formatXe, parseCarbs, parseXeToGrams } from '@/diary/carbs'
import { newEntry } from '@/diary/types'

describe('XE and grams', () => {
  it('typed XE becomes grams at 10 g per XE and shows back the same', () => {
    expect(parseXeToGrams('3')).toBeCloseTo(30)
    expect(parseXeToGrams('2.5')).toBeCloseTo(25)
    expect(parseXeToGrams('2,5')).toBeCloseTo(25)
    expect(formatXe(30)).toBe('3')
    expect(formatXe(25)).toBe('2.5')
  })

  it('XE are shown to one decimal without a trailing zero', () => {
    expect(formatXe(25, 12)).toBe('2.1')
    expect(formatXe(40, 12)).toBe('3.3')
    expect(formatXe(10.4)).toBe('1')
    expect(formatXe(5)).toBe('0.5')
    expect(formatXe(0)).toBe('0')
  })

  it('totals are summed in grams and rounded once', () => {
    expect(formatXe(10.4 + 10.4 + 10.4)).toBe('3.1')
  })

  it('not a number parses to null', () => {
    expect(parseXeToGrams('')).toBeNull()
    expect(parseXeToGrams('abc')).toBeNull()
    expect(parseXeToGrams('1.2.3')).toBeNull()
  })

  it('in grams, amounts are shown whole and typed as grams', () => {
    const grams = { unit: 'GRAMS', gramsPerXe: 10 } as const
    expect(formatCarbs(25, grams)).toBe('25')
    expect(formatCarbs(24.6, grams)).toBe('25')
    expect(parseCarbs('30', grams)).toBeCloseTo(30)
    expect(parseCarbs('12,5', grams)).toBeCloseTo(12.5)
  })

  it('changing grams per XE only changes how the same grams are shown', () => {
    expect(formatCarbs(36, { unit: 'XE', gramsPerXe: 10 })).toBe('3.6')
    expect(formatCarbs(36, { unit: 'XE', gramsPerXe: 12 })).toBe('3')
    expect(parseCarbs('3', { unit: 'XE', gramsPerXe: 12 })).toBeCloseTo(36)
  })
})

describe('carbsGramsOn', () => {
  const day = '2026-10-09'
  const at = (d: string, hhmm: string, carbsGrams: number | null, products: number[] = []) =>
    newEntry(`${d}T${hhmm}:00`, { carbsGrams, products: products.map((g) => ({ name: 'x', carbsGrams: g })) })

  it("sums the day's entries and ignores other days", () => {
    const entries = [
      at(day, '08:00', 30),
      at(day, '13:00', 45),
      at(day, '23:59', 10),
      at('2026-10-08', '23:59', 50),
      at('2026-10-10', '00:00', 20),
    ]
    expect(carbsGramsOn(day, entries)).toBeCloseTo(85)
  })

  it("an entry's own total wins over its products, which count only when it has none", () => {
    expect(carbsGramsOn(day, [at(day, '09:00', 40, [15]), at(day, '12:00', null, [12, 8])])).toBeCloseTo(60)
  })

  it('entries without carbs add nothing', () => {
    expect(carbsGramsOn(day, [at(day, '10:00', null)])).toBe(0)
  })
})
