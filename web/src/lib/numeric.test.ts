import { describe, expect, it } from 'vitest'
import { numeric } from '@/lib/numeric'

describe('numeric', () => {
  it('keeps digits and one decimal comma, a typed dot as a comma', () => {
    expect(numeric('6,4')).toBe('6,4')
    expect(numeric('6.4')).toBe('6,4')
    expect(numeric('1.2,3')).toBe('1,23')
    expect(numeric('12 ммоль')).toBe('12')
    expect(numeric('-5')).toBe('5')
  })
})
