import { useLiveQuery } from 'dexie-react-hooks'
import { Icon } from '@/components/icons'
import { backupDueDays } from '@/backup/reminder'
import { useArchiveExport } from '@/backup/useArchiveExport'
import { diary } from '@/diary/repository'
import type { LocalDateTime } from '@/diary/time'
import type { SavedEntry } from '@/diary/types'
import { t } from '@/i18n'

/**
 * A quiet line on Home when no archive was saved for a week: the diary lives only in this browser,
 * so this is the safety net. "Сохранить" makes the archive right here.
 */
export function BackupReminder({ entries, now }: { entries: SavedEntry[]; now: LocalDateTime }) {
  const lastBackup = useLiveQuery(() => diary.lastBackup(), [], undefined)
  const exporter = useArchiveExport()
  // undefined: not read yet. Entries come newest first, so the first record is the last one.
  const days = lastBackup === undefined ? null : backupDueDays(lastBackup, entries.at(-1)?.createdAt, now)
  if (days == null) return exporter.sheet

  return (
    <div className="mt-2 flex items-center gap-3 rounded-card bg-surface py-2.5 pr-2.5 pl-4 shadow-soft">
      <Icon name="alertTriangle" size={16} strokeWidth={1.9} className="shrink-0 text-text-label" />
      <span className="min-w-0 flex-1">
        <span className="block text-body">
          {lastBackup == null ? t().backupReminder.never : t().backupReminder.days(days)}
        </span>
        <span className="block text-hint text-text-tertiary">{t().backupReminder.hint}</span>
      </span>
      <button
        type="button"
        disabled={exporter.busy}
        onClick={() => void exporter.start()}
        className="shrink-0 rounded-full bg-ink px-3.5 py-2 text-[13px] leading-[18px] text-surface active:bg-ink-hover disabled:opacity-60"
      >
        {t().backupReminder.save}
      </button>
      {exporter.sheet}
    </div>
  )
}
