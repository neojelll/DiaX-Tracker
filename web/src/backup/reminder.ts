import { dayOf, minutesBetween, type LocalDateTime } from '@/diary/time'

/** After this many days without an archive Home reminds to save one. */
export const BACKUP_REMINDER_DAYS = 7

/**
 * Days since the diary was last saved, once that is [BACKUP_REMINDER_DAYS] or more - null while
 * the reminder isn't due. With no archive ever saved it counts from the first record, so a fresh
 * diary isn't nagged on day one but an imported one is reminded right away.
 */
export function backupDueDays(
  lastBackup: LocalDateTime | null,
  firstRecord: LocalDateTime | undefined,
  now: LocalDateTime,
): number | null {
  const from = lastBackup ?? firstRecord
  if (from == null) return null
  const days = Math.round(minutesBetween(`${dayOf(from)}T12:00:00`, `${dayOf(now)}T12:00:00`) / (24 * 60))
  return days >= BACKUP_REMINDER_DAYS ? days : null
}
