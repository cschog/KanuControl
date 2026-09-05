import React, { useEffect, useState } from "react";
import { Box, Button, Typography, Paper } from "@mui/material";
import PictureAsPdfIcon from "@mui/icons-material/PictureAsPdf";
import { getActiveVeranstaltung } from "@/api/services/veranstaltungApi";
import { VeranstaltungDetail } from "@/api/types/veranstaltung/VeranstaltungDetail";
import apiClient from "@/api/client/apiClient";
import { ErrorDialog } from "@/components/common/ErrorDialog";
import { getApiErrorMessage } from "@/api/utils/apiError";

const Erhebungsbogen: React.FC = () => {
  const [veranstaltung, setVeranstaltung] = useState<VeranstaltungDetail | null>(null);
  const [error, setError] = useState<string | null>(null);

  /* ================= Aktive Veranstaltung laden ================= */

  useEffect(() => {
    (async () => {
      try {
        const v = await getActiveVeranstaltung();
        setVeranstaltung(v);
      } catch (err: unknown) {
        console.error("Aktive Veranstaltung konnte nicht geladen werden", err);
        setError(getApiErrorMessage(err));
      }
    })();
  }, []);

  /* ================= PDF Vorschau ================= */

  const handlePreview = async () => {
    if (!veranstaltung?.id) return;

    try {
      const res = await apiClient.get(
        `/veranstaltungen/${veranstaltung.id}/erhebungsbogen/pdf/view`,
        {
          responseType: "blob",
        },
      );

      const blob = new Blob([res.data], {
        type: "application/pdf",
      });

      const url = window.URL.createObjectURL(blob);

      window.open(url, "_blank");
    } catch (err: unknown) {
      console.error("Erhebungsbogen konnte nicht als Vorschau geöffnet werden", err);
      setError(getApiErrorMessage(err));
    }
  };

  /* ================= PDF Download ================= */

  const handleDownload = async () => {
    if (!veranstaltung?.id) return;

    try {
      const res = await apiClient.get(
        `/veranstaltungen/${veranstaltung.id}/erhebungsbogen/pdf/download`,
        {
          responseType: "blob",
        },
      );

      const disposition = res.headers["content-disposition"];

      let filename = "erhebungsbogen.pdf";

      const match = disposition?.match(/filename="?([^";]+)"?/);

      if (match?.[1]) {
        filename = match[1];
      }

      const blob = new Blob([res.data], {
        type: "application/pdf",
      });

      const url = window.URL.createObjectURL(blob);

      const link = document.createElement("a");

      link.href = url;
      link.download = filename;

      document.body.appendChild(link);

      link.click();

      link.remove();

      window.URL.revokeObjectURL(url);
    } catch (err: unknown) {
      console.error("Erhebungsbogen konnte nicht heruntergeladen werden", err);
      setError(getApiErrorMessage(err));
    }
  };

  /* ================= UI ================= */

  return (
    <Box maxWidth={700} mx="auto" mt={4}>
      <Paper sx={{ p: 3 }}>
        <Typography variant="h5" gutterBottom>
          Erhebungsbogen
        </Typography>

        {veranstaltung ? (
          <>
            <Typography variant="body1" sx={{ mb: 2 }}>
              Veranstaltung: <b>{veranstaltung.name}</b>
            </Typography>

            <Box display="flex" flexDirection="column" gap={2}>
              <Button variant="contained" startIcon={<PictureAsPdfIcon />} onClick={handlePreview}>
                Erhebungsbogen Vorschau
              </Button>

              <Button variant="contained" startIcon={<PictureAsPdfIcon />} onClick={handleDownload}>
                Erhebungsbogen Download
              </Button>
            </Box>
          </>
        ) : (
          <Typography color="text.secondary">Keine aktive Veranstaltung gefunden</Typography>
        )}
      </Paper>

      <ErrorDialog open={!!error} message={error ?? ""} onClose={() => setError(null)} />
    </Box>
  );
};

export default Erhebungsbogen;
