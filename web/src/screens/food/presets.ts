import { formatCarbs, parseCarbs, parseNumber, type CarbDisplay } from '@/diary/carbs'
import type { MealPreset, Product, SavedPreset } from '@/diary/types'
import { decimal } from '@/lib/format'

export const MAX_PRESET_PRODUCTS = 10

/** A product row in the editor, as typed. [key] keeps React rows apart while they're added and removed. */
export interface ProductDraft {
  key: number
  name: string
  amount: string
}

export interface PresetDraft {
  name: string
  comment: string
  products: ProductDraft[]
}

let nextKey = 0
export const emptyProduct = (): ProductDraft => ({ key: nextKey++, name: '', amount: '' })

/** The editor's start: the preset as text, or one empty product for a new one. */
export function draftOf(preset: SavedPreset | null, display: CarbDisplay): PresetDraft {
  if (!preset) return { name: '', comment: '', products: [emptyProduct()] }
  return {
    name: preset.name,
    comment: preset.comment,
    products: preset.products.map((p) => ({
      key: nextKey++,
      name: p.name,
      amount: decimal(formatCarbs(p.carbsGrams, display)),
    })),
  }
}

/** A name, at least one product, every product named, every amount empty or a number. */
export function isValidDraft(d: PresetDraft): boolean {
  return (
    d.name.trim() !== '' &&
    d.products.length > 0 &&
    d.products.every((p) => p.name.trim() !== '' && (p.amount.trim() === '' || parseNumber(p.amount) != null))
  )
}

/** Summed in grams - every carbohydrate total is rounded once, when shown. */
export function draftGrams(d: PresetDraft, display: CarbDisplay): number {
  return d.products.reduce((sum, p) => sum + (parseCarbs(p.amount, display) ?? 0), 0)
}

export function presetOf(d: PresetDraft, display: CarbDisplay, id?: number): MealPreset {
  const products: Product[] = d.products.map((p) => ({
    name: p.name.trim(),
    carbsGrams: parseCarbs(p.amount, display) ?? 0,
  }))
  return { ...(id == null ? {} : { id }), name: d.name.trim(), comment: d.comment.trim(), products }
}

const PLURAL = new Intl.PluralRules('ru')
/** "1 пресет", "3 пресета", "5 пресетов". */
export function presetsCount(n: number): string {
  const form = PLURAL.select(n)
  return `${n} ${form === 'one' ? 'пресет' : form === 'few' ? 'пресета' : 'пресетов'}`
}
