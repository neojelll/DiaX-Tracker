import { fromLocalDateTime, today, dayOf, type LocalDateTime } from '@/diary/time'

/** A decimal as the app shows it - with a comma, the Russian way: "6,4". */
export function decimal(text: string): string {
  return text.replace('.', ',')
}

/** One decimal without a trailing ",0": "6,4", "7". */
export function oneDecimal(value: number): string {
  const rounded = Math.round(value * 10) / 10
  return decimal(Number.isInteger(rounded) ? String(rounded) : rounded.toFixed(1))
}

/** "09:41". */
export function hhmm(at: LocalDateTime): string {
  return at.slice(11, 16)
}

const SHORT_DATE = new Intl.DateTimeFormat('ru', { day: 'numeric', month: 'short' })

/** "15 нояб.". */
export function shortDate(at: LocalDateTime): string {
  return SHORT_DATE.format(fromLocalDateTime(at))
}

/** "Сегодня, 09:41" for today, "10 нояб., 13:20" otherwise - the second line of a toast. */
export function moment(at: LocalDateTime): string {
  return dayOf(at) === today() ? `Сегодня, ${hhmm(at)}` : `${shortDate(at)}, ${hhmm(at)}`
}

/** "1 ч 20 мин" / "45 мин". */
export function duration(minutes: number): string {
  const h = Math.floor(minutes / 60)
  const m = Math.round(minutes % 60)
  return h > 0 ? `${h} ч ${m} мин` : `${m} мин`
}
