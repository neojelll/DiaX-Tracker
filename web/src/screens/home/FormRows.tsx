import type { ReactNode } from 'react'
import { Icon, type IconName } from '@/components/icons'
import { formatCarbs, type CarbDisplay } from '@/diary/carbs'
import type { SavedPreset } from '@/diary/types'
import { decimal } from '@/lib/format'
import { numeric } from '@/lib/numeric'
import { cn } from '@/lib/utils'
import { NOTES_MAX_LENGTH, presetGrams } from '@/screens/home/entryForm'

/*
 * The form's "Еда" and "Комментарий" rows (Android HomeFoodDetails.kt). Grey only behind fields
 * where numbers are typed; picking a preset and the comment are rows on white: an icon square and
 * a small label over the value. The square is grey while empty, ink once filled.
 */

function PlateRow({
  icon,
  filled,
  label,
  value,
  onClick,
  expanded,
  trailing,
}: {
  icon: IconName
  filled: boolean
  label: string
  value: string
  onClick: () => void
  expanded: boolean
  trailing?: ReactNode
}) {
  return (
    <button
      type="button"
      aria-expanded={expanded}
      onClick={onClick}
      className="-ml-1.5 flex h-[46px] min-w-0 flex-1 items-center rounded-field pr-2.5 pl-1.5 text-left active:bg-tile"
    >
      <span
        className={cn(
          'flex size-[34px] shrink-0 items-center justify-center rounded-[11px] transition-colors',
          filled ? 'bg-ink text-surface' : 'bg-tile text-ink',
        )}
      >
        <Icon name={icon} size={16} />
      </span>
      <span className="ml-2.5 min-w-0 flex-1">
        <span className="block text-card-label text-text-tertiary">{label}</span>
        <span
          className={cn(
            'mt-px block truncate text-[13.5px] leading-[18px] font-medium',
            filled ? 'text-ink' : 'text-text-tertiary',
          )}
        >
          {value}
        </span>
      </span>
      {trailing}
    </button>
  )
}

/**
 * "Еда": picking a preset on the left, the grey carbs field on the right. A preset fills the carbs;
 * typing them drops the preset ("Без пресета"). The list unfolds inside the card.
 */
export function FoodRow({
  presets,
  presetId,
  mealLabel = null,
  carbs,
  display,
  open,
  onToggle,
  onPick,
  onCarbs,
}: {
  presets: SavedPreset[]
  presetId: number | null
  /** The preset name an edited entry was saved with - the preset itself may be gone. */
  mealLabel?: string | null
  carbs: string
  display: CarbDisplay
  open: boolean
  onToggle: () => void
  onPick: (preset: SavedPreset) => void
  onCarbs: (text: string) => void
}) {
  const selected = presets.find((p) => p.id === presetId)
  const filled = selected != null || mealLabel != null || carbs.trim() !== ''
  const title = selected?.name ?? mealLabel ?? (carbs.trim() !== '' ? 'Без пресета' : 'Выбрать пресет')
  const unit = display.unit === 'XE' ? 'ХЕ' : 'г'
  return (
    <div>
      <div className="flex items-center gap-1.5">
        <PlateRow
          icon="dish"
          filled={filled}
          label="Еда"
          value={title}
          onClick={onToggle}
          expanded={open}
          trailing={
            <Icon
              name="chevronDown"
              size={13}
              strokeWidth={2.2}
              className={cn('shrink-0 transition-transform duration-200', open && 'rotate-180')}
            />
          }
        />
        <label className="flex h-[46px] w-[76px] shrink-0 flex-col justify-center rounded-field bg-tile px-3">
          <span className="text-card-label text-text-tertiary">{unit}</span>
          <input
            inputMode="decimal"
            aria-label={display.unit === 'XE' ? 'Углеводы, ХЕ' : 'Углеводы, г'}
            placeholder="0"
            value={carbs}
            onChange={(e) => onCarbs(numeric(e.target.value))}
            // 16px, or iOS zooms in on focus; the design's 15 is close enough.
            className="w-full bg-transparent text-base leading-[18px] font-medium tabular-nums caret-ink outline-none placeholder:text-[#b3aea7]"
          />
        </label>
      </div>
      {open && (
        <div className="mt-1.5 animate-fade-in rounded-tile border border-preset-cube-border bg-surface p-1 shadow-[0_8px_26px_rgb(13_13_13/0.08)]">
          {presets.map((p) => {
            const on = p.id === presetId
            return (
              <button
                key={p.id}
                type="button"
                aria-pressed={on}
                onClick={() => onPick(p)}
                className={cn(
                  'flex w-full items-center rounded-strip py-2 pr-2.5 pl-2 text-left',
                  on ? 'bg-screen' : 'active:bg-screen',
                )}
              >
                <span
                  aria-hidden="true"
                  className={cn(
                    'flex size-[30px] shrink-0 items-center justify-center rounded-[10px] text-[12.5px] font-medium',
                    on ? 'bg-ink text-surface' : 'bg-tile text-ink',
                  )}
                >
                  {p.name.slice(0, 1)}
                </span>
                <span className="ml-2.5 min-w-0 flex-1">
                  <span className="block truncate text-[13px] leading-[17px]">{p.name}</span>
                  <span className="block truncate text-[11px] leading-[14px] text-text-tertiary">
                    {p.products.map((x) => x.name).join(', ')}
                  </span>
                </span>
                <span
                  className={cn(
                    'ml-2 shrink-0 rounded-full px-2 py-[3px] text-[12px] leading-4 font-medium tabular-nums',
                    on ? 'bg-surface' : 'bg-tile',
                  )}
                >
                  {decimal(formatCarbs(presetGrams(p), display))} {unit}
                </span>
              </button>
            )
          })}
          {presets.length > 0 && <div className="mx-2 my-1 h-px bg-divider" />}
          <a href="#/food" className="flex items-center rounded-strip p-2 active:bg-screen">
            <span className="flex size-[30px] items-center justify-center rounded-[10px] border border-dashed border-border-hover text-text-secondary">
              <Icon name="plus" size={12} strokeWidth={2.4} />
            </span>
            <span className="ml-2.5 text-[12.5px] text-text-secondary">Новый пресет</span>
          </a>
        </div>
      )}
    </div>
  )
}

/** "Комментарий": a tap unfolds the field under the row; with text the row shows its start. */
export function NoteRow({
  notes,
  open,
  onToggle,
  onChange,
}: {
  notes: string
  open: boolean
  onToggle: () => void
  onChange: (text: string) => void
}) {
  return (
    <div>
      <div className="flex">
        <PlateRow
          icon="messageCircle"
          filled={notes.trim() !== '' || open}
          label="Комментарий"
          value={notes.trim() || 'Добавить'}
          onClick={onToggle}
          expanded={open}
        />
      </div>
      {open && (
        <textarea
          aria-label="Комментарий"
          placeholder="Самочувствие, активность…"
          rows={2}
          maxLength={NOTES_MAX_LENGTH}
          value={notes}
          onChange={(e) => onChange(e.target.value)}
          className="mt-1.5 block w-full animate-fade-in resize-none rounded-field bg-tile px-3.5 py-[13px] text-base leading-5 caret-ink outline-none placeholder:text-placeholder"
        />
      )}
    </div>
  )
}
