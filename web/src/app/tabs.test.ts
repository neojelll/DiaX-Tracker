import { describe, expect, it } from 'vitest'
import { DEFAULT_TAB, tabFromHash } from '@/app/tabs'

describe('tabFromHash', () => {
  it('reads the tab from the hash', () => {
    expect(tabFromHash('#/history')).toBe('history')
    expect(tabFromHash('#food')).toBe('food')
  })

  it('opens the default tab for an empty or unknown hash', () => {
    expect(tabFromHash('')).toBe(DEFAULT_TAB)
    expect(tabFromHash('#/nope')).toBe(DEFAULT_TAB)
  })
})
