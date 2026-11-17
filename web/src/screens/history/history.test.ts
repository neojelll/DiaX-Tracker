import { describe, expect, it } from 'vitest'
import { DEFAULT_CARB_DISPLAY } from '@/diary/carbs'
import { newEntry, type SavedEntry } from '@/diary/types'
import { editedEntry, fromEntry } from '@/screens/home/entryForm'
import {
  byDay,
  dayLabel,
  hasMoreInside,
  inFilter,
  matchesSearch,
  recordKind,
  shownAfterMeal,
} from '@/screens/history/history'

const TODAY = '2026-11-15'
const at = (createdAt: string, fields = {}, id = 1): SavedEntry => ({ ...newEntry(createdAt, fields), id })

describe('history', () => {
  it('filters by day', () => {
    const yesterday = at('2026-11-14T20:00:00')
    const weekAgo = at('2026-11-08T08:00:00')
    expect(inFilter(yesterday, { kind: 'yesterday' }, TODAY)).toBe(true)
    expect(inFilter(weekAgo, { kind: 'week' }, TODAY)).toBe(false)
    expect(inFilter(at('2026-11-09T08:00:00'), { kind: 'week' }, TODAY)).toBe(true)
    expect(inFilter(weekAgo, { kind: 'month' }, TODAY)).toBe(true)
    expect(inFilter(weekAgo, { kind: 'range', from: '2026-11-08', to: '2026-11-08' }, TODAY)).toBe(true)
  })

  it('searches notes, presets, products and sugar as shown', () => {
    const meal = at('2026-11-15T08:00:00', {
      notes: 'После бега',
      mealLabel: 'Овсянка',
      products: [{ name: 'Молоко', carbsGrams: 5 }],
      bloodSugar: 6.4,
    })
    for (const q of ['бег', 'овсян', 'молоко', '6,4', '6.4', '']) expect(matchesSearch(meal, q)).toBe(true)
    expect(matchesSearch(meal, 'гречка')).toBe(false)
  })

  it('groups records by day in order', () => {
    const days = byDay([at('2026-11-15T09:00:00'), at('2026-11-15T08:00:00'), at('2026-11-14T22:00:00')])
    expect(days.map((d) => [d.day, d.records.length])).toEqual([
      ['2026-11-15', 2],
      ['2026-11-14', 1],
    ])
  })

  it('names what a record is; food wins over its dose', () => {
    expect(recordKind(newEntry(TODAY + 'T08:00:00', { carbsGrams: 30, shortInsulinDose: 3 }))).toBe('meal')
    expect(recordKind(newEntry(TODAY + 'T08:00:00', { longInsulinDose: 18 }))).toBe('shot')
    expect(recordKind(newEntry(TODAY + 'T08:00:00', { bloodSugar: 5 }))).toBe('check')
  })

  it('shows the after-meal line only once a check is in', () => {
    expect(shownAfterMeal({ values: [null, null, null, null] })).toBeUndefined()
    expect(shownAfterMeal({ values: [8.1, null, null, null] })).toBeDefined()
    expect(hasMoreInside(newEntry(TODAY + 'T08:00:00', { carbsGrams: 30 }), undefined)).toBe(false)
    expect(hasMoreInside(newEntry(TODAY + 'T08:00:00', { notes: 'x' }), undefined)).toBe(true)
    expect(hasMoreInside(newEntry(TODAY + 'T08:00:00', { carbsGrams: 30 }), undefined, true)).toBe(true)
  })

  it('labels days', () => {
    expect(dayLabel(TODAY, TODAY)).toBe('Сегодня')
    expect(dayLabel('2026-11-14', TODAY)).toBe('Вчера')
    expect(dayLabel('2026-11-12', TODAY)).toBe('12 ноября')
  })
})

describe('editing an entry', () => {
  const entry = at(
    '2026-11-15T08:00:00',
    {
      bloodSugar: 6.4,
      sugarSource: 'SENSOR',
      carbsGrams: 33,
      shortInsulinDose: 3.5,
      notes: 'утро',
    },
    5,
  )

  it('fills the form from the entry', () => {
    expect(fromEntry(entry, DEFAULT_CARB_DISPLAY)).toMatchObject({
      pinnedAt: '2026-11-15T08:00:00',
      sugar: '6,4',
      shortInsulin: '3,5',
      carbs: '3,3',
      notes: 'утро',
    })
  })

  it('keeps what was not touched exactly, including the sugar source and grams', () => {
    const form = { ...fromEntry(entry, DEFAULT_CARB_DISPLAY), shortInsulin: '4' }
    expect(editedEntry(entry, form, DEFAULT_CARB_DISPLAY)).toEqual({ ...entry, shortInsulinDose: 4 })
  })

  it('a retyped sugar becomes a finger check', () => {
    const form = { ...fromEntry(entry, DEFAULT_CARB_DISPLAY), sugar: '7', pinnedAt: '2026-11-15T07:30:00' }
    expect(editedEntry(entry, form, DEFAULT_CARB_DISPLAY)).toMatchObject({
      id: 5,
      bloodSugar: 7,
      sugarSource: 'MANUAL',
      carbsGrams: 33,
      createdAt: '2026-11-15T07:30:00',
    })
  })
})
