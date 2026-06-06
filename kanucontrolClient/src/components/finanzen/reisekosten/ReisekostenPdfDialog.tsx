import React, { useEffect, useState } from "react";

import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  RadioGroup,
  FormControlLabel,
  Radio,
  Typography,
  Stack,
  CircularProgress,
} from "@mui/material";

import apiClient from "@/api/client/apiClient";
import { ReisekostenabrechnungListResponse } from "@/api/types/Reisekostenabrechnung";

interface ReisekostenPdfDialogProps {
  open: boolean;
  veranstaltungId: number;
  onClose: () => void;
}

export const ReisekostenPdfDialog: React.FC<ReisekostenPdfDialogProps> = ({
  open,
  veranstaltungId,
  onClose,
}) => {
  const [loading, setLoading] = useState(false);

  const [abrechnungen, setAbrechnungen] = useState<ReisekostenabrechnungListResponse[]>([]);

  const [selectedId, setSelectedId] = useState<number | null>(null);

  /* =========================================================
     Laden
     ========================================================= */

  useEffect(() => {
    if (!open) {
      return;
    }

    const loadData = async () => {
      try {
        setLoading(true);

        const res = await apiClient.get(`/veranstaltungen/${veranstaltungId}/reisekosten`);

        const data = res.data as ReisekostenabrechnungListResponse[];

        setAbrechnungen(data);

        if (data.length > 0) {
          setSelectedId(data[0].id);
        }
      } catch (err) {
        console.error("Fehler beim Laden der Reisekosten", err);
      } finally {
        setLoading(false);
      }
    };

    loadData();
  }, [open, veranstaltungId]);


  /* =========================================================
     Vorschau
     ========================================================= */

  const handlePreview = async () => {
    if (!selectedId) {
      return;
    }

    const res = await apiClient.get(
      `/veranstaltungen/${veranstaltungId}/reisekosten/${selectedId}/pdf/view`,
      {
        responseType: "blob",
      },
    );

    const blob = new Blob([res.data], {
      type: "application/pdf",
    });

    const url = URL.createObjectURL(blob);

    window.open(url, "_blank");
  };

  /* =========================================================
     Download
     ========================================================= */

  const handleDownload = async () => {
    if (!selectedId) {
      return;
    }

    const res = await apiClient.get(
      `/veranstaltungen/${veranstaltungId}/reisekosten/${selectedId}/pdf/download`,
      {
        responseType: "blob",
      },
    );

    const disposition = res.headers["content-disposition"];

    let filename = "fahrkosten.pdf";

    const match = disposition?.match(/filename="?([^";]+)"?/);

    if (match?.[1]) {
      filename = match[1];
    }

    const blob = new Blob([res.data], {
      type: "application/pdf",
    });

    const url = URL.createObjectURL(blob);

    const link = document.createElement("a");

    link.href = url;
    link.download = filename;

    document.body.appendChild(link);

    link.click();

    link.remove();

    URL.revokeObjectURL(url);
  };

  /* =========================================================
     Render
     ========================================================= */

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>Fahrkostenabrechnung auswählen</DialogTitle>

      <DialogContent>
        {loading ? (
          <Stack alignItems="center" justifyContent="center" py={4}>
            <CircularProgress />
          </Stack>
        ) : abrechnungen.length === 0 ? (
          <Typography color="text.secondary">
            Für diese Veranstaltung wurden noch keine Reisekostenabrechnungen erfasst.
          </Typography>
        ) : (
          <RadioGroup value={selectedId} onChange={(e) => setSelectedId(Number(e.target.value))}>
            {abrechnungen.map((rk) => (
              <FormControlLabel
                key={rk.id}
                value={rk.id}
                control={<Radio />}
                label={
                  `${rk.fahrerName} • ` + `${rk.gesamtKilometer} km • ` + `${rk.gesamtBetrag} €`
                }
              />
            ))}
          </RadioGroup>
        )}
      </DialogContent>

      <DialogActions>
        <Button onClick={onClose}>Schließen</Button>

        <Button variant="outlined" disabled={!selectedId} onClick={handlePreview}>
          Vorschau
        </Button>

        <Button variant="contained" disabled={!selectedId} onClick={handleDownload}>
          Download
        </Button>
      </DialogActions>
    </Dialog>
  );
};
