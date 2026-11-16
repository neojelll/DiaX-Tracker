import { formatCarbs, parseCarbs, parseNumber, type CarbDisplay } from '@/diary/carbs'
import type { LocalDate, LocalDateTime } from '@/diary/time'
import { newEntry, type DiaryEntry, type Product, type SavedEntry, type SavedPreset } from '@/diary/types'
import { t } from '@/i18n'
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

const typed = (value: number | null) => (value == null ? '' : decimal(String(value)))

/** A saved entry as the form shows it, for editing: its moment pinned, numbers as text. */
export function fromEntry(entry: SavedEntry, display: CarbDisplay): EntryForm {
  return {
    ...EMPTY_FORM,
    pinnedAt: entry.createdAt,
    sugar: typed(entry.bloodSugar),
    shortInsulin: typed(entry.shortInsulinDose),
    longInsulin: typed(entry.longInsulinDose),
    carbs: entry.carbsGrams == null ? '' : decimal(formatCarbs(entry.carbsGrams, display)),
    mealLabel: entry.mealLabel,
    products: entry.products,
    notes: entry.notes,
  }
}

/**
 * The entry with the form's changes. What the form didn't touch keeps its exact stored value - the
 * carbs aren't rounded through XE, the sugar keeps its source - and a meal's checks stay its own.
 */
export function editedEntry(entry: SavedEntry, f: EntryForm, display: CarbDisplay): SavedEntry {
  const before = fromEntry(entry, display)
  const changed = toEntry(f, f.pinnedAt ?? entry.createdAt, display)
  return {
    ...entry,
    createdAt: changed.createdAt,
    bloodSugar: f.sugar === before.sugar ? entry.bloodSugar : changed.bloodSugar,
    sugarSource: f.sugar === before.sugar ? entry.sugarSource : changed.sugarSource,
    carbsGrams: f.carbs === before.carbs ? entry.carbsGrams : changed.carbsGrams,
    mealLabel: changed.mealLabel,
    products: changed.products,
    shortInsulinDose: changed.shortInsulinDose,
    longInsulinDose: changed.longInsulinDose,
    notes: changed.notes,
  }
}

/** Good morning / afternoon / evening / night, by the hour. */
export function greeting(hour: number): string {
  const g = t().home.greeting
  if (hour >= 5 && hour <= 10) return g.morning
  if (hour >= 11 && hour <= 16) return g.afternoon
  if (hour >= 17 && hour <= 22) return g.evening
  return g.night
}
