import { useLiveQuery } from 'dexie-react-hooks'
import { useState } from 'react'
import { DateSheet, TimeSheet } from '@/components/DateTimeSheets'
import { Icon, InsulinIcon } from '@/components/icons'
import { PhotoInput, PhotoStrip } from '@/components/photos'
import { Button } from '@/components/ui/button'
import { Field } from '@/components/ui/field'
import { Sheet } from '@/components/ui/sheet'
import { usePresets } from '@/diary/hooks'
import { diary } from '@/diary/repository'
import type { Settings } from '@/diary/settings'
import { dayOf, today } from '@/diary/time'
import type { SavedEntry } from '@/diary/types'
import { hhmm, moment, shortDate } from '@/lib/format'
import { numeric } from '@/lib/numeric'
import { useToast } from '@/lib/toast'
import { usePhotoPicker } from '@/lib/usePhotoUrl'
import {
  applyPreset,
  editedEntry,
  fromEntry,
  isFillable,
  NOTES_MAX_LENGTH,
  typeCarbs,
  withDate,
  withTime,
  type EntryForm,
} from '@/screens/home/entryForm'
import { FoodRow } from '@/screens/home/FormRows'
import { t } from '@/i18n'

/**
 * Editing a record: the same fields as the home form, in a sheet. Saving says "Изменения сохранены";
 * deleting says "Запись удалена" with «Вернуть», which puts it back under its old id - its
 * after-meal checks stay linked.
 */
export function EditRecordSheet({
  entry,
  settings,
  onClose,
}: {
  entry: SavedEntry | null
  settings: Settings
  onClose: () => void
}) {
  // While sliding out it keeps showing the entry it had.
  const [shown, setShown] = useState(entry)
  if (entry && entry !== shown) setShown(entry)
  return (
    <Sheet open={entry != null} onOpenChange={(o) => !o && onClose()} title={t().history.editTitle}>
      {shown && <EditForm key={shown.id} entry={shown} settings={settings} onClose={onClose} />}
    </Sheet>
  )
}

