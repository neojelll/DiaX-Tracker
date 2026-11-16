import { t } from '@/i18n'

/**
 * A typed number as the field keeps it: digits and one decimal separator - the language's own,
 * whichever of "," or "." was typed (#221). parseNumber (diary/carbs.ts) reads both back.
 */
export function numeric(input: string): string {
  const sep = t().decimal
  const cleaned = input
    .replace(/[.,]/g, sep)
    .replace(new RegExp(`[^\\d${sep === '.' ? '\\.' : sep}]`, 'g'), '')
  const at = cleaned.indexOf(sep)
  return at < 0
    ? cleaned
    : cleaned.slice(0, at + 1) +
        cleaned
          .slice(at + 1)
          .split(sep)
          .join('')
}
