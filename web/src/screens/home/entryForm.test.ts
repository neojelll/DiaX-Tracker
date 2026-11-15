import { describe, expect, it } from 'vitest'
import { DEFAULT_CARB_DISPLAY } from '@/diary/carbs'
import {
  applyPreset,
  cleared,
  EMPTY_FORM,
  greeting,
  isDirty,
  isFillable,
  shownAt,
  toEntry,
  typeCarbs,
  withDate,
  withTime,
} from '@/screens/home/entryForm'
import { sugarAge } from '@/screens/home/sugarAge'

const NOW = '2026-11-15T09:41:00'
const OATS = {
  id: 7,
  name: 'Овсянка',
  comment: '',
  products: [
    { name: 'Хлопья', carbsGrams: 30 },
    { name: 'Молоко', carbsGrams: 5 },
  ],
}

describe('entry form', () => {
  it('needs a number to be saved; a comment alone only makes it dirty', () => {
    expect(isFillable(EMPTY_FORM)).toBe(false)
    const note = { ...EMPTY_FORM, notes: 'после бега' }
    expect(isFillable(note)).toBe(false)
    expect(isDirty(note)).toBe(true)
    expect(isFillable({ ...EMPTY_FORM, shortInsulin: '4' })).toBe(true)
  })

  it('follows the clock until a date or time is picked, and clearing keeps the pick', () => {
    expect(shownAt(EMPTY_FORM, NOW)).toBe(NOW)
    const timed = withTime(EMPTY_FORM, '08:30', NOW)
    expect(shownAt(timed, '2026-11-15T10:00:00')).toBe('2026-11-15T08:30:00')
    const dated = withDate(timed, '2026-11-14', NOW)
    expect(dated.pinnedAt).toBe('2026-11-14T08:30:00')
    expect(cleared({ ...dated, sugar: '6' })).toEqual({ ...EMPTY_FORM, pinnedAt: dated.pinnedAt })
  })

  it('a preset fills the carbs; typing carbs drops it', () => {
    const picked = applyPreset({ ...EMPTY_FORM, foodOpen: true }, OATS, DEFAULT_CARB_DISPLAY)
    expect(picked).toMatchObject({ carbs: '3,5', presetId: 7, mealLabel: 'Овсянка', foodOpen: false })
    expect(typeCarbs(picked, '2')).toMatchObject({
      carbs: '2',
      presetId: null,
      mealLabel: null,
      products: [],
    })
  })

  it('saves typed text as numbers, carbs in grams, sugar as a finger check', () => {
    const form = applyPreset(
      { ...EMPTY_FORM, sugar: '6,4', shortInsulin: '4,5', notes: '  сытно ' },
      OATS,
      DEFAULT_CARB_DISPLAY,
    )
    expect(toEntry(form, NOW, DEFAULT_CARB_DISPLAY)).toMatchObject({
      bloodSugar: 6.4,
      sugarSource: 'MANUAL',
      carbsGrams: 35,
      mealLabel: 'Овсянка',
      products: OATS.products,
      shortInsulinDose: 4.5,
      longInsulinDose: null,
      notes: 'сытно',
      createdAt: NOW,
    })
    expect(toEntry({ ...EMPTY_FORM, longInsulin: '18' }, NOW, DEFAULT_CARB_DISPLAY).sugarSource).toBeNull()
  })

  it('greets by the time of day', () => {
    expect(greeting(6)).toBe('Доброе утро!')
    expect(greeting(13)).toBe('Добрый день!')
    expect(greeting(20)).toBe('Добрый вечер!')
    expect(greeting(2)).toBe('Доброй ночи!')
  })
})

describe('sugarAge', () => {
  it('ages the last sugar as the Android sensor tile did', () => {
    expect(sugarAge(15)).toBe('только что')
    expect(sugarAge(40)).toBe('40 мин назад')
    expect(sugarAge(150)).toBe('2 ч назад')
    expect(sugarAge(2 * 24 * 60 + 5)).toBe('2 дн. назад')
    expect(sugarAge(3 * 24 * 60 + 1)).toBeNull()
  })
})
