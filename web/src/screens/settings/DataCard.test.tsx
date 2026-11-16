import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { App } from '@/app/App'
import { readArchive } from '@/backup/archive'
import { BackupError, type DiaryArchive } from '@/backup/types'
import { db } from '@/diary/db'
import { diary } from '@/diary/repository'
import { DEFAULT_SETTINGS } from '@/diary/settings'
import { newEntry, type SavedEntry } from '@/diary/types'

// The zip and sql.js are covered in androidBackup.test.ts; here the card gets a read archive.
vi.mock('@/backup/archive', async (actual) => ({
  ...(await actual<typeof import('@/backup/archive')>()),
  readArchive: vi.fn(),
}))
const read = vi.mocked(readArchive)

const meal: SavedEntry = { ...newEntry('2025-03-02T08:20:00', { carbsGrams: 48 }), id: 7 }
const check: SavedEntry = {
  ...newEntry('2025-03-02T09:20:00', {
    bloodSugar: 8.4,
    sugarSource: 'SENSOR',
    sourceEntryId: 7,
    sourceHour: 1,
  }),
  id: 9,
}
const shot: SavedEntry = { ...newEntry('2026-11-01T22:00:00', { longInsulinDose: 18 }), id: 12 }
const backup: DiaryArchive = {
  source: 'android',
  settings: null,
  entries: [meal, check, shot],
  presets: [{ name: 'Овсянка', comment: '', products: [{ name: 'хлопья', carbsGrams: 30 }] }],
  photos: new Map([[7, new Blob(['jpeg'])]]),
}
const zip = new File(['zip'], 'diax-2026-11-01.zip', { type: 'application/zip' })

describe('DataCard', () => {
  beforeEach(async () => {
    window.location.hash = '#/settings'
    read.mockReset()
    await Promise.all([db.entries.clear(), db.presets.clear(), db.values.clear(), db.photos.clear()])
  })

  it('shows what the archive holds and imports it into an empty diary', async () => {
    read.mockResolvedValue(backup)
    render(<App />)
    await userEvent.upload(await screen.findByLabelText('Восстановить из архива'), zip)

    const sheet = await screen.findByRole('dialog', { name: 'Импорт из Android' })
    // The check after the meal is inside it, as History counts.
    expect(sheet).toHaveTextContent('Записей2')
    expect(sheet).toHaveTextContent('2025')
    expect(sheet).toHaveTextContent('Приёмов пищи1')
    expect(sheet).toHaveTextContent('Фото1')
    expect(sheet).not.toHaveTextContent('заменит')

    await userEvent.click(screen.getByRole('button', { name: 'Восстановить' }))
    expect(await screen.findByText('Восстановлено записей: 2')).toBeInTheDocument()
    expect(screen.getByText('Настройки задайте заново')).toBeInTheDocument()
    expect((await diary.allEntries()).map((e) => e.id)).toEqual([12, 9, 7])
    expect((await diary.allPresets()).map((p) => p.name)).toEqual(['Овсянка'])
  })

  it('asks before replacing a diary that has records, counted as History counts them', async () => {
    const own = await diary.addEntry(newEntry('2026-11-10T10:00:00', { carbsGrams: 20 }))
    await diary.addEntry(
      newEntry('2026-11-10T11:00:00', {
        bloodSugar: 7,
        sugarSource: 'SENSOR',
        sourceEntryId: own,
        sourceHour: 1,
      }),
    )
    read.mockResolvedValue(backup)
    render(<App />)
    await userEvent.upload(await screen.findByLabelText('Восстановить из архива'), zip)

    const sheet = await screen.findByRole('dialog', { name: 'Импорт из Android' })
    expect(sheet).toHaveTextContent('В дневнике уже есть записи (1)')
    await userEvent.click(screen.getByRole('button', { name: 'Заменить дневник' }))
    await waitFor(async () => expect((await diary.allEntries()).map((e) => e.id)).toEqual([12, 9, 7]))
  })

  it('leaves the diary alone when the sheet is closed', async () => {
    await diary.addEntry(newEntry('2026-11-10T10:00:00', { bloodSugar: 5 }))
    read.mockResolvedValue(backup)
    render(<App />)
    await userEvent.upload(await screen.findByLabelText('Восстановить из архива'), zip)
    await screen.findByRole('dialog', { name: 'Импорт из Android' })
    await userEvent.keyboard('{Escape}')
    await waitFor(() => expect(screen.queryByRole('dialog')).not.toBeInTheDocument())
    expect(await diary.countEntries()).toBe(1)
  })

  it('says what is wrong with a file it can not import', async () => {
    read.mockRejectedValueOnce(new BackupError('not-backup'))
    read.mockRejectedValueOnce(new BackupError('old-version'))
    render(<App />)
    const input = await screen.findByLabelText('Восстановить из архива')

    await userEvent.upload(input, zip)
    expect(await screen.findByText('Это не архив DiaX')).toBeInTheDocument()
    await userEvent.upload(input, zip)
    expect(await screen.findByText('Архив старой версии')).toBeInTheDocument()
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
  })

  it('restores a PWA archive with its settings', async () => {
    read.mockResolvedValue({
      ...backup,
      source: 'web',
      settings: { ...DEFAULT_SETTINGS, insulinDurationHours: 5 },
    })
    render(<App />)
    await userEvent.upload(await screen.findByLabelText('Восстановить из архива'), zip)
    const sheet = await screen.findByRole('dialog', { name: 'Восстановление' })
    expect(sheet).toHaveTextContent('Настройки тоже вернутся из архива.')
    await userEvent.click(screen.getByRole('button', { name: 'Восстановить' }))
    expect(await screen.findByText('Восстановлено записей: 2')).toBeInTheDocument()
    expect(screen.queryByText('Настройки задайте заново')).not.toBeInTheDocument()
    expect((await diary.getSettings()).insulinDurationHours).toBe(5)
  })
})

