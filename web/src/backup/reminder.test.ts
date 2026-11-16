import { describe, expect, it } from 'vitest'
import { backupDueDays } from '@/backup/reminder'

const now = '2026-11-16T09:00:00'

describe('backupDueDays', () => {
  it('is due a week after the last archive, counted in calendar days', () => {
    expect(backupDueDays('2026-11-10T23:59:00', undefined, now)).toBeNull()
    expect(backupDueDays('2026-11-09T23:59:00', undefined, now)).toBe(7)
    expect(backupDueDays('2026-10-16T08:00:00', '2025-01-01T08:00:00', now)).toBe(31)
  })

  it('counts from the first record when nothing was ever saved', () => {
    expect(backupDueDays(null, '2026-11-14T08:00:00', now)).toBeNull()
    expect(backupDueDays(null, '2025-02-14T22:05:00', now)).toBe(640)
    expect(backupDueDays(null, undefined, now)).toBeNull()
  })
})
