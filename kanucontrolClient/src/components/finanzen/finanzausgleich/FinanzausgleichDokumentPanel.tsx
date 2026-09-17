import { useCallback, useEffect, useRef, useState } from "react";
import {
  Box,
  Button,
  FormControl,
  FormControlLabel,
  IconButton,
  List,
  ListItem,
  MenuItem,
  Radio,
  RadioGroup,
  Select,
  Stack,
  Typography,
  useMediaQuery,
  useTheme,
} from "@mui/material";

import { ReferenzObjekt } from "@/api/enums/ReferenzObjekt";
import {
  deleteFinanzausgleichDokument,
  downloadFinanzausgleichDokument,
  findFinanzausgleichDokumente,
  updateFinanzausgleichDokumentReferenzObjekt,
  uploadFinanzausgleichDokument,
} from "@/api/services/finanzgruppenApi";

import DeleteIcon from "@mui/icons-material/Delete";
import DownloadIcon from "@mui/icons-material/Download";
import UploadFileIcon from "@mui/icons-material/UploadFile";
import VisibilityIcon from "@mui/icons-material/Visibility";

import { DokumentDTO } from "@/api/types/dokument";
import LoadingOverlay from "@/components/common/LoadingOverlay";
import DeleteConfirmDialog from "@/components/common/DeleteConfirmDialog";

interface Props {
  veranstaltungId: number;
  finanzGruppeId: number;
  readOnly?: boolean;
}

const REFERENZ_STORAGE_KEY = "kanucontrol.dokument.referenzObjekt";

