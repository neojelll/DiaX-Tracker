/**
 * Photos are made small before they are stored: a phone camera gives 3-5 MB, and every photo also
 * goes into every archive (#99). The long side is brought down to [PHOTO_MAX_SIDE] and the picture
 * saved as JPEG - a few hundred KB, still sharp on a phone screen.
 */
export const PHOTO_MAX_SIDE = 1600
const PHOTO_QUALITY = 0.8

/** [width] x [height] scaled down to fit [max] on the long side; a smaller picture stays as it is. */
export function fitWithin(width: number, height: number, max: number = PHOTO_MAX_SIDE) {
  const scale = Math.min(1, max / Math.max(width, height))
  return { width: Math.max(1, Math.round(width * scale)), height: Math.max(1, Math.round(height * scale)) }
}

/** A picked or taken photo, downscaled and re-encoded as JPEG, turned the way the camera held it. */
export async function compressPhoto(file: Blob): Promise<Blob> {
  const image = await decode(file)
  try {
    const { width, height } = fitWithin(image.width, image.height)
    const canvas = document.createElement('canvas')
    canvas.width = width
    canvas.height = height
    const context = canvas.getContext('2d')
    if (!context) throw new Error('No 2d canvas')
    context.drawImage(image.source, 0, 0, width, height)
    return await new Promise<Blob>((resolve, reject) =>
      canvas.toBlob(
        (blob) => (blob ? resolve(blob) : reject(new Error('Photo could not be encoded'))),
        'image/jpeg',
        PHOTO_QUALITY,
      ),
    )
  } finally {
    image.close()
  }
}

interface Decoded {
  source: CanvasImageSource
  width: number
  height: number
  close: () => void
}

/** createImageBitmap where it can read the file; an <img> otherwise (older Safari, some formats). */
async function decode(file: Blob): Promise<Decoded> {
  try {
    const bitmap = await createImageBitmap(file, { imageOrientation: 'from-image' })
    return { source: bitmap, width: bitmap.width, height: bitmap.height, close: () => bitmap.close() }
  } catch {
    const url = URL.createObjectURL(file)
    const img = new Image()
    img.src = url
    try {
      await img.decode()
    } catch (e) {
      URL.revokeObjectURL(url)
      throw e
    }
    return {
      source: img,
      width: img.naturalWidth,
      height: img.naturalHeight,
      close: () => URL.revokeObjectURL(url),
    }
  }
}

/**
 * Photos from an Android archive are stored at the camera's full size; they are made small the same
 * way as a new photo. One that can't be read stays as it was.
 */
export async function compressAll(photos: Map<number, Blob>): Promise<Map<number, Blob>> {
  const result = new Map<number, Blob>()
  for (const [id, blob] of photos) {
    try {
      const small = await compressPhoto(blob)
      result.set(id, small.size < blob.size ? small : blob)
    } catch {
      result.set(id, blob)
    }
  }
  return result
}
