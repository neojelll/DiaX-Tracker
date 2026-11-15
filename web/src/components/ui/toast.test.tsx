import { act, render, screen } from '@testing-library/react'
import { useEffect } from 'react'
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest'
import { ToastProvider } from '@/components/ui/toast'
import { TOAST_MS, TOAST_WITH_UNDO_MS, useToast, type Toasts } from '@/lib/toast'

const grabbed: { current?: Toasts } = {}
function Grab() {
  const toasts = useToast()
  useEffect(() => {
    grabbed.current = toasts
  }, [toasts])
  return null
}
const toasts = {
  show: (...args: Parameters<Toasts['show']>) => grabbed.current!.show(...args),
}

function renderToasts() {
  render(
    <ToastProvider>
      <Grab />
    </ToastProvider>,
  )
}

describe('toasts', () => {
  beforeEach(() => vi.useFakeTimers())
  afterEach(() => vi.useRealTimers())

  it('shows one toast and hides it after its time', () => {
    renderToasts()
    act(() => toasts.show('Запись сохранена', { sub: 'Сегодня, 09:41' }))
    expect(screen.getByRole('status')).toHaveTextContent('Запись сохранена')
    expect(screen.getByRole('status')).toHaveTextContent('Сегодня, 09:41')
    act(() => vi.advanceTimersByTime(TOAST_MS))
    act(() => vi.advanceTimersByTime(200)) // its slide-out
    expect(screen.getByRole('status')).toBeEmptyDOMElement()
  })

  it('replaces the current toast with a new one', () => {
    renderToasts()
    act(() => toasts.show('Первый'))
    act(() => toasts.show('Второй'))
    expect(screen.getByRole('status')).toHaveTextContent('Второй')
    expect(screen.getByRole('status')).not.toHaveTextContent('Первый')
  })

  it('undo runs the action, says restored, and waits longer', () => {
    renderToasts()
    const undo = vi.fn()
    act(() => toasts.show('Запись удалена', { kind: 'delete', onUndo: undo }))
    act(() => vi.advanceTimersByTime(TOAST_MS + 200))
    expect(screen.getByRole('status')).toHaveTextContent('Запись удалена')
    act(() => screen.getByRole('button', { name: 'Вернуть' }).click())
    expect(undo).toHaveBeenCalledOnce()
    expect(screen.getByRole('status')).toHaveTextContent('Восстановлено')
    expect(TOAST_WITH_UNDO_MS).toBeGreaterThan(TOAST_MS)
  })

  it('hides on tap', () => {
    renderToasts()
    act(() => toasts.show('Готово'))
    act(() => screen.getByText('Готово').click())
    act(() => vi.advanceTimersByTime(200))
    expect(screen.getByRole('status')).toBeEmptyDOMElement()
  })
})
