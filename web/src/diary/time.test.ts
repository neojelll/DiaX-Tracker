import { afterEach, describe, expect, it } from 'vitest'
import {
  clock,
  dayOf,
  fromLocalDateTime,
  minutesBetween,
  nowLocal,
  plusDays,
  plusMinutes,
  resetClock,
  setClock,
  toLocalDateTime,
  today,
} from '@/diary/time'

afterEach(resetClock)

describe('local date-time', () => {
  it('round-trips through Date in the local zone', () => {
    expect(toLocalDateTime(fromLocalDateTime('2026-10-21T14:30:05'))).toBe('2026-10-21T14:30:05')
    expect(toLocalDateTime(fromLocalDateTime('2026-10-21T14:30'))).toBe('2026-10-21T14:30:00')
  })

  it('moves across hours, midnight and month ends', () => {
    expect(plusMinutes('2026-10-21T23:30:00', 45)).toBe('2026-10-22T00:15:00')
    expect(plusMinutes('2026-10-21T00:10:00', -20)).toBe('2026-10-20T23:50:00')
    expect(plusDays('2026-10-31', 1)).toBe('2026-11-01')
    expect(plusDays('2026-03-01', -1)).toBe('2026-02-28')
  })

  it('counts whole minutes between two times', () => {
    expect(minutesBetween('2026-10-21T12:00:00', '2026-10-21T13:30:59')).toBe(90)
    expect(minutesBetween('2026-10-21T13:30:00', '2026-10-21T12:00:00')).toBe(-90)
  })

  it('reads the day of a date-time', () => {
    expect(dayOf('2026-10-21T14:30:00')).toBe('2026-10-21')
  })

  it('a fixed clock fixes now and today', () => {
    setClock(new Date(2026, 9, 21, 14, 30))
    expect(nowLocal()).toBe('2026-10-21T14:30:00')
    expect(today()).toBe('2026-10-21')
    expect(clock.now().getHours()).toBe(14)
  })
})
