import { render, screen } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import { describe, expect, it } from 'vitest'
import { App } from '@/app/App'

describe('App', () => {
  it('opens on the entry tab', () => {
    render(<App />)
    expect(screen.getByRole('tab', { name: 'Запись' })).toHaveAttribute('aria-selected', 'true')
    expect(screen.getByRole('heading')).toHaveTextContent('Добрый день!')
  })

  it('switches tabs from the dock and keeps the tab in the URL', async () => {
    render(<App />)
    await userEvent.click(screen.getByRole('tab', { name: 'История' }))
    expect(window.location.hash).toBe('#/history')
    expect(screen.getByRole('tab', { name: 'История' })).toHaveAttribute('aria-selected', 'true')
    expect(screen.getByRole('heading')).toHaveTextContent('История')
  })
})
