import { dayOf, minutesBetween, type LocalDate, type LocalDateTime } from '@/diary/time'
import type { DiaryEntry } from '@/diary/types'

// Peak-to-duration ratio, not an absolute peak time: AndroidAPS's "rapid-acting" curve pairs a
// 75 min peak with a 300 min (5 h) duration. Scaling the peak with whatever duration is configured
// keeps the curve's shape identical to that validated ratio at every duration.
const PEAK_RATIO = 75 / 300

/** Insulin still acting: how much, until when, from which dose. */
export interface InsulinOnBoard {
  units: number
  /** Minutes until the most recent active dose has fully worn off. */
  minutesLeft: number
  /** When the most recent active dose was taken. */
  fromTime: LocalDateTime
  durationMinutes: number
}

/** How much of the action is still ahead, 0..1. */
export function insulinProgress(iob: InsulinOnBoard): number {
  return Math.min(1, Math.max(0, iob.minutesLeft / iob.durationMinutes))
}

/**
 * Fraction of a single dose still on board [elapsedMinutes] after it was taken, using the
 * "scalable exponential" model (Maksimovic; used by Loop/AndroidAPS/OpenAPS) rather than a flat
 * cutoff: activity peaks at [peakMinutes] and both activity and IOB reach exactly zero at
 * [durationMinutes]. A peak at exactly half the duration divides by zero - never pass one.
 */
export function insulinOnBoardFraction(
  elapsedMinutes: number,
  peakMinutes: number,
  durationMinutes: number,
): number {
  if (elapsedMinutes <= 0) return 1
  if (elapsedMinutes >= durationMinutes) return 0
  const tau = (peakMinutes * (1 - peakMinutes / durationMinutes)) / (1 - (2 * peakMinutes) / durationMinutes)
  const a = (2 * tau) / durationMinutes
  const s = 1 / (1 - a + (1 + a) * Math.exp(-durationMinutes / tau))
  const t = elapsedMinutes
  const fraction =
    1 - s * (1 - a) * ((t ** 2 / (tau * durationMinutes * (1 - a)) - t / tau - 1) * Math.exp(-t / tau) + 1)
  return Math.min(1, Math.max(0, fraction))
}

/** Short-acting insulin still on board at [now]: every dose taken within [durationHours], summed. */
export function activeInsulin(
  entries: DiaryEntry[],
  now: LocalDateTime,
  durationHours: number,
): InsulinOnBoard | null {
  const durationMinutes = durationHours * 60
  const active = entries.flatMap((entry) => {
    const dose = entry.shortInsulinDose
    if (dose == null) return []
    const elapsed = minutesBetween(entry.createdAt, now)
    if (elapsed < 0 || elapsed >= durationMinutes) return []
    return [
      {
        at: entry.createdAt,
        onBoard: dose * insulinOnBoardFraction(elapsed, durationMinutes * PEAK_RATIO, durationMinutes),
        left: durationMinutes - elapsed,
      },
    ]
  })
  if (active.length === 0) return null
  return {
    units: active.reduce((sum, d) => sum + d.onBoard, 0),
    minutesLeft: Math.max(...active.map((d) => d.left)),
    fromTime: active.map((d) => d.at).reduce((a, b) => (b > a ? b : a)),
    durationMinutes,
  }
}

/**
 * Insulin logged on a day, short- and long-acting kept apart: they do different jobs, so a combined
 * total would hide whether it's 14 + 10 or 4 + 20. [long] is null when no long-acting dose was
 * logged that day.
 */
export interface DayInsulin {
  short: number
  long: number | null
}

export function insulinOn(day: LocalDate, entries: DiaryEntry[]): DayInsulin {
  const ofDay = entries.filter((e) => dayOf(e.createdAt) === day)
  const longDoses = ofDay.flatMap((e) => (e.longInsulinDose == null ? [] : [e.longInsulinDose]))
  return {
    short: ofDay.reduce((sum, e) => sum + (e.shortInsulinDose ?? 0), 0),
    long: longDoses.length === 0 ? null : longDoses.reduce((a, b) => a + b, 0),
  }
}
