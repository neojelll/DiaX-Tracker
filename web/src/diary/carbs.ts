import { dayOf, type LocalDate } from '@/diary/time'
import type { DiaryEntry } from '@/diary/types'

/*
 * Carbohydrates are stored in grams - the physical quantity. Bread units (XE) are only a way of
 * showing and typing them; every screen converts through here, none does its own arithmetic.
 */

/** Grams in one XE by default. The Android diary's XE were entered at 10 g. */
export const DEFAULT_GRAMS_PER_XE = 10

export const gramsToXe = (grams: number, gramsPerXe = DEFAULT_GRAMS_PER_XE) => grams / gramsPerXe

export const xeToGrams = (xe: number, gramsPerXe = DEFAULT_GRAMS_PER_XE) => xe * gramsPerXe

/**
 * XE to one decimal without a trailing ".0": "3", "2.5", "2.1". A dot - this is the plain number;
 * the screens show it with the locale's separator.
 */
export function formatXe(grams: number, gramsPerXe = DEFAULT_GRAMS_PER_XE): string {
  const tenths = Math.round(gramsToXe(grams, gramsPerXe) * 10)
  return tenths % 10 === 0 ? String(tenths / 10) : `${Math.trunc(tenths / 10)}.${tenths % 10}`
}

/** A typed number, "2.5" or "2,5"; null if it isn't one. */
export function parseNumber(text: string): number | null {
  const trimmed = text.trim().replace(',', '.')
  if (trimmed === '' || !/^-?\d*\.?\d+$|^-?\d+\.$/.test(trimmed)) return null
  return Number(trimmed)
}

/** Typed XE to grams; null if it isn't a number. */
export function parseXeToGrams(text: string, gramsPerXe = DEFAULT_GRAMS_PER_XE): number | null {
  const xe = parseNumber(text)
  return xe == null ? null : xeToGrams(xe, gramsPerXe)
}

/** The unit carbohydrates are shown and typed in. Storage is always grams. */
export type CarbUnit = 'XE' | 'GRAMS'

/** How carbohydrates are shown and typed, from the settings. */
export interface CarbDisplay {
  unit: CarbUnit
  gramsPerXe: number
}

export const DEFAULT_CARB_DISPLAY: CarbDisplay = { unit: 'XE', gramsPerXe: DEFAULT_GRAMS_PER_XE }

/** A stored amount as the number alone in the display unit: XE to one decimal, grams whole. */
export function formatCarbs(grams: number, display: CarbDisplay): string {
  return display.unit === 'XE' ? formatXe(grams, display.gramsPerXe) : String(Math.round(grams))
}

/** What was typed, in the display unit, as grams; null if it isn't a number. */
export function parseCarbs(text: string, display: CarbDisplay): number | null {
  return display.unit === 'XE' ? parseXeToGrams(text, display.gramsPerXe) : parseNumber(text)
}

/** The carbohydrates an entry counts for: its own total, or - for older records - its products. */
export function entryCarbsGrams(entry: DiaryEntry): number {
  return entry.carbsGrams ?? entry.products.reduce((sum, p) => sum + p.carbsGrams, 0)
}

/** Carbohydrates eaten on [day], in grams. */
export function carbsGramsOn(day: LocalDate, entries: DiaryEntry[]): number {
  return entries.filter((e) => dayOf(e.createdAt) === day).reduce((sum, e) => sum + entryCarbsGrams(e), 0)
}
