export interface CompressOptions {
  maxWidth?: number;
  maxHeight?: number;
  quality?: number;
  minFileSize?: number;
}

export interface CropOptions {
  /**
   * Anteil der kleineren Bilddimension,
   * der als zusätzlicher Rand um den
   * erkannten Dokumentbereich erhalten bleibt.
   *
   * 0.10 = 10 % der kleineren Bilddimension
   */
  marginRatio?: number;
}

const DEFAULT_CROP_OPTIONS: Required<CropOptions> = {
  marginRatio: 0.07,
};

const DEFAULT_OPTIONS: Required<CompressOptions> = {
  maxWidth: 2000,
  maxHeight: 2000,
  quality: 0.82,
  minFileSize: 1_000_000,
};

/**
 * Lädt ein Bild als ImageBitmap.
 */
async function loadImageBitmap(file: File): Promise<ImageBitmap> {
  return createImageBitmap(file, {
    imageOrientation: "from-image",
  });
}

/**
 * Ermittelt den tatsächlichen Dokumentbereich.
 *
 * Die Funktion sucht von allen vier Seiten nach dem
 * ersten Bereich, der nicht nahezu vollständig weiß ist.
 *
 * Damit werden z. B. weiße Bereiche außerhalb eines
 * fotografierten Belegs entfernt.
 */
export function detectCropArea(
  image: CanvasImageSource,
  width: number,
  height: number,
  options: CropOptions = {},
): {
  x: number;
  y: number;
  width: number;
  height: number;
} {
  const config = {
    ...DEFAULT_CROP_OPTIONS,
    ...options,
  };

  /*
   * Für die Analyse verkleinern wir das Bild.
   * Das macht die Erkennung erheblich schneller.
   */
  const analysisWidth = Math.min(800, width);
  const analysisScale = analysisWidth / width;

  const analysisHeight = Math.round(height * analysisScale);

  const canvas = document.createElement("canvas");

  canvas.width = analysisWidth;
  canvas.height = analysisHeight;

  const ctx = canvas.getContext("2d", {
    willReadFrequently: true,
  });

  if (!ctx) {
    return {
      x: 0,
      y: 0,
      width,
      height,
    };
  }

  ctx.drawImage(image, 0, 0, analysisWidth, analysisHeight);

  const imageData = ctx.getImageData(0, 0, analysisWidth, analysisHeight);

  const { data } = imageData;

  /*
   * =========================================================
   * 1. HELLE PIXEL ERMITTELN
   * =========================================================
   *
   * Ein Beleg ist normalerweise deutlich heller
   * als der Hintergrund.
   */
  const threshold = 170;

  const mask = new Uint8Array(analysisWidth * analysisHeight);

  for (let y = 0; y < analysisHeight; y++) {
    for (let x = 0; x < analysisWidth; x++) {
      const index = (y * analysisWidth + x) * 4;

      const r = data[index];
      const g = data[index + 1];
      const b = data[index + 2];

      const brightness = (r + g + b) / 3;

      if (brightness >= threshold) {
        mask[y * analysisWidth + x] = 1;
      }
    }
  }

  /*
   * =========================================================
   * 2. ZUSAMMENHÄNGENDE FLÄCHEN SUCHEN
   * =========================================================
   *
   * Wir suchen die größte zusammenhängende helle Fläche.
   *
   * 8-Nachbarschaft:
   *
   *  XXX
   *  XXX
   *  XXX
   */
  const visited = new Uint8Array(mask.length);

  let bestArea = 0;

  let bestMinX = 0;
  let bestMinY = 0;
  let bestMaxX = 0;
  let bestMaxY = 0;

  const queueX = new Int32Array(mask.length);

  const queueY = new Int32Array(mask.length);

  for (let startY = 0; startY < analysisHeight; startY++) {
    for (let startX = 0; startX < analysisWidth; startX++) {
      const startIndex = startY * analysisWidth + startX;

      if (mask[startIndex] === 0 || visited[startIndex] === 1) {
        continue;
      }

      let queueStart = 0;
      let queueEnd = 0;

      queueX[queueEnd] = startX;
      queueY[queueEnd] = startY;
      queueEnd++;

      visited[startIndex] = 1;

      let area = 0;

      let minX = startX;
      let maxX = startX;
      let minY = startY;
      let maxY = startY;

      while (queueStart < queueEnd) {
        const x = queueX[queueStart];

        const y = queueY[queueStart];

        queueStart++;

        area++;

        minX = Math.min(minX, x);

        maxX = Math.max(maxX, x);

        minY = Math.min(minY, y);

        maxY = Math.max(maxY, y);

        for (let dy = -1; dy <= 1; dy++) {
          for (let dx = -1; dx <= 1; dx++) {
            if (dx === 0 && dy === 0) {
              continue;
            }

            const nx = x + dx;
            const ny = y + dy;

            if (nx < 0 || nx >= analysisWidth || ny < 0 || ny >= analysisHeight) {
              continue;
            }

            const index = ny * analysisWidth + nx;

            if (mask[index] === 1 && visited[index] === 0) {
              visited[index] = 1;

              queueX[queueEnd] = nx;
              queueY[queueEnd] = ny;

              queueEnd++;
            }
          }
        }
      }

      /*
       * Größte helle Fläche merken.
       */
      if (area > bestArea) {
        bestArea = area;

        bestMinX = minX;
        bestMinY = minY;
        bestMaxX = maxX;
        bestMaxY = maxY;
      }
    }
  }

  /*
   * =========================================================
   * 3. KEIN SINNVOLLER BEREICH GEFUNDEN
   * =========================================================
   */
  const imageArea = analysisWidth * analysisHeight;

  /*
   * Wenn praktisch das komplette Foto hell ist,
   * haben wir vermutlich keinen Beleg erkannt.
   */
  if (bestArea < imageArea * 0.03 || bestArea > imageArea * 0.9) {
    canvas.width = 0;
    canvas.height = 0;

    return {
      x: 0,
      y: 0,
      width,
      height,
    };
  }

  /*
   * =========================================================
   * 4. IN ORIGINALKOORDINATEN UMRECHNEN
   * =========================================================
   */

  const detectedX = bestMinX / analysisScale;
  const detectedY = bestMinY / analysisScale;

  const detectedWidth = (bestMaxX - bestMinX + 1) / analysisScale;

  const detectedHeight = (bestMaxY - bestMinY + 1) / analysisScale;

  /*
   * =========================================================
   * 5. SICHERHEITSRAND
   * =========================================================
   */

  const margin = Math.round(Math.min(width, height) * config.marginRatio);

  let cropX = Math.max(0, detectedX - margin);
  let cropY = Math.max(0, detectedY - margin);

  let cropRight = Math.min(width, detectedX + detectedWidth + margin);

  let cropBottom = Math.min(height, detectedY + detectedHeight + margin);

  /*
   * =========================================================
   * 6. SEITENVERHÄLTNIS DES ORIGINALFOTOS BEIBEHALTEN
   * =========================================================
   *
   * Wir verwenden das Seitenverhältnis des Originalbildes.
   *
   * Dadurch kann der erkannte Bereich niemals durch
   * einen zu engen Crop abgeschnitten werden.
   */

  const originalAspect = width / height;

  const cropWidth = cropRight - cropX;
  const cropHeight = cropBottom - cropY;

  const cropAspect = cropWidth / cropHeight;

  if (cropAspect > originalAspect) {
    /*
     * Crop ist zu breit.
     * Höhe vergrößern.
     */
    const requiredHeight = cropWidth / originalAspect;
    const additionalHeight = requiredHeight - cropHeight;

    cropY -= additionalHeight / 2;
    cropBottom += additionalHeight / 2;
  } else {
    /*
     * Crop ist zu hoch.
     * Breite vergrößern.
     */
    const requiredWidth = cropHeight * originalAspect;
    const additionalWidth = requiredWidth - cropWidth;

    cropX -= additionalWidth / 2;
    cropRight += additionalWidth / 2;
  }

  /*
   * =========================================================
   * 7. NICHT ÜBER BILDRAND HINAUS
   * =========================================================
   */

  if (cropX < 0) {
    cropRight -= cropX;
    cropX = 0;
  }

  if (cropY < 0) {
    cropBottom -= cropY;
    cropY = 0;
  }

  if (cropRight > width) {
    cropX -= cropRight - width;
    cropRight = width;
  }

  if (cropBottom > height) {
    cropY -= cropBottom - height;
    cropBottom = height;
  }

  /*
   * Sicherheit: auch nach dem Verschieben
   * innerhalb des Bildes bleiben.
   */
  cropX = Math.max(0, cropX);
  cropY = Math.max(0, cropY);
  cropRight = Math.min(width, cropRight);
  cropBottom = Math.min(height, cropBottom);

  const cropResult = {
    x: Math.round(cropX),
    y: Math.round(cropY),
    width: Math.round(cropRight - cropX),
    height: Math.round(cropBottom - cropY),
  };

  canvas.width = 0;
  canvas.height = 0;

  return cropResult;
}

