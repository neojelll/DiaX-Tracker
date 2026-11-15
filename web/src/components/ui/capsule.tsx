import type { ReactNode } from 'react'
import { cn } from '@/lib/utils'

/** Between a number and its unit: a narrow no-break space, so "4 ХЕ" never splits over two lines. */
export const NUMBER_UNIT_GAP = ' '

/** "4 ХЕ" with the number set bold and ink, the unit quieter after it. */
export function NumberWithUnit({ number, unit }: { number: string; unit?: string }) {
  return (
    <>
      <span className="text-[12.5px] font-medium text-ink">{number}</span>
      {unit && NUMBER_UNIT_GAP + unit}
    </>
  )
}

/**
 * A grey value capsule of a record: an icon and a value, "🍽 4 ХЕ". [label] is what a screen reader
 * says, since the icon carries part of the meaning ("4 ХЕ" vs "4 ЕД, короткий").
 */
export function Capsule({ label, icon, children }: { label: string; icon?: ReactNode; children: ReactNode }) {
  return (
    <span
      role="img"
      aria-label={label}
      className="inline-flex items-center gap-0.5 rounded-[9px] bg-tile py-[3px] pr-[7px] pl-[5px] text-hint whitespace-nowrap text-text-secondary tabular-nums"
    >
      {icon}
      <span aria-hidden="true">{children}</span>
    </span>
  )
}

/** A small white pill with a number - "2.5 ХЕ" on a tile. */
export function Badge({ className, children }: { className?: string; children: ReactNode }) {
  return (
    <span
      className={cn(
        'inline-flex rounded-full bg-surface px-2 py-[3px] text-card-label whitespace-nowrap text-ink tabular-nums',
        className,
      )}
    >
      {children}
    </span>
  )
}
