import type { ComponentProps } from 'react'
import { cn } from '@/lib/utils'

/**
 * A text field: a grey tile with no border or focus ring by design - the caret alone shows focus.
 * The text is never under 16 px: below that iOS zooms the whole page in on focus.
 * Numbers: pass inputMode="decimal" for the number pad with a decimal key.
 */
export function Field({ className, ...props }: ComponentProps<'input'>) {
  return (
    <input
      className={cn(
        'w-full rounded-field bg-tile px-3.5 py-[13px] text-base leading-5 text-ink caret-ink outline-none placeholder:text-placeholder',
        className,
      )}
      {...props}
    />
  )
}
