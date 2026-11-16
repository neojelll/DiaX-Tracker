import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { App } from '@/app/App'
import { BackupError, readAndroidBackup, type AndroidBackup } from '@/backup/androidBackup'
import { db } from '@/diary/db'
import { diary } from '@/diary/repository'
import { newEntry, type SavedEntry } from '@/diary/types'

// The zip and sql.js are covered in androidBackup.test.ts; here the card gets a read archive.
vi.mock('@/backup/androidBackup', async (actual) => ({
  ...(await actual<typeof import('@/backup/androidBackup')>()),
  readAndroidBackup: vi.fn(),
}))
const read = vi.mocked(readAndroidBackup)

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
const backup: AndroidBackup = {
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
    await userEvent.upload(await screen.findByLabelText('Импорт из Android'), zip)

    const sheet = await screen.findByRole('dialog', { name: 'Импорт из Android' })
    // The check after the meal is inside it, as History counts.
    expect(sheet).toHaveTextContent('Записей2')
    expect(sheet).toHaveTextContent('2025')
    expect(sheet).toHaveTextContent('Приёмов пищи1')
    expect(sheet).toHaveTextContent('Фото1')
    expect(sheet).not.toHaveTextContent('заменит')

    await userEvent.click(screen.getByRole('button', { name: 'Импортировать' }))
    expect(await screen.findByText('Импортировано записей: 2')).toBeInTheDocument()
    expect(screen.getByText('Настройки задайте заново')).toBeInTheDocument()
    expect((await diary.allEntries()).map((e) => e.id)).toEqual([12, 9, 7])
    expect((await diary.allPresets()).map((p) => p.name)).toEqual(['Овсянка'])
  })

  it('asks before replacing a diary that has records', async () => {
    await diary.addEntry(newEntry('2026-11-10T10:00:00', { bloodSugar: 5 }))
    read.mockResolvedValue(backup)
    render(<App />)
    await userEvent.upload(await screen.findByLabelText('Импорт из Android'), zip)

    const sheet = await screen.findByRole('dialog', { name: 'Импорт из Android' })
    expect(sheet).toHaveTextContent('В дневнике уже есть записи (1)')
    await userEvent.click(screen.getByRole('button', { name: 'Заменить дневник' }))
    await waitFor(async () => expect((await diary.allEntries()).map((e) => e.id)).toEqual([12, 9, 7]))
  })

  it('leaves the diary alone when the sheet is closed', async () => {
    await diary.addEntry(newEntry('2026-11-10T10:00:00', { bloodSugar: 5 }))
    read.mockResolvedValue(backup)
    render(<App />)
    await userEvent.upload(await screen.findByLabelText('Импорт из Android'), zip)
    await screen.findByRole('dialog', { name: 'Импорт из Android' })
    await userEvent.keyboard('{Escape}')
    await waitFor(() => expect(screen.queryByRole('dialog')).not.toBeInTheDocument())
    expect(await diary.countEntries()).toBe(1)
  })

  it('says what is wrong with a file it can not import', async () => {
    read.mockRejectedValueOnce(new BackupError('not-backup'))
    read.mockRejectedValueOnce(new BackupError('old-version'))
    render(<App />)
    const input = await screen.findByLabelText('Импорт из Android')

    await userEvent.upload(input, zip)
    expect(await screen.findByText('Это не архив DiaX')).toBeInTheDocument()
    await userEvent.upload(input, zip)
    expect(await screen.findByText('Архив старой версии')).toBeInTheDocument()
    expect(screen.queryByRole('dialog')).not.toBeInTheDocument()
  })
})