function EditForm({
  entry,
  settings,
  onClose,
}: {
  entry: SavedEntry
  settings: Settings
  onClose: () => void
}) {
  const display = settings.carbDisplay
  const presets = usePresets()
  const toast = useToast()
  const [form, setForm] = useState<EntryForm>(() => fromEntry(entry, display))
  const [picker, setPicker] = useState<'date' | 'time' | null>(null)
  const at = form.pinnedAt ?? entry.createdAt
  const update = (change: Partial<EntryForm>) => setForm((f) => ({ ...f, ...change }))
  // undefined - the stored photo stays as it is; a Blob or null - replaced or removed on save.
  const [photo, setPhoto] = useState<Blob | null | undefined>(undefined)
  const stored = useLiveQuery(() => diary.photoOf(entry.id), [entry.id])
  const shownPhoto = photo === undefined ? stored : photo
  const photoPicker = usePhotoPicker()

  const save = async () => {
    await diary.updateEntry(editedEntry(entry, form, display))
    if (photo !== undefined) await diary.setPhoto(entry.id, photo)
    onClose()
    toast.show(t().history.edited)
  }
  const remove = async () => {
    const removed = await diary.deleteEntry(entry.id)
    onClose()
    if (removed)
      toast.show(t().history.deleted, {
        sub: moment(removed.entry.createdAt),
        kind: 'delete',
        onUndo: () => void diary.restoreEntries([removed]),
      })
  }

  const kicker = 'mb-2 flex items-center gap-[5px] text-kicker text-text-label uppercase'
  return (
    <form
      onSubmit={(e) => {
        e.preventDefault()
        if (isFillable(form)) void save()
      }}
    >
      <div className="flex gap-[9px]">
        <PickerButton
          icon="calendar"
          label={t().time.dateIs(shortDate(at))}
          text={shortDate(at)}
          onClick={() => setPicker('date')}
        />
        <PickerButton
          icon="clock"
          label={t().time.timeIs(hhmm(at))}
          text={hhmm(at)}
          onClick={() => setPicker('time')}
        />
      </div>

      <p className={`${kicker} mt-4`}>
        <Icon name="droplet" size={14} strokeWidth={1.7} />
        {t().home.sugarField}
      </p>
      <Field
        inputMode="decimal"
        aria-label={t().home.sugarField}
        placeholder={`0${t().decimal}0`}
        value={form.sugar}
        onChange={(e) => update({ sugar: numeric(e.target.value) })}
      />

      <div className="mt-3.5 flex gap-[9px]">
        <label className="min-w-0 flex-1">
          <span className={kicker}>
            <InsulinIcon kind="short" size={13} />
            {t().insulin.shortShort}
          </span>
          <Field
            inputMode="decimal"
            aria-label={`${t().insulin.short}, ${t().units.insulin}`}
            placeholder="0"
            value={form.shortInsulin}
            onChange={(e) => update({ shortInsulin: numeric(e.target.value) })}
          />
        </label>
        <label className="min-w-0 flex-1">
          <span className={kicker}>
            <InsulinIcon kind="long" size={13} />
            {t().insulin.longShort}
          </span>
          <Field
            inputMode="decimal"
            aria-label={`${t().insulin.long}, ${t().units.insulin}`}
            placeholder="0"
            value={form.longInsulin}
            onChange={(e) => update({ longInsulin: numeric(e.target.value) })}
          />
        </label>
      </div>

      <p className={`${kicker} mt-4 mb-1`}>{t().home.food}</p>
      <div className="pl-1.5">
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
        />
      </div>

      <p className={`${kicker} mt-4`}>{t().home.comment}</p>
      <textarea
        aria-label={t().home.comment}
        placeholder={t().home.commentPlaceholder}
        rows={2}
        maxLength={NOTES_MAX_LENGTH}
        value={form.notes}
        onChange={(e) => update({ notes: e.target.value })}
        className="block w-full resize-none rounded-field bg-tile px-3.5 py-[13px] text-base leading-5 caret-ink outline-none placeholder:text-placeholder"
      />

      <p className={`${kicker} mt-4`}>{t().home.photo}</p>
      {shownPhoto ? (
        <PhotoStrip photo={shownPhoto} onReplace={photoPicker.open} onRemove={() => setPhoto(null)} />
      ) : (
        <Button variant="secondary" className="w-full" onClick={photoPicker.open}>
          <Icon name="camera" size={15} strokeWidth={1.8} />
          {t().home.addPhoto}
        </Button>
      )}
      <PhotoInput inputRef={photoPicker.ref} onPhoto={setPhoto} />

      <div className="mt-4 space-y-[9px]">
        <Button type="submit" disabled={!isFillable(form)}>
          {t().history.edit}
        </Button>
        <Button variant="secondary" className="w-full text-text-label" onClick={() => void remove()}>
          {t().history.deleteEntry}
        </Button>
      </div>

      <DateSheet
        open={picker === 'date'}
        value={dayOf(at)}
        today={today()}
        onClose={() => setPicker(null)}
        onDone={(date) => {
          setForm((f) => withDate(f, date, entry.createdAt))
          setPicker(null)
        }}
      />
      <TimeSheet
        open={picker === 'time'}
        value={hhmm(at)}
        onClose={() => setPicker(null)}
        onDone={(time) => {
          setForm((f) => withTime(f, time, entry.createdAt))
          setPicker(null)
        }}
      />
    </form>
  )
}

function PickerButton({
  icon,
  label,
  text,
  onClick,
}: {
  icon: 'calendar' | 'clock'
  label: string
  text: string
  onClick: () => void
}) {
  return (
    <button
      type="button"
      aria-label={label}
      onClick={onClick}
      className="flex flex-1 items-center justify-center gap-2 rounded-field bg-tile px-2.5 py-[13px] text-body tabular-nums active:bg-tile-hover"
    >
      <Icon name={icon} size={15} />
      {text}
    </button>
  )
}
