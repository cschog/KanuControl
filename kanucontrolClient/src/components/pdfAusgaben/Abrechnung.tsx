import { Button, Box, Typography } from "@mui/material";
import { useEffect, useState } from "react";

import { getActiveVeranstaltung, downloadAbrechnungPdf } from "@/api/services/veranstaltungApi";

const Abrechnung = () => {
  const [veranstaltungId, setVeranstaltungId] = useState<number | null>(null);
  const [veranstaltungName, setVeranstaltungName] = useState<string | null>(null);

  /* ================= Aktive Veranstaltung laden ================= */

 useEffect(() => {
   const load = async () => {
     try {
       const v = await getActiveVeranstaltung();

       if (!v) return;

       setVeranstaltungId(v.id);
       setVeranstaltungName(v.name);
     } catch (err) {
       console.error("Aktive Veranstaltung konnte nicht geladen werden", err);
     }
   };

   load();
 }, []);

  /* ================= PDF öffnen ================= */

  const handleAbrechnung = async () => {
    if (!veranstaltungId) return;

    const blob = await downloadAbrechnungPdf(veranstaltungId);

    const url = window.URL.createObjectURL(blob);
    window.open(url, "_blank"); // 👉 öffnet PDF im neuen Tab
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

      <Button variant="contained" onClick={handleAbrechnung} disabled={!veranstaltungId}>
        Abrechnung (Deckblatt) öffnen
      </Button>
    </Box>
  );
};

export default Abrechnung;
