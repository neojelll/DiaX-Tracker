import { useState } from 'react'
import { Icon, InsulinIcon } from '@/components/icons'
import { activeInsulin, insulinProgress } from '@/diary/insulin'
import type { LocalDateTime } from '@/diary/time'
import type { DiaryEntry } from '@/diary/types'
import { duration, hhmm } from '@/lib/format'

/**
 * Short insulin still acting, above every tab: a thin ink strip with what's left; a tap opens the
 * card with the amount, a bar of the time left and when the last dose was. Nothing once worn off.
 */
export function InsulinBanner({
  entries,
  durationHours,
  now,
}: {
  entries: DiaryEntry[]
  durationHours: number
  now: LocalDateTime
}) {
  const [open, setOpen] = useState(false)
  const iob = activeInsulin(entries, now, durationHours)
  if (!iob) return null
  const units = iob.units.toFixed(1).replace('.', ',')
  const left = `осталось ${duration(iob.minutesLeft)}`

  return (
    <div className="px-3.5 pt-2">
      <button
        type="button"
        aria-expanded={open}
        onClick={() => setOpen((o) => !o)}
        className="block w-full bg-ink text-left text-surface transition-[border-radius] duration-300"
        style={{ borderRadius: open ? 18 : 12 }}
      >
        {open ? (
          <span className="block px-[15px] pt-[13px] pb-3.5">
            <span className="flex items-center gap-[9px]">
              <span className="flex size-[26px] items-center justify-center rounded-full bg-ink-soft">
                <InsulinIcon kind="short" size={13} />
              </span>
              <span className="flex-1 text-body">Инсулин ещё действует</span>
              <span className="flex size-[26px] items-center justify-center rounded-full bg-ink-soft">
                <Icon name="chevronUp" size={12} strokeWidth={2.2} />
              </span>
            </span>
            <span className="mt-[11px] flex items-baseline gap-[5px]">
              <span className="text-display-insulin tabular-nums">{units}</span>
              <span className="text-card-label text-text-on-dark">ед активны</span>
            </span>
            <span className="mt-2.5 block h-[3px] overflow-hidden rounded-full bg-track-on-dark">
              <span
                className="block h-full rounded-full bg-surface"
                style={{ width: `${insulinProgress(iob) * 100}%` }}
              />
            </span>
            <span className="mt-2 flex justify-between text-card-label text-text-on-dark">
              <span>с записи {hhmm(iob.fromTime)}</span>
              <span>{left}</span>
            </span>
          </span>
        ) : (
          <span className="flex items-center px-3 py-[7px]">
            <InsulinIcon kind="short" size={12} />
            <span className="ml-2 text-[12px] leading-4 tabular-nums">{units}</span>
            <span className="ml-[3px] text-unit text-text-on-dark">ед активны</span>
            <span className="ml-auto text-card-label text-text-on-dark">{left}</span>
            <Icon name="chevronDown" size={11} strokeWidth={2.2} className="ml-1.5 text-text-on-dark" />
          </span>
        )}
      </button>
    </div>
  )
}
