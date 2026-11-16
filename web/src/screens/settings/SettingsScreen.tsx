import { useState, type ReactNode } from 'react'
import { Icon } from '@/components/icons'
import { CircleButton } from '@/components/ui/button'
import { Stepper } from '@/components/ui/stepper'
import { Card } from '@/components/ui/surface'
import { carbGoalStepGrams, MAX_CARB_GOAL_GRAMS, stepCarbGoal } from '@/diary/carbGoal'
import { formatCarbs, parseCarbs, type CarbDisplay } from '@/diary/carbs'
import { useLoadedSettings } from '@/diary/hooks'
import { diary } from '@/diary/repository'
import {
  GLUCOSE_RANGE_BOUNDS,
  GRAMS_PER_XE_BOUNDS,
  INSULIN_DURATION_BOUNDS,
  type Settings,
} from '@/diary/settings'
import { LANGS, setLang, t, useLang, type Lang } from '@/i18n'
import { carbUnit, decimal, fixedOne } from '@/lib/format'
import { numeric } from '@/lib/numeric'
import { cn } from '@/lib/utils'

// The bounds can't cross: the range keeps at least this much between them.
const MIN_RANGE_GAP = 0.5
const INSULIN_STEP_HOURS = 0.5

const LANG_NAMES: Record<Lang, string> = { ru: 'Русский', en: 'English' }

/**
 * The Настройки tab: language, target range, insulin duration, carb unit and grams per XE, the
 * daily carb goal. Each change is saved at once (diary/settings.ts keeps the bounds); the data
 * section - export and import - comes with #250 / #251.
 */
export function SettingsScreen({ kitLink }: { kitLink?: ReactNode }) {
  const settings = useLoadedSettings()
  const save = (change: Partial<Settings>) => void diary.updateSettings(change)

  return (
    <section className="px-3.5 pt-[18px]">
      <div className="px-2 pb-3.5">
        <h1 className="text-screen-title">{t().settings.title}</h1>
        <p className="mt-[5px] text-label text-text-label">{t().settings.subtitle}</p>
      </div>
      <div className="space-y-2.5">
        <LanguageCard />
        {settings && (
          <>
            <RangeCard settings={settings} save={save} />
            <InsulinCard settings={settings} save={save} />
            <CarbsCard settings={settings} save={save} />
            <GoalCard settings={settings} save={save} />
          </>
        )}
        <Card>
          <h2 className="text-label text-text-label">{t().settings.data}</h2>
          <p className="mt-2 text-hint text-text-tertiary">{t().settings.dataSoon}</p>
        </Card>
        {kitLink}
      </div>
    </section>
  )
}

type CardProps = { settings: Settings; save: (change: Partial<Settings>) => void }

/** Two tiles side by side, the chosen one ink. */
function Choice<T extends string>({
  label,
  options,
  value,
  onChange,
}: {
  label: string
  options: { value: T; text: string }[]
  value: T
  onChange: (value: T) => void
}) {
  return (
    <div role="radiogroup" aria-label={label} className="flex gap-2">
      {options.map((o) => (
        <button
          key={o.value}
          type="button"
          role="radio"
          aria-checked={o.value === value}
          onClick={() => onChange(o.value)}
          className={cn(
            'flex-1 rounded-field px-2.5 py-3 text-body transition-colors',
            o.value === value ? 'bg-ink text-surface' : 'bg-tile text-ink active:bg-tile-hover',
          )}
        >
          {o.text}
        </button>
      ))}
    </div>
  )
}

function LanguageCard() {
  const lang = useLang()
  return (
    <Card>
      <h2 className="mb-[11px] text-label text-text-label">{t().settings.language}</h2>
      <Choice
        label={t().settings.language}
        options={LANGS.map((l) => ({ value: l, text: LANG_NAMES[l] }))}
        value={lang}
        onChange={setLang}
      />
    </Card>
  )
}

function CardHead({ title, unit }: { title: string; unit?: string }) {
  return (
    <div className="flex items-end">
      <h2 className="flex-1 text-label text-text-label">{title}</h2>
      {unit && <span className="text-hint text-text-tertiary">{unit}</span>}
    </div>
  )
}

const BIG = 'text-[30px] leading-[34px] font-medium tracking-[-0.02em] tabular-nums'

