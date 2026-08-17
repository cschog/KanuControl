import { useCallback, useEffect, useRef, useState } from "react";
import {
  Box,
  Button,
  FormControl,
  FormControlLabel,
  IconButton,
  List,
  ListItem,
  ListItemText,
  Radio,
  RadioGroup,
  Stack,
  Typography,
} from "@mui/material";

import { ReferenzObjekt } from "@/api/enums/ReferenzObjekt";


import DeleteIcon from "@mui/icons-material/Delete";
import DownloadIcon from "@mui/icons-material/Download";
import UploadFileIcon from "@mui/icons-material/UploadFile";
import VisibilityIcon from "@mui/icons-material/Visibility";

import {
  deleteZahlungsnachweisDokument,
  download,
  findAll,
  upload,
} from "@/api/services/zahlungsnachweisApi";

import { DokumentDTO } from "@/api/types/dokument";
import LoadingOverlay from "@/components/common/LoadingOverlay";
import DeleteConfirmDialog from "@/components/common/DeleteConfirmDialog";

interface Props {
  veranstaltungId: number;
  zahlungsnachweisId: number;
  readOnly?: boolean;
}

const REFERENZ_STORAGE_KEY = "kanucontrol.dokument.referenzObjekt";

export default function ZahlungsnachweisDokumentPanel({
  veranstaltungId,
  zahlungsnachweisId,
  readOnly = false,
}: Props) {
  const [dokumente, setDokumente] = useState<DokumentDTO[]>([]);
  const [loading, setLoading] = useState(false);

  const fileInputRef = useRef<HTMLInputElement>(null);
  const [deleteId, setDeleteId] = useState<number | null>(null);

  const [referenzObjekt, setReferenzObjekt] = useState<ReferenzObjekt>(() => {
    const gespeichert = localStorage.getItem(REFERENZ_STORAGE_KEY);

    if (gespeichert && Object.values(ReferenzObjekt).includes(gespeichert as ReferenzObjekt)) {
      return gespeichert as ReferenzObjekt;
    }

    return ReferenzObjekt.DIN_A6;
  });

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
   await uploadFile(file, referenzObjekt);
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

  async function handleDownload(dokument: DokumentDTO) {
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

  async function handlePreview(dokument: DokumentDTO) {
    const blob = await download(veranstaltungId, zahlungsnachweisId, dokument.id);

    const url = URL.createObjectURL(blob);

    window.open(url, "_blank");

    setTimeout(() => URL.revokeObjectURL(url), 60_000);
  }

  

 async function uploadFile(file: File, referenz: ReferenzObjekt) {
   setLoading(true);

   try {
     await upload(veranstaltungId, zahlungsnachweisId, file, referenz);

     await loadDokumente(false);
   } finally {
     setLoading(false);
   }
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
          <Stack direction="row" spacing={2} alignItems="center">
            <FormControl>
              <RadioGroup
                row
                value={referenzObjekt}
                onChange={(event) => {
                  const value = event.target.value as ReferenzObjekt;

                  setReferenzObjekt(value);

                  localStorage.setItem(REFERENZ_STORAGE_KEY, value);
                }}
              >
                <FormControlLabel
                  value={ReferenzObjekt.DIN_A7}
                  control={<Radio size="small" />}
                  label="A7"
                />

                <FormControlLabel
                  value={ReferenzObjekt.DIN_A6}
                  control={<Radio size="small" />}
                  label="A6"
                />

                <FormControlLabel
                  value={ReferenzObjekt.DIN_A5}
                  control={<Radio size="small" />}
                  label="A5"
                />

                <FormControlLabel
                  value={ReferenzObjekt.DIN_A4}
                  control={<Radio size="small" />}
                  label="A4"
                />
              </RadioGroup>
            </FormControl>

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
          </Stack>
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
