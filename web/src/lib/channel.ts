/**
 * Which build this is. The release build at /DiaX-Tracker/ is the owner's real diary and changes
 * only with a release; the dev build at /DiaX-Tracker/dev/ follows main and is the place to try
 * things - the PWA's version of the Android `.dev` app. Both live on one origin, so everything the
 * app stores is named per channel and the two never see each other's data.
 */
export type Channel = 'release' | 'dev'

export const CHANNEL: Channel = import.meta.env.VITE_CHANNEL === 'dev' ? 'dev' : 'release'

/** The IndexedDB database of this channel's diary. */
export const DATABASE_NAME = CHANNEL === 'dev' ? 'diax-dev' : 'diax'
