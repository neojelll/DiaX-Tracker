// The Android sensor tile's freshness, applied to the last logged sugar.
export const FRESH_MINUTES = 15
const ACTIVE_DAYS = 3

/** "только что" / "N мин назад" / "N ч назад" / "N дн. назад"; null once older than three days. */
export function sugarAge(minutes: number): string | null {
  if (minutes <= FRESH_MINUTES) return 'только что'
  if (minutes < 60) return `${minutes} мин назад`
  if (minutes < 24 * 60) return `${Math.floor(minutes / 60)} ч назад`
  if (minutes <= ACTIVE_DAYS * 24 * 60) return `${Math.floor(minutes / (24 * 60))} дн. назад`
  return null
}
