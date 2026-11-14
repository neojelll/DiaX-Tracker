import type { Tab } from '@/app/tabs'
import { Icon, type IconName } from '@/components/icons'
import { cn } from '@/lib/utils'

export type DockItem = { tab: Tab; label: string; icon: IconName }

/**
 * Floating bottom dock, as on Android: a translucent white capsule of icons; the open tab sits on
 * an ink plate and its name slides out beside the icon. It floats above the home indicator.
 */
export function Dock({
  items,
  current,
  onSelect,
}: {
  items: DockItem[]
  current: Tab
  onSelect: (tab: Tab) => void
}) {
  return (
    <nav
      role="tablist"
      className="fixed inset-x-0 z-20 flex justify-center"
      style={{ bottom: 'calc(env(safe-area-inset-bottom) + 10px)' }}
    >
      <div className="flex gap-1 rounded-[22px] border border-ink/5 bg-surface/94 p-1.5 shadow-[0_8px_28px_-6px_rgb(13_13_13/0.22)] backdrop-blur-md">
        {items.map((item) => {
          const selected = item.tab === current
          return (
            <button
              key={item.tab}
              type="button"
              role="tab"
              aria-selected={selected}
              aria-label={item.label}
              onClick={() => onSelect(item.tab)}
              className={cn(
                'flex h-[46px] items-center rounded-[17px] px-[13px] transition-colors duration-300',
                selected ? 'bg-ink text-surface' : 'text-text-tertiary active:bg-tile',
              )}
            >
              <Icon name={item.icon} />
              {/* The name slides out on the open tab only; every tab is named for screen readers above. */}
              <span
                aria-hidden="true"
                className={cn(
                  'overflow-hidden text-[13px] font-medium whitespace-nowrap transition-[max-width,opacity,margin] duration-400 ease-[cubic-bezier(0.2,0.8,0.2,1)]',
                  selected ? 'mr-[3px] ml-2 max-w-28 opacity-100' : 'ml-0 max-w-0 opacity-0',
                )}
              >
                {item.label}
              </span>
            </button>
          )
        })}
      </div>
    </nav>
  )
}
