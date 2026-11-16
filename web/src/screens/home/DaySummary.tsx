import type { ReactNode } from 'react'
import { Icon } from '@/components/icons'
import { activeGoalGrams, carbGoalProgress, carbGoalReached } from '@/diary/carbGoal'
import { carbsGramsOn, formatCarbs } from '@/diary/carbs'
import { insulinOn } from '@/diary/insulin'
import type { Settings } from '@/diary/settings'
import { dayOf, minutesBetween, type LocalDateTime } from '@/diary/time'
import type { SavedEntry } from '@/diary/types'
import { decimal, oneDecimal } from '@/lib/format'
import { FRESH_MINUTES, sugarAge } from '@/screens/home/sugarAge'
import { t } from '@/i18n'
import { carbUnit } from '@/lib/format'

/**
 * Today at a glance, above the form: the last sugar on ink - the screen's one dark accent - then
 * carbs and insulin on white. (Android's DaySummaryCard; the sugar is the last one logged, as
 * there's no sensor yet.)
 */
export function DaySummary({
  entries,
  settings,
  now,
}: {
  entries: SavedEntry[]
  settings: Settings
  now: LocalDateTime
}) {
  const day = dayOf(now)
  return (
    <div className="grid grid-cols-[1.25fr_1fr_1fr] gap-2">
      <SugarTile entries={entries} now={now} />
      <CarbsTile grams={carbsGramsOn(day, entries)} settings={settings} />
      <InsulinTile entries={entries} day={day} />
    </div>
  )
}

function SugarTile({ entries, now }: { entries: SavedEntry[]; now: LocalDateTime }) {
  // By the minute: [now] is the current minute, and a record saved during it is not in the future.
  const last = entries.find((e) => e.bloodSugar != null && e.createdAt.slice(0, 16) <= now.slice(0, 16))
  const minutes = last ? minutesBetween(last.createdAt, now) : Infinity
  const age = last ? sugarAge(minutes) : null
  const fresh = minutes <= FRESH_MINUTES
  return (
    <section aria-label={t().summary.sugar} className="rounded-panel bg-ink px-3 py-[11px]">
      <p className="flex items-center gap-[5px] text-[11px] leading-[14px] text-text-on-dark">
        <span className={`size-1.5 rounded-full ${fresh ? 'bg-sensor-fresh' : 'bg-sensor-stale'}`} />
        {t().summary.sugar}
      </p>
      <p
        className={`mt-[5px] text-[26px] leading-[26px] font-medium tracking-[-0.02em] tabular-nums ${fresh ? 'text-surface' : 'text-placeholder'}`}
      >
        {age && last ? oneDecimal(last.bloodSugar!) : '—'}
      </p>
      <p className="mt-[5px] truncate text-[10.5px] leading-[14px] text-text-on-dark">
        {age ?? t().summary.noData}
      </p>
    </section>
  )
}

function LightTile({ label, children }: { label: string; children: ReactNode }) {
  return (
    <section aria-label={label} className="flex flex-col rounded-panel bg-surface px-3 py-[11px] shadow-soft">
      <p className="truncate text-[11px] leading-[14px] text-text-label">{label}</p>
      <div className="mt-[5px] flex flex-1 flex-col">{children}</div>
    </section>
  )
}

const VALUE = 'text-[21px] leading-[21px] font-medium tracking-[-0.02em] tabular-nums'
const NOTE = 'mt-1.5 truncate text-[10.5px] leading-[14px] text-text-tertiary'

/**
 * Today's carbohydrates in the unit from the settings. With a daily goal: "из 12,5 ХЕ" (or "цель
 * набрана") and a thin bar filling toward it.
 */
function CarbsTile({ grams, settings }: { grams: number; settings: Settings }) {
  const display = settings.carbDisplay
  const unit = carbUnit(display)
  const goal = activeGoalGrams(settings.carbGoal)
  return (
    <LightTile label={t().summary.carbs}>
      <p className={VALUE}>{decimal(formatCarbs(grams, display))}</p>
      {goal == null ? (
        <p className={NOTE}>{t().summary.perDay(unit)}</p>
      ) : carbGoalReached(grams, goal) ? (
        <p className={`${NOTE} flex items-center gap-[3px] text-ink`}>
          <Icon name="check" size={10} strokeWidth={2.6} />
          {t().summary.goalReached}
        </p>
      ) : (
        <p className={NOTE}>{t().summary.ofGoal(`${decimal(formatCarbs(goal, display))} ${unit}`)}</p>
      )}
      {goal != null && <div className="min-h-[7px] flex-1" />}
      {goal != null && (
        <div
          role="progressbar"
          aria-label={t().summary.goal}
          aria-valuenow={Math.round(carbGoalProgress(grams, goal) * 100)}
          className="h-1 overflow-hidden rounded-full bg-divider"
        >
          <div
            className="h-full rounded-full bg-ink"
            style={{ width: `${carbGoalProgress(grams, goal) * 100}%` }}
          />
        </div>
      )}
    </LightTile>
  )
}

/** "12 / 18": short, then long muted - no total: they do different jobs. */
function InsulinTile({ entries, day }: { entries: SavedEntry[]; day: string }) {
  const insulin = insulinOn(day, entries)
  return (
    <LightTile label={t().summary.insulin}>
      <p className="flex items-baseline whitespace-nowrap">
        <span className={VALUE}>{oneDecimal(insulin.short)}</span>
        {insulin.long != null && (
          <span className="text-[14px] font-medium text-text-tertiary tabular-nums">
            &nbsp;/ {oneDecimal(insulin.long)}
          </span>
        )}
      </p>
      <p className={NOTE}>{insulin.long != null ? t().summary.split : t().summary.shortOnly}</p>
    </LightTile>
  )
}
