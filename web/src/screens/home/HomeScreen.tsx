import { useState } from 'react'
import { DateSheet, TimeSheet } from '@/components/DateTimeSheets'
import { Icon, InsulinIcon, type InsulinKind } from '@/components/icons'
import { useEntries, usePresets, useSettings } from '@/diary/hooks'
import { diary } from '@/diary/repository'
import { dayOf, nowLocal } from '@/diary/time'
import { hhmm, moment, shortDate } from '@/lib/format'
import { numeric } from '@/lib/numeric'
import { useToast } from '@/lib/toast'
import { useNow } from '@/lib/useNow'
import { cn } from '@/lib/utils'
import { DaySummary } from '@/screens/home/DaySummary'
import {
  applyPreset,
  cleared,
  EMPTY_FORM,
  formStore,
  greeting,
  isDirty,
  isFillable,
  shownAt,
  toEntry,
  typeCarbs,
  withDate,
  withTime,
  type EntryForm,
} from '@/screens/home/entryForm'
import { FoodRow, NoteRow } from '@/screens/home/FormRows'
import { PresetEditSheet } from '@/screens/food/PresetSheets'
import { useT } from '@/i18n'

/**
 * The home tab: greeting, today at a glance and the entry form - sugar, insulin, food, comment -
 * in one card, as on Android. Saving says so with an undo that takes the entry back and puts the
 * form back as it was.
 */
export function HomeScreen() {
  const [form, setForm] = formStore.use()
  const entries = useEntries()
  const presets = usePresets()
  const settings = useSettings()
  const now = useNow()
  const toast = useToast()
  const t = useT()
  const [picker, setPicker] = useState<'date' | 'time' | null>(null)
  const [newPreset, setNewPreset] = useState(false)

  const display = settings.carbDisplay
  const at = shownAt(form, now)
  const update = (change: Partial<EntryForm>) => setForm((f) => ({ ...f, ...change }))
  // "Сейчас" in the pickers only once something was picked - while following the clock it would do nothing.
  const backToNow = form.pinnedAt ? () => (update({ pinnedAt: null }), setPicker(null)) : undefined

  const save = async () => {
    const previous = form
    // The real moment of saving while no time was picked, not the last minute tick.
    const createdAt = form.pinnedAt ?? nowLocal()
    setForm(EMPTY_FORM)
    const id = await diary.addEntry(toEntry(previous, createdAt, display))
    toast.show(t.home.saved, {
      sub: moment(createdAt),
      onUndo: () => {
        void diary.deleteEntry(id)
        setForm(previous)
      },
    })
  }

  const clear = () => {
    const previous = form
    setForm(cleared(form))
    toast.show(t.home.cleared, { kind: 'delete', onUndo: () => setForm(previous) })
  }

  return (
    <section className="px-3.5 pt-3">
      <h1 className="px-2 pb-2.5 text-screen-title">{greeting(Number(now.slice(11, 13)))}</h1>
      <DaySummary entries={entries} settings={settings} now={now} />

      <form
        aria-label={t.home.newEntry}
        className="mt-2 rounded-card bg-surface pt-3 pr-3.5 pb-3.5 pl-4 shadow-soft"
        onSubmit={(e) => {
          e.preventDefault()
          if (isFillable(form)) void save()
        }}
      >
        <div className="flex items-center">
          <Icon name="droplet" size={14} strokeWidth={1.7} className="text-text-label" />
          <span className="ml-[7px] flex-1 text-label text-text-label">{t.home.bloodSugar}</span>
          <DateTimeChip
            date={shortDate(at)}
            time={hhmm(at)}
            onDate={() => setPicker('date')}
            onTime={() => setPicker('time')}
          />
        </div>
        <label className="mt-0.5 flex items-baseline gap-1">
          <input
            inputMode="decimal"
            aria-label={t.home.sugarField}
            placeholder={`0${t.decimal}0`}
            value={form.sugar}
            onChange={(e) => update({ sugar: numeric(e.target.value) })}
            className="w-[84px] bg-transparent text-display-sugar tabular-nums caret-ink/55 outline-none placeholder:text-placeholder"
          />
          <span className="text-label text-text-label">{t.units.mmol}</span>
        </label>

        <hr className="my-2.5 border-divider" />

        <div className="flex gap-[9px]">
          <InsulinField
            kind="short"
            label={t.insulin.short}
            value={form.shortInsulin}
            onChange={(v) => update({ shortInsulin: v })}
          />
          <InsulinField
            kind="long"
            label={t.insulin.long}
            value={form.longInsulin}
            onChange={(v) => update({ longInsulin: v })}
          />
        </div>

        <hr className="mt-3 mb-1 border-divider" />
        <FoodRow
          presets={presets}
          presetId={form.presetId}
          mealLabel={form.mealLabel}
          carbs={form.carbs}
          display={display}
          open={form.foodOpen}
          onToggle={() => update({ foodOpen: !form.foodOpen })}
          onPick={(p) => setForm((f) => applyPreset(f, p, display))}
          onCarbs={(text) => setForm((f) => typeCarbs(f, text))}
          onCreatePreset={() => setNewPreset(true)}
        />
        <hr className="my-1 border-divider" />
        <NoteRow
          notes={form.notes}
          open={form.noteOpen}
          onToggle={() => update({ noteOpen: !form.noteOpen })}
          onChange={(notes) => update({ notes })}
        />

        <div className="mt-4 flex gap-1.5">
          {isDirty(form) && (
            <button
              type="button"
              aria-label={t.home.clear}
              onClick={clear}
              className="flex size-[50px] shrink-0 animate-fade-in items-center justify-center rounded-full bg-tile active:bg-tile-hover"
            >
              <Icon name="rotateCcw" size={16} strokeWidth={2} />
            </button>
          )}
          <button
            type="submit"
            disabled={!isFillable(form)}
            className="flex h-[50px] flex-1 items-center justify-center gap-[9px] rounded-full bg-ink text-button text-surface transition-colors active:bg-ink-hover disabled:bg-border disabled:text-placeholder"
          >
            {t.home.save}
            <Icon name="arrowRight" size={16} strokeWidth={2} />
          </button>
        </div>
      </form>

      <PresetEditSheet
        open={newPreset}
        preset={null}
        display={display}
        onClose={() => setNewPreset(false)}
        // A preset made from the form is the one being eaten: it goes straight in.
        onSaved={(p) => setForm((f) => applyPreset(f, p, display))}
      />
      <DateSheet
        open={picker === 'date'}
        value={dayOf(at)}
        today={dayOf(now)}
        onNow={backToNow}
        onClose={() => setPicker(null)}
        onDone={(date) => {
          setForm((f) => withDate(f, date, nowLocal()))
          setPicker(null)
        }}
      />
      <TimeSheet
        open={picker === 'time'}
        value={hhmm(at)}
        onNow={backToNow}
        onClose={() => setPicker(null)}
        onDone={(time) => {
          setForm((f) => withTime(f, time, nowLocal()))
          setPicker(null)
        }}
      />
    </section>
  )
}

