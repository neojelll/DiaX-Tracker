import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { afterEach, beforeEach, describe, expect, it } from 'vitest'
import { App } from '@/app/App'
import { resetClock, setClock } from '@/diary/time'

describe('App', () => {
  beforeEach(() => setClock(new Date(2026, 10, 15, 13, 0)))
  afterEach(resetClock)

  it('opens on the entry tab', () => {
    render(<App />)
    expect(screen.getByRole('tab', { name: 'Запись' })).toHaveAttribute('aria-selected', 'true')
    expect(screen.getByRole('heading', { level: 1 })).toHaveTextContent('Добрый день!')
  })

  it('switches tabs from the dock and keeps the tab in the URL', async () => {
    render(<App />)
    await userEvent.click(screen.getByRole('tab', { name: 'История' }))
    expect(window.location.hash).toBe('#/history')
    expect(screen.getByRole('tab', { name: 'История' })).toHaveAttribute('aria-selected', 'true')
    expect(screen.getByRole('heading', { level: 1 })).toHaveTextContent('История')
  })
})
