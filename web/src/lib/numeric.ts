/** Digits and one decimal separator; a comma becomes a dot, as the Russian number pad types a comma. */
export function numeric(input: string): string {
  const cleaned = input.replace(',', '.').replace(/[^\d.]/g, '')
  const dot = cleaned.indexOf('.')
  return dot < 0 ? cleaned : cleaned.slice(0, dot + 1) + cleaned.slice(dot + 1).replace(/\./g, '')
}
