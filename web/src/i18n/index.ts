import { en } from '@/i18n/en'
import { ru, type Messages } from '@/i18n/ru'
import { DATABASE_NAME } from '@/lib/channel'
import { createStore } from '@/lib/store'

export type Lang = 'ru' | 'en'
export const LANGS: Lang[] = ['ru', 'en']
const MESSAGES: Record<Lang, Messages> = { ru, en }

// On the device, per channel like the database: the dev build can be in English while the real one isn't.
const KEY = `${DATABASE_NAME}:lang`

function initialLang(): Lang {
  try {
    const stored = localStorage.getItem(KEY)
    if (stored === 'ru' || stored === 'en') return stored
  } catch {
    // Storage blocked - fall back to the device language.
  }
  // Until picked, the phone's language: Russian if it's among them, English otherwise.
  const langs = typeof navigator === 'undefined' ? [] : navigator.languages
  return langs.some((l) => l.toLowerCase().startsWith('ru')) ? 'ru' : 'en'
}

const store = createStore<Lang>(initialLang())

/** The language now - for code outside components; components use [useLang] / [useT]. */
export function lang(): Lang {
  return store.get()
}

/** The texts of the language now. */
export function t(): Messages {
  return MESSAGES[store.get()]
}

export function setLang(next: Lang) {
  try {
    localStorage.setItem(KEY, next)
  } catch {
    // Not kept across launches then; the switch still applies now.
  }
  if (typeof document !== 'undefined') document.documentElement.lang = next
  store.set(next)
}

export function useLang(): Lang {
  return store.use()[0]
}

/** The texts, re-rendering when the language changes. */
export function useT(): Messages {
  return MESSAGES[useLang()]
}
