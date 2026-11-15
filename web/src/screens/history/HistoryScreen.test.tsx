import { render, screen, waitFor, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, beforeEach, describe, expect, it } from 'vitest'
import { ToastProvider } from '@/components/ui/toast'
import { db } from '@/diary/db'
import { diary } from '@/diary/repository'
import { resetClock, setClock } from '@/diary/time'
import { newEntry } from '@/diary/types'
import { HistoryScreen } from '@/screens/history/HistoryScreen'

function renderHistory() {
  render(
    <ToastProvider>
      <HistoryScreen />
    </ToastProvider>,
  )
}

describe('HistoryScreen', () => {
  let mealId: number
  beforeEach(async () => {
    setClock(new Date(2026, 10, 15, 14, 0))
    await Promise.all([db.entries.clear(), db.presets.clear(), db.values.clear()])
    mealId = await diary.addEntry(
      newEntry('2026-11-15T08:00:00', {
        bloodSugar: 6.1,
        sugarSource: 'MANUAL',
        carbsGrams: 40,
        mealLabel: 'Овсянка',
        products: [{ name: 'Хлопья', carbsGrams: 40 }],
        shortInsulinDose: 4,
      }),
    )
    // The meal's +1h check: it moves into the meal's line and leaves the feed.
    await diary.addEntry(
      newEntry('2026-11-15T09:00:00', {
        bloodSugar: 9.4,
        sugarSource: 'SENSOR',
        sourceEntryId: mealId,
        sourceHour: 1,
      }),
    )
    await diary.addEntry(newEntry('2026-11-14T22:00:00', { longInsulinDose: 18, notes: 'на ночь' }))
  })
  afterEach(resetClock)

  it('lists records by day with totals; a meal check lives inside the meal', async () => {
    renderHistory()
    expect(await screen.findByText('2 зап. · 2 дн.')).toBeInTheDocument()
    const today = screen.getByRole('heading', { name: /Сегодня/ })
    expect(within(today).getByRole('img', { name: '4 ХЕ' })).toBeInTheDocument()
    expect(within(today).getByRole('img', { name: '4 ед, короткий' })).toBeInTheDocument()
    expect(screen.getByRole('heading', { name: /Вчера/ })).toBeInTheDocument()

    await userEvent.click(screen.getByRole('button', { name: '08:00, еда' }))
    expect(screen.getByText('Хлопья')).toBeInTheDocument()
    expect(screen.getByRole('img', { name: 'до еды 6,1, 1 ч 9,4' })).toBeInTheDocument()
  })

  it('searches', async () => {
    renderHistory()
    await userEvent.type(await screen.findByLabelText('Поиск'), 'ночь')
    expect(screen.getByText('совпадений: 1')).toBeInTheDocument()
    expect(screen.queryByRole('button', { name: '08:00, еда' })).not.toBeInTheDocument()
  })

  it('edits a record, and deleting one can be undone', async () => {
    renderHistory()
    await userEvent.click(await screen.findByRole('button', { name: '22:00, укол' }))
    await userEvent.click(screen.getByRole('button', { name: 'Изменить запись' }))
    const sheet = screen.getByRole('dialog', { name: 'Редактировать запись' })
    const long = within(sheet).getByLabelText('Длинный инсулин, ед')
    await userEvent.clear(long)
    await userEvent.type(long, '20')
    await userEvent.click(within(sheet).getByRole('button', { name: 'Изменить запись' }))
    await waitFor(() => expect(screen.getByRole('status')).toHaveTextContent('Изменения сохранены'))
    expect((await diary.allEntries()).find((e) => e.notes === 'на ночь')?.longInsulinDose).toBe(20)

    await userEvent.click(screen.getByRole('button', { name: '08:00, еда' }))
    await userEvent.click(screen.getByRole('button', { name: /Изменить запись/ }))
    await userEvent.click(screen.getByRole('button', { name: 'Удалить запись' }))
    await waitFor(() => expect(screen.getByRole('status')).toHaveTextContent('Запись удалена'))
    expect(await diary.getEntry(mealId)).toBeUndefined()
    await userEvent.click(screen.getByRole('button', { name: 'Вернуть' }))
    await waitFor(async () => expect(await diary.getEntry(mealId)).toBeDefined())
    // Back under its old id, so its check is still its own.
    expect(await diary.checksOf(mealId)).toHaveLength(1)
  })
})
