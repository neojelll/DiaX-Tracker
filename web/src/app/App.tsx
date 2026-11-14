import { useTab, type Tab } from '@/app/tabs'
import { Dock, type DockItem } from '@/components/Dock'
import { CHANNEL } from '@/lib/channel'
import { Placeholder } from '@/screens/Placeholder'

const DOCK: DockItem[] = [
  { tab: 'entry', label: 'Запись', icon: 'home' },
  { tab: 'food', label: 'Питание', icon: 'dish' },
  { tab: 'history', label: 'История', icon: 'history' },
  { tab: 'settings', label: 'Настройки', icon: 'settings' },
]

const SCREENS: Record<Tab, { title: string; note: string }> = {
  entry: { title: 'Добрый день!', note: 'Здесь будет новая запись: сахар, инсулин, еда.' },
  food: { title: 'Питание', note: 'Здесь будут пресеты блюд.' },
  history: { title: 'История', note: 'Здесь будут записи по дням.' },
  settings: { title: 'Настройки', note: 'Здесь будут настройки и бэкап.' },
}

export function App() {
  const [tab, openTab] = useTab()
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
      <main role="tabpanel">
        <Placeholder {...SCREENS[tab]} />
      </main>
      <Dock items={DOCK} current={tab} onSelect={openTab} />
    </div>
  )
}
