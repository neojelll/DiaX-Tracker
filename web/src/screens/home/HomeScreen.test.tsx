import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, beforeEach, describe, expect, it } from 'vitest'
import { ToastProvider } from '@/components/ui/toast'
import { db } from '@/diary/db'
import { diary } from '@/diary/repository'
import { resetClock, setClock } from '@/diary/time'
import { newEntry } from '@/diary/types'
import { EMPTY_FORM, formStore } from '@/screens/home/entryForm'
import { HomeScreen } from '@/screens/home/HomeScreen'

function renderHome() {
  render(
    <ToastProvider>
      <HomeScreen />
    </ToastProvider>,
  )
}

describe('HomeScreen', () => {
  beforeEach(async () => {
    setClock(new Date(2026, 10, 15, 9, 41))
    formStore.set(EMPTY_FORM)
    await Promise.all([db.entries.clear(), db.presets.clear(), db.values.clear()])
  })
  afterEach(resetClock)

  it('saves an entry, then undo takes it back and refills the form', async () => {
    renderHome()
    const save = screen.getByRole('button', { name: /Сохранить запись/ })
    expect(save).toBeDisabled()

    await userEvent.type(screen.getByLabelText('Сахар, ммоль/л'), '6.4')
    await userEvent.type(screen.getByLabelText('Короткий инсулин, ед'), '4')
    await userEvent.type(screen.getByLabelText('Углеводы, ХЕ'), '3,5')
    expect(screen.getByLabelText('Сахар, ммоль/л')).toHaveValue('6,4')
    await userEvent.click(save)

    await waitFor(() => expect(screen.getByRole('status')).toHaveTextContent('Запись сохранена'))
    expect(screen.getByRole('status')).toHaveTextContent('Сегодня, 09:41')
    const [entry] = await diary.allEntries()
    expect(entry).toMatchObject({
      bloodSugar: 6.4,
      sugarSource: 'MANUAL',
      shortInsulinDose: 4,
      carbsGrams: 35,
      createdAt: '2026-11-15T09:41:00',
    })
    expect(screen.getByLabelText('Сахар, ммоль/л')).toHaveValue('')

    await userEvent.click(screen.getByRole('button', { name: 'Вернуть' }))
    await waitFor(async () => expect(await diary.allEntries()).toHaveLength(0))
    expect(screen.getByLabelText('Сахар, ммоль/л')).toHaveValue('6,4')
  })

  it('shows today in the tiles once saved', async () => {
    await diary.addEntry(
      newEntry('2026-11-15T09:30:00', {
        bloodSugar: 7.2,
        longInsulinDose: 18,
      }),
    )
    renderHome()
    await waitFor(() => expect(screen.getByRole('region', { name: 'Сахар' })).toHaveTextContent('7,2'))
    expect(screen.getByRole('region', { name: 'Сахар' })).toHaveTextContent('только что')
    expect(screen.getByRole('region', { name: 'Инсулин, ед' })).toHaveTextContent('0 / 18')
  })

  it('a preset fills the carbs', async () => {
    await diary.savePreset({ name: 'Овсянка', comment: '', products: [{ name: 'Хлопья', carbsGrams: 30 }] })
    renderHome()
    await userEvent.click(screen.getByRole('button', { name: /Еда/ }))
    await userEvent.click(await screen.findByRole('button', { name: /Овсянка/ }))
    expect(screen.getByLabelText('Углеводы, ХЕ')).toHaveValue('3')
    expect(screen.getByRole('button', { name: /Еда/ })).toHaveTextContent('Овсянка')
  })

  it('clearing the form can be undone', async () => {
    renderHome()
    await userEvent.type(screen.getByLabelText('Длинный инсулин, ед'), '18')
    await userEvent.click(screen.getByRole('button', { name: 'Очистить форму' }))
    expect(screen.getByLabelText('Длинный инсулин, ед')).toHaveValue('')
    await userEvent.click(screen.getByRole('button', { name: 'Вернуть' }))
    expect(screen.getByLabelText('Длинный инсулин, ед')).toHaveValue('18')
  })
})
