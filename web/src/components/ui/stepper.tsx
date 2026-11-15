import { CircleButton } from '@/components/ui/button'
import { Icon } from '@/components/icons'
import { cn } from '@/lib/utils'
import { t } from '@/i18n'

/**
 * A number set with − and +, on a tile: a target bound, a duration, grams per XE. The value stays in
 * [min, max]; at a bound its button is off. [step] is added as is - round in [format] if the steps
 * are fractional.
 */
export function Stepper({
  label,
  value,
  onChange,
  step = 1,
  min = -Infinity,
  max = Infinity,
  format = String,
  className,
}: {
  label: string
  value: number
  onChange: (value: number) => void
  step?: number
  min?: number
  max?: number
  format?: (value: number) => string
  className?: string
}) {
  // Rounded to the step's decimals, so 0.1 steps don't drift into 3.9000000000000004.
  const decimals = (String(step).split('.')[1] ?? '').length
  const set = (next: number) => onChange(Math.min(max, Math.max(min, Number(next.toFixed(decimals)))))
  const shown = format(value)
  return (
    <div className={cn('rounded-tile bg-tile px-[13px] py-[11px]', className)}>
      <p className="text-[11px] leading-[14px] text-text-label">{label}</p>
      <div className="mt-[7px] flex items-center">
        <CircleButton
          size={28}
          tone="surface"
          aria-label={t().common.less(label)}
          disabled={value <= min}
          onClick={() => set(value - step)}
        >
          <Icon name="minus" size={12} strokeWidth={2.4} />
        </CircleButton>
        <output
          aria-live="polite"
          aria-label={label}
          className="flex-1 text-center text-[16px] leading-[18px] font-medium"
        >
          {shown}
        </output>
        <CircleButton
          size={28}
          tone="surface"
          aria-label={t().common.more(label)}
          disabled={value >= max}
          onClick={() => set(value + step)}
        >
          <Icon name="plus" size={12} strokeWidth={2.4} />
        </CircleButton>
      </div>
    </div>
  )
}
