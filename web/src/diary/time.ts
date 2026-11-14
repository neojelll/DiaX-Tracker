/*
 * Time in the diary is wall-clock time where the person is, with no zone - the same as the Android
 * app's LocalDateTime, and stored the same way: an ISO string "2026-10-21T14:30:00". Strings of
 * this shape sort in time order, so they index well in IndexedDB, and an Android backup's
 * timestamps carry over as they are.
 */

/** A local date-time, "YYYY-MM-DDTHH:mm:ss". */
export type LocalDateTime = string
/** A local date, "YYYY-MM-DD". */
export type LocalDate = string

let source: () => Date = () => new Date()

/**
 * The one clock the app reads - the AppClock of the Android app. Tests fix it with [setClock] so
 * "today", greetings and due times are the same on every run.
 */
export const clock = {
  now: (): Date => source(),
}

export function setClock(now: Date | (() => Date)) {
  source = typeof now === 'function' ? now : () => new Date(now)
}

export function resetClock() {
  source = () => new Date()
}

const pad = (n: number, width = 2) => String(n).padStart(width, '0')

/** A Date as local date-time text, to the second. */
export function toLocalDateTime(date: Date): LocalDateTime {
  return (
    `${pad(date.getFullYear(), 4)}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}` +
    `T${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
  )
}

/** Local date-time text (seconds optional, fractions ignored) as a Date in the local zone. */
export function fromLocalDateTime(text: LocalDateTime): Date {
  const m = /^(\d{4})-(\d{2})-(\d{2})T(\d{2}):(\d{2})(?::(\d{2}))?/.exec(text)
  if (!m) throw new Error(`Not a local date-time: ${text}`)
  const [, y, mo, d, h, mi, s] = m
  return new Date(Number(y), Number(mo) - 1, Number(d), Number(h), Number(mi), Number(s ?? 0))
}

/** The day a local date-time falls on. */
export function dayOf(dateTime: LocalDateTime): LocalDate {
  return dateTime.slice(0, 10)
}

export function nowLocal(): LocalDateTime {
  return toLocalDateTime(clock.now())
}

export function today(): LocalDate {
  return dayOf(nowLocal())
}

/** [dateTime] moved by [minutes] (negative goes back). */
export function plusMinutes(dateTime: LocalDateTime, minutes: number): LocalDateTime {
  const date = fromLocalDateTime(dateTime)
  date.setMinutes(date.getMinutes() + minutes)
  return toLocalDateTime(date)
}

/** [date] moved by [days] (negative goes back). */
export function plusDays(date: LocalDate, days: number): LocalDate {
  const d = fromLocalDateTime(`${date}T12:00`)
  d.setDate(d.getDate() + days)
  return dayOf(toLocalDateTime(d))
}

/** Whole minutes from [from] to [to]; negative when [to] is earlier. */
export function minutesBetween(from: LocalDateTime, to: LocalDateTime): number {
  const ms = fromLocalDateTime(to).getTime() - fromLocalDateTime(from).getTime()
  return Math.trunc(ms / 60_000)
}
