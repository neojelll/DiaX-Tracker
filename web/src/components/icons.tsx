import type { SVGProps } from 'react'
import { ICON_PATHS, type IconName } from '@/components/iconPaths'

export type { IconName }

type IconProps = SVGProps<SVGSVGElement> & { name: IconName; size?: number; strokeWidth?: number }

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
