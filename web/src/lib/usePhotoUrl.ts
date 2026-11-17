import { useEffect, useRef, useState } from 'react'

/**
 * An object URL for showing [blob] in an <img>, released when the blob changes or the view goes.
 * The URL is handed over from a microtask rather than set right in the effect: StrictMode runs the
 * effect twice in development, and the first URL is already released by then.
 */
export function usePhotoUrl(blob: Blob | null | undefined): string | null {
  const [state, setState] = useState<{ blob: Blob; url: string } | null>(null)
  useEffect(() => {
    if (!blob) return
    const url = URL.createObjectURL(blob)
    let alive = true
    void Promise.resolve().then(() => alive && setState({ blob, url }))
    return () => {
      alive = false
      URL.revokeObjectURL(url)
    }
  }, [blob])
  return state && state.blob === blob ? state.url : null
}

/** Opens the picker of a PhotoInput (components/photos.tsx). */
export function usePhotoPicker() {
  const ref = useRef<HTMLInputElement>(null)
  return { ref, open: () => ref.current?.click() }
}
