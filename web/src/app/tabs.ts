import { useCallback, useSyncExternalStore } from 'react'

/** The four tabs of the dock, in dock order. Their ids are the URL hash: #/entry, #/food... */
export const TABS = ['entry', 'food', 'history', 'settings'] as const
export type Tab = (typeof TABS)[number]

export const DEFAULT_TAB: Tab = 'entry'

/** "#/history" -> "history". */
export function hashPath(hash: string): string {
  return hash.replace(/^#\/?/, '')
}

/** The tab a hash points at; anything unknown opens the default tab. */
export function tabFromHash(hash: string): Tab {
  const id = hashPath(hash)
  return (TABS as readonly string[]).includes(id) ? (id as Tab) : DEFAULT_TAB
}

function subscribe(onChange: () => void) {
  window.addEventListener('hashchange', onChange)
  return () => window.removeEventListener('hashchange', onChange)
}

/**
 * The open tab, kept in the URL hash: switching tabs adds a history entry, so the iOS swipe back
 * and a reload land where they should.
 */
export function useTab(): [Tab, (tab: Tab) => void] {
  const tab = tabFromHash(useHash())
  const open = useCallback((next: Tab) => {
    window.location.hash = `/${next}`
  }, [])
  return [tab, open]
}

/** The URL hash, following its changes. */
export function useHash(): string {
  return useSyncExternalStore(subscribe, () => window.location.hash)
}
