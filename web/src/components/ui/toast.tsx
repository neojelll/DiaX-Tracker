import { useCallback, useEffect, useMemo, useRef, useState, type ReactNode } from 'react'
import { Icon } from '@/components/icons'
import { useKeyboardInset } from '@/lib/keyboard'
import { useAnySheetOpen } from '@/lib/sheets'
import { TOAST_MS, TOAST_WITH_UNDO_MS, ToastContext, type ToastKind, type ToastOptions } from '@/lib/toast'
import { cn } from '@/lib/utils'
import { t } from '@/i18n'

/**
 * One toast at a time, as on Android: a new one replaces the current one (no queue). It sits above
 * the dock, or near the bottom over an open sheet, and above the keyboard.
 */
const LEAVE_MS = 160

interface Toast extends ToastOptions {
  id: number
  text: string
}

export function ToastProvider({ children }: { children: ReactNode }) {
  const [toast, setToast] = useState<Toast | null>(null)
  const [leaving, setLeaving] = useState(false)
  const nextId = useRef(0)

  const show = useCallback((text: string, options: ToastOptions = {}) => {
    setLeaving(false)
    setToast({ ...options, text, id: ++nextId.current })
  }, [])
  const hide = useCallback(() => setLeaving(true), [])
  const toasts = useMemo(() => ({ show, hide }), [show, hide])

  const duration = toast?.onUndo ? TOAST_WITH_UNDO_MS : TOAST_MS
  useEffect(() => {
    if (!toast) return
    const timer = setTimeout(hide, duration)
    return () => clearTimeout(timer)
  }, [toast, duration, hide])

  useEffect(() => {
    if (!leaving || !toast) return
    const id = toast.id
    const timer = setTimeout(() => setToast((t) => (t?.id === id ? null : t)), LEAVE_MS)
    return () => clearTimeout(timer)
  }, [leaving, toast])

  const undo = () => {
    const onUndo = toast?.onUndo
    if (!onUndo) return
    onUndo()
    show(t().common.restored)
  }

  return (
    <ToastContext value={toasts}>
      {children}
      <ToastHost toast={toast} leaving={leaving} duration={duration} onHide={hide} onUndo={undo} />
    </ToastContext>
  )
}

function ToastHost({
  toast,
  leaving,
  duration,
  onHide,
  onUndo,
}: {
  toast: Toast | null
  leaving: boolean
  duration: number
  onHide: () => void
  onUndo: () => void
}) {
  const sheetOpen = useAnySheetOpen()
  const keyboard = useKeyboardInset()
  const bottom =
    keyboard.bottom > 0
      ? keyboard.bottom + 12
      : sheetOpen
        ? 'calc(env(safe-area-inset-bottom) + 12px)'
        : // Clear of the floating dock (Dock.tsx).
          'calc(env(safe-area-inset-bottom) + 84px)'
  return (
    <div
      data-toast
      role="status"
      // pointer-events: an open sheet turns them off for the page; the toast keeps its undo.
      className="pointer-events-auto fixed inset-x-3.5 z-50 mx-auto max-w-[452px] transition-[bottom] duration-200 ease-gluko"
      style={{ bottom }}
    >
      {toast && (
        <div
          key={toast.id}
          onClick={onHide}
          className={cn(
            'relative overflow-hidden rounded-[20px] bg-ink shadow-toast',
            leaving ? 'animate-toast-out' : 'animate-toast-in',
          )}
        >
          <div className="flex items-center gap-[11px] py-[11px] pr-3 pl-[11px]">
            <ToastIcon kind={toast.kind ?? 'ok'} />
            <div className="min-w-0 flex-1">
              <p className="text-[13px] leading-[17px] text-surface">{toast.text}</p>
              {toast.sub ? (
                <p className="mt-0.5 line-clamp-2 text-[11px] leading-[14px] text-text-on-dark">
                  {toast.sub}
                </p>
              ) : null}
            </div>
            {toast.onUndo ? (
              <button
                type="button"
                onClick={(e) => {
                  e.stopPropagation()
                  onUndo()
                }}
                // The pill stays its drawn size; the invisible padding makes the touch target 44 px.
                className="relative -my-1 rounded-full bg-surface px-[13px] py-2 text-[12px] leading-4 text-ink after:absolute after:-inset-x-1 after:-inset-y-2"
              >
                {t().common.undo}
              </button>
            ) : null}
          </div>
          <div
            aria-hidden="true"
            className="absolute inset-x-0 bottom-0 h-0.5 origin-left animate-drain bg-surface/28"
            style={{ animationDuration: `${duration}ms` }}
          />
        </div>
      )}
    </div>
  )
}

function ToastIcon({ kind }: { kind: ToastKind }) {
  return (
    <span
      aria-hidden="true"
      className={cn(
        'flex size-[30px] shrink-0 items-center justify-center rounded-full',
        kind === 'delete' ? 'bg-surface/14 text-surface' : 'bg-surface text-ink',
      )}
    >
      {kind === 'ok' && <Icon name="check" size={15} strokeWidth={2.4} />}
      {kind === 'delete' && <Icon name="trash" size={15} strokeWidth={1.9} />}
      {kind === 'error' && <Icon name="alert" size={15} strokeWidth={2.4} />}
    </span>
  )
}
