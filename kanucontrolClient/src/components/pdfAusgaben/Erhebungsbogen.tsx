import React, { useEffect, useState } from "react";
import { Box, Button, Typography, Paper } from "@mui/material";
import PictureAsPdfIcon from "@mui/icons-material/PictureAsPdf";

import { getActiveVeranstaltung, downloadErhebungsbogenPdf } from "@/api/services/veranstaltungApi";

import { VeranstaltungDetail } from "@/api/types/VeranstaltungDetail";

const Erhebungsbogen: React.FC = () => {
  const [veranstaltung, setVeranstaltung] = useState<VeranstaltungDetail | null>(null);

  /* ================= Aktive Veranstaltung laden ================= */

  useEffect(() => {
    (async () => {
      try {
        const v = await getActiveVeranstaltung();
        setVeranstaltung(v);
      } catch {
        console.error("Keine aktive Veranstaltung gefunden");
      }
    })();
  }, []);

  /* ================= PDF Download ================= */

 const handleOpen = async () => {
   if (!veranstaltung?.id) return;

   const res = await downloadErhebungsbogenPdf(veranstaltung.id);

   const blob = new Blob([res.data], { type: "application/pdf" });
   const url = window.URL.createObjectURL(blob);
   window.open(url, "_blank");
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

            <Button
              variant="contained"
              startIcon={<PictureAsPdfIcon />}
              onClick={handleOpen}
         
            >
              Erhebungsbogen in neuem Tab öffnen
            </Button>
          </>
        ) : (
          <Typography color="text.secondary">Keine aktive Veranstaltung gefunden</Typography>
        )}
      </Paper>
    </Box>
  );
};

export default Erhebungsbogen;
