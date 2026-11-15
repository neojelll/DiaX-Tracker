import { Icon, InsulinIcon } from '@/components/icons'
import { Capsule, NumberWithUnit } from '@/components/ui/capsule'
import { afterMealPoints, type AfterMeal } from '@/diary/afterMeal'
import { formatCarbs } from '@/diary/carbs'
import { carbGoalReached } from '@/diary/carbGoal'
import { GLUCOSE_TEXT, glucoseLevel } from '@/diary/glucose'
import type { DayInsulin } from '@/diary/insulin'
import type { Settings } from '@/diary/settings'
import type { LocalDateTime } from '@/diary/time'
import type { SavedEntry } from '@/diary/types'
import { decimal, hhmm, oneDecimal } from '@/lib/format'
import { cn } from '@/lib/utils'
import { AfterMealLine } from '@/screens/history/AfterMealLine'
import { recordKind } from '@/screens/history/history'

/**
 * A History record: one line - time and what it is, the values as capsules, the sugar in its range
 * colour. [open] null - nothing more inside, a tap edits; otherwise a tap opens the preset, the
 * sugar after the meal and the note under the line. (Android HistoryRecordRow / RecordCard.)
 */
export function RecordCard({
  entry,
  settings,
  afterMeal,
  open,
  now,
  onTap,
  onEdit,
}: {
  entry: SavedEntry
  settings: Settings
  afterMeal: AfterMeal | undefined
  open: boolean | null
  now: LocalDateTime
  onTap: () => void
  onEdit: () => void
}) {
  const display = settings.carbDisplay
  const unit = display.unit === 'XE' ? 'ХЕ' : 'г'
  const carbs = entry.carbsGrams == null ? null : decimal(formatCarbs(entry.carbsGrams, display))
  return (
    <article
      className={cn(
        'rounded-record bg-surface shadow-soft transition-shadow duration-300',
        open && 'shadow-[0_12px_24px_-8px_rgb(13_13_13/0.2)]',
      )}
    >
      <button
        type="button"
        onClick={onTap}
        aria-expanded={open ?? undefined}
        aria-label={`${hhmm(entry.createdAt)}, ${recordKind(entry)}${open == null ? ', изменить' : ''}`}
        className="flex w-full items-start px-3.5 py-[11px] text-left"
      >
        <span className="w-11 shrink-0">
          <span className="block text-record-time tabular-nums">{hhmm(entry.createdAt)}</span>
          <span className="mt-[3px] flex items-center gap-0.5 text-[10px] leading-[13px] text-placeholder">
            {recordKind(entry)}
            {open != null && (
              <Icon
                name="chevronDown"
                size={10}
                strokeWidth={2.4}
                className={cn('transition-transform duration-200', open && 'rotate-180')}
              />
            )}
          </span>
        </span>
        <span className="mx-2 mt-px flex min-w-0 flex-1 flex-wrap gap-[5px]">
          {carbs != null && (
            <Capsule label={`${carbs} ${unit}`} icon={<Icon name="dish" size={14} strokeWidth={2} />}>
              <NumberWithUnit number={carbs} unit={unit} />
            </Capsule>
          )}
          {entry.shortInsulinDose != null && (
            <Capsule
              label={`${oneDecimal(entry.shortInsulinDose)} ед, короткий`}
              icon={<InsulinIcon kind="short" size={14} />}
            >
              <NumberWithUnit number={oneDecimal(entry.shortInsulinDose)} unit="ед" />
            </Capsule>
          )}
          {entry.longInsulinDose != null && (
            <Capsule
              label={`${oneDecimal(entry.longInsulinDose)} ед, длинный`}
              icon={<InsulinIcon kind="long" size={14} />}
            >
              <NumberWithUnit number={oneDecimal(entry.longInsulinDose)} unit="ед" />
            </Capsule>
          )}
        </span>
        <span className="shrink-0 text-right">
          {entry.bloodSugar == null ? (
            <span className="block text-[18px] leading-5 font-medium text-placeholder">—</span>
          ) : (
            <>
              <span
                className={cn(
                  'block text-[18px] leading-5 font-medium tabular-nums',
                  GLUCOSE_TEXT[glucoseLevel(entry.bloodSugar, settings.glucoseRange)],
                )}
              >
                {entry.bloodSugar.toFixed(1).replace('.', ',')}
              </span>
              {entry.sugarSource && (
                <span className="mt-[3px] block text-[10px] leading-[13px] text-placeholder">
                  {entry.sugarSource === 'SENSOR' ? 'сенсор' : 'вручную'}
                </span>
              )}
            </>
          )}
        </span>
      </button>

      {open && (
        <div className="mx-3.5 animate-fade-in space-y-[9px] border-t border-divider pt-[11px] pb-[11px]">
          {entry.mealLabel != null && (
            <div className="rounded-field bg-tile px-3 py-2.5">
              <p className="flex items-center gap-1.5 text-body font-medium">
                <Icon name="bookmark" size={13} strokeWidth={2} className="text-text-secondary" />
                {entry.mealLabel}
              </p>
              {entry.products.length > 0 && (
                <>
                  <p className="mt-2 text-card-label text-text-label">состав</p>
                  <ul>
                    {entry.products.map((p, i) => (
                      <li key={i} className="mt-[5px] flex gap-2">
                        <span className="flex-1 text-[12.5px] leading-[17px]">{p.name}</span>
                        <span className="text-hint text-text-tertiary tabular-nums">
                          {decimal(formatCarbs(p.carbsGrams, display))} {unit}
                        </span>
                      </li>
                    ))}
                  </ul>
                </>
              )}
            </div>
          )}
          {afterMeal && (
            <AfterMealLine
              points={afterMealPoints(entry.bloodSugar, afterMeal, entry.createdAt, now)}
              range={settings.glucoseRange}
            />
          )}
          {entry.notes.trim() !== '' && (
            <p className="flex gap-2 rounded-field bg-tile px-3 py-2.5">
              <Icon
                name="messageCircle"
                size={13}
                strokeWidth={1.9}
                className="mt-0.5 shrink-0 text-text-label"
              />
              <span className="text-note whitespace-pre-wrap">{entry.notes}</span>
            </p>
          )}
          <button
            type="button"
            onClick={onEdit}
            className="flex w-full items-center justify-center gap-1.5 rounded-field bg-tile py-[11px] text-body text-text-secondary active:bg-tile-hover"
          >
            <Icon name="pencil" size={13} strokeWidth={2} />
            Изменить запись
          </button>
        </div>
      )}
    </article>
  )
}

