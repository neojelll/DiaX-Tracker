import { useEffect, useState } from 'react'
import { nowLocal, type LocalDateTime } from '@/diary/time'

/**
 * The current minute, re-rendering when it turns - greetings, "today", active insulin and "N min
 * ago" stay right while a screen sits open past an hour or midnight. Also catches up when the app
 * comes back from the background, where timers sleep.
 */
export function useNow(): LocalDateTime {
  const [now, setNow] = useState(() => minute(nowLocal()))
  useEffect(() => {
    let timer: ReturnType<typeof setTimeout>
    const tick = () => {
      setNow(minute(nowLocal()))
      const seconds = new Date().getSeconds()
      timer = setTimeout(tick, (60 - seconds) * 1000 + 50)
    }
    tick()
    const onVisible = () => document.visibilityState === 'visible' && setNow(minute(nowLocal()))
    document.addEventListener('visibilitychange', onVisible)
    return () => {
      clearTimeout(timer)
      document.removeEventListener('visibilitychange', onVisible)
    }
  }, [])
  return now
}

const minute = (at: LocalDateTime) => `${at.slice(0, 16)}:00`
