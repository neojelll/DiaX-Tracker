import * as Dialog from '@radix-ui/react-dialog'
import type { RefObject } from 'react'
import { Icon } from '@/components/icons'
import { CircleButton } from '@/components/ui/button'
import { t } from '@/i18n'
import { compressPhoto } from '@/lib/photo'
import { useToast } from '@/lib/toast'
import { usePhotoUrl } from '@/lib/usePhotoUrl'

/**
 * The hidden file input behind "Фото". Without `capture`, iOS and Android both offer the camera
 * and the photo library in their own menu. The picked photo is compressed before it is handed on.
 */
export function PhotoInput({
  inputRef,
  onPhoto,
}: {
  inputRef: RefObject<HTMLInputElement | null>
  onPhoto: (photo: Blob) => void
}) {
  const toast = useToast()
  return (
    <input
      ref={inputRef}
      type="file"
      accept="image/*"
      aria-label={t().home.addPhoto}
      className="hidden"
      onChange={(e) => {
        const file = e.target.files?.[0]
        // Cleared so the same photo can be picked again.
        e.target.value = ''
        if (file) compressPhoto(file).then(onPhoto, () => toast.show(t().home.photoFailed, { kind: 'error' }))
      }}
    />
  )
}

/** An attached photo in a form: the thumbnail, "Заменить фото" and ×. */
export function PhotoStrip({
  photo,
  onReplace,
  onRemove,
  className,
}: {
  photo: Blob
  onReplace: () => void
  onRemove: () => void
  className?: string
}) {
  const url = usePhotoUrl(photo)
  return (
    <div className={`flex animate-fade-in items-center rounded-[14px] bg-tile p-1.5 ${className ?? ''}`}>
      {url && (
        <img
          src={url}
          alt={t().home.entryPhoto}
          className="size-[34px] shrink-0 rounded-[10px] object-cover"
        />
      )}
      <button
        type="button"
        onClick={onReplace}
        className="ml-2.5 min-w-0 flex-1 py-2 text-left text-[12.5px] leading-[17px] text-text-secondary"
      >
        {t().home.replacePhoto}
      </button>
      <CircleButton size={30} tone="surface" aria-label={t().home.removePhoto} onClick={onRemove}>
        <Icon name="close" size={11} strokeWidth={2.4} />
      </CircleButton>
    </div>
  )
}

/** A photo over the whole screen on black, with its caption; a tap anywhere or × closes it. */
export function PhotoViewer({
  photo,
  caption,
  onClose,
}: {
  photo: Blob | null
  caption: string
  onClose: () => void
}) {
  const url = usePhotoUrl(photo)
  return (
    <Dialog.Root open={photo != null} onOpenChange={(o) => !o && onClose()}>
      <Dialog.Portal>
        <Dialog.Overlay className="fixed inset-0 z-50 animate-fade-in bg-black" />
        <Dialog.Content
          onClick={onClose}
          className="fixed inset-0 z-50 flex animate-fade-in flex-col items-center justify-center outline-none"
          style={{ paddingTop: 'env(safe-area-inset-top)', paddingBottom: 'env(safe-area-inset-bottom)' }}
        >
          <Dialog.Title className="sr-only">{t().home.entryPhoto}</Dialog.Title>
          <Dialog.Description className="absolute bottom-0 w-full pb-[max(env(safe-area-inset-bottom),20px)] text-center text-body text-white/70">
            {caption}
          </Dialog.Description>
          {url && (
            <img src={url} alt={t().home.entryPhoto} className="max-h-full max-w-full object-contain" />
          )}
          <Dialog.Close asChild>
            <CircleButton
              size={36}
              tone="surface"
              aria-label={t().common.close}
              className="absolute right-4 bg-white/15 text-white"
              style={{ top: 'calc(env(safe-area-inset-top) + 12px)' }}
            >
              <Icon name="close" size={14} strokeWidth={2.4} />
            </CircleButton>
          </Dialog.Close>
        </Dialog.Content>
      </Dialog.Portal>
    </Dialog.Root>
  )
}
