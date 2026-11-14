import { useEffect, useSyncExternalStore } from 'react'

/** How many sheets are open, for the toast to sit above them rather than above the dock. */

let openSheets = 0
const listeners = new Set<() => void>()

export function useSheetCount(open: boolean) {
  useEffect(() => {
    if (!open) return
    openSheets++
    listeners.forEach((l) => l())
    return () => {
      openSheets--
      listeners.forEach((l) => l())
    }
  }, [open])
}

export function useAnySheetOpen(): boolean {
  return useSyncExternalStore(
    (l) => {
      listeners.add(l)
      return () => listeners.delete(l)
    },
    () => openSheets > 0,
    () => false,
  )
}
