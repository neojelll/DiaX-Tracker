interface ImportMetaEnv {
  /** "dev" for the build at /DiaX-Tracker/dev/; unset for the release build. */
  readonly VITE_CHANNEL?: 'dev'
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}