/** "📅 15 нояб. | 🕒 09:41": two halves, each opening its own sheet. */
function DateTimeChip({
  date,
  time,
  onDate,
  onTime,
}: {
  date: string
  time: string
  onDate: () => void
  onTime: () => void
}) {
  const t = useT()
  const half =
    'flex items-center gap-[5px] rounded-full px-[9px] py-[5px] text-[12px] leading-4 tabular-nums active:bg-surface'
  return (
    <span className="flex items-center rounded-full bg-tile p-[3px]">
      <button type="button" aria-label={t.time.dateIs(date)} onClick={onDate} className={half}>
        <Icon name="calendar" size={12} strokeWidth={2} />
        {date}
      </button>
      <span className="h-3 w-px bg-border" />
      <button type="button" aria-label={t.time.timeIs(time)} onClick={onTime} className={half}>
        <Icon name="clock" size={12} strokeWidth={2} />
        {time}
      </button>
    </span>
  )
}

function InsulinField({
  kind,
  label,
  value,
  onChange,
}: {
  kind: InsulinKind
  label: string
  value: string
  onChange: (value: string) => void
}) {
  const t = useT()
  return (
    <label className={cn('min-w-0 flex-1 rounded-tile bg-tile px-[13px] py-[11px]')}>
      <span className="flex items-center gap-[5px]">
        <InsulinIcon kind={kind} size={14} />
        <span className="truncate text-hint text-text-label">{label}</span>
      </span>
      <span className="mt-1.5 flex items-baseline">
        <input
          inputMode="decimal"
          aria-label={`${label}, ${t.units.insulin}`}
          placeholder="0"
          value={value}
          onChange={(e) => onChange(numeric(e.target.value))}
          className="w-full min-w-0 bg-transparent pl-1 text-value-large tabular-nums caret-ink/55 outline-none placeholder:text-placeholder"
        />
        <span className="text-hint text-text-label">{t.units.insulin}</span>
      </span>
    </label>
  )
}
