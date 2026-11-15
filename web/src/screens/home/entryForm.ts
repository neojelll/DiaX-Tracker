import { formatCarbs, parseCarbs, parseNumber, type CarbDisplay } from '@/diary/carbs'
import type { LocalDate, LocalDateTime } from '@/diary/time'
import { newEntry, type DiaryEntry, type Product, type SavedPreset } from '@/diary/types'
import { decimal } from '@/lib/format'
import { createStore } from '@/lib/store'

// A guard against an accidental giant paste, not an editorial limit.
export const NOTES_MAX_LENGTH = 2500

/**
 * The home screen's entry form, as typed. Numbers stay text (with a comma) until saving. Ported
 * from the Android app's EntryFormState / EntryForm.kt; no sensor and no photo yet.
 */
export interface EntryForm {
  /** The moment picked for the entry; null - the form follows the clock. */
  pinnedAt: LocalDateTime | null
  sugar: string
  shortInsulin: string
  longInsulin: string
  /** Carbohydrates in the display unit (XE or grams). */
  carbs: string
  /** The preset picked; null when none, or once carbs were typed by hand - the two are either/or. */
  presetId: number | null
  mealLabel: string | null
  products: Product[]
  notes: string
  noteOpen: boolean
  foodOpen: boolean
}

export const EMPTY_FORM: EntryForm = {
  pinnedAt: null,
  sugar: '',
  shortInsulin: '',
  longInsulin: '',
  carbs: '',
  presetId: null,
  mealLabel: null,
  products: [],
  notes: '',
  noteOpen: false,
  foodOpen: false,
}

/** The form outlives the screen: switch tabs and come back, it's as you left it. */
export const formStore = createStore<EntryForm>(EMPTY_FORM)

/** Something worth saving: a number. A comment alone isn't a record. */
export function isFillable(f: EntryForm): boolean {
  return [f.sugar, f.shortInsulin, f.longInsulin, f.carbs].some((v) => v.trim() !== '')
}

/** Anything entered at all - the "clear" button shows. */
export function isDirty(f: EntryForm): boolean {
  return isFillable(f) || f.presetId != null || f.mealLabel != null || f.notes.trim() !== ''
}

/** An empty form that keeps the picked moment, if any. */
export function cleared(f: EntryForm): EntryForm {
  return { ...EMPTY_FORM, pinnedAt: f.pinnedAt }
}

/** What the form shows and saves: the picked moment, or [now] while none is picked. */
export function shownAt(f: EntryForm, now: LocalDateTime): LocalDateTime {
  return f.pinnedAt ?? now
}

/** Picking the date pins the moment; the time it showed stays. */
export function withDate(f: EntryForm, date: LocalDate, now: LocalDateTime): EntryForm {
  return { ...f, pinnedAt: `${date}T${shownAt(f, now).slice(11, 16)}:00` }
}

/** Picking the time pins the moment; the date it showed stays. */
export function withTime(f: EntryForm, time: string, now: LocalDateTime): EntryForm {
  return { ...f, pinnedAt: `${shownAt(f, now).slice(0, 10)}T${time}:00` }
}

/** A preset fills the carbs from its products and folds the list. */
export function applyPreset(f: EntryForm, preset: SavedPreset, display: CarbDisplay): EntryForm {
  const grams = presetGrams(preset)
  return {
    ...f,
    carbs: decimal(formatCarbs(grams, display)),
    presetId: preset.id,
    mealLabel: preset.name,
    products: preset.products,
    foodOpen: false,
  }
}

/** Carbs typed by hand drop the preset. */
export function typeCarbs(f: EntryForm, text: string): EntryForm {
  return { ...f, carbs: text, presetId: null, mealLabel: null, products: [] }
}

export function presetGrams(preset: { products: Product[] }): number {
  return preset.products.reduce((sum, p) => sum + p.carbsGrams, 0)
}

/** The form as a diary entry at [createdAt]. Typed sugar is a finger check - there's no sensor yet. */
export function toEntry(f: EntryForm, createdAt: LocalDateTime, display: CarbDisplay): DiaryEntry {
  const sugar = parseNumber(f.sugar)
  return newEntry(createdAt, {
    bloodSugar: sugar,
    sugarSource: sugar == null ? null : 'MANUAL',
    carbsGrams: parseCarbs(f.carbs, display),
    mealLabel: f.mealLabel,
    products: f.products,
    shortInsulinDose: parseNumber(f.shortInsulin),
    longInsulinDose: parseNumber(f.longInsulin),
    notes: f.notes.trim(),
  })
}

/** Good morning / afternoon / evening / night, by the hour. */
export function greeting(hour: number): string {
  if (hour >= 5 && hour <= 10) return 'Доброе утро!'
  if (hour >= 11 && hour <= 16) return 'Добрый день!'
  if (hour >= 17 && hour <= 22) return 'Добрый вечер!'
  return 'Доброй ночи!'
}
