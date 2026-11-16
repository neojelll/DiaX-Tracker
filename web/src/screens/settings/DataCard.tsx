import { useLiveQuery } from 'dexie-react-hooks'
import { useRef, useState } from 'react'
import { Icon, type IconName } from '@/components/icons'
import { Button } from '@/components/ui/button'
import { Sheet } from '@/components/ui/sheet'
import { Card } from '@/components/ui/surface'
import { readArchive } from '@/backup/archive'
import { BackupError, type DiaryArchive } from '@/backup/types'
import { useArchiveExport } from '@/backup/useArchiveExport'
import { feedRecords } from '@/diary/afterMeal'
import { diary } from '@/diary/repository'
import { nowLocal } from '@/diary/time'
import { t } from '@/i18n'
import { dateWithYear, moment } from '@/lib/format'
import { useToast } from '@/lib/toast'

/** A read archive with what the sheet shows about it. */
interface Pending {
  archive: DiaryArchive
  /** Records as History counts them - a meal's checks come with it. */
  records: number
  /** Records already in the diary, which restoring will replace. */
  existing: number
}

/**
 * The Данные card: saving the whole diary as an archive, and restoring one - the PWA's own or the
 * Android app's. An archive is read first and shown - how many records, from when to when - and
 * only "Восстановить" changes the diary. A diary that already has records is replaced, never
 * merged, and the sheet says so.
 */
export function DataCard() {
  const toast = useToast()
  const input = useRef<HTMLInputElement>(null)
  const [reading, setReading] = useState(false)
  const [pending, setPending] = useState<Pending | null>(null)
  const exporter = useArchiveExport()
  const lastBackup = useLiveQuery(() => diary.lastBackup(), [])

  const pick = async (file: File) => {
    setReading(true)
    try {
      const archive = await readArchive(file)
      const records = feedRecords(archive.entries, nowLocal()).length
      // Counted the same way, so "уже есть записи (N)" matches what History shows.
      const existing = feedRecords(await diary.allEntries(), nowLocal()).length
      setPending({ archive, records, existing })
    } catch (e) {
      const s = t().settings
      const problem = e instanceof BackupError ? e.problem : null
      if (problem === 'old-version')
        toast.show(s.importOldVersion, { kind: 'error', sub: s.importOldVersionSub })
      else if (problem === 'newer-version')
        toast.show(s.importNewerVersion, { kind: 'error', sub: s.importNewerVersionSub })
      else if (problem === 'not-backup')
        toast.show(s.importNotBackup, { kind: 'error', sub: s.importNotBackupSub })
      else toast.show(s.importFailed, { kind: 'error' })
    } finally {
      setReading(false)
    }
  }

  const confirm = async ({ archive, records }: Pending) => {
    try {
      await diary.replaceDiary(archive)
      setPending(null)
      toast.show(t().settings.imported(records), {
        sub: archive.settings ? undefined : t().settings.importedSub,
      })
    } catch {
      setPending(null)
      toast.show(t().settings.importFailed, { kind: 'error' })
    }
  }

  return (
    <Card>
      <h2 className="text-label text-text-label">{t().settings.data}</h2>
      <div className="mt-[11px] space-y-2">
        <DataRow
          icon="upload"
          title={t().settings.save}
          hint={
            exporter.busy
              ? t().settings.preparing
              : lastBackup
                ? t().settings.lastSaved(moment(lastBackup))
                : t().settings.neverSaved
          }
          disabled={exporter.busy}
          onClick={() => void exporter.start()}
        />
        <DataRow
          icon="download"
          title={t().settings.restore}
          hint={reading ? t().settings.importReading : t().settings.restoreHint}
          disabled={reading}
          onClick={() => input.current?.click()}
        />
      </div>
      <input
        ref={input}
        type="file"
        accept=".zip,application/zip"
        aria-label={t().settings.restore}
        className="hidden"
        onChange={(e) => {
          const file = e.target.files?.[0]
          // Cleared so the same file can be picked again after a cancel.
          e.target.value = ''
          if (file) void pick(file)
        }}
      />
      <p className="mt-2.5 text-hint text-text-tertiary">{t().settings.dataHint}</p>

      {exporter.sheet}
      <RestoreSheet pending={pending} onConfirm={confirm} onClose={() => setPending(null)} />
    </Card>
  )
}

function DataRow({
  icon,
  title,
  hint,
  disabled,
  onClick,
}: {
  icon: IconName
  title: string
  hint: string
  disabled: boolean
  onClick: () => void
}) {
  return (
    <button
      type="button"
      disabled={disabled}
      onClick={onClick}
      className="flex w-full items-center gap-3 rounded-field bg-tile px-3.5 py-3 text-left active:bg-tile-hover disabled:opacity-60"
    >
      <Icon name={icon} size={18} strokeWidth={1.9} className="shrink-0 text-text-secondary" />
      <span className="min-w-0 flex-1">
        <span className="block text-body">{title}</span>
        <span className="mt-0.5 block truncate text-hint text-text-tertiary">{hint}</span>
      </span>
      <Icon name="chevronRight" size={16} strokeWidth={2} className="shrink-0 text-text-tertiary" />
    </button>
  )
}

function RestoreSheet({
  pending,
  onConfirm,
  onClose,
}: {
  pending: Pending | null
  onConfirm: (p: Pending) => Promise<void>
  onClose: () => void
}) {
  // While sliding out it keeps showing the archive it had.
  const [last, setLast] = useState(pending)
  if (pending && pending !== last) setLast(pending)
  const shown = pending ?? last
  const [busy, setBusy] = useState(false)

  const entries = shown?.archive.entries ?? []
  const first = entries[0]?.createdAt
  const lastAt = entries.at(-1)?.createdAt
  const rows: [string, string][] = shown
    ? [
        [t().settings.importRecords, String(shown.records)],
        ...(first && lastAt
          ? [
              [t().settings.importPeriod, `${dateWithYear(first)} — ${dateWithYear(lastAt)}`] as [
                string,
                string,
              ],
            ]
          : []),
        [t().settings.importPresets, String(shown.archive.presets.length)],
        [t().settings.importPhotos, String(shown.archive.photos.size)],
      ]
    : []
  const android = shown?.archive.source === 'android'

  return (
    <Sheet
      open={pending != null}
      onOpenChange={(o) => !o && onClose()}
      title={android ? t().settings.importTitle : t().settings.restoreTitle}
    >
      {shown && (
        <>
          <dl className="divide-y divide-divider rounded-tile bg-tile px-3.5">
            {rows.map(([label, value]) => (
              <div key={label} className="flex gap-3 py-[11px] text-body">
                <dt className="flex-1 text-text-secondary">{label}</dt>
                <dd className="text-right tabular-nums">{value}</dd>
              </div>
            ))}
          </dl>
          {shown.existing > 0 && (
            <p className="mt-3 flex gap-2 rounded-field border border-border px-3 py-2.5 text-note">
              <Icon name="alertTriangle" size={14} strokeWidth={2} className="mt-0.5 shrink-0" />
              {t().settings.importReplaceWarning(shown.existing)}
            </p>
          )}
          <p className="mt-3 px-1 text-hint text-text-tertiary">
            {android ? t().settings.importSettingsNote : t().settings.restoreSettingsNote}
          </p>
          <Button
            className="mt-4"
            disabled={busy}
            onClick={() => {
              setBusy(true)
              void onConfirm(shown).finally(() => setBusy(false))
            }}
          >
            {shown.existing > 0 ? t().settings.importReplace : t().settings.importStart}
          </Button>
        </>
      )}
    </Sheet>
  )
}
