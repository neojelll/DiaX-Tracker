import { useState } from 'react'
import { Icon } from '@/components/icons'
import { Badge } from '@/components/ui/capsule'
import { formatCarbs, type CarbDisplay } from '@/diary/carbs'
import { usePresets, useSettings } from '@/diary/hooks'
import type { SavedPreset } from '@/diary/types'
import { decimal } from '@/lib/format'
import { applyPreset, formStore, presetGrams } from '@/screens/home/entryForm'
import { PresetDetailSheet, PresetEditSheet } from '@/screens/food/PresetSheets'
import { presetsCount } from '@/screens/food/presets'
import { t } from '@/i18n'
import { carbUnit } from '@/lib/format'

const PREVIEW_PRODUCTS = 3

/**
 * The Питание tab: the presets as a grid of cards, a preset's sheet with "Выбрать для записи", and
 * the editor. Picking fills the home form and opens the home tab.
 */
export function FoodScreen() {
  const presets = usePresets()
  const { carbDisplay: display } = useSettings()
  const [detailId, setDetailId] = useState<number | null>(null)
  const [editing, setEditing] = useState<{ preset: SavedPreset | null } | null>(null)
  const detail = presets.find((p) => p.id === detailId) ?? null

  const pick = (preset: SavedPreset) => {
    formStore.set((f) => applyPreset(f, preset, display))
    setDetailId(null)
    window.location.hash = '/entry'
  }

  return (
    <section className="px-3.5 pt-[18px]">
      <div className="px-2">
        <h1 className="text-screen-title">{t().food.title}</h1>
        <p className="mt-[5px] text-label text-text-label">{presetsCount(presets.length)}</p>
      </div>

      <ul className="mt-2.5 grid grid-cols-2 gap-2.5">
        {presets.map((p) => (
          <li key={p.id} className="flex">
            <PresetCard preset={p} display={display} onOpen={() => setDetailId(p.id)} />
          </li>
        ))}
        <li className="col-span-2">
          <button
            type="button"
            onClick={() => setEditing({ preset: null })}
            className="flex w-full items-center justify-center gap-2 rounded-record border border-dashed border-border-dashed p-[15px] text-body text-text-secondary active:bg-tile"
          >
            <Icon name="plus" size={15} strokeWidth={2} />
            {t().food.add}
          </button>
        </li>
      </ul>

      <button
        type="button"
        aria-label={t().food.addNew}
        onClick={() => setEditing({ preset: null })}
        className="fixed right-5 z-10 flex size-14 items-center justify-center rounded-full bg-ink text-surface shadow-[0_8px_20px_-6px_rgb(13_13_13/0.4)] active:bg-ink-hover"
        style={{ bottom: 'calc(env(safe-area-inset-bottom) + 92px)' }}
      >
        <Icon name="plus" size={22} strokeWidth={2} />
      </button>

      <PresetDetailSheet
        preset={detail}
        display={display}
        onPick={pick}
        onEdit={(p) => {
          setDetailId(null)
          setEditing({ preset: p })
        }}
        onClose={() => setDetailId(null)}
      />
      <PresetEditSheet
        open={editing != null}
        preset={editing?.preset ?? null}
        display={display}
        onClose={() => setEditing(null)}
      />
    </section>
  )
}

/**
 * A preset on the grid: the name in full width - two lines, then an ellipsis (#222) - the first
 * products, and the total at the bottom. Cards in a row are the same height.
 */
function PresetCard({
  preset,
  display,
  onOpen,
}: {
  preset: SavedPreset
  display: CarbDisplay
  onOpen: () => void
}) {
  const unit = carbUnit(display)
  const hidden = preset.products.length - PREVIEW_PRODUCTS
  return (
    <button
      type="button"
      onClick={onOpen}
      className="flex min-h-[150px] w-full flex-col rounded-record bg-surface p-3.5 text-left shadow-soft active:bg-tile/50"
    >
      <span className="line-clamp-2 w-full text-[14px] leading-[18px] break-words">{preset.name}</span>
      <span className="mt-2.5 mb-2.5 block w-full">
        {preset.products.slice(0, PREVIEW_PRODUCTS).map((p, i) => (
          <span key={i} className="flex gap-2 py-[2.5px] text-hint">
            <span className="min-w-0 flex-1 truncate text-text-label">{p.name}</span>
            <span className="text-text-secondary tabular-nums">
              {decimal(formatCarbs(p.carbsGrams, display))}
            </span>
          </span>
        ))}
        {hidden > 0 && (
          <span className="mt-[2.5px] block text-card-label text-text-tertiary">{t().food.more(hidden)}</span>
        )}
      </span>
      <Badge className="mt-auto self-start bg-tile">
        {decimal(formatCarbs(presetGrams(preset), display))}&#8239;{unit}
      </Badge>
    </button>
  )
}
