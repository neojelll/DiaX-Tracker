import type { LocalDateTime } from '@/diary/time'

export type SugarSource = 'MANUAL' | 'SENSOR'

/** A product of a meal or a preset, carbohydrates in grams. Kept in order inside its owner. */
export interface Product {
  name: string
  carbsGrams: number
}

/**
 * One diary record: any mix of sugar, insulin, food and a note - or an automatic sugar check
 * after a meal. Mirrors the Android `DiaryEntry`, except that a meal's products live inside it
 * (one document, so a product can't outlive its entry).
 */
export interface DiaryEntry {
  /** Assigned by the database. */
  id?: number
  /** mmol/L. */
  bloodSugar: number | null
  sugarSource: SugarSource | null
  /** Carbohydrates of the meal in grams - shown as XE or grams through carbs.ts. */
  carbsGrams: number | null
  /** The preset the meal came from. */
  mealLabel: string | null
  products: Product[]
  shortInsulinDose: number | null
  longInsulinDose: number | null
  notes: string
  createdAt: LocalDateTime
  /** For an automatic sugar check: the meal entry it belongs to. Not enforced - see afterMeal.ts. */
  sourceEntryId: number | null
  /** For an automatic sugar check: which hour after the meal, 1..4. */
  sourceHour: number | null
}

/** An entry as saved by the database - it has its id. */
export type SavedEntry = DiaryEntry & { id: number }

/** A blank entry at [createdAt]; spread the fields that are set over it. */
export function newEntry(createdAt: LocalDateTime, fields: Partial<DiaryEntry> = {}): DiaryEntry {
  return {
    bloodSugar: null,
    sugarSource: null,
    carbsGrams: null,
    mealLabel: null,
    products: [],
    shortInsulinDose: null,
    longInsulinDose: null,
    notes: '',
    createdAt,
    sourceEntryId: null,
    sourceHour: null,
    ...fields,
  }
}

/** Food was logged: a preset or an amount. Sugar checks and insulin-only entries aren't meals. */
export function isMeal(entry: DiaryEntry): boolean {
  return entry.mealLabel != null || entry.carbsGrams != null
}

/** A saved meal: a name, an optional comment and its products. */
export interface MealPreset {
  id?: number
  name: string
  comment: string
  products: Product[]
}

export type SavedPreset = MealPreset & { id: number }
