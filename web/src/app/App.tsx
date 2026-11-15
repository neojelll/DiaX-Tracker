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
import { SettingsScreen } from '@/screens/settings/SettingsScreen'
import { t, useLang } from '@/i18n'

const DOCK_ICONS: Record<Tab, DockItem['icon']> = {
  entry: 'home',
  food: 'dish',
  history: 'history',
  settings: 'settings',
}

// The design-system showcase (#/kit): in the dev build and `pnpm dev` only. Both conditions are
// replaced at build time, so the release build doesn't even contain it.
const KIT_ENABLED = import.meta.env.DEV || import.meta.env.VITE_CHANNEL === 'dev'
const Kit = KIT_ENABLED ? lazy(() => import('@/screens/Kit')) : null

export function App() {
  // A language switch remounts the app: every text - and every number's decimal separator - is
  // read again. What must survive (the half-filled form, the open tab) lives outside it.
  const lang = useLang()
  return (
    <ToastProvider key={lang}>
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
          <SettingsScreen
            kitLink={
              KIT_ENABLED && (
                <a href="#/kit" className="mx-2 inline-block text-body text-text-secondary underline">
                  {t().settings.kit}
                </a>
              )
            }
          />
        )}
      </main>
      <Dock
        items={(Object.keys(DOCK_ICONS) as Tab[]).map((id) => ({
          tab: id,
          label: t().tabs[id],
          icon: DOCK_ICONS[id],
        }))}
        current={tab}
        onSelect={openTab}
      />
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
