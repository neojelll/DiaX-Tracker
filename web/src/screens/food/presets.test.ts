import { describe, expect, it } from 'vitest'
import { DEFAULT_CARB_DISPLAY } from '@/diary/carbs'
import { draftGrams, draftOf, isValidDraft, presetOf, presetsCount } from '@/screens/food/presets'

const XE = DEFAULT_CARB_DISPLAY
const OATS = { id: 3, name: 'Овсянка', comment: 'дома', products: [{ name: 'Хлопья', carbsGrams: 33 }] }

describe('preset editor', () => {
  it('starts a new preset with one empty product', () => {
    const d = draftOf(null, XE)
    expect(d.products).toHaveLength(1)
    expect(isValidDraft(d)).toBe(false)
  })

  it('shows a saved preset in the display unit and saves it back in grams', () => {
    const d = draftOf(OATS, XE)
    expect(d.products[0]).toMatchObject({ name: 'Хлопья', amount: '3,3' })
    expect(isValidDraft(d)).toBe(true)
    const changed = { ...d, products: [...d.products, { key: 99, name: ' Молоко ', amount: '1' }] }
    expect(draftGrams(changed, XE)).toBe(43)
    expect(presetOf(changed, XE, 3)).toEqual({
      id: 3,
      name: 'Овсянка',
      comment: 'дома',
      products: [
        { name: 'Хлопья', carbsGrams: 33 },
        { name: 'Молоко', carbsGrams: 10 },
      ],
    })
  })

  it('needs every product named and every amount a number or empty', () => {
    const d = draftOf(OATS, XE)
    expect(isValidDraft({ ...d, products: [{ key: 1, name: 'Хлеб', amount: '' }] })).toBe(true)
    expect(isValidDraft({ ...d, products: [{ key: 1, name: '', amount: '1' }] })).toBe(false)
    expect(isValidDraft({ ...d, products: [{ key: 1, name: 'Хлеб', amount: ',' }] })).toBe(false)
    expect(isValidDraft({ ...d, name: ' ' })).toBe(false)
  })

  it('counts presets in Russian', () => {
    expect([1, 3, 5, 21, 12].map(presetsCount)).toEqual([
      '1 пресет',
      '3 пресета',
      '5 пресетов',
      '21 пресет',
      '12 пресетов',
    ])
  })
})
