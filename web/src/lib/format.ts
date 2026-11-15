import type { CarbDisplay } from '@/diary/carbs'
import { fromLocalDateTime, today, dayOf, type LocalDateTime } from '@/diary/time'
import { t } from '@/i18n'

/** A decimal with the language's one separator: "6,4" in Russian, "6.4" in English (#221). */
export function decimal(text: string): string {
  return text.replace('.', t().decimal)
}

/** One decimal without a trailing zero: "6,4", "7". */
export function oneDecimal(value: number): string {
  const rounded = Math.round(value * 10) / 10
  return decimal(Number.isInteger(rounded) ? String(rounded) : rounded.toFixed(1))
}

/** Always one decimal: sugar - "6,0". */
export function fixedOne(value: number): string {
  return decimal(value.toFixed(1))
}

/** "ХЕ" / "г" in Russian, "BU" / "g" in English. */
export function carbUnit(display: CarbDisplay): string {
  return display.unit === 'XE' ? t().units.xe : t().units.grams
}

/** "09:41". */
export function hhmm(at: LocalDateTime): string {
  return at.slice(11, 16)
}

/** "15 нояб." / "15 Nov". */
export function shortDate(at: LocalDateTime): string {
  return new Intl.DateTimeFormat(t().locale, { day: 'numeric', month: 'short' }).format(fromLocalDateTime(at))
}

/** "Сегодня, 09:41" for today, "10 нояб., 13:20" otherwise - the second line of a toast. */
export function moment(at: LocalDateTime): string {
  return dayOf(at) === today() ? t().time.todayAt(hhmm(at)) : `${shortDate(at)}, ${hhmm(at)}`
}

/** "1 ч 20 мин" / "1h 20m". */
export function duration(minutes: number): string {
  return t().time.duration(Math.floor(minutes / 60), Math.round(minutes % 60))
}
