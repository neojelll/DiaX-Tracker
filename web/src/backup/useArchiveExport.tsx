import { useState } from 'react'
import { Button } from '@/components/ui/button'
import { Sheet } from '@/components/ui/sheet'
import { prepareArchive, saveFile, type SaveResult } from '@/backup/save'
import { diary } from '@/diary/repository'
import { nowLocal } from '@/diary/time'
import { t } from '@/i18n'
import { fileSize } from '@/lib/format'
import { useToast } from '@/lib/toast'

/**
 * Saving the archive from a tap - in Settings and from the reminder on Home. The archive is made
 * and handed to the share sheet at once; if Safari refuses because that took too long after the
 * tap, a sheet with the ready file asks for one more tap. Returns the sheet to render.
 */
export function useArchiveExport() {
  const toast = useToast()
  const [busy, setBusy] = useState(false)
  const [ready, setReady] = useState<File | null>(null)
  // While sliding out the sheet keeps showing the file it had.
  const [last, setLast] = useState(ready)
  if (ready && ready !== last) setLast(ready)

  const finish = async (file: File, result: SaveResult) => {
    if (result === 'needs-tap') return setReady(file)
    setReady(null)
    if (result === 'saved') {
      await diary.markBackedUp(nowLocal())
      toast.show(t().settings.saved, { sub: file.name })
    }
  }

  const failed = () => {
    setReady(null)
    toast.show(t().settings.saveFailed, { kind: 'error' })
  }

  const start = async () => {
    setBusy(true)
    try {
      const file = await prepareArchive()
      await finish(file, await saveFile(file))
    } catch {
      failed()
    } finally {
      setBusy(false)
    }
  }

  const shown = ready ?? last
  const sheet = (
    <Sheet open={ready != null} onOpenChange={(o) => !o && setReady(null)} title={t().settings.readyTitle}>
      {shown && (
        <>
          <p className="rounded-tile bg-tile px-3.5 py-3 text-body">
            {shown.name}
            <span className="ml-2 text-text-tertiary tabular-nums">{fileSize(shown.size)}</span>
          </p>
          <Button
            className="mt-4"
            onClick={() => {
              // Called right in the tap, so the share sheet is allowed now.
              saveFile(shown).then((result) => finish(shown, result), failed)
            }}
          >
            {t().settings.readySave}
          </Button>
        </>
      )}
    </Sheet>
  )

  return { busy, start, sheet }
}
