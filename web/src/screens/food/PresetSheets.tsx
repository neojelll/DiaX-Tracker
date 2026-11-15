import { useState } from 'react'
import { Icon } from '@/components/icons'
import { Button, CircleButton } from '@/components/ui/button'
import { Field } from '@/components/ui/field'
import { Sheet } from '@/components/ui/sheet'
import { formatCarbs, type CarbDisplay } from '@/diary/carbs'
import { diary } from '@/diary/repository'
import type { SavedPreset } from '@/diary/types'
import { carbUnit, decimal } from '@/lib/format'
import { numeric } from '@/lib/numeric'
import { useToast } from '@/lib/toast'
import { cn } from '@/lib/utils'
import { presetGrams } from '@/screens/home/entryForm'
import {
  draftGrams,
  draftOf,
  emptyProduct,
  isValidDraft,
  MAX_PRESET_PRODUCTS,
  presetOf,
  type PresetDraft,
} from '@/screens/food/presets'
import { t } from '@/i18n'

const carbsText = (grams: number, display: CarbDisplay) =>
  `${decimal(formatCarbs(grams, display))} ${carbUnit(display)}`

/**
 * A preset: its comment, the total, the composition; "Выбрать для записи" puts it into the home
 * form. Delete says "Приём пищи удалён" with «Вернуть» - entries made from it keep their own copy.
 */
export function PresetDetailSheet({
  preset,
  display,
  onPick,
  onEdit,
  onClose,
}: {
  preset: SavedPreset | null
  display: CarbDisplay
  onPick: (preset: SavedPreset) => void
  onEdit: (preset: SavedPreset) => void
  onClose: () => void
}) {
  const toast = useToast()
  // While sliding out it keeps showing the preset it had.
  const [last, setLast] = useState(preset)
  if (preset && preset !== last) setLast(preset)
  const shown = preset ?? last
  const remove = async (p: SavedPreset) => {
    const removed = await diary.deletePreset(p.id)
    onClose()
    if (removed)
      toast.show(t().food.deleted, {
        sub: removed.name,
        kind: 'delete',
        onUndo: () => void diary.restorePreset(removed),
      })
  }
  return (
    <Sheet
      open={preset != null}
      onOpenChange={(o) => !o && onClose()}
      title={shown?.name ?? ''}
      description={shown?.comment || undefined}
    >
      {shown && (
        <>
          <div className="flex items-center rounded-tile bg-tile px-3.5 py-[13px]">
            <span className="flex-1 text-body text-text-secondary">{t().food.totalCarbs}</span>
            <span className="text-[19px] leading-[22px] font-medium tabular-nums">
              {carbsText(presetGrams(shown), display)}
            </span>
          </div>
          <p className="mt-4 text-kicker text-text-label uppercase">{t().food.composition}</p>
          <ul className="mt-1 divide-y divide-divider">
            {shown.products.map((p, i) => (
              <li key={i} className="flex gap-3 py-[11px] text-body">
                <span className="flex-1">{p.name}</span>
                <span className="text-text-secondary tabular-nums">{carbsText(p.carbsGrams, display)}</span>
              </li>
            ))}
          </ul>
          <div className="mt-4 space-y-[9px]">
            <Button onClick={() => onPick(shown)}>{t().food.pick}</Button>
            <div className="flex gap-[9px]">
              <Button variant="secondary" className="flex-1" onClick={() => onEdit(shown)}>
                {t().common.edit}
              </Button>
              <Button
                variant="secondary"
                className="flex-1 text-text-label"
                onClick={() => void remove(shown)}
              >
                {t().common.delete}
              </Button>
            </div>
          </div>
        </>
      )}
    </Sheet>
  )
}

/**
 * Creating or changing a preset: a name, up to ten products with their carbs (in the display unit),
 * the total, a comment. Saving returns the saved preset - the home form picks a new one right away.
 */
export function PresetEditSheet({
  open,
  preset,
  display,
  onSaved,
  onClose,
}: {
  open: boolean
  /** null - a new preset. */
  preset: SavedPreset | null
  display: CarbDisplay
  onSaved?: (preset: SavedPreset) => void
  onClose: () => void
}) {
  // While sliding out it keeps what it showed; the form remounts fresh on the next opening.
  const [shown, setShown] = useState(preset)
  if (open && preset !== shown) setShown(preset)
  return (
    <Sheet
      open={open}
      onOpenChange={(o) => !o && onClose()}
      title={shown ? t().food.editTitle : t().food.add}
    >
      <EditForm
        key={shown?.id ?? 'new'}
        preset={shown}
        display={display}
        onSaved={onSaved}
        onClose={onClose}
      />
    </Sheet>
  )
}

