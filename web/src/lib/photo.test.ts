import { describe, expect, it } from 'vitest'
import { fitWithin } from '@/lib/photo'

describe('fitWithin', () => {
  it('brings the long side down to the limit and keeps the shape', () => {
    expect(fitWithin(4032, 3024)).toEqual({ width: 1600, height: 1200 })
    expect(fitWithin(3024, 4032)).toEqual({ width: 1200, height: 1600 })
  })

  it('leaves a small picture as it is', () => {
    expect(fitWithin(800, 600)).toEqual({ width: 800, height: 600 })
  })
})
