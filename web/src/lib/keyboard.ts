import { useSyncExternalStore } from 'react'

/**
 * The part of the screen the on-screen keyboard covers, as seen from a fixed element.
 *
 * Android Chrome shrinks the page for the keyboard (interactive-widget in index.html), so there it
 * is 0. iOS Safari doesn't: the keyboard slides over the page and only the visual viewport shrinks -
 * a bottom sheet would end up under it. The visible part of the page is the visual viewport, which
 * iOS may also scroll down inside the layout viewport ([offsetTop]); what lies below it is covered.
 */
export interface KeyboardInset {
  /** Px from the bottom of the layout viewport up to the bottom of what's visible. */
  bottom: number
  /** Px of the screen still visible. */
  visibleHeight: number
}

const NONE: KeyboardInset = { bottom: 0, visibleHeight: 0 }
let current: KeyboardInset = NONE

function read(): KeyboardInset {
  const vv = window.visualViewport
  if (!vv) return NONE
  // Under a pixel is rounding; a zoomed-in page also shrinks the visual viewport, but by much more
  // than nothing and with a scale - only a keyboard leaves scale at 1.
  const bottom = vv.scale > 1.01 ? 0 : Math.max(0, Math.round(window.innerHeight - vv.height - vv.offsetTop))
  const visibleHeight = Math.round(vv.height)
  if (bottom !== current.bottom || visibleHeight !== current.visibleHeight)
    current = { bottom, visibleHeight }
  return current
}

function subscribe(onChange: () => void) {
  const vv = window.visualViewport
  if (!vv) return () => {}
  vv.addEventListener('resize', onChange)
  vv.addEventListener('scroll', onChange)
  return () => {
    vv.removeEventListener('resize', onChange)
    vv.removeEventListener('scroll', onChange)
  }
}

export function useKeyboardInset(): KeyboardInset {
  return useSyncExternalStore(subscribe, read, () => NONE)
}
