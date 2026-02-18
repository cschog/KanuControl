import React, { useEffect, useState } from "react";
import { Box, Button, Typography, Paper } from "@mui/material";
import PictureAsPdfIcon from "@mui/icons-material/PictureAsPdf";

import { getActiveVeranstaltung, downloadTeilnehmerPdf } from "@/api/services/veranstaltungApi";

import { VeranstaltungDetail } from "@/api/types/VeranstaltungDetail";

const Teilnehmerliste: React.FC = () => {
  const [veranstaltung, setVeranstaltung] = useState<VeranstaltungDetail | null>(null);

  const [loading, setLoading] = useState(false);

  /* ================= Load aktive Veranstaltung ================= */

  useEffect(() => {
    (async () => {
      try {
        const v = await getActiveVeranstaltung();
        setVeranstaltung(v);
      } catch {
        console.error("Keine aktive Veranstaltung");
      }
    })();
  }, []);

  /* ================= PDF Download ================= */

  const handleDownload = async () => {
    if (!veranstaltung?.id) return;

    setLoading(true);

    try {
      const res = await downloadTeilnehmerPdf(veranstaltung.id);

      const blob = new Blob([res.data], { type: "application/pdf" });

    const disposition = res.headers["content-disposition"];
    let filename = "Teilnehmerliste.pdf";

    if (disposition) {
      // RFC5987 (filename*=UTF-8'')
      const utfMatch = disposition.match(/filename\*=UTF-8''([^;]+)/);
      if (utfMatch?.[1]) {
        filename = decodeURIComponent(utfMatch[1]);
      } else {
        // Fallback filename=""
       const asciiMatch = disposition.match(/filename="?([^";]+)"?/);
        if (asciiMatch?.[1]) filename = asciiMatch[1];
      }
    }

      const url = window.URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = url;
      link.download = filename;
      document.body.appendChild(link);
      link.click();
      link.remove();
      window.URL.revokeObjectURL(url);
    } finally {
      setLoading(false);
    }
  };

  /* ================= UI ================= */

  return (
    <Box maxWidth={700} mx="auto" mt={4}>
      <Paper sx={{ p: 3 }}>
        <Typography variant="h5" gutterBottom>
          Teilnehmerliste
        </Typography>

        {veranstaltung ? (
          <>
            <Typography variant="body1" sx={{ mb: 2 }}>
              Veranstaltung: <b>{veranstaltung.name}</b>
            </Typography>

            <Button
              variant="contained"
              startIcon={<PictureAsPdfIcon />}
              onClick={handleDownload}
              disabled={loading}
            >
              Teilnehmerliste als PDF erzeugen
            </Button>
          </>
        ) : (
          <Typography color="text.secondary">Keine aktive Veranstaltung gefunden</Typography>
        )}
      </Paper>
    </Box>
  );
};

export default Teilnehmerliste;
