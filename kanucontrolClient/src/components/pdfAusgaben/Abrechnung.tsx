import { Button, Box, Typography } from "@mui/material";
import { useEffect, useState } from "react";
import apiClient from "@/api/client/apiClient";
import { getActiveVeranstaltung } from "@/api/services/veranstaltungApi";
import { ErrorDialog } from "@/components/common/ErrorDialog";
import { getApiErrorMessage } from "@/api/utils/apiError";

const Abrechnung = () => {
  const [veranstaltungId, setVeranstaltungId] = useState<number | null>(null);
  const [veranstaltungName, setVeranstaltungName] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);

  /* ================= Aktive Veranstaltung laden ================= */

  useEffect(() => {
    const load = async () => {
      try {
        const v = await getActiveVeranstaltung();

        if (!v) return;

        setVeranstaltungId(v.id);
        setVeranstaltungName(v.name);
      } catch (err: unknown) {
        console.error("Aktive Veranstaltung konnte nicht geladen werden", err);
        setError(getApiErrorMessage(err));
      }
    };

    load();
  }, []);

  /* ================= PDF öffnen ================= */

  const handlePreview = async () => {
    if (!veranstaltungId) return;

    try {
      const response = await apiClient.get(
        `/veranstaltungen/${veranstaltungId}/abrechnung/pdf/view`,
        {
          responseType: "blob",
        },
      );

      const url = window.URL.createObjectURL(response.data);

      window.open(url, "_blank");
    } catch (err: unknown) {
      console.error("Abrechnung konnte nicht als Vorschau geöffnet werden", err);
      setError(getApiErrorMessage(err));
    }
  };

  /* ================= PDF Download ================= */

  const handleDownload = async () => {
    if (!veranstaltungId) return;

    try {
      const response = await apiClient.get(
        `/veranstaltungen/${veranstaltungId}/abrechnung/pdf/download`,
        {
          responseType: "blob",
        },
      );

      // Dateiname aus Header lesen
      const disposition = response.headers["content-disposition"];

      let filename = "download.pdf";

      const match = disposition?.match(/filename="(.+)"/);

      if (match?.[1]) {
        filename = match[1];
      }

      // Blob erzeugen
      const blob = new Blob([response.data], {
        type: "application/pdf",
      });

      const url = window.URL.createObjectURL(blob);

      // Download-Link erzeugen
      const a = document.createElement("a");

      a.href = url;
      a.download = filename;

      document.body.appendChild(a);

      a.click();

      a.remove();

      window.URL.revokeObjectURL(url);
    } catch (err: unknown) {
      console.error("Abrechnung konnte nicht heruntergeladen werden", err);
      setError(getApiErrorMessage(err));
    }
  };

  /* ================= UI ================= */

  return (
    <Box m="auto" maxWidth={600}>
      <Typography variant="h4" gutterBottom>
        Abrechnung Deckblatt
      </Typography>

      {veranstaltungName && (
        <Typography variant="body1" sx={{ mb: 2 }}>
          Veranstaltung: <b>{veranstaltungName}</b>
        </Typography>
      )}

      <Box display="flex" flexDirection="column" gap={2}>
        <Button variant="contained" onClick={handlePreview} disabled={!veranstaltungId}>
          Abrechnung (Vorschau)
        </Button>

        <Button variant="contained" onClick={handleDownload} disabled={!veranstaltungId}>
          Abrechnung (Download)
        </Button>
      </Box>

      <ErrorDialog open={!!error} message={error ?? ""} onClose={() => setError(null)} />
    </Box>
  );
};

export default Abrechnung;
