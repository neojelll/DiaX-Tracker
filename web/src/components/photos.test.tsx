import { render, screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it, vi } from 'vitest'
import { App } from '@/app/App'
import { db } from '@/diary/db'
import { diary } from '@/diary/repository'
import { nowLocal } from '@/diary/time'
import { newEntry } from '@/diary/types'
import { compressPhoto } from '@/lib/photo'

// jsdom can't draw on a canvas; compressing is covered by fitWithin and checked in the browser.
vi.mock('@/lib/photo', async (actual) => ({
  ...(await actual<typeof import('@/lib/photo')>()),
  compressPhoto: vi.fn(async () => new Blob(['small jpeg'], { type: 'image/jpeg' })),
}))

const camera = () => new File(['big heic'], 'IMG_0001.HEIC', { type: 'image/heic' })

describe('meal photos', () => {
  beforeEach(async () => {
    vi.mocked(compressPhoto).mockClear()
    await Promise.all([db.entries.clear(), db.presets.clear(), db.values.clear(), db.photos.clear()])
  })

  it('attaches a photo on Home and saves it with the entry', async () => {
    window.location.hash = '#/entry'
    render(<App />)
    const form = await screen.findByRole('form')
    await userEvent.upload(within(form).getByLabelText('Добавить фото'), camera())

    expect(await within(form).findByText('Добавлено')).toBeInTheDocument()
    expect(within(form).getByRole('button', { name: 'Заменить фото' })).toBeInTheDocument()
    await userEvent.type(within(form).getByLabelText('Сахар, ммоль/л'), '6.4')
    await userEvent.click(within(form).getByRole('button', { name: /Сохранить запись/ }))

    await waitFor(async () => expect(await diary.photoIds()).toHaveLength(1))
    const [entry] = await diary.allEntries()
    expect(await diary.photoIds()).toEqual([entry.id])
    // The form is empty again, the photo with it.
    expect(within(form).queryByText('Добавлено')).not.toBeInTheDocument()
  })

  it('drops the attached photo with ×', async () => {
    window.location.hash = '#/entry'
    render(<App />)
    const form = await screen.findByRole('form')
    await userEvent.upload(within(form).getByLabelText('Добавить фото'), camera())
    await userEvent.click(await within(form).findByRole('button', { name: 'Удалить фото' }))
    expect(within(form).queryByText('Добавлено')).not.toBeInTheDocument()
  })

  it('shows the photo in the opened record and over the whole screen', async () => {
    const id = await diary.addEntry(newEntry(nowLocal(), { carbsGrams: 40 }), new Blob(['jpeg']))
    window.location.hash = '#/history'
    render(<App />)
    await userEvent.click(await screen.findByRole('button', { name: /еда/ }))
    await userEvent.click(await screen.findByRole('button', { name: 'Фото записи' }))

    const viewer = await screen.findByRole('dialog', { name: 'Фото записи' })
    expect(viewer).toHaveTextContent(/Сегодня, \d\d:\d\d/)
    await userEvent.click(within(viewer).getByRole('button', { name: 'Закрыть' }))
    await waitFor(() => expect(screen.queryByRole('dialog')).not.toBeInTheDocument())
    expect(await diary.photoIds()).toEqual([id])
  })

  it('replaces and removes a photo when editing a record', async () => {
    const id = await diary.addEntry(newEntry(nowLocal(), { bloodSugar: 6 }), new Blob(['old']))
    window.location.hash = '#/history'
    render(<App />)
    // A photo is something to open: the first tap opens the record, then "Редактировать".
    await userEvent.click(await screen.findByRole('button', { name: /замер/ }))
    await userEvent.click(await screen.findByRole('button', { name: 'Изменить запись' }))
    const sheet = await screen.findByRole('dialog', { name: 'Редактировать запись' })

    await userEvent.click(await within(sheet).findByRole('button', { name: 'Удалить фото' }))
    expect(within(sheet).getByRole('button', { name: /Добавить фото/ })).toBeInTheDocument()
    await userEvent.click(within(sheet).getByRole('button', { name: 'Изменить запись' }))
    await waitFor(async () => expect(await diary.photoIds()).toEqual([]))
    // The sheet slides out first; its own "Изменить запись" is gone with it.
    await waitFor(() => expect(screen.queryByRole('dialog')).not.toBeInTheDocument())

    await diary.setPhoto(id, new Blob(['old']))
    await userEvent.click(await screen.findByRole('button', { name: 'Изменить запись' }))
    const again = await screen.findByRole('dialog', { name: 'Редактировать запись' })
    await userEvent.upload(within(again).getByLabelText('Добавить фото'), camera())
    await userEvent.click(within(again).getByRole('button', { name: 'Изменить запись' }))
    await waitFor(async () => expect(await diary.photoIds()).toEqual([id]))
    expect(compressPhoto).toHaveBeenCalledOnce()
  })
})
