import { createContext, use } from 'react'

/** How long a plain toast stays; one with undo stays longer, so there's time to react. */
export const TOAST_MS = 3000
export const TOAST_WITH_UNDO_MS = 5000

export type ToastKind = 'ok' | 'delete' | 'error'

export interface ToastOptions {
  /** A second, quieter line: what was saved, the reason of an error. */
  sub?: string
  kind?: ToastKind
  /** Shows "Вернуть"; tapping it hides the toast, runs this and says "Восстановлено". */
  onUndo?: () => void
}

export interface Toasts {
  show: (text: string, options?: ToastOptions) => void
  hide: () => void
}

export const ToastContext = createContext<Toasts | null>(null)

/** Toasts from any screen: `useToast().show('Запись сохранена', { onUndo })`. */
export function useToast(): Toasts {
  const toasts = use(ToastContext)
  if (!toasts) throw new Error('useToast outside ToastProvider (components/ui/toast.tsx)')
  return toasts
}