/**
 * Erstellt aus einem Bild einen zugeschnittenen Blob.
 */
export async function cropImage(file: File, options: CropOptions = {}): Promise<File> {
  if (!file.type.startsWith("image/")) {
    return file;
  }

  const bitmap = await loadImageBitmap(file);

  const canvas = document.createElement("canvas");

  try {
    const crop = detectCropArea(bitmap, bitmap.width, bitmap.height, options);

    canvas.width = crop.width;
    canvas.height = crop.height;

    const ctx = canvas.getContext("2d");

    if (!ctx) {
      return file;
    }

    ctx.drawImage(
      bitmap,

      crop.x,
      crop.y,
      crop.width,
      crop.height,

      0,
      0,
      crop.width,
      crop.height,
    );

    const blob = await new Promise<Blob>((resolve, reject) => {
      canvas.toBlob(
        (result) => {
          if (result) {
            resolve(result);
          } else {
            reject(new Error("Bild konnte nicht zugeschnitten werden."));
          }
        },
        "image/jpeg",
        0.95,
      );
    });

    return new File([blob], file.name.replace(/\.[^.]+$/, ".jpg"), {
      type: "image/jpeg",
      lastModified: file.lastModified,
    });
  } finally {
    bitmap.close();

    canvas.width = 0;
    canvas.height = 0;
  }
}

/**
 * Komprimiert ein Bild für den Upload.
 *
 * PDFs werden unverändert zurückgegeben.
 */
export async function optimizeUploadFile(file: File, options: CompressOptions = {}): Promise<File> {
  const config = {
    ...DEFAULT_OPTIONS,
    ...options,
  };

  if (!file.type.startsWith("image/")) {
    return file;
  }

  if (file.size < config.minFileSize) {
    return file;
  }

  const bitmap = await loadImageBitmap(file);

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

    /*
     * Nur verwenden, wenn das neue Bild
     * tatsächlich kleiner ist.
     */
    if (blob.size >= file.size) {
      return file;
    }

    return new File([blob], file.name.replace(/\.[^.]+$/, ".jpg"), {
      type: "image/jpeg",
      lastModified: file.lastModified,
    });
  } finally {
    bitmap.close();

    canvas.width = 0;
    canvas.height = 0;
  }
}
