// ReisekostenDetailPage.tsx

import { Alert, Stack, Box, Button, Paper, Typography } from "@mui/material";
import { useParams } from "react-router-dom";
import { useEffect, useState } from "react";
import FahrtabschnittDialog from "./FahrtabschnittDialog";
import ReisekostenFahrtabschnittTable from "./ReisekostenFahrtabschnittTable";
import { FahrtabschnittRequest } from "@/api/types/Reisekostenabrechnung";
import Money from "@/components/common/Money";
import { useReisekostenabrechnung } from "@/hooks/reisekosten/useReisekostenabrechnung";
import { updateReisekostenabrechnung } from "@/api/services/reisekostenApi";
import { BottomActionBar } from "@/components/layout/BottomActionBar";
import { useNavigate } from "react-router-dom";
import { PersonRef } from "@/api/types/PersonRef";
import { ReisekostenMitfahrerAutocomplete } from "@/components/finanzen/reisekosten/ReisekostenMitfahrerAutocomplete";

export default function ReisekostenDetailPage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const abrechnungId = Number(id);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [selectedAbschnitt, setSelectedAbschnitt] = useState<FahrtabschnittRequest | null>(null);
  const [fahrtabschnitte, setFahrtabschnitte] = useState<FahrtabschnittRequest[]>([]);
  const { data, isLoading, refetch } = useReisekostenabrechnung(abrechnungId);
  const [mitfahrerPool, setMitfahrerPool] = useState<PersonRef[]>([]);

 useEffect(() => {
   if (!data) {
     return;
   }

   setFahrtabschnitte(
     data.fahrtabschnitte.map((a) => ({
       id: a.id,
       reihenfolge: a.reihenfolge,
       beschreibung: a.beschreibung ?? "",
       vonPlz: a.vonPlz ?? "",
       vonOrt: a.vonOrt,
       vonCountryCode: a.vonCountryCode ?? null,
       nachPlz: a.nachPlz ?? "",
       nachOrt: a.nachOrt,
       nachCountryCode: a.nachCountryCode ?? null,
       kilometer: a.kilometer,
       anhaenger: a.anhaenger,
       mitfahrerIds: a.mitfahrer.map((m) => m.id),
     })),
   );

   const personen = new Map<number, PersonRef>();

   data.fahrtabschnitte.forEach((a) => {
     a.mitfahrer.forEach((p) => {
       personen.set(p.id, p);
     });
   });

   setMitfahrerPool(Array.from(personen.values()));
 }, [data]);

  if (Number.isNaN(abrechnungId)) {
    return <Alert severity="error">Ungültige Reisekosten-ID</Alert>;
  }

  if (isLoading || !data) {
    return <>Lade...</>;
  }

  const saveToBackend = async (neueFahrtabschnitte: FahrtabschnittRequest[]) => {
    await updateReisekostenabrechnung(abrechnungId, {
      abrechnungsdatum: data.abrechnungsdatum,
      bemerkung: data.bemerkung ?? "",
      fahrtabschnitte: neueFahrtabschnitte,
    });

    await refetch();

    setFahrtabschnitte(neueFahrtabschnitte);
  };

  return (
    <Box sx={{ p: 3 }}>
      <Paper sx={{ p: 2, mb: 3 }}>
        <Typography variant="h6" gutterBottom>
          Mitfahrer
        </Typography>

        <Stack spacing={1}>
          {mitfahrerPool.map((p) => (
            <Paper
              key={p.id}
              sx={{
                p: 1,
                display: "flex",
                justifyContent: "space-between",
                alignItems: "center",
              }}
            >
              <span>
                {p.vorname} {p.name}
              </span>

              <Button
                color="error"
                size="small"
                onClick={() => setMitfahrerPool((current) => current.filter((x) => x.id !== p.id))}
              >
                Entfernen
              </Button>
            </Paper>
          ))}
        </Stack>

        <Box sx={{ mt: 2 }}>
          <ReisekostenMitfahrerAutocomplete
            veranstaltungId={data.veranstaltungId}
            onChange={(person) => {
              if (!person) {
                return;
              }

              if (mitfahrerPool.some((p) => p.id === person.id)) {
                return;
              }

              setMitfahrerPool((current) => [...current, person]);
            }}
          />
        </Box>
      </Paper>
      <Typography variant="h5" gutterBottom>
        Reisekostenabrechnung
      </Typography>
      <Paper sx={{ p: 3, mb: 3 }}>
        <Box
          display="grid"
          gridTemplateColumns={{
            xs: "1fr",
            md: "repeat(3, 1fr)",
          }}
          gap={3}
          sx={{ mt: 3 }}
        >
          <Box>
            <Typography variant="caption" color="text.secondary">
              Fahrer
            </Typography>

            <Typography variant="h6">{data.fahrerName}</Typography>
          </Box>

          <Box>
            <Typography variant="caption" color="text.secondary">
              Veranstaltung
            </Typography>

            <Typography variant="h6">{data.veranstaltungName}</Typography>
          </Box>

          <Box>
            <Typography variant="caption" color="text.secondary">
              Datum
            </Typography>

            <Typography variant="h6">{data.abrechnungsdatum}</Typography>
          </Box>
        </Box>

        <Box
          display="grid"
          gridTemplateColumns={{
            xs: "1fr",
            md: "repeat(3, 1fr)",
          }}
          gap={3}
          sx={{ mt: 3 }}
        >
          <Box>
            <Typography variant="caption" color="text.secondary">
              Gesamtkilometer
            </Typography>

            <Typography variant="h6">{data.gesamtKilometer} km</Typography>
          </Box>

          <Box>
            <Typography variant="caption" color="text.secondary">
              Gesamtbetrag
            </Typography>

            <Typography variant="h6">
              <Money value={data.gesamtBetrag} />
            </Typography>
          </Box>
        </Box>

        {data.bemerkung && (
          <Box sx={{ mt: 3 }}>
            <Typography variant="caption" color="text.secondary">
              Bemerkung
            </Typography>

            <Typography>{data.bemerkung}</Typography>
          </Box>
        )}
      </Paper>

      <Typography variant="h6" gutterBottom>
        Fahrtabschnitte
      </Typography>

      <ReisekostenFahrtabschnittTable
        data={fahrtabschnitte}
        onEdit={(abschnitt) => {
          setSelectedAbschnitt(abschnitt);
          setDialogOpen(true);
        }}
        onDelete={async (abschnitt) => {
          const neueListe = fahrtabschnitte.filter((a) => a.id !== abschnitt.id);

          await saveToBackend(neueListe);
        }}
      />

      <FahrtabschnittDialog
        open={dialogOpen}
        veranstaltungId={data.veranstaltungId}
        mitfahrerPool={mitfahrerPool}
        initialData={selectedAbschnitt}
        onClose={() => {
          setDialogOpen(false);

          setSelectedAbschnitt(null);
        }}
        onSave={async (dto) => {
          let neueListe: FahrtabschnittRequest[];

          if (selectedAbschnitt) {
            neueListe = fahrtabschnitte.map((a) => (a.id === dto.id ? dto : a));
          } else {
            neueListe = [
              ...fahrtabschnitte,
              {
                ...dto,
                reihenfolge: Math.max(0, ...fahrtabschnitte.map((a) => a.reihenfolge)) + 1,
              },
            ];
          }

          await saveToBackend(neueListe);

          setDialogOpen(false);
        }}
      />
      <BottomActionBar
        left={[
          {
            label: "Zurück",
            variant: "outlined",
            onClick: () =>
              navigate(`/veranstaltungen/${data.veranstaltungId}/finanzen`, {
                state: {
                  tab: 4,
                },
              }),
          },
          {
            label: "Fahrtabschnitt hinzufügen",
            variant: "contained",
            onClick: () => {
              setSelectedAbschnitt(null);
              setDialogOpen(true);
            },
          },
        ]}
      />
    </Box>
  );
}
