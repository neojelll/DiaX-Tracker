import type { ComponentProps } from 'react'
import { cn } from '@/lib/utils'

/** The white surface everything sits on: cards of a screen. */
export function Card({ className, ...props }: ComponentProps<'div'>) {
  return <div className={cn('rounded-card bg-surface p-4 shadow-soft', className)} {...props} />
}

/** A grey block inside a card: a field, a value, a small group. Pressable when given onClick. */
export function Tile({ className, onClick, ...props }: ComponentProps<'div'>) {
  const pressable = onClick != null
  return (
    <div
      role={pressable ? 'button' : undefined}
      tabIndex={pressable ? 0 : undefined}
      onClick={onClick}
      onKeyDown={
        pressable
          ? (e) => {
              if (e.key === 'Enter' || e.key === ' ') {
                e.preventDefault()
                e.currentTarget.click()
              }
            }
          : undefined
      }
      className={cn(
        'rounded-tile bg-tile px-[13px] py-[11px]',
        pressable && 'cursor-pointer transition-colors active:bg-tile-hover',
        className,
      )}
      {...props}
    />
  )
}

/** A small caps heading over a group. */
export function Kicker({ className, ...props }: ComponentProps<'p'>) {
  return <p className={cn('text-kicker text-text-label uppercase', className)} {...props} />
}

export function Divider({ className }: { className?: string }) {
  return <hr className={cn('h-px border-0 bg-divider', className)} />
}
