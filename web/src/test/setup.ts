import 'fake-indexeddb/auto'
import '@testing-library/jest-dom/vitest'
import { cleanup } from '@testing-library/react'
import { afterEach, beforeEach } from 'vitest'
import { setLang } from '@/i18n'

// Photos only need some URL for an <img>. Node's own wants a real Blob, and fake-indexeddb gives a
// photo back as a plain object.
URL.createObjectURL = () => 'blob:test'
URL.revokeObjectURL = () => {}

// Tests read the Russian texts unless they switch; jsdom's own language is English.
beforeEach(() => setLang('ru'))

afterEach(() => {
  cleanup()
  window.location.hash = ''
  localStorage.clear()
})
