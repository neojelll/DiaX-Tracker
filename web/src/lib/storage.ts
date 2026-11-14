/**
 * Asks the browser to keep this app's storage (the diary) instead of clearing it when space runs
 * low. Safari grants it to apps added to the home screen. Resolves to whether storage is kept;
 * false where the API is missing or the browser says no - the diary still works, only less safe.
 */
export async function keepStorage(
  storage: StorageManager | undefined = globalThis.navigator?.storage,
): Promise<boolean> {
  if (!storage?.persist) return false
  try {
    return (await storage.persisted()) || (await storage.persist())
  } catch {
    return false
  }
}
