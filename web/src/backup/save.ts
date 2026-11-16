import { archiveName, writeArchive } from '@/backup/archive'
import { diary } from '@/diary/repository'
import { nowLocal } from '@/diary/time'

/**
 * Handing a file to the person. On a phone it goes through the share sheet - "Сохранить в Файлы",
 * iCloud, a messenger; where files can't be shared (a desktop browser) it is downloaded.
 *
 * - saved: shared or downloaded;
 * - cancelled: the share sheet was closed;
 * - needs-tap: the browser wants the share to start right from a tap, and preparing the file took
 *   too long for that (Safari) - ask for one more tap and call again.
 */
export type SaveResult = 'saved' | 'cancelled' | 'needs-tap'

export async function saveFile(file: File): Promise<SaveResult> {
  if (navigator.canShare?.({ files: [file] })) {
    try {
      await navigator.share({ files: [file] })
      return 'saved'
    } catch (e) {
      if (e instanceof DOMException && e.name === 'AbortError') return 'cancelled'
      if (e instanceof DOMException && e.name === 'NotAllowedError') return 'needs-tap'
      throw e
    }
  }
  const url = URL.createObjectURL(file)
  const link = document.createElement('a')
  link.href = url
  link.download = file.name
  link.click()
  setTimeout(() => URL.revokeObjectURL(url), 60_000)
  return 'saved'
}

/** The whole diary as an archive file, named by today. */
export async function prepareArchive(): Promise<File> {
  const now = nowLocal()
  const blob = await writeArchive(await diary.wholeDiary(), now)
  return new File([blob], archiveName(now), { type: 'application/zip' })
}
