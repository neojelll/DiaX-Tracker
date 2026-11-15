import * as Dialog from '@radix-ui/react-dialog'
import { useEffect, useRef, useState, type PointerEvent, type ReactNode } from 'react'
import { CircleButton } from '@/components/ui/button'
import { Icon } from '@/components/icons'
import { useKeyboardInset } from '@/lib/keyboard'
import { useSheetCount } from '@/lib/sheets'
import { cn } from '@/lib/utils'
import { t } from '@/i18n'

// A drag down past this, or a flick faster than this, closes the sheet.
const CLOSE_DISTANCE_PX = 120
const CLOSE_VELOCITY_PX_PER_MS = 0.5

/**
 * A bottom sheet, as on Android: slides up over a dimmed screen; a tap on the dim, Escape, the ×
 * or a drag down by the handle closes it. Radix Dialog gives it focus trapping, the page scroll
 * lock and screen-reader semantics.
 *
 * The keyboard: on iOS it covers the page instead of shrinking it, so the sheet rides up on top of
 * it (lib/keyboard.ts) and a focused field scrolls into view inside the sheet.
 */
export function Sheet({
  open,
  onOpenChange,
  title,
  description,
  children,
}: {
  open: boolean
  onOpenChange: (open: boolean) => void
  title: string
  description?: string
  children: ReactNode
}) {
  const keyboard = useKeyboardInset()
  const body = useRef<HTMLDivElement>(null)
  const [drag, setDrag] = useState(0)
  const [dragging, setDragging] = useState(false)
  const start = useRef<{ y: number; t: number; id: number } | null>(null)
  useSheetCount(open)

  // The keyboard just came up or went away - keep the focused field in sight.
  useEffect(() => {
    const focused = document.activeElement
    if (focused instanceof HTMLElement && body.current?.contains(focused))
      focused.scrollIntoView({ block: 'nearest' })
  }, [keyboard.bottom])

  const onPointerDown = (e: PointerEvent) => {
    if ((e.target as Element).closest('button')) return
    start.current = { y: e.clientY, t: e.timeStamp, id: e.pointerId }
    setDragging(true)
    e.currentTarget.setPointerCapture(e.pointerId)
  }
  const onPointerMove = (e: PointerEvent) => {
    if (start.current?.id !== e.pointerId) return
    const dy = e.clientY - start.current.y
    // Down follows the finger; up gives a little, as an elastic end stop.
    setDrag(dy >= 0 ? dy : -Math.sqrt(-dy) * 2)
  }
  const onPointerUp = (e: PointerEvent) => {
    const from = start.current
    if (from?.id !== e.pointerId) return
    start.current = null
    setDragging(false)
    const dy = e.clientY - from.y
    const velocity = dy / Math.max(1, e.timeStamp - from.t)
    // Closing keeps the dragged offset: the slide-out continues from where the finger let go.
    if (dy > CLOSE_DISTANCE_PX || (dy > 10 && velocity > CLOSE_VELOCITY_PX_PER_MS)) onOpenChange(false)
    else setDrag(0)
  }

  const keyboardUp = keyboard.bottom > 0
  return (
    <Dialog.Root open={open} onOpenChange={onOpenChange}>
      <Dialog.Portal>
        <Dialog.Overlay className="fixed inset-0 z-40 bg-scrim data-[state=closed]:animate-fade-out data-[state=open]:animate-fade-in" />
        <Dialog.Content
          // Radix wants a description or an explicit "none".
          {...(description ? {} : { 'aria-describedby': undefined })}
          // Opening doesn't focus the first field: on a phone that would throw the keyboard up at once.
          // Closed (after its slide-out): the next opening starts undragged.
          onCloseAutoFocus={() => setDrag(0)}
          onOpenAutoFocus={(e) => {
            e.preventDefault()
            ;(e.currentTarget as HTMLElement).focus()
          }}
          // A toast stays usable over an open sheet (its undo) without closing it.
          onPointerDownOutside={(e) => {
            if ((e.target as Element | null)?.closest('[data-toast]')) e.preventDefault()
          }}
          className={cn(
            'fixed inset-x-0 z-40 mx-auto flex max-w-[480px] flex-col bg-surface outline-none',
            'rounded-t-sheet data-[state=closed]:animate-sheet-out data-[state=open]:animate-sheet-in',
            keyboardUp ? 'rounded-b-sheet' : 'rounded-b-phone',
            !dragging && 'transition-transform duration-200 ease-gluko',
          )}
          style={{
            bottom: keyboard.bottom,
            maxHeight: keyboardUp
              ? keyboard.visibleHeight - 12
              : 'min(90dvh, calc(100dvh - env(safe-area-inset-top) - 12px))',
            transform: drag ? `translateY(${drag}px)` : undefined,
          }}
        >
          <div
            className="shrink-0 cursor-grab touch-none px-5 pt-3"
            onPointerDown={onPointerDown}
            onPointerMove={onPointerMove}
            onPointerUp={onPointerUp}
            onPointerCancel={onPointerUp}
          >
            <div className="mx-auto h-1 w-[38px] rounded-full bg-border" />
            <div className="mt-4 flex items-start gap-3">
              <div className="flex-1">
                <Dialog.Title className="text-sheet-title">{title}</Dialog.Title>
                {description ? (
                  <Dialog.Description className="mt-1 text-note text-text-label">
                    {description}
                  </Dialog.Description>
                ) : null}
              </div>
              <Dialog.Close asChild>
                <CircleButton size={32} aria-label={t().common.close}>
                  <Icon name="close" size={14} strokeWidth={2.2} />
                </CircleButton>
              </Dialog.Close>
            </div>
          </div>
          <div
            ref={body}
            className="min-h-0 overflow-y-auto overscroll-contain px-5 pt-4"
            style={{ paddingBottom: keyboardUp ? 12 : 'max(26px, env(safe-area-inset-bottom))' }}
          >
            {children}
          </div>
        </Dialog.Content>
      </Dialog.Portal>
    </Dialog.Root>
  )
}
