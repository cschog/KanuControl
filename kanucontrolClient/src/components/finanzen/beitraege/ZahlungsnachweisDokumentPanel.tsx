import { useCallback, useEffect, useRef, useState } from "react";
import {
  Box,
  Button,
  IconButton,
  List,
  ListItem,
  ListItemText,
  Stack,
  Typography,
} from "@mui/material";

import DeleteIcon from "@mui/icons-material/Delete";
import DownloadIcon from "@mui/icons-material/Download";
import UploadFileIcon from "@mui/icons-material/UploadFile";
import VisibilityIcon from "@mui/icons-material/Visibility";
import { optimizeUploadFile } from "@/utils/imageUtils";
import ImageCropPreviewDialog from "@/components/common/ImageCropPreviewDialog";

import {
  deleteZahlungsnachweisDokument,
  download,
  findAll,
  upload,
} from "@/api/services/zahlungsnachweisDokumentApi";

import { ZahlungsnachweisDokumentDTO } from "@/api/types/beitraege";

import LoadingOverlay from "@/components/common/LoadingOverlay";
import DeleteConfirmDialog from "@/components/common/DeleteConfirmDialog";

interface Props {
  veranstaltungId: number;
  zahlungsnachweisId: number;
  readOnly?: boolean;
}

export default function ZahlungsnachweisDokumentPanel({
  veranstaltungId,
  zahlungsnachweisId,
  readOnly = false,
}: Props) {
  const [dokumente, setDokumente] = useState<ZahlungsnachweisDokumentDTO[]>([]);
  const [loading, setLoading] = useState(false);

  const fileInputRef = useRef<HTMLInputElement>(null);
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const [cropDialogOpen, setCropDialogOpen] = useState(false);

  const [selectedFile, setSelectedFile] = useState<File | null>(null);

  /* =========================================================
     LOAD
  ========================================================= */

  const loadDokumente = useCallback(
    async (showLoading = true) => {
      if (showLoading) {
        setLoading(true);
      }

      try {
        const data = await findAll(veranstaltungId, zahlungsnachweisId);

        setDokumente(data ?? []);
      } finally {
        if (showLoading) {
          setLoading(false);
        }
      }
    },
    [veranstaltungId, zahlungsnachweisId],
  );

  useEffect(() => {
    void loadDokumente();
  }, [loadDokumente]);

  /* =========================================================
     UPLOAD
  ========================================================= */

  async function handleUpload(file: File) {
    if (file.type.startsWith("image/")) {
      setSelectedFile(file);
      setCropDialogOpen(true);
      return;
    }

    setLoading(true);

    try {
      await upload(veranstaltungId, zahlungsnachweisId, file);

      await loadDokumente(false);
    } finally {
      setLoading(false);
    }
  }

  async function handleCropConfirm(file: File) {
    setCropDialogOpen(false);
    setSelectedFile(null);

    setLoading(true);

    try {
      const optimizedFile = await optimizeUploadFile(file);

      await upload(veranstaltungId, zahlungsnachweisId, optimizedFile);

      await loadDokumente(false);
    } finally {
      setLoading(false);
    }
  }

  /* =========================================================
     DELETE
  ========================================================= */

  function handleDelete(id: number) {
    setDeleteId(id);
  }

  async function confirmDelete() {
    if (deleteId == null) {
      return;
    }

    setLoading(true);

    try {
      await deleteZahlungsnachweisDokument(veranstaltungId, zahlungsnachweisId, deleteId);

      setDeleteId(null);

      await loadDokumente();
    } finally {
      setLoading(false);
    }
  }

  /* =========================================================
     DOWNLOAD
  ========================================================= */

  async function handleDownload(dokument: ZahlungsnachweisDokumentDTO) {
    const blob = await download(veranstaltungId, zahlungsnachweisId, dokument.id);

    const url = URL.createObjectURL(blob);

    const a = document.createElement("a");
    a.href = url;
    a.download = dokument.originalDateiname;
    a.click();

    setTimeout(() => URL.revokeObjectURL(url), 1000);
  }

  /* =========================================================
     PREVIEW
  ========================================================= */

  async function handlePreview(dokument: ZahlungsnachweisDokumentDTO) {
    const blob = await download(veranstaltungId, zahlungsnachweisId, dokument.id);

    const url = URL.createObjectURL(blob);

    window.open(url, "_blank");

    setTimeout(() => URL.revokeObjectURL(url), 60_000);
  }

  /* =========================================================
     FORMAT SIZE
  ========================================================= */

  function formatSize(bytes: number) {
    if (bytes < 1024) {
      return `${bytes} B`;
    }

    if (bytes < 1024 * 1024) {
      return `${(bytes / 1024).toFixed(1)} KB`;
    }

    return `${(bytes / 1024 / 1024).toFixed(1)} MB`;
  }

  /* =========================================================
     RENDER
  ========================================================= */

  return (
    <Box>
      <Stack direction="row" justifyContent="space-between" alignItems="center" mb={2}>
        <Typography variant="h6">Dokumente</Typography>

        {!readOnly && (
          <>
            <Button
              variant="contained"
              startIcon={<UploadFileIcon />}
              onClick={() => fileInputRef.current?.click()}
            >
              Hochladen
            </Button>

            <input
              hidden
              type="file"
              accept=".pdf,image/*"
              ref={fileInputRef}
              onChange={(e) => {
                const file = e.target.files?.[0];

                if (file) {
                  void handleUpload(file);
                }

                e.target.value = "";
              }}
            />
          </>
        )}
      </Stack>

      <LoadingOverlay loading={loading} text="Dokumente werden geladen..." />

      {!loading && dokumente.length === 0 && (
        <Typography color="text.secondary">Noch keine Dokumente vorhanden.</Typography>
      )}

      {!loading && dokumente.length > 0 && (
        <List disablePadding>
          {dokumente.map((dokument) => (
            <ListItem
              key={dokument.id}
              divider
              secondaryAction={
                <>
                  <IconButton title="Anzeigen" onClick={() => void handlePreview(dokument)}>
                    <VisibilityIcon />
                  </IconButton>

                  <IconButton title="Herunterladen" onClick={() => void handleDownload(dokument)}>
                    <DownloadIcon />
                  </IconButton>

                  {!readOnly && (
                    <IconButton
                      color="error"
                      title="Löschen"
                      onClick={() => handleDelete(dokument.id)}
                    >
                      <DeleteIcon />
                    </IconButton>
                  )}
                </>
              }
            >
              <ListItemText
                primary={dokument.originalDateiname}
                secondary={`${formatSize(dokument.dateigroesse)} • ${dokument.mimeType}`}
              />
            </ListItem>
          ))}
        </List>
      )}
      <ImageCropPreviewDialog
        open={cropDialogOpen}
        file={selectedFile}
        onCancel={() => {
          setCropDialogOpen(false);
          setSelectedFile(null);
        }}
        onConfirm={(file) => {
          void handleCropConfirm(file);
        }}
      />
      <DeleteConfirmDialog
        open={deleteId !== null}
        title="Dokument löschen"
        message="Soll dieses Dokument wirklich gelöscht werden?"
        onClose={() => setDeleteId(null)}
        onConfirm={() => void confirmDelete()}
      />
    </Box>
  );
}
