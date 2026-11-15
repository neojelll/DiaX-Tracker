/**
 * A typed number as the field keeps it: digits and one decimal separator, always a comma - the
 * Russian way, as the iPhone number pad types it and as the app shows numbers (lib/format.ts).
 * parseNumber (diary/carbs.ts) reads it back.
 */
export function numeric(input: string): string {
  const cleaned = input.replace(/\./g, ',').replace(/[^\d,]/g, '')
  const at = cleaned.indexOf(',')
  return at < 0 ? cleaned : cleaned.slice(0, at + 1) + cleaned.slice(at + 1).replace(/,/g, '')
}
