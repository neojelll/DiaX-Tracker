import { useEffect, useRef, useState } from 'react'
import { Icon } from '@/components/icons'
import { Button, CircleButton } from '@/components/ui/button'
import { Sheet } from '@/components/ui/sheet'
import type { LocalDate } from '@/diary/time'
import { cn } from '@/lib/utils'

const pad = (n: number) => String(n).padStart(2, '0')
const MONTH = new Intl.DateTimeFormat('ru', { month: 'long', year: 'numeric' })
const WEEKDAYS = ['пн', 'вт', 'ср', 'чт', 'пт', 'сб', 'вс']

/**
 * "Готово", and under it "Сейчас" when the form can go back to following the clock - as on Android.
 */
function SheetActions({ onDone, onNow }: { onDone: () => void; onNow?: () => void }) {
  return (
    <div className="mt-3.5 space-y-[9px]">
      <Button onClick={onDone}>Готово</Button>
      {onNow && (
        <Button variant="secondary" className="w-full" onClick={onNow}>
          Сейчас
        </Button>
      )}
    </div>
  )
}

/** A month to pick a day from, with previous / next month. Days after [today] can't be picked. */
export function DateSheet({
  open,
  value,
  today,
  onDone,
  onNow,
  onClose,
}: {
  open: boolean
  value: LocalDate
  today: LocalDate
  onDone: (date: LocalDate) => void
  onNow?: () => void
  onClose: () => void
}) {
  const [selected, setSelected] = useState(value)
  const [month, setMonth] = useState(value.slice(0, 7))
  // Each opening starts from the form's date.
  const [shownFor, setShownFor] = useState<string | null>(null)
  if (open && shownFor !== value) {
    setShownFor(value)
    setSelected(value)
    setMonth(value.slice(0, 7))
  }
  if (!open && shownFor !== null) setShownFor(null)

  const [y, m] = month.split('-').map(Number)
  const first = new Date(y, m - 1, 1)
  const blanks = (first.getDay() + 6) % 7
  const days = new Date(y, m, 0).getDate()
  const step = (delta: number) => {
    const d = new Date(y, m - 1 + delta, 1)
    setMonth(`${d.getFullYear()}-${pad(d.getMonth() + 1)}`)
  }
  const title = MONTH.format(first).replace(' г.', '')

  return (
    <Sheet open={open} onOpenChange={(o) => !o && onClose()} title="Дата">
      <div className="flex items-center">
        <CircleButton size={28} aria-label="Предыдущий месяц" onClick={() => step(-1)}>
          <Icon name="chevronLeft" size={13} strokeWidth={2.2} />
        </CircleButton>
        <p className="flex-1 text-center text-sheet-title first-letter:uppercase">{title}</p>
        <CircleButton
          size={28}
          aria-label="Следующий месяц"
          disabled={month >= today.slice(0, 7)}
          onClick={() => step(1)}
        >
          <Icon name="chevronRight" size={13} strokeWidth={2.2} />
        </CircleButton>
      </div>
      <div className="mt-3.5 grid grid-cols-7 text-center">
        {WEEKDAYS.map((d) => (
          <span key={d} className="pb-1.5 text-card-label text-text-tertiary">
            {d}
          </span>
        ))}
        {Array.from({ length: blanks }, (_, i) => (
          <span key={`b${i}`} />
        ))}
        {Array.from({ length: days }, (_, i) => {
          const date = `${month}-${pad(i + 1)}`
          const on = date === selected
          return (
            <button
              key={date}
              type="button"
              disabled={date > today}
              aria-pressed={on}
              aria-label={date}
              onClick={() => setSelected(date)}
              className={cn(
                'mx-auto flex size-[38px] items-center justify-center rounded-strip text-body tabular-nums',
                on ? 'bg-ink text-surface' : 'text-ink active:bg-tile disabled:text-placeholder',
              )}
            >
              {i + 1}
            </button>
          )
        })}
      </div>
      <SheetActions onDone={() => onDone(selected)} onNow={onNow} />
    </Sheet>
  )
}

/** Hours 00-23 and minutes 00-55 in steps of 5, the nearest step picked. [value] is "HH:mm". */
export function TimeSheet({
  open,
  value,
  onDone,
  onNow,
  onClose,
}: {
  open: boolean
  value: string
  onDone: (time: string) => void
  onNow?: () => void
  onClose: () => void
}) {
  const [hour, setHour] = useState(0)
  const [minute, setMinute] = useState(0)
  const [shownFor, setShownFor] = useState<string | null>(null)
  if (open && shownFor !== value) {
    setShownFor(value)
    setHour(Number(value.slice(0, 2)))
    setMinute(Math.floor(Number(value.slice(3, 5)) / 5) * 5)
  }
  if (!open && shownFor !== null) setShownFor(null)

  return (
    <Sheet open={open} onOpenChange={(o) => !o && onClose()} title="Время">
      <p className="-mt-2 mb-3 text-display-insulin tabular-nums">
        {pad(hour)}:{pad(minute)}
      </p>
      <div className="flex gap-[9px]">
        <Wheel
          label="Часы"
          values={Array.from({ length: 24 }, (_, i) => i)}
          value={hour}
          onPick={setHour}
          open={open}
        />
        <Wheel
          label="Минуты"
          values={Array.from({ length: 12 }, (_, i) => i * 5)}
          value={minute}
          onPick={setMinute}
          open={open}
        />
      </div>
      <SheetActions onDone={() => onDone(`${pad(hour)}:${pad(minute)}`)} onNow={onNow} />
    </Sheet>
  )
}

function Wheel({
  label,
  values,
  value,
  onPick,
  open,
}: {
  label: string
  values: number[]
  value: number
  onPick: (v: number) => void
  open: boolean
}) {
  const list = useRef<HTMLDivElement>(null)
  // On opening, the picked value is brought into view.
  useEffect(() => {
    if (!open) return
    const frame = requestAnimationFrame(() =>
      list.current?.querySelector('[aria-pressed="true"]')?.scrollIntoView({ block: 'center' }),
    )
    return () => cancelAnimationFrame(frame)
  }, [open])
  return (
    <div className="flex-1">
      <p className="pb-[7px] text-card-label text-text-label">{label}</p>
      <div
        ref={list}
        role="group"
        aria-label={label}
        className="h-[184px] overflow-y-auto overscroll-contain rounded-panel bg-screen p-1.5"
      >
        {values.map((v) => (
          <button
            key={v}
            type="button"
            aria-pressed={v === value}
            onClick={() => onPick(v)}
            className={cn(
              'block w-full rounded-strip py-[9px] text-center text-[14px] tabular-nums',
              v === value ? 'bg-ink text-surface' : 'text-ink active:bg-tile',
            )}
          >
            {pad(v)}
          </button>
        ))}
      </div>
    </div>
  )
}