function RangeCard({ settings, save }: CardProps) {
  const { low, high } = settings.glucoseRange
  const span = GLUCOSE_RANGE_BOUNDS.max - GLUCOSE_RANGE_BOUNDS.min
  const at = (v: number) => ((v - GLUCOSE_RANGE_BOUNDS.min) / span) * 100
  return (
    <Card>
      <CardHead title={t().settings.range} unit={t().units.mmol} />
      <p className={`mt-[13px] ${BIG}`}>
        {fixedOne(low)} — {fixedOne(high)}
      </p>
      <div className="relative mt-3.5 h-1.5 rounded-full bg-divider" aria-hidden="true">
        <div
          className="absolute inset-y-0 rounded-full bg-ink"
          style={{ left: `${at(low)}%`, width: `${at(high) - at(low)}%` }}
        />
      </div>
      <div className="mt-4 flex gap-[9px]">
        <Stepper
          className="flex-1"
          label={t().settings.rangeLow}
          value={low}
          step={0.1}
          min={GLUCOSE_RANGE_BOUNDS.min}
          max={Number((high - MIN_RANGE_GAP).toFixed(1))}
          format={fixedOne}
          onChange={(v) => save({ glucoseRange: { low: v, high } })}
        />
        <Stepper
          className="flex-1"
          label={t().settings.rangeHigh}
          value={high}
          step={0.1}
          min={Number((low + MIN_RANGE_GAP).toFixed(1))}
          max={GLUCOSE_RANGE_BOUNDS.max}
          format={fixedOne}
          onChange={(v) => save({ glucoseRange: { low, high: v } })}
        />
      </div>
      <p className="mt-[11px] text-hint text-text-tertiary">{t().settings.rangeHint}</p>
    </Card>
  )
}

function InsulinCard({ settings, save }: CardProps) {
  const hours = settings.insulinDurationHours
  const set = (v: number) =>
    save({
      insulinDurationHours: Math.min(INSULIN_DURATION_BOUNDS.max, Math.max(INSULIN_DURATION_BOUNDS.min, v)),
    })
  const label = t().settings.insulinDuration
  return (
    <Card>
      <CardHead title={label} unit={t().units.hours} />
      <div className="mt-[13px] flex items-center gap-[9px]">
        <output aria-label={label} className={`flex-1 ${BIG}`}>
          {fixedOne(hours)}
        </output>
        <CircleButton
          size={28}
          aria-label={t().common.less(label)}
          disabled={hours <= INSULIN_DURATION_BOUNDS.min}
          onClick={() => set(hours - INSULIN_STEP_HOURS)}
        >
          <Icon name="minus" size={12} strokeWidth={2.4} />
        </CircleButton>
        <CircleButton
          size={28}
          aria-label={t().common.more(label)}
          disabled={hours >= INSULIN_DURATION_BOUNDS.max}
          onClick={() => set(hours + INSULIN_STEP_HOURS)}
        >
          <Icon name="plus" size={12} strokeWidth={2.4} />
        </CircleButton>
      </div>
      <p className="mt-[11px] text-hint text-text-tertiary">{t().settings.insulinDurationHint}</p>
    </Card>
  )
}

function CarbsCard({ settings, save }: CardProps) {
  const display = settings.carbDisplay
  return (
    <Card>
      <h2 className="mb-[11px] text-label text-text-label">{t().settings.carbs}</h2>
      <Choice
        label={t().settings.carbs}
        options={[
          { value: 'XE', text: t().settings.unitXe },
          { value: 'GRAMS', text: t().settings.unitGrams },
        ]}
        value={display.unit}
        onChange={(unit) => save({ carbDisplay: { ...display, unit } })}
      />
      {/* Only XE need a size; in grams there's nothing to convert. */}
      {display.unit === 'XE' && (
        <Stepper
          className="mt-4"
          label={t().settings.gramsPerXe}
          value={display.gramsPerXe}
          step={GRAMS_PER_XE_BOUNDS.step}
          min={GRAMS_PER_XE_BOUNDS.min}
          max={GRAMS_PER_XE_BOUNDS.max}
          onChange={(gramsPerXe) => save({ carbDisplay: { ...display, gramsPerXe } })}
        />
      )}
      <p className="mt-[11px] text-hint text-text-tertiary">{t().settings.carbsHint}</p>
    </Card>
  )
}

