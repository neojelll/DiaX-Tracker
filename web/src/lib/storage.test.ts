import { describe, expect, it, vi } from 'vitest'
import { keepStorage } from '@/lib/storage'

const manager = (persisted: boolean, persist: boolean | Error) =>
  ({
    persisted: vi.fn(async () => persisted),
    persist: vi.fn(async () => {
      if (persist instanceof Error) throw persist
      return persist
    }),
  }) as unknown as StorageManager

describe('keepStorage', () => {
  it('asks only when storage is not kept yet', async () => {
    const kept = manager(true, false)
    expect(await keepStorage(kept)).toBe(true)
    expect(kept.persist).not.toHaveBeenCalled()

    const asked = manager(false, true)
    expect(await keepStorage(asked)).toBe(true)
    expect(asked.persist).toHaveBeenCalledOnce()
  })

  it('says no when the browser refuses, fails or has no API', async () => {
    expect(await keepStorage(manager(false, false))).toBe(false)
    expect(await keepStorage(manager(false, new Error('denied')))).toBe(false)
    expect(await keepStorage({} as StorageManager)).toBe(false)
  })
})
