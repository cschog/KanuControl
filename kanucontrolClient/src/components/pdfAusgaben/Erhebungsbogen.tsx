import React, { useEffect, useState } from "react";
import { Box, Button, Typography, Paper } from "@mui/material";
import PictureAsPdfIcon from "@mui/icons-material/PictureAsPdf";
import { getActiveVeranstaltung } from "@/api/services/veranstaltungApi";
import { VeranstaltungDetail } from "@/api/types/VeranstaltungDetail";
import apiClient from "@/api/client/apiClient";

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

 const handlePreview = async () => {
   if (!veranstaltung?.id) return;

   const res = await apiClient.get(`/veranstaltungen/${veranstaltung.id}/erhebungsbogen/pdf/view`, {
     responseType: "blob",
   });

   const blob = new Blob([res.data], {
     type: "application/pdf",
   });

   const url = window.URL.createObjectURL(blob);

   window.open(url, "_blank");
 };

 const handleDownload = async () => {
   if (!veranstaltung?.id) return;

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
    </Box>
  );
};

export default Erhebungsbogen;