/** "≈ 15 г" for XE, "≈ 1,5 ХЕ" for grams: the other unit, for orientation. */
function equivalent(grams: number, display: CarbDisplay): string {
  const other: CarbDisplay = { ...display, unit: display.unit === 'XE' ? 'GRAMS' : 'XE' }
  return t().settings.goalEquivalent(`${decimal(formatCarbs(grams, other))} ${carbUnit(other)}`)
}

/**
 * The daily goal: a switch, then the goal - typed into the field or nudged with −/+ - in the carb
 * unit, with the other unit beside it.
 */
function GoalCard({ settings, save }: CardProps) {
  const goal = settings.carbGoal
  const display = settings.carbDisplay
  const shown = (grams: number | null) =>
    grams != null && grams > 0 ? decimal(formatCarbs(grams, display)) : ''
  // The field keeps what's being typed ("12," mid-entry) - each keystroke is saved, and an older
  // value coming back from the database must not overwrite it. Only a unit change rewrites it.
  const [text, setText] = useState(() => shown(goal.grams))
  const [unitShown, setUnitShown] = useState(`${display.unit}/${display.gramsPerXe}`)
  if (unitShown !== `${display.unit}/${display.gramsPerXe}`) {
    setUnitShown(`${display.unit}/${display.gramsPerXe}`)
    setText(shown(goal.grams))
  }
  const typed = parseCarbs(text, display)
  const step = (steps: number) => {
    const grams = stepCarbGoal(typed ?? goal.grams, steps, display)
    setText(shown(grams))
    save({ carbGoal: { ...goal, grams } })
  }
  const unit = carbUnit(display)

  return (
    <Card>
      <div className="flex items-center">
        <h2 id="goal-title" className="flex-1 text-label text-text-label">
          {t().settings.goal}
        </h2>
        <button
          type="button"
          role="switch"
          aria-checked={goal.enabled}
          aria-labelledby="goal-title"
          onClick={() => save({ carbGoal: { ...goal, enabled: !goal.enabled } })}
          className={cn(
            'relative h-[27px] w-[46px] shrink-0 rounded-full p-[3px] transition-colors',
            goal.enabled ? 'bg-ink' : 'bg-border-dashed',
          )}
        >
          <span
            className={cn(
              'block size-[21px] rounded-full bg-surface transition-transform duration-200',
              goal.enabled && 'translate-x-[19px]',
            )}
          />
        </button>
      </div>
      {goal.enabled && (
        <>
          <div className="mt-2.5 flex items-end gap-1">
            <input
              inputMode="decimal"
              aria-label={t().settings.goalField(unit)}
              placeholder="0"
              value={text}
              onChange={(e) => {
                const next = numeric(e.target.value)
                setText(next)
                const grams = parseCarbs(next, display)
                save({
                  carbGoal: { ...goal, grams: grams == null ? null : Math.min(MAX_CARB_GOAL_GRAMS, grams) },
                })
              }}
              className={`w-28 bg-transparent caret-ink/55 outline-none placeholder:text-placeholder ${BIG}`}
            />
            <span className="mb-[5px] text-label text-text-label">{unit}</span>
            <span className="mb-1.5 ml-auto text-hint text-text-tertiary">
              {typed != null && typed > 0 ? equivalent(typed, display) : ''}
            </span>
          </div>
          <div className="mt-3 flex items-center gap-2.5 rounded-tile bg-tile px-2.5 py-2">
            <CircleButton
              size={32}
              tone="surface"
              aria-label={t().common.less(t().settings.goal)}
              onClick={() => step(-1)}
            >
              <Icon name="minus" size={12} strokeWidth={2.4} />
            </CircleButton>
            <span className="flex-1 text-center text-hint text-text-secondary">
              {t().settings.goalStep(`${decimal(formatCarbs(carbGoalStepGrams(display), display))} ${unit}`)}
            </span>
            <CircleButton
              size={32}
              tone="surface"
              aria-label={t().common.more(t().settings.goal)}
              onClick={() => step(1)}
            >
              <Icon name="plus" size={12} strokeWidth={2.4} />
            </CircleButton>
          </div>
        </>
      )}
      <p className="mt-[11px] text-hint text-text-tertiary">{t().settings.goalHint}</p>
    </Card>
  )
}
