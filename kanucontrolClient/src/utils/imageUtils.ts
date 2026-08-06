export interface CompressOptions {
  maxWidth?: number;
  maxHeight?: number;
  quality?: number;
  minFileSize?: number;
}

const DEFAULT_OPTIONS: Required<CompressOptions> = {
  maxWidth: 2000,
  maxHeight: 2000,
  quality: 0.82,
  minFileSize: 1_000_000, // 1 MB
};

/**
 * Verkleinert Bilder vor dem Upload.
 * PDFs und kleine Bilder werden unverändert zurückgegeben.
 */
export async function optimizeUploadFile(file: File, options: CompressOptions = {}): Promise<File> {
  const config = { ...DEFAULT_OPTIONS, ...options };

  if (!file.type.startsWith("image/")) {
    return file;
  }

  if (file.size < config.minFileSize) {
    return file;
  }

  const bitmap = await createImageBitmap(file, {
    imageOrientation: "from-image",
  });

  const canvas = document.createElement("canvas");

  try {
    let width = bitmap.width;
    let height = bitmap.height;

    const scale = Math.min(config.maxWidth / width, config.maxHeight / height, 1);

    width = Math.round(width * scale);
    height = Math.round(height * scale);

    canvas.width = width;
    canvas.height = height;

    const ctx = canvas.getContext("2d");

    if (!ctx) {
      return file;
    }

    ctx.drawImage(bitmap, 0, 0, width, height);

    const blob = await new Promise<Blob>((resolve, reject) => {
      canvas.toBlob(
        (b) => {
          if (b) {
            resolve(b);
          } else {
            reject(new Error("Bild konnte nicht komprimiert werden."));
          }
        },
        "image/jpeg",
        config.quality,
      );
    });

    // Nur verwenden, wenn das neue Bild wirklich kleiner ist
    if (blob.size >= file.size) {
      return file;
    }

    return new File([blob], file.name.replace(/\.[^.]+$/, ".jpg"), {
      type: "image/jpeg",
      lastModified: file.lastModified,
    });
  } finally {
    // Wird IMMER ausgeführt – auch bei return oder Exception
    bitmap.close();
    canvas.width = 0;
    canvas.height = 0;
  }
}
