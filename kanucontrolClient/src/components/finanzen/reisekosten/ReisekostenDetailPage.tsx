// ReisekostenDetailPage.tsx

import { Alert, Box, Paper, Stack, Typography } from "@mui/material";
import { useParams } from "react-router-dom";

import Money from "@/components/common/Money";

import { useReisekostenabrechnung } from "@/hooks/reisekosten/useReisekostenabrechnung";

export default function ReisekostenDetailPage() {
  const { id } = useParams();

  const abrechnungId = Number(id);

  const { data, isLoading } = useReisekostenabrechnung(abrechnungId);

  if (Number.isNaN(abrechnungId)) {
    return <Alert severity="error">Ungültige Reisekosten-ID</Alert>;
  }

  if (isLoading || !data) {
    return <>Lade...</>;
  }

  return (
    <Box sx={{ p: 3 }}>
      <Typography variant="h5" gutterBottom>
        Reisekostenabrechnung
      </Typography>

      <Paper sx={{ p: 2, mb: 3 }}>
        <Stack spacing={1}>
          <Typography>Fahrer: {data.fahrerName}</Typography>

          <Typography>Veranstaltung: {data.veranstaltungName}</Typography>

          <Typography>Datum: {data.abrechnungsdatum}</Typography>

          <Typography>Kilometer: {data.gesamtKilometer}</Typography>

          <Typography>
            Betrag: <Money value={data.gesamtBetrag} />
          </Typography>

          {data.bemerkung && <Typography>Bemerkung: {data.bemerkung}</Typography>}
        </Stack>
      </Paper>

      <Typography variant="h6" gutterBottom>
        Fahrtabschnitte
      </Typography>

      {data.fahrtabschnitte.length === 0 ? (
        <Alert severity="info">Noch keine Fahrtabschnitte vorhanden.</Alert>
      ) : (
        <Stack spacing={2}>
          {data.fahrtabschnitte.map((abschnitt) => (
            <Paper key={abschnitt.id} sx={{ p: 2 }}>
              <Typography fontWeight={600}>
                {abschnitt.vonOrt}
                {" → "}
                {abschnitt.nachOrt}
              </Typography>

              <Typography>{abschnitt.kilometer} km</Typography>

              {abschnitt.beschreibung && (
                <Typography color="text.secondary">{abschnitt.beschreibung}</Typography>
              )}
            </Paper>
          ))}
        </Stack>
      )}
    </Box>
  );
}
