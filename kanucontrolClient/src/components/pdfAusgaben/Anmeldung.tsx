import { Button, Box } from "@mui/material";
import { downloadFmJemReport } from "@/api/services/veranstaltungApi";
import { useEffect, useState } from "react";
import { getActiveVeranstaltung } from "@/api/services/veranstaltungApi";

const Anmeldung = () => {
  const [veranstaltungId, setVeranstaltungId] = useState<number | null>(null);

useEffect(() => {
  const load = async () => {
    try {
      const v = await getActiveVeranstaltung();

      if (!v) return;

      setVeranstaltungId(v.id);
    } catch (err) {
      console.error("Aktive Veranstaltung konnte nicht geladen werden", err);
    }
  };

  load();
}, []);

  const handleReport = async () => {
    if (!veranstaltungId) return;

    const blob = await downloadFmJemReport(veranstaltungId);

    const url = window.URL.createObjectURL(blob);
    window.open(url, "_blank"); // 👉 öffnet PDF im neuen Tab
  };

  return (
    <Box m="auto" maxWidth={600}>
      <h1>Anmeldung einer Maßnahme</h1>

      <Button variant="contained" onClick={handleReport}>
        FM / JEM Report öffnen
      </Button>
    </Box>
  );
};

export default Anmeldung;


