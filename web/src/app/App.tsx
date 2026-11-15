import { lazy, Suspense } from 'react'
import { hashPath, useHash, useTab, type Tab } from '@/app/tabs'
import { Dock, type DockItem } from '@/components/Dock'
import { InsulinBanner } from '@/components/InsulinBanner'
import { ToastProvider } from '@/components/ui/toast'
import { useEntries, useSettings } from '@/diary/hooks'
import { CHANNEL } from '@/lib/channel'
import { useNow } from '@/lib/useNow'
import { FoodScreen } from '@/screens/food/FoodScreen'
import { HistoryScreen } from '@/screens/history/HistoryScreen'
import { HomeScreen } from '@/screens/home/HomeScreen'
import { Placeholder } from '@/screens/Placeholder'

const DOCK: DockItem[] = [
  { tab: 'entry', label: 'Запись', icon: 'home' },
  { tab: 'food', label: 'Питание', icon: 'dish' },
  { tab: 'history', label: 'История', icon: 'history' },
  { tab: 'settings', label: 'Настройки', icon: 'settings' },
]

const PLACEHOLDERS: Record<Exclude<Tab, 'entry' | 'history' | 'food'>, { title: string; note: string }> = {
  settings: { title: 'Настройки', note: 'Здесь будут настройки и бэкап.' },
}

// The design-system showcase (#/kit): in the dev build and `pnpm dev` only. Both conditions are
// replaced at build time, so the release build doesn't even contain it.
const KIT_ENABLED = import.meta.env.DEV || import.meta.env.VITE_CHANNEL === 'dev'
const Kit = KIT_ENABLED ? lazy(() => import('@/screens/Kit')) : null

export function App() {
  return (
    <ToastProvider>
      <Shell />
    </ToastProvider>
  )
}

function Shell() {
  const [tab, openTab] = useTab()
  const hash = useHash()
  const kit = Kit != null && hashPath(hash) === 'kit'
  return (
    <div
      className="min-h-dvh"
      style={{
        paddingTop: 'env(safe-area-inset-top)',
        // Room for the floating dock so the last line of a screen scrolls clear of it.
        paddingBottom: 'calc(env(safe-area-inset-bottom) + 90px)',
      }}
    >
      {CHANNEL === 'dev' && (
        // The dev build looks like the real one; this keeps it from being mistaken for the real diary.
        <div
          className="fixed right-3.5 z-30 rounded-full bg-ink px-2 py-0.5 text-[10.5px] font-medium tracking-wider text-surface"
          style={{ top: 'calc(env(safe-area-inset-top) + 8px)' }}
        >
          DEV
        </div>
      )}
      <ActiveInsulin />
      <main role="tabpanel">
        {kit ? (
          <Suspense>
            <Kit />
          </Suspense>
        ) : tab === 'entry' ? (
          <HomeScreen />
        ) : tab === 'history' ? (
          <HistoryScreen />
        ) : tab === 'food' ? (
          <FoodScreen />
        ) : (
          <Placeholder {...PLACEHOLDERS[tab]} />
        )}
        {KIT_ENABLED && tab === 'settings' && !kit && (
          <a href="#/kit" className="mx-3.5 mt-4 inline-block text-body text-text-secondary underline">
            Компоненты (dev)
          </a>
        )}
      </main>
      <Dock items={DOCK} current={tab} onSelect={openTab} />
    </div>
  )
}

/** Short insulin still acting, above every tab. */
function ActiveInsulin() {
  const entries = useEntries()
  const settings = useSettings()
  const now = useNow()
  return <InsulinBanner entries={entries} durationHours={settings.insulinDurationHours} now={now} />
}
