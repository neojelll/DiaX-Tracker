import { afterMealPeak, type AfterMealPoint } from '@/diary/afterMeal'
import { glucoseLevel } from '@/diary/glucose'
import type { GlucoseRange } from '@/diary/settings'
import { oneDecimal } from '@/lib/format'

const W = 320
const H = 88
const SIDE = 18
const TOP = 20
const BOTTOM = H - 20

// Peak colours by level; in range the peak stays ink - colour only where it matters.
const PEAK_FILL: Record<string, string> = {
  veryLow: 'var(--color-sugar-very-low)',
  low: 'var(--color-sugar-low)',
  high: 'var(--color-sugar-high)',
  veryHigh: 'var(--color-sugar-very-high)',
}

/**
 * "Сахар после еды" in an opened record: a quiet line - the sugar logged with the meal (pale), then
 * +1h..+4h, a number over each point, the peak a little bolder. No band, no axis: a diary entry,
 * not a monitor. (Android AfterMealLine.kt.)
 */
export function AfterMealLine({ points, range }: { points: AfterMealPoint[]; range: GlucoseRange }) {
  const peak = afterMealPeak(points)
  const label = (p: AfterMealPoint) => (p.hour === 0 ? 'до еды' : `${p.hour} ч`)
  const values = points.flatMap((p) => (p.value == null ? [] : [p.value]))
  // Room around the readings, but a normal day never squeezed into a steep-looking line.
  const lo = Math.min(4, Math.min(...values, 4) - 0.5)
  const hi = Math.max(11, Math.max(...values, 11) + 0.5)
  const x = (i: number) => SIDE + (i * (W - SIDE * 2)) / (points.length - 1)
  const y = (v: number) => BOTTOM - ((v - lo) / (hi - lo)) * (BOTTOM - TOP)
  const line = points
    .map((p, i) => (p.value == null ? null : `${x(i)},${y(p.value)}`))
    .filter(Boolean)
    .join(' ')
  const readable = points
    .filter((p) => p.value != null)
    .map((p) => `${label(p)} ${oneDecimal(p.value!)}`)
    .join(', ')

  return (
    <div className="rounded-field bg-tile px-3 py-2.5">
      <p className="text-card-label text-text-label">сахар после еды</p>
      <svg viewBox={`0 0 ${W} ${H}`} role="img" aria-label={readable} className="mt-1 block w-full">
        <polyline
          points={line}
          fill="none"
          stroke="var(--color-ink)"
          strokeWidth={2}
          strokeLinecap="round"
          strokeLinejoin="round"
        />
        {points.map((p, i) => {
          const before = p.hour === 0
          const isPeak = i === peak
          const level = p.value == null ? null : glucoseLevel(p.value, range)
          const tint = before
            ? 'var(--color-placeholder)'
            : isPeak && level
              ? (PEAK_FILL[level] ?? 'var(--color-ink)')
              : 'var(--color-ink)'
          return (
            <g key={p.hour}>
              {p.value != null && (
                <>
                  <circle
                    cx={x(i)}
                    cy={y(p.value)}
                    r={isPeak ? 3.5 : 2.5}
                    fill={before ? 'var(--color-placeholder)' : 'var(--color-ink)'}
                  />
                  <text
                    x={x(i)}
                    y={y(p.value) - 7}
                    textAnchor="middle"
                    fill={tint}
                    fontSize={isPeak ? 12 : 11}
                    fontWeight={isPeak ? 600 : 400}
                    className="tabular-nums"
                  >
                    {oneDecimal(p.value)}
                  </text>
                </>
              )}
              <text x={x(i)} y={H - 2} textAnchor="middle" fontSize={9.5} fill="var(--color-text-tertiary)">
                {label(p)}
              </text>
            </g>
          )
        })}
      </svg>
    </div>
  )
}
