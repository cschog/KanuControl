import React, { useEffect, useState } from "react";

import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  FormControlLabel,
  Typography,
  Stack,
  CircularProgress,
} from "@mui/material";

import Checkbox from "@mui/material/Checkbox";
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

  const [selectedIds, setSelectedIds] = useState<number[]>([]);

  const toggleSelection = (id: number) => {
    setSelectedIds((prev) => (prev.includes(id) ? prev.filter((x) => x !== id) : [...prev, id]));
  };

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

        setSelectedIds(data.filter((rk) => rk.druckbar).map((rk) => rk.id));
      } catch (err) {
        console.error("Fehler beim Laden der Reisekosten", err);
      } finally {
        setLoading(false);
      }
    };

    loadData();
  }, [open, veranstaltungId]);

  /* =========================================================
     Download
     ========================================================= */

  const handleDownloadAll = async () => {
    for (const rk of abrechnungen) {
      if (!rk.druckbar) {
        continue;
      }

      if (!selectedIds.includes(rk.id)) {
        continue;
      }

      await downloadPdf(rk.id);

      await new Promise((resolve) => setTimeout(resolve, 300));
    }
  };

  const downloadPdf = async (id: number) => {
    const res = await apiClient.get(
      `/veranstaltungen/${veranstaltungId}/reisekosten/${id}/pdf/download`,
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
          <Stack spacing={1}>
            {abrechnungen.map((rk) => (
              <FormControlLabel
                key={rk.id}
                control={
                  <Checkbox
                    checked={selectedIds.includes(rk.id)}
                    onChange={() => toggleSelection(rk.id)}
                    disabled={!rk.druckbar}
                  />
                }
                label={
                  <Stack spacing={0.5}>
                    <Typography>
                      {rk.fahrerName} • {rk.gesamtKilometer} km • {rk.gesamtBetrag} €
                    </Typography>

                    {!rk.druckbar && (
                      <Typography variant="caption" color="error">
                        {rk.fehler.join(", ")}
                      </Typography>
                    )}
                  </Stack>
                }
              />
            ))}
          </Stack>
        )}
      </DialogContent>

      <DialogActions>
        <Button onClick={onClose}>Schließen</Button>

        <Button
          onClick={() =>
            setSelectedIds(abrechnungen.filter((rk) => rk.druckbar).map((rk) => rk.id))
          }
        >
          Alle auswählen
        </Button>
        <Button onClick={() => setSelectedIds([])}>Auswahl löschen</Button>
        <Button variant="contained" disabled={selectedIds.length === 0} onClick={handleDownloadAll}>
          Alle ausgeben
        </Button>
      </DialogActions>
    </Dialog>
  );
};
