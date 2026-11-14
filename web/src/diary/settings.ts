import { DEFAULT_CARB_GOAL, MAX_CARB_GOAL_GRAMS, type CarbGoal } from '@/diary/carbGoal'
import { DEFAULT_CARB_DISPLAY, type CarbDisplay, type CarbUnit } from '@/diary/carbs'

/** The target sugar range, mmol/L. */
export interface GlucoseRange {
  low: number
  high: number
}

/** Everything the person sets in Settings. Bounds and defaults are the Android app's. */
export interface Settings {
  carbDisplay: CarbDisplay
  carbGoal: CarbGoal
  /** How long short-acting insulin acts, hours. */
  insulinDurationHours: number
  glucoseRange: GlucoseRange
}

// 10 and 12 g are the common values; the range leaves room for other conventions.
export const GRAMS_PER_XE_BOUNDS = { min: 8, max: 15, step: 1 }

// Brackets the commonly recommended rapid-acting starting point (~4.5-5 h) rather than the full
// clinical range: too short a duration underestimates insulin on board and invites stacking.
export const INSULIN_DURATION_BOUNDS = { min: 3.5, max: 5.5 }

// The international consensus target range for CGM data (Battelino et al., Diabetes Care 2019); a
// personal target can't cross these fixed clinical thresholds.
export const GLUCOSE_RANGE_BOUNDS = { min: 3.9, max: 15 }

export const DEFAULT_SETTINGS: Settings = {
  carbDisplay: DEFAULT_CARB_DISPLAY,
  carbGoal: DEFAULT_CARB_GOAL,
  // The flat 4 h cutoff the Android active-insulin banner used before it modelled decay.
  insulinDurationHours: 4,
  glucoseRange: { low: 3.9, high: 10 },
}

const clamp = (value: number, min: number, max: number) => Math.min(max, Math.max(min, value))
const num = (value: unknown, fallback: number) =>
  typeof value === 'number' && Number.isFinite(value) ? value : fallback

/**
 * Settings as stored, made safe to use: missing or broken fields fall back to defaults and numbers
 * are pulled into their bounds - a value saved under an older, looser bound can't leak out.
 */
export function normalizeSettings(stored: unknown): Settings {
  const s = (stored ?? {}) as Partial<Record<keyof Settings, Record<string, unknown>>> &
    Partial<Record<'insulinDurationHours', unknown>>
  const unit: CarbUnit = s.carbDisplay?.unit === 'GRAMS' ? 'GRAMS' : 'XE'
  const goalGrams = s.carbGoal?.grams
  return {
    carbDisplay: {
      unit,
      gramsPerXe: clamp(
        num(s.carbDisplay?.gramsPerXe, DEFAULT_CARB_DISPLAY.gramsPerXe),
        GRAMS_PER_XE_BOUNDS.min,
        GRAMS_PER_XE_BOUNDS.max,
      ),
    },
    carbGoal: {
      enabled: s.carbGoal?.enabled === true,
      grams:
        typeof goalGrams === 'number' && Number.isFinite(goalGrams)
          ? clamp(goalGrams, 0, MAX_CARB_GOAL_GRAMS)
          : null,
    },
    insulinDurationHours: clamp(
      num(s.insulinDurationHours, DEFAULT_SETTINGS.insulinDurationHours),
      INSULIN_DURATION_BOUNDS.min,
      INSULIN_DURATION_BOUNDS.max,
    ),
    glucoseRange: {
      low: clamp(
        num(s.glucoseRange?.low, DEFAULT_SETTINGS.glucoseRange.low),
        GLUCOSE_RANGE_BOUNDS.min,
        GLUCOSE_RANGE_BOUNDS.max,
      ),
      high: clamp(
        num(s.glucoseRange?.high, DEFAULT_SETTINGS.glucoseRange.high),
        GLUCOSE_RANGE_BOUNDS.min,
        GLUCOSE_RANGE_BOUNDS.max,
      ),
    },
  }
}