describe('saving an archive', () => {
  const share = vi.fn<(data: ShareData) => Promise<void>>()

  beforeEach(async () => {
    window.location.hash = '#/settings'
    share.mockReset()
    Object.assign(navigator, { canShare: () => true, share })
    await Promise.all([db.entries.clear(), db.presets.clear(), db.values.clear(), db.photos.clear()])
  })

  afterEach(() => {
    Reflect.deleteProperty(navigator, 'canShare')
    Reflect.deleteProperty(navigator, 'share')
  })

  it('hands the whole diary to the share sheet and remembers when', async () => {
    await diary.addEntry(newEntry('2026-11-10T10:00:00', { bloodSugar: 5 }))
    share.mockResolvedValue()
    render(<App />)
    expect(await screen.findByText('Ещё не сохранялся')).toBeInTheDocument()
    await userEvent.click(screen.getByRole('button', { name: /Сохранить архив/ }))

    expect(await screen.findByText('Архив сохранён')).toBeInTheDocument()
    const file = share.mock.calls[0][0].files![0]
    expect(file.name).toMatch(/^diax-\d{4}-\d{2}-\d{2}\.zip$/)
    expect(await diary.lastBackup()).not.toBeNull()
    expect(await screen.findByText(/^Последний: Сегодня/)).toBeInTheDocument()
  })

  it('asks for one more tap when the browser refuses a late share, and says nothing on cancel', async () => {
    share
      .mockRejectedValueOnce(new DOMException('late', 'NotAllowedError'))
      .mockRejectedValueOnce(new DOMException('closed', 'AbortError'))
    render(<App />)
    await userEvent.click(await screen.findByRole('button', { name: /Сохранить архив/ }))

    await screen.findByRole('dialog', { name: 'Архив готов' })
    await userEvent.click(screen.getByRole('button', { name: 'Сохранить' }))
    await waitFor(() => expect(screen.queryByRole('dialog')).not.toBeInTheDocument())
    expect(share).toHaveBeenCalledTimes(2)
    expect(screen.queryByText('Архив сохранён')).not.toBeInTheDocument()
    expect(await diary.lastBackup()).toBeNull()
  })
})
