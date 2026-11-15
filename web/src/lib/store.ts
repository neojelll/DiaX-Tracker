import { useSyncExternalStore } from 'react'

/**
 * A value that outlives the component showing it - a half-filled form stays when you switch tabs
 * and come back, as on Android.
 */
export function createStore<T>(initial: T) {
  let value = initial
  const listeners = new Set<() => void>()
  const get = () => value
  const set = (next: T | ((current: T) => T)) => {
    value = typeof next === 'function' ? (next as (current: T) => T)(value) : next
    listeners.forEach((l) => l())
  }
  const subscribe = (l: () => void) => {
    listeners.add(l)
    return () => listeners.delete(l)
  }
  const use = (): [T, typeof set] => [useSyncExternalStore(subscribe, get, get), set]
  return { get, set, use }
}
