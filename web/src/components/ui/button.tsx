import { cva, type VariantProps } from 'class-variance-authority'
import type { ComponentProps } from 'react'
import { cn } from '@/lib/utils'

const button = cva(
  'inline-flex items-center justify-center rounded-full transition-colors select-none disabled:pointer-events-none disabled:opacity-40',
  {
    variants: {
      variant: {
        /** Black full-width pill - the one main action of a screen or sheet. */
        primary: 'w-full gap-2 bg-ink px-5 py-4 text-button text-surface active:bg-ink-hover',
        /** White outlined pill - the actions beside it. */
        secondary:
          'gap-[7px] border border-border bg-surface px-4 py-3 text-[13px] leading-[18px] text-ink active:border-border-hover active:bg-tile',
      },
    },
    defaultVariants: { variant: 'primary' },
  },
)

export function Button({
  variant,
  className,
  type = 'button',
  ...props
}: ComponentProps<'button'> & VariantProps<typeof button>) {
  return <button type={type} className={cn(button({ variant }), className)} {...props} />
}

/**
 * A round icon button. It is drawn [size] px wide, but its touch area is never under 44 px (Apple's
 * minimum) - the extra is invisible padding around it.
 */
export function CircleButton({
  size = 32,
  tone = 'tile',
  className,
  style,
  type = 'button',
  ...props
}: ComponentProps<'button'> & { size?: number; tone?: 'tile' | 'surface' | 'ink'; 'aria-label': string }) {
  return (
    <button
      type={type}
      style={{ width: size, height: size, ...style }}
      className={cn(
        'relative inline-flex shrink-0 items-center justify-center rounded-full transition-colors select-none disabled:pointer-events-none disabled:opacity-40',
        'after:absolute after:top-1/2 after:left-1/2 after:size-11 after:-translate-1/2',
        tone === 'tile' && 'bg-tile text-ink active:bg-tile-hover',
        tone === 'surface' && 'bg-surface text-ink active:bg-tile-hover',
        tone === 'ink' && 'bg-ink text-surface active:bg-ink-hover',
        className,
      )}
      {...props}
    />
  )
}

/** A filter pill: ink when on, white when off. */
export function Chip({
  selected,
  className,
  ...props
}: Omit<ComponentProps<'button'>, 'type'> & { selected: boolean }) {
  return (
    <button
      type="button"
      aria-pressed={selected}
      className={cn(
        'max-w-full truncate rounded-full px-3.5 py-2 text-[12px] leading-4 transition-colors',
        selected ? 'bg-ink text-surface' : 'bg-surface text-text-secondary active:bg-tile',
        className,
      )}
      {...props}
    />
  )
}
