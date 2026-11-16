import { render, screen, waitFor } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { beforeEach, describe, expect, it } from 'vitest'
import { App } from '@/app/App'
import { db } from '@/diary/db'
import { diary } from '@/diary/repository'
import { lang } from '@/i18n'
import { numeric } from '@/lib/numeric'
import { oneDecimal } from '@/lib/format'

describe('SettingsScreen', () => {
  beforeEach(async () => {
    window.location.hash = '#/settings'
    await Promise.all([db.entries.clear(), db.presets.clear(), db.values.clear()])
  })

  it('changes the range, insulin duration and grams per XE, saved at once', async () => {
    render(<App />)
    await userEvent.click(await screen.findByRole('button', { name: 'Меньше: Нижняя граница' }))
    await userEvent.click(screen.getByRole('button', { name: 'Больше: Длительность действия инсулина' }))
    await userEvent.click(screen.getByRole('button', { name: 'Больше: Граммов в 1 ХЕ' }))
    await waitFor(async () =>
      expect(await diary.getSettings()).toMatchObject({
        insulinDurationHours: 4.5,
        carbDisplay: { unit: 'XE', gramsPerXe: 11 },
      }),
    )
    // 3.9 is the clinical floor: the low bound can't go under it.
    expect((await diary.getSettings()).glucoseRange.low).toBe(3.9)
    expect(screen.getByRole('button', { name: 'Меньше: Нижняя граница' })).toBeDisabled()
  })

  it('switches carbs to grams; the grams-per-XE step goes away', async () => {
    render(<App />)
    await userEvent.click(await screen.findByRole('radio', { name: 'Граммы' }))
    await waitFor(async () => expect((await diary.getSettings()).carbDisplay.unit).toBe('GRAMS'))
    await waitFor(() => expect(screen.queryByText('Граммов в 1 ХЕ')).not.toBeInTheDocument())
  })

  it('sets a daily goal by typing and by steps', async () => {
    render(<App />)
    await userEvent.click(await screen.findByRole('switch', { name: 'Цель по углеводам на день' }))
    await userEvent.type(await screen.findByLabelText('Цель, ХЕ'), '12')
    expect(screen.getByText('≈ 120 г')).toBeInTheDocument()
    await userEvent.click(screen.getByRole('button', { name: 'Больше: Цель по углеводам на день' }))
    expect(screen.getByLabelText('Цель, ХЕ')).toHaveValue('12,5')
    await waitFor(async () =>
      expect((await diary.getSettings()).carbGoal).toEqual({ enabled: true, grams: 125 }),
    )
  })

  it('switches the whole app to English, decimals with a dot', async () => {
    render(<App />)
    await userEvent.click(await screen.findByRole('radio', { name: 'English' }))
    expect(lang()).toBe('en')
    expect(await screen.findByRole('heading', { level: 1 })).toHaveTextContent('Settings')
    expect(screen.getByRole('tab', { name: 'History' })).toBeInTheDocument()
    // The remount reads the settings again before the cards show.
    expect(await screen.findByText('3.9 — 10.0')).toBeInTheDocument()
    expect(oneDecimal(6.4)).toBe('6.4')
    expect(numeric('6,4')).toBe('6.4')
    expect(localStorage.getItem('diax:lang')).toBe('en')
  })
})
