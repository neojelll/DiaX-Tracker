import 'fake-indexeddb/auto'
import '@testing-library/jest-dom/vitest'
import { cleanup } from '@testing-library/react'
import { afterEach, beforeEach } from 'vitest'
import { setLang } from '@/i18n'

// Tests read the Russian texts unless they switch; jsdom's own language is English.
beforeEach(() => setLang('ru'))

afterEach(() => {
  cleanup()
  window.location.hash = ''
  localStorage.clear()
})
