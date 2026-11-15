import type { AfterMeal } from '@/diary/afterMeal'
import { isMeal, type DiaryEntry, type SavedEntry } from '@/diary/types'
import { dayOf, plusDays, type LocalDate } from '@/diary/time'
import { oneDecimal } from '@/lib/format'

/** Which days History shows. */
export type DayFilter =
  | { kind: 'all' }
  | { kind: 'yesterday' }
  | { kind: 'week' }
  | { kind: 'month' }
  | { kind: 'range'; from: LocalDate; to: LocalDate }

export function inFilter(entry: DiaryEntry, filter: DayFilter, today: LocalDate): boolean {
  const day = dayOf(entry.createdAt)
  switch (filter.kind) {
    case 'all':
      return true
    case 'yesterday':
      return day === plusDays(today, -1)
    case 'week':
      return day >= plusDays(today, -6)
    case 'month':
      return day >= plusDays(today, -29)
    case 'range':
      return day >= filter.from && day <= filter.to
  }
}

/** The search matches the note, the preset, a product or the sugar as shown ("6,4"). */
export function matchesSearch(entry: DiaryEntry, query: string): boolean {
  const q = query.trim().toLocaleLowerCase('ru')
  if (q === '') return true
  const has = (text: string | null) => text != null && text.toLocaleLowerCase('ru').includes(q)
  return (
    has(entry.notes) ||
    has(entry.mealLabel) ||
    entry.products.some((p) => has(p.name)) ||
    (entry.bloodSugar != null &&
      (oneDecimal(entry.bloodSugar).includes(q) || String(entry.bloodSugar).includes(q)))
  )
}

/** Records grouped by day, in the order given. */
export function byDay(records: SavedEntry[]): { day: LocalDate; records: SavedEntry[] }[] {
  const days: { day: LocalDate; records: SavedEntry[] }[] = []
  for (const r of records) {
    const day = dayOf(r.createdAt)
    const last = days.at(-1)
    if (last?.day === day) last.records.push(r)
    else days.push({ day, records: [r] })
  }
  return days
}

/** The word under a record's time. Food wins: the insulin with it is that meal's dose. */
export function recordKind(entry: DiaryEntry): 'еда' | 'укол' | 'замер' {
  if (isMeal(entry)) return 'еда'
  if (entry.shortInsulinDose != null || entry.longInsulinDose != null) return 'укол'
  return 'замер'
}

/**
 * The after-meal line worth showing: only once a check is in. Without a sensor nothing logs the
 * checks by itself, so the due times of checks that will never come would be noise.
 */
export function shownAfterMeal(afterMeal: AfterMeal | undefined): AfterMeal | undefined {
  return afterMeal?.values.some((v) => v != null) ? afterMeal : undefined
}

/** Whether opening the record shows anything; without it a tap goes straight to editing. */
export function hasMoreInside(entry: DiaryEntry, afterMeal: AfterMeal | undefined): boolean {
  return entry.mealLabel != null || entry.notes.trim() !== '' || afterMeal != null
}

/** "Сегодня", "Вчера", "12 ноября". */
export function dayLabel(day: LocalDate, today: LocalDate): string {
  if (day === today) return 'Сегодня'
  if (day === plusDays(today, -1)) return 'Вчера'
  const [y, m, d] = day.split('-').map(Number)
  return new Intl.DateTimeFormat('ru', { day: 'numeric', month: 'long' }).format(new Date(y, m - 1, d))
}
