import type { GlucoseRange } from '@/diary/settings'

/** Where a sugar reading falls clinically; it tints the value wherever it's shown. */
export type GlucoseLevel = 'veryLow' | 'low' | 'inRange' | 'high' | 'veryHigh'

// Fixed clinical hypoglycaemia thresholds (AGP low / very-low bands): they never move with the
// person's target range, unlike the high side below.
const VERY_LOW_MMOL = 3.0
const LOW_MMOL = 3.9

// How far above the high bound is a warning before it escalates - the width of the AGP "high" zone
// (180-250 mg/dL).
const HIGH_ALERT_MARGIN_MMOL = 3.9

// Absorbs floating-point noise in high + margin at the boundary.
const BOUNDARY_EPSILON_MMOL = 0.001

/** (Ported from the Android app's GlucoseColor.kt.) */
export function glucoseLevel(value: number, range: GlucoseRange): GlucoseLevel {
  if (value < VERY_LOW_MMOL) return 'veryLow'
  if (value < Math.max(range.low, LOW_MMOL)) return 'low'
  if (value <= range.high) return 'inRange'
  if (value <= range.high + HIGH_ALERT_MARGIN_MMOL + BOUNDARY_EPSILON_MMOL) return 'high'
  return 'veryHigh'
}

/** The text colour class of each level (theme colours in index.css). */
export const GLUCOSE_TEXT: Record<GlucoseLevel, string> = {
  veryLow: 'text-sugar-very-low',
  low: 'text-sugar-low',
  inRange: 'text-sugar-in-range',
  high: 'text-sugar-high',
  veryHigh: 'text-sugar-very-high',
}
