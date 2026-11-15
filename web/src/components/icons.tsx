import type { SVGProps } from 'react'
import { ICON_PATHS, type IconName } from '@/components/iconPaths'
import { cn } from '@/lib/utils'

export type { IconName }

type IconProps = SVGProps<SVGSVGElement> & { name: IconName; size?: number; strokeWidth?: number }

/** A stroked icon in the current text colour; decorative - name the control it sits in instead. */
export function Icon({ name, size = 20, strokeWidth = 1.8, ...props }: IconProps) {
  return (
    <svg
      width={size}
      height={size}
      viewBox="0 0 24 24"
      fill="none"
      stroke="currentColor"
      strokeWidth={strokeWidth}
      strokeLinecap="round"
      strokeLinejoin="round"
      aria-hidden="true"
      {...props}
    >
      <path d={ICON_PATHS[name]} />
    </svg>
  )
}

export type InsulinKind = 'short' | 'long'

/**
 * The insulin type as an icon: short - a blue syringe, long - a violet capped pen. Shape and colour
 * both differ, so it still reads without colour.
 */
export function InsulinIcon({
  kind,
  size = 16,
  className,
}: {
  kind: InsulinKind
  size?: number
  className?: string
}) {
  return kind === 'short' ? (
    <Icon name="syringe" size={size} strokeWidth={2} className={cn('text-insulin-short', className)} />
  ) : (
    <Icon name="insulinPen" size={size} strokeWidth={2} className={cn('text-insulin-long', className)} />
  )
}
