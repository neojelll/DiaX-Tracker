import { minutesBetween, plusMinutes, type LocalDateTime } from '@/diary/time'
import { isMeal, type SavedEntry } from '@/diary/types'

/** The four sugar checks of one meal, +1h..+4h - the after-meal line of its record. */
export interface AfterMeal {
  /** Sugar at +1h..+4h; null where that check hasn't happened (yet). Always 4 long. */
  values: (number | null)[]
}

export interface AfterMealIndex {
  /** Meal id -> its line. Only meals at least an hour old. */
  byMeal: Map<number, AfterMeal>
  /** Checks shown inside a meal's line, so the feed doesn't list them a second time. */
  hidden: Set<number>
}

const LINE_APPEARS_AFTER_MINUTES = 60
const LEGACY_FIRST_OFFSET_MINUTES = 30
const LEGACY_LAST_OFFSET_MINUTES = 270
const HOURS = 4

/**
 * Each meal owns its own checks: a check records which meal it is for and for which hour, so a
 * meal's line is exactly its own four checks, whatever else was logged around it. Manual readings
 * are not part of it. A meal gets its line an hour after it; checks move into the line and leave
 * the feed. A check whose meal is gone, or is no longer a meal, stays an ordinary row.
 *
 * Checks recorded before the link existed carry no meal; see [legacyChecks] for how those are
 * still placed. (Ported from the Android app's AfterMeal.kt.)
 */
export function buildAfterMeal(entries: SavedEntry[], now: LocalDateTime): AfterMealIndex {
  const checksByMeal = new Map<number, SavedEntry[]>()
  for (const e of entries) {
    if (
      e.sourceEntryId != null &&
      e.sourceHour != null &&
      e.sourceHour >= 1 &&
      e.sourceHour <= HOURS &&
      e.bloodSugar != null
    ) {
      checksByMeal.set(e.sourceEntryId, [...(checksByMeal.get(e.sourceEntryId) ?? []), e])
    }
  }
  const meals = entries.filter(isMeal)
  const legacyByMeal = legacyChecks(entries, meals)

  const byMeal = new Map<number, AfterMeal>()
  const hidden = new Set<number>()
  for (const meal of meals) {
    if (minutesBetween(meal.createdAt, now) < LINE_APPEARS_AFTER_MINUTES) continue
    const checks = checksByMeal.get(meal.id) ?? []
    // Two checks for the same hour can only come from a meal edited and rescheduled: the later wins.
    const byHour = new Map<number, SavedEntry>()
    for (const check of checks) {
      const current = byHour.get(check.sourceHour!)
      if (!current || check.createdAt > current.createdAt) byHour.set(check.sourceHour!, check)
    }
    const legacy = legacyByMeal.get(meal.id) ?? new Map<number, SavedEntry>()
    const values: (number | null)[] = []
    for (let hour = 1; hour <= HOURS; hour++) {
      values.push((byHour.get(hour) ?? legacy.get(hour))?.bloodSugar ?? null)
    }
    byMeal.set(meal.id, { values })
    checks.forEach((c) => hidden.add(c.id))
    for (const [hour, old] of legacy) if (!byHour.has(hour)) hidden.add(old.id)
  }
  return { byMeal, hidden }
}

/**
 * The entries that are records of their own: everything except the checks shown inside a meal's
 * line. The one definition of a record - History, export and import all count by it.
 */
export function feedRecords(entries: SavedEntry[], now: LocalDateTime): SavedEntry[] {
  const { hidden } = buildAfterMeal(entries, now)
  return entries.filter((e) => !hidden.has(e.id))
}

/**
 * Old automatic checks have no stored meal. They are sensor readings with nothing else in them
 * (no food, insulin or note), given to the last meal before them when they come 30 min to 4 h 30
 * after it, in the cell of the nearest whole hour (the closest to the hour if several).
 * Returns meal id -> hour -> entry.
 */
function legacyChecks(entries: SavedEntry[], meals: SavedEntry[]): Map<number, Map<number, SavedEntry>> {
  const sortedMeals = [...meals].sort((a, b) =>
    a.createdAt < b.createdAt ? -1 : a.createdAt > b.createdAt ? 1 : 0,
  )
  const result = new Map<number, Map<number, SavedEntry>>()
  const readings = entries.filter(
    (e) =>
      e.sourceEntryId == null &&
      e.sugarSource === 'SENSOR' &&
      e.bloodSugar != null &&
      !isMeal(e) &&
      e.shortInsulinDose == null &&
      e.longInsulinDose == null &&
      e.notes.trim() === '',
  )
  for (const reading of readings) {
    const latest = plusMinutes(reading.createdAt, -LEGACY_FIRST_OFFSET_MINUTES)
    const meal = sortedMeals.filter((m) => m.createdAt <= latest).at(-1)
    if (!meal) continue
    const elapsed = minutesBetween(meal.createdAt, reading.createdAt)
    if (elapsed >= LEGACY_LAST_OFFSET_MINUTES) continue
    const hour = Math.round(elapsed / 60)
    if (hour < 1 || hour > HOURS) continue
    const cells = result.get(meal.id) ?? new Map<number, SavedEntry>()
    result.set(meal.id, cells)
    const current = cells.get(hour)
    const offCenter = (e: SavedEntry) => Math.abs(minutesBetween(meal.createdAt, e.createdAt) - hour * 60)
    if (!current || offCenter(reading) < offCenter(current)) cells.set(hour, reading)
  }
  return result
}

/**
 * One point of the after-meal line: sugar before the meal ([hour] 0) or at +1h..+4h. A missing
 * reading has [value] null and, while its check is still ahead, the time it's due at.
 */
export interface AfterMealPoint {
  hour: number
  value: number | null
  /** "HH:mm" when the check is still ahead. */
  dueAt: string | null
}

/**
 * What the after-meal line shows: the sugar logged with the meal as the starting point, then the
 * four checks. A check not in yet shows when it's due; one whose time passed without a reading
 * shows nothing.
 */
export function afterMealPoints(
  before: number | null,
  afterMeal: AfterMeal,
  mealAt: LocalDateTime,
  now: LocalDateTime,
): AfterMealPoint[] {
  return [
    { hour: 0, value: before, dueAt: null },
    ...afterMeal.values.map((value, i) => {
      const hour = i + 1
      const due = plusMinutes(mealAt, hour * 60)
      return { hour, value, dueAt: value == null && due > now ? due.slice(11, 16) : null }
    }),
  ]
}

/** Index of the highest check after the meal - the starting point doesn't count; null with none in. */
export function afterMealPeak(points: AfterMealPoint[]): number | null {
  let peak: number | null = null
  points.forEach((p, i) => {
    if (p.hour > 0 && p.value != null && (peak == null || p.value > points[peak].value!)) peak = i
  })
  return peak
}
