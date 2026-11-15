import { t } from '@/i18n'

// The Android sensor tile's freshness, applied to the last logged sugar.
export const FRESH_MINUTES = 15
const ACTIVE_DAYS = 3

/** "только что" / "N мин назад" / "N ч назад" / "N дн. назад"; null once older than three days. */
export function sugarAge(minutes: number): string | null {
  const s = t().summary
  if (minutes <= FRESH_MINUTES) return s.justNow
  if (minutes < 60) return s.minutesAgo(minutes)
  if (minutes < 24 * 60) return s.hoursAgo(Math.floor(minutes / 60))
  if (minutes <= ACTIVE_DAYS * 24 * 60) return s.daysAgo(Math.floor(minutes / (24 * 60)))
  return null
}