function EditForm({
  preset,
  display,
  onSaved,
  onClose,
}: {
  preset: SavedPreset | null
  display: CarbDisplay
  onSaved?: (preset: SavedPreset) => void
  onClose: () => void
}) {
  const toast = useToast()
  const [draft, setDraft] = useState<PresetDraft>(() => draftOf(preset, display))
  const unit = carbUnit(display)
  const atLimit = draft.products.length >= MAX_PRESET_PRODUCTS
  const setProduct = (key: number, change: { name?: string; amount?: string }) =>
    setDraft((d) => ({ ...d, products: d.products.map((p) => (p.key === key ? { ...p, ...change } : p)) }))

  const save = async () => {
    const saved = presetOf(draft, display, preset?.id)
    const id = await diary.savePreset(saved)
    onClose()
    toast.show(preset ? t().food.updated : t().food.created, { sub: saved.name })
    onSaved?.({ ...saved, id })
  }

  const kicker = 'text-kicker text-text-label uppercase'
  return (
    <form
      onSubmit={(e) => {
        e.preventDefault()
        if (isValidDraft(draft)) void save()
      }}
    >
      <p className={kicker}>{t().food.name}</p>
      <Field
        className="mt-2"
        aria-label={t().food.name}
        placeholder={t().food.namePlaceholder}
        value={draft.name}
        onChange={(e) => setDraft((d) => ({ ...d, name: e.target.value }))}
      />

      <p className={`${kicker} mt-4 flex`}>
        <span className="flex-1">{t().food.product}</span>
        <span className="mr-[42px] w-16">{unit}</span>
      </p>
      <ul className="mt-2 space-y-2">
        {draft.products.map((p, i) => (
          <li key={p.key} className="flex items-center gap-2">
            <Field
              aria-label={t().food.productN(i + 1)}
              placeholder={t().food.productPlaceholder}
              value={p.name}
              onChange={(e) => setProduct(p.key, { name: e.target.value })}
              className="min-w-0 flex-1"
            />
            <Field
              inputMode="decimal"
              aria-label={t().food.productCarbs(i + 1, unit)}
              placeholder="0"
              value={p.amount}
              onChange={(e) => setProduct(p.key, { amount: numeric(e.target.value) })}
              className="w-16 shrink-0 px-3 tabular-nums"
            />
            <CircleButton
              size={34}
              aria-label={t().food.removeProduct(i + 1)}
              disabled={draft.products.length <= 1}
              onClick={() => setDraft((d) => ({ ...d, products: d.products.filter((x) => x.key !== p.key) }))}
            >
              <Icon name="minus" size={14} strokeWidth={2.2} />
            </CircleButton>
          </li>
        ))}
      </ul>
      <button
        type="button"
        disabled={atLimit}
        onClick={() => setDraft((d) => ({ ...d, products: [...d.products, emptyProduct()] }))}
        className="mt-1 flex items-center gap-[9px] py-1 text-body text-text-secondary disabled:text-placeholder"
      >
        <span className="flex size-[26px] items-center justify-center rounded-full bg-tile">
          <Icon name="plus" size={13} strokeWidth={2.2} />
        </span>
        {t().food.addProduct}
      </button>
      {atLimit && <p className="text-card-label text-text-tertiary">{t().food.limit(MAX_PRESET_PRODUCTS)}</p>}

      <div className="mt-3.5 flex items-center rounded-tile bg-tile px-3.5 py-3">
        <span className="flex-1 text-body text-text-secondary">{t().food.total}</span>
        <span className="text-value tabular-nums">{carbsText(draftGrams(draft, display), display)}</span>
      </div>

      <p className={`${kicker} mt-4`}>{t().home.comment}</p>
      <textarea
        aria-label={t().home.comment}
        placeholder={t().food.commentPlaceholder}
        rows={2}
        value={draft.comment}
        onChange={(e) => setDraft((d) => ({ ...d, comment: e.target.value }))}
        className={cn(
          'mt-2 block w-full resize-none rounded-field bg-tile px-3.5 py-[13px] text-base leading-5 caret-ink outline-none placeholder:text-placeholder',
        )}
      />

      <Button type="submit" className="mt-3.5" disabled={!isValidDraft(draft)}>
        {t().common.save}
      </Button>
    </form>
  )
}
