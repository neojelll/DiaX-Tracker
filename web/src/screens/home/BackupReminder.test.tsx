import { render, screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { App } from '@/app/App'
import { db } from '@/diary/db'
import { diary } from '@/diary/repository'
import { nowLocal, plusDays, today } from '@/diary/time'
import { newEntry } from '@/diary/types'

describe('BackupReminder', () => {
  const share = vi.fn<(data: ShareData) => Promise<void>>()

  beforeEach(async () => {
    window.location.hash = '#/entry'
    share.mockReset().mockResolvedValue()
    Object.assign(navigator, { canShare: () => true, share })
    await Promise.all([db.entries.clear(), db.presets.clear(), db.values.clear(), db.photos.clear()])
  })

  afterEach(() => {
    Reflect.deleteProperty(navigator, 'canShare')
    Reflect.deleteProperty(navigator, 'share')
  })

  it('stays away from a diary a few days old', async () => {
    await diary.addEntry(newEntry(`${plusDays(today(), -3)}T10:00:00`, { bloodSugar: 5 }))
    render(<App />)
    await screen.findByRole('form')
    await new Promise((r) => setTimeout(r, 50))
    expect(screen.queryByText('Архив ещё не сохранялся')).not.toBeInTheDocument()
  })

  it('reminds after a week without an archive and saves one from Home', async () => {
    await diary.addEntry(newEntry(`${plusDays(today(), -30)}T10:00:00`, { bloodSugar: 5 }))
    await diary.markBackedUp(`${plusDays(today(), -9)}T10:00:00`)
    render(<App />)
    const line = (await screen.findByText('Архив не сохранялся 9 дн.')).closest('div')!
    await userEvent.click(within(line).getByRole('button', { name: 'Сохранить' }))

    await waitFor(() => expect(screen.queryByText('Архив не сохранялся 9 дн.')).not.toBeInTheDocument())
    expect(share).toHaveBeenCalledOnce()
    expect((await diary.lastBackup())!.slice(0, 10)).toBe(nowLocal().slice(0, 10))
  })
})