export default function FinanzausgleichDokumentPanel({
  veranstaltungId,
  finanzGruppeId,
  readOnly = false,
}: Props) {
  const [dokumente, setDokumente] = useState<DokumentDTO[]>([]);
  const [loading, setLoading] = useState(false);

  const fileInputRef = useRef<HTMLInputElement>(null);
  const [deleteId, setDeleteId] = useState<number | null>(null);
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down("sm"));

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
        const data = await findFinanzausgleichDokumente(veranstaltungId, finanzGruppeId);

        setDokumente(data ?? []);
      } finally {
        if (showLoading) {
          setLoading(false);
        }
      }
    },
    [veranstaltungId, finanzGruppeId],
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

  async function uploadFile(file: File, referenz: ReferenzObjekt) {
    setLoading(true);

    try {
      await uploadFinanzausgleichDokument(veranstaltungId, finanzGruppeId, file, referenz);

      await loadDokumente(false);
    } finally {
      setLoading(false);
    }
  }

  /* =========================================================
     REFERENZOBJEKT
  ========================================================= */

  async function handleReferenzObjektChange(dokumentId: number, value: ReferenzObjekt) {
    setLoading(true);

    try {
      const aktualisiert = await updateFinanzausgleichDokumentReferenzObjekt(
        veranstaltungId,
        finanzGruppeId,
        dokumentId,
        value,
      );

      setDokumente((aktuell) =>
        aktuell.map((dokument) => (dokument.id === dokumentId ? aktualisiert : dokument)),
      );
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
      await deleteFinanzausgleichDokument(veranstaltungId, finanzGruppeId, deleteId);

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
    const blob = await downloadFinanzausgleichDokument(
      veranstaltungId,
      finanzGruppeId,
      dokument.id,
    );

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
    const blob = await downloadFinanzausgleichDokument(
      veranstaltungId,
      finanzGruppeId,
      dokument.id,
    );

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
      <Stack
        direction={{ xs: "column", sm: "row" }}
        justifyContent="space-between"
        alignItems={{ xs: "stretch", sm: "center" }}
        spacing={{ xs: 1.5, sm: 2 }}
        mb={2}
      >
        <Typography variant="h6">Nachweis Finanzausgleich</Typography>

        {!readOnly && (
          <Stack
            direction={{ xs: "column", sm: "row" }}
            spacing={{ xs: 1.5, sm: 2 }}
            alignItems={{ xs: "stretch", sm: "center" }}
            sx={{ width: { xs: "100%", sm: "auto" } }}
          >
            <FormControl
              sx={{
                width: { xs: "100%", sm: "auto" },
              }}
            >
              <RadioGroup
                row
                value={referenzObjekt}
                onChange={(event) => {
                  const value = event.target.value as ReferenzObjekt;

                  setReferenzObjekt(value);
                  localStorage.setItem(REFERENZ_STORAGE_KEY, value);
                }}
                sx={{
                  width: "100%",
                  display: "grid",
                  gridTemplateColumns: {
                    xs: "repeat(2, 1fr)",
                    sm: "repeat(4, auto)",
                  },
                  columnGap: { xs: 1, sm: 0.5 },
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
              fullWidth={isMobile}
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
        <Typography color="text.secondary">Noch keine Nachweisdokumente vorhanden.</Typography>
      )}

      {!loading && dokumente.length > 0 && (
        <List disablePadding>
          {dokumente.map((dokument) => (
            <ListItem
              key={dokument.id}
              divider
              disableGutters
              sx={{
                py: 1.5,
              }}
            >
              <Box sx={{ width: "100%", minWidth: 0 }}>
                {/* Dateiname */}
                <Typography
                  variant="body1"
                  sx={{
                    fontWeight: 500,
                    overflow: "hidden",
                    textOverflow: "ellipsis",
                    whiteSpace: "nowrap",
                    pr: 1,
                  }}
                  title={dokument.originalDateiname}
                >
                  {dokument.originalDateiname}
                </Typography>

                {/* Informationen und Format */}
                <Stack
                  direction={{ xs: "column", sm: "row" }}
                  spacing={{ xs: 0.5, sm: 2 }}
                  alignItems={{ xs: "stretch", sm: "center" }}
                  sx={{ mt: 0.5 }}
                >
                  <Typography variant="body2" color="text.secondary">
                    {formatSize(dokument.dateigroesse)}
                    {" • "}
                    {dokument.mimeType}
                  </Typography>

                  {!readOnly && (
                    <Stack direction="row" spacing={0.75} alignItems="center">
                      <Typography component="span" variant="body2" color="text.secondary">
                        Format:
                      </Typography>

                      <FormControl
                        size="small"
                        sx={{
                          minWidth: 75,
                          width: { xs: 85, sm: "auto" },
                        }}
                      >
                        <Select
                          value={dokument.referenzObjekt ?? ReferenzObjekt.DIN_A6}
                          onChange={(event) => {
                            void handleReferenzObjektChange(
                              dokument.id,
                              event.target.value as ReferenzObjekt,
                            );
                          }}
                          inputProps={{
                            "aria-label": "Dokumentformat",
                          }}
                        >
                          <MenuItem value={ReferenzObjekt.DIN_A7}>A7</MenuItem>
                          <MenuItem value={ReferenzObjekt.DIN_A6}>A6</MenuItem>
                          <MenuItem value={ReferenzObjekt.DIN_A5}>A5</MenuItem>
                          <MenuItem value={ReferenzObjekt.DIN_A4}>A4</MenuItem>
                        </Select>
                      </FormControl>
                    </Stack>
                  )}
                </Stack>

                {/* Aktionen */}
                <Stack direction="row" justifyContent="flex-end" spacing={0.5} sx={{ mt: 0.75 }}>
                  <IconButton
                    size="small"
                    title="Anzeigen"
                    onClick={() => void handlePreview(dokument)}
                  >
                    <VisibilityIcon />
                  </IconButton>

                  <IconButton
                    size="small"
                    title="Herunterladen"
                    onClick={() => void handleDownload(dokument)}
                  >
                    <DownloadIcon />
                  </IconButton>

                  {!readOnly && (
                    <IconButton
                      size="small"
                      color="error"
                      title="Löschen"
                      onClick={() => handleDelete(dokument.id)}
                    >
                      <DeleteIcon />
                    </IconButton>
                  )}
                </Stack>
              </Box>
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
