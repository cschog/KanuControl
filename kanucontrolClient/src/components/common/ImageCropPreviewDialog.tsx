import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Stack,
  Typography,
} from "@mui/material";

import { useEffect, useState } from "react";

import { cropImage } from "@/utils/imageUtils";

interface Props {
  open: boolean;
  file: File | null;
  onCancel: () => void;
  onConfirm: (file: File) => void;
}

export default function ImageCropPreviewDialog({ open, file, onCancel, onConfirm }: Props) {
  const [previewUrl, setPreviewUrl] = useState<string | null>(null);

  const [croppedFile, setCroppedFile] = useState<File | null>(null);

  const [loading, setLoading] = useState(false);

useEffect(() => {
  if (!open || !file) {
    setPreviewUrl(null);
    setCroppedFile(null);
    return;
  }

  const selectedFile = file;

  let url: string | null = null;
  let cancelled = false;

  async function prepare() {
    setLoading(true);

    try {
      /*
       * PDFs brauchen keinen Crop.
       */
      if (!selectedFile.type.startsWith("image/")) {
        url = URL.createObjectURL(selectedFile);

        if (!cancelled) {
          setPreviewUrl(url);
          setCroppedFile(selectedFile);
        }

        return;
      }

      const cropped = await cropImage(selectedFile);

      url = URL.createObjectURL(cropped);

      if (!cancelled) {
        setPreviewUrl(url);
        setCroppedFile(cropped);
      }
    } catch (error) {
      console.error("Bild konnte nicht vorbereitet werden:", error);

      /*
       * Im Fehlerfall das Original verwenden.
       */
      url = URL.createObjectURL(selectedFile);

      if (!cancelled) {
        setPreviewUrl(url);
        setCroppedFile(selectedFile);
      }
    } finally {
      if (!cancelled) {
        setLoading(false);
      }
    }
  }

  void prepare();

  return () => {
    cancelled = true;

    if (url) {
      URL.revokeObjectURL(url);
    }
  };
}, [open, file]);

  function handleConfirm() {
    if (!croppedFile) {
      return;
    }

    onConfirm(croppedFile);
  }

  return (
    <Dialog open={open} onClose={onCancel} maxWidth="md" fullWidth>
      <DialogTitle>Dokument prüfen</DialogTitle>

      <DialogContent dividers>
        <Stack spacing={2}>
          <Typography variant="body2" color="text.secondary">
            Das Bild wurde automatisch zugeschnitten. Bitte kontrolliere den Ausschnitt vor dem
            Hochladen.
          </Typography>

          {loading && <Typography>Bild wird vorbereitet ...</Typography>}

          {!loading && previewUrl && (
            <Stack
              alignItems="center"
              sx={{
                width: "100%",
              }}
            >
              <img
                src={previewUrl}
                alt="Vorschau"
                style={{
                  display: "block",
                  maxWidth: "100%",
                  maxHeight: "65vh",
                  objectFit: "contain",
                }}
              />
            </Stack>
          )}
        </Stack>
      </DialogContent>

      <DialogActions>
        <Button onClick={onCancel}>Abbrechen</Button>

        <Button variant="contained" disabled={loading || !croppedFile} onClick={handleConfirm}>
          Verwenden
        </Button>
      </DialogActions>
    </Dialog>
  );
}
