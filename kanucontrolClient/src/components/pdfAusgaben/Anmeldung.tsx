import { Button, Box, Typography } from "@mui/material";
import { useEffect, useState } from "react";
import { getActiveVeranstaltung } from "@/api/services/veranstaltungApi";
import apiClient from "@/api/client/apiClient";

const Anmeldung = () => {
  const [veranstaltungId, setVeranstaltungId] = useState<number | null>(null);
 const [veranstaltungName, setVeranstaltungName] = useState<string | null>(null);

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

 const handlePreview = async () => {
   if (!veranstaltungId) return;

   const response = await apiClient.get(`/veranstaltungen/${veranstaltungId}/fm-jem-report/view`, {
     responseType: "blob",
   });

   const url = window.URL.createObjectURL(response.data);

   window.open(url, "_blank");
 };

 const handleDownload = async () => {
   if (!veranstaltungId) return;

   const response = await apiClient.get(
     `/veranstaltungen/${veranstaltungId}/fm-jem-report/download`,
     {
       responseType: "blob",
     },
   );

   const disposition = response.headers["content-disposition"];

   let filename = "fm-jem-antrag.pdf";

   const match = disposition?.match(/filename="?([^";]+)"?/);

   if (match?.[1]) {
     filename = match[1];
   }

   const blob = new Blob([response.data], {
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

  return (
    <Box m="auto" maxWidth={600}>
      <Typography variant="h4" gutterBottom>
        Anmeldung Deckblatt
      </Typography>

      {veranstaltungName && (
        <Typography variant="body1" sx={{ mb: 2 }}>
          Veranstaltung: <b>{veranstaltungName}</b>
        </Typography>
      )}

      <Box display="flex" flexDirection="column" gap={2}>
        <Button variant="contained" onClick={handlePreview} disabled={!veranstaltungId}>
          Anmeldung (Vorschau)
        </Button>

        <Button variant="contained" onClick={handleDownload} disabled={!veranstaltungId}>
          Anmeldung (Download)
        </Button>
      </Box>
    </Box>
  );
};

export default Anmeldung;


