import { useMemo, useState } from 'react'
import { DateSheet } from '@/components/DateTimeSheets'
import { Icon } from '@/components/icons'
import { Chip, CircleButton } from '@/components/ui/button'
import { buildAfterMeal } from '@/diary/afterMeal'
import { carbsGramsOn } from '@/diary/carbs'
import { useEntries, useSettings } from '@/diary/hooks'
import { insulinOn } from '@/diary/insulin'
import { dayOf, type LocalDate } from '@/diary/time'
import { useNow } from '@/lib/useNow'
import { EditRecordSheet } from '@/screens/history/EditRecordSheet'
import {
  byDay,
  dayLabel,
  hasMoreInside,
  inFilter,
  matchesSearch,
  shownAfterMeal,
  type DayFilter,
} from '@/screens/history/history'
import { DayHeader, RecordCard } from '@/screens/history/RecordCard'
import { t } from '@/i18n'

const FILTERS: DayFilter['kind'][] = ['all', 'yesterday', 'week', 'month']

/**
 * The History tab: search, day filters and sorting over the records, grouped by day under pinned
 * headers with the day's totals. A meal's after-meal checks live inside it, not as rows of their own.
 */
export function HistoryScreen() {
  const entries = useEntries()
  const settings = useSettings()
  const now = useNow()
  const todayDay = dayOf(now)
  const [query, setQuery] = useState('')
  const [filter, setFilter] = useState<DayFilter>({ kind: 'all' })
  const [newestFirst, setNewestFirst] = useState(true)
  const [openId, setOpenId] = useState<number | null>(null)
  const [editId, setEditId] = useState<number | null>(null)
  // The custom range is two picks: from, then to.
  const [rangeStep, setRangeStep] = useState<{ from?: LocalDate } | null>(null)

  const afterMeal = useMemo(() => buildAfterMeal(entries, now), [entries, now])
  const records = useMemo(() => entries.filter((e) => !afterMeal.hidden.has(e.id)), [entries, afterMeal])
  const shown = useMemo(() => {
    const matching = records.filter((e) => inFilter(e, filter, todayDay) && matchesSearch(e, query))
    return newestFirst ? matching : [...matching].reverse()
  }, [records, filter, todayDay, query, newestFirst])
  const days = byDay(shown)
  const editing = entries.find((e) => e.id === editId) ?? null

  return (
    <section className="px-3.5 pt-[18px]">
      <div className="px-2">
        <h1 className="text-screen-title">{t().history.title}</h1>
        <p className="mt-[5px] text-label text-text-label">
          {t().history.summary(shown.length, days.length)}
        </p>
      </div>

      <label className="mt-3.5 flex items-center gap-[9px] rounded-tile bg-surface px-3.5 py-[11px] shadow-soft">
        <Icon name="search" size={16} strokeWidth={1.9} className="shrink-0 text-text-tertiary" />
        <input
          type="search"
          aria-label={t().history.search}
          placeholder={t().history.searchPlaceholder}
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          className="min-w-0 flex-1 bg-transparent text-base leading-5 caret-ink/55 outline-none placeholder:text-placeholder [&::-webkit-search-cancel-button]:hidden"
        />
        {query !== '' && (
          <CircleButton size={22} aria-label={t().history.clearSearch} onClick={() => setQuery('')}>
            <Icon name="close" size={11} strokeWidth={2.4} />
          </CircleButton>
        )}
      </label>

      <div className="-mx-3.5 mt-2.5 flex [scrollbar-width:none] gap-[7px] overflow-x-auto px-3.5">
        {FILTERS.map((kind) => (
          <Chip
            key={kind}
            selected={filter.kind === kind}
            onClick={() => setFilter({ kind } as DayFilter)}
            className="shrink-0"
          >
            {t().history[kind]}
          </Chip>
        ))}
        <Chip selected={filter.kind === 'range'} onClick={() => setRangeStep({})} className="shrink-0">
          {filter.kind === 'range' ? rangeLabel(filter.from, filter.to) : t().history.range}
        </Chip>
      </div>

      <div className="mt-2 flex items-center px-2 py-1">
        <span className="flex-1 text-hint text-text-tertiary">
          {query === '' ? t().history.shown(shown.length) : t().history.matches(shown.length)}
        </span>
        <button
          type="button"
          onClick={() => setNewestFirst((n) => !n)}
          className="flex items-center gap-1.5 text-hint text-ink"
        >
          {newestFirst ? t().history.newestFirst : t().history.oldestFirst}
          <Icon name="sort" size={13} strokeWidth={1.9} />
        </button>
      </div>

      <div className="mt-2">
        {days.length === 0 && (
          <p className="py-6 text-center text-hint text-text-tertiary">
            {entries.length === 0 ? t().history.empty : t().history.nothingFound}
          </p>
        )}
        {days.map(({ day, records: dayRecords }) => (
          <div key={day}>
            {/* The day's totals are the whole day's, as on the home screen - not only what the search left. */}
            <DayHeader
              label={dayLabel(day, todayDay)}
              carbsGrams={carbsGramsOn(day, records)}
              insulin={insulinOn(day, records)}
              settings={settings}
            />
            <ul className="space-y-[9px] pb-[9px]">
              {dayRecords.map((entry) => {
                const line = shownAfterMeal(afterMeal.byMeal.get(entry.id))
                const opens = hasMoreInside(entry, line)
                return (
                  <li key={entry.id}>
                    <RecordCard
                      entry={entry}
                      settings={settings}
                      afterMeal={line}
                      open={opens ? openId === entry.id : null}
                      now={now}
                      onTap={() =>
                        opens ? setOpenId((id) => (id === entry.id ? null : entry.id)) : setEditId(entry.id)
                      }
                      onEdit={() => setEditId(entry.id)}
                    />
                  </li>
                )
              })}
            </ul>
          </div>
        ))}
        {days.length > 0 && (
          <p className="pt-1 text-center text-hint text-text-tertiary">{t().history.noEarlier}</p>
        )}
      </div>

      <EditRecordSheet entry={editing} settings={settings} onClose={() => setEditId(null)} />
      <DateSheet
        open={rangeStep != null}
        title={rangeStep?.from ? t().history.rangeTo : t().history.rangeFrom}
        value={rangeStep?.from ?? todayDay}
        today={todayDay}
        onClose={() => setRangeStep(null)}
        onDone={(day) => {
          if (rangeStep?.from == null) return setRangeStep({ from: day })
          const from = rangeStep.from
          setFilter({ kind: 'range', from: from < day ? from : day, to: from < day ? day : from })
          setRangeStep(null)
        }}
      />
    </section>
  )
}

function rangeLabel(from: LocalDate, to: LocalDate): string {
  const short = (d: LocalDate) => `${Number(d.slice(8))}.${d.slice(5, 7)}`
  return from === to ? short(from) : `${short(from)}–${short(to)}`
}