/**
 * "СЕГОДНЯ —— totals", pinned while its day scrolls under it: the day's carbs (with a tick once the
 * goal is reached), short and long insulin - each only when there is some - in outlined capsules,
 * quieter than the records.
 */
export function DayHeader({
  label,
  carbsGrams,
  insulin,
  settings,
}: {
  label: string
  carbsGrams: number
  insulin: DayInsulin
  settings: Settings
}) {
  const display = settings.carbDisplay
  const unit = display.unit === 'XE' ? 'ХЕ' : 'г'
  const goal = settings.carbGoal.enabled ? settings.carbGoal.grams : null
  const reached = goal != null && carbGoalReached(carbsGrams, goal)
  const carbs = decimal(formatCarbs(carbsGrams, display))
  const total =
    'flex items-center gap-0.5 rounded-[9px] border border-border py-0.5 pr-[7px] pl-[5px] text-[11px] leading-[15px] text-text-secondary tabular-nums'
  return (
    <h2
      className="sticky z-10 flex items-center gap-2 bg-screen pt-1.5 pb-2.5"
      style={{ top: 'env(safe-area-inset-top)' }}
    >
      <span className="text-kicker text-text-label uppercase">{label}</span>
      <span className="h-px flex-1 bg-border-dashed" />
      <span className="flex gap-[5px]">
        {carbsGrams > 0 && (
          <span
            role="img"
            aria-label={`${carbs} ${unit}${reached ? ', цель набрана' : ''}`}
            className={total}
          >
            <Icon name="dish" size={13} strokeWidth={2} />
            <NumberWithUnit number={carbs} unit={unit} />
            {reached && <Icon name="check" size={10} strokeWidth={3} className="ml-[3px] text-ink" />}
          </span>
        )}
        {insulin.short > 0 && (
          <span role="img" aria-label={`${oneDecimal(insulin.short)} ед, короткий`} className={total}>
            <InsulinIcon kind="short" size={13} />
            <NumberWithUnit number={oneDecimal(insulin.short)} unit="ед" />
          </span>
        )}
        {insulin.long != null && (
          <span role="img" aria-label={`${oneDecimal(insulin.long)} ед, длинный`} className={total}>
            <InsulinIcon kind="long" size={13} />
            <NumberWithUnit number={oneDecimal(insulin.long)} unit="ед" />
          </span>
        )}
      </span>
    </h2>
  )
}
