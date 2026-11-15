import { afterEach, describe, expect, it } from 'vitest'
import { resetClock, setClock } from '@/diary/time'
import { decimal, duration, hhmm, moment, oneDecimal } from '@/lib/format'

afterEach(resetClock)

describe('format', () => {
  it('writes decimals with a comma, one place, no trailing zero', () => {
    expect(decimal('2.5')).toBe('2,5')
    expect(oneDecimal(6.44)).toBe('6,4')
    expect(oneDecimal(7)).toBe('7')
    expect(oneDecimal(6.96)).toBe('7')
  })

  it('says when, relative to today', () => {
    setClock(new Date(2026, 10, 15, 12, 0))
    expect(hhmm('2026-11-15T09:41:00')).toBe('09:41')
    expect(moment('2026-11-15T09:41:00')).toBe('Сегодня, 09:41')
    expect(moment('2026-11-10T13:20:00')).toMatch(/^10 нояб\.?, 13:20$/)
  })

  it('says how long', () => {
    expect(duration(80)).toBe('1 ч 20 мин')
    expect(duration(45)).toBe('45 мин')
  })
})
