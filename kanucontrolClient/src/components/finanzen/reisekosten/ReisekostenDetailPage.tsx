// ReisekostenDetailPage.tsx

import { Alert, Stack, Box, Button, Paper, Typography } from "@mui/material";
import Tooltip from "@mui/material/Tooltip";
import LockIcon from "@mui/icons-material/Lock";
import { useParams } from "react-router-dom";
import { useEffect, useState } from "react";
import FahrtabschnittDialog from "./FahrtabschnittDialog";
import ReisekostenFahrtabschnittTable from "@/components/finanzen/reisekosten/ReisekostenFahrtabschnittTable";
import { FahrtabschnittRequest } from "@/api/types/Reisekostenabrechnung";
import Money from "@/components/common/Money";
import { useReisekostenabrechnung } from "@/hooks/reisekosten/useReisekostenabrechnung";
import { updateReisekostenabrechnung } from "@/api/services/reisekostenApi";
import { BottomActionBar } from "@/components/layout/BottomActionBar";
import { useNavigate } from "react-router-dom";
import { PersonRef } from "@/api/types/person/PersonRef";
import { ReisekostenMitfahrerAutocomplete } from "@/components/finanzen/reisekosten/ReisekostenMitfahrerAutocomplete";
import { useTheme } from "@mui/material/styles";
import useMediaQuery from "@mui/material/useMediaQuery";


export default function ReisekostenDetailPage() {
  const navigate = useNavigate();
  const { id } = useParams();
  const abrechnungId = Number(id);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [selectedAbschnitt, setSelectedAbschnitt] = useState<FahrtabschnittRequest | null>(null);
  const [fahrtabschnitte, setFahrtabschnitte] = useState<FahrtabschnittRequest[]>([]);
  const { data, isLoading, refetch } = useReisekostenabrechnung(abrechnungId);
  const [mitfahrerPool, setMitfahrerPool] = useState<PersonRef[]>([]);
  const [selectedMitfahrer, setSelectedMitfahrer] = useState<PersonRef | undefined>();
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down("sm"));

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

    setMitfahrerPool(data.mitfahrer);
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
      mitfahrerIds: mitfahrerPool.map((p) => p.id),
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

              <Tooltip
                title={
                  p.verwendetInFahrtabschnitten
                    ? "Mitfahrer wird in einem Fahrtabschnitt verwendet"
                    : "Mitfahrer entfernen"
                }
              >
                <span>
                  <Button
                    color={p.verwendetInFahrtabschnitten ? "inherit" : "error"}
                    size="small"
                    disabled={p.verwendetInFahrtabschnitten}
                    startIcon={p.verwendetInFahrtabschnitten ? <LockIcon /> : undefined}
                    onClick={() =>
                      setMitfahrerPool((current) => current.filter((x) => x.id !== p.id))
                    }
                  >
                    {p.verwendetInFahrtabschnitten ? "Verwendet" : "Entfernen"}
                  </Button>
                </span>
              </Tooltip>
            </Paper>
          ))}
        </Stack>

        <Box sx={{ mt: 2 }}>
          <ReisekostenMitfahrerAutocomplete
            veranstaltungId={data.veranstaltungId}
            value={selectedMitfahrer}
            excludeIds={mitfahrerPool.map((p) => p.id)}
            onChange={async (person) => {
              if (!person) {
                setSelectedMitfahrer(undefined);
                return;
              }

              if (mitfahrerPool.some((p) => p.id === person.id)) {
                setSelectedMitfahrer(undefined);
                return;
              }

              const neuerPool = [...mitfahrerPool, person];

              setMitfahrerPool(neuerPool);

              await updateReisekostenabrechnung(abrechnungId, {
                abrechnungsdatum: data.abrechnungsdatum,
                bemerkung: data.bemerkung ?? "",
                mitfahrerIds: neuerPool.map((p) => p.id),
                fahrtabschnitte,
              });

              await refetch();

              setSelectedMitfahrer(undefined);
            }}
          />
        </Box>
      </Paper>
      {!isMobile && (
        <Typography variant="h5" gutterBottom>
          Fahrkostenabrechnung
        </Typography>
      )}
      <Paper sx={{ p: 2, mb: 3 }}>
        {isMobile ? (
          <>
            {/* Mobile Layout */}
            <Stack spacing={2}>
              <Stack direction="row" spacing={3}>
                <Box flex={1}>
                  <Typography variant="caption" color="text.secondary">
                    Fahrer
                  </Typography>
                  <Typography variant="body1" fontWeight={600}>
                    {data.fahrerName}
                  </Typography>
                </Box>

                <Box flex={1}>
                  <Typography variant="caption" color="text.secondary">
                    Veranstaltung
                  </Typography>
                  <Typography variant="body1" fontWeight={600}>
                    {data.veranstaltungName}
                  </Typography>
                </Box>
              </Stack>

              <Stack direction="row" spacing={3}>
                <Box flex={1}>
                  <Typography variant="caption" color="text.secondary">
                    Gesamtkilometer
                  </Typography>
                  <Typography variant="body1" fontWeight={600}>
                    {data.gesamtKilometer} km
                  </Typography>
                </Box>

                <Box flex={1}>
                  <Typography variant="caption" color="text.secondary">
                    Gesamtbetrag
                  </Typography>
                  <Typography>
                    <Money value={data.gesamtBetrag} variant="body1" sx={{ fontWeight: 600 }} />
                  </Typography>
                </Box>
              </Stack>

              {data.bemerkung && (
                <Box>
                  <Typography variant="caption" color="text.secondary">
                    Bemerkung
                  </Typography>

                  <Typography variant="body1" fontWeight={600}>
                    {data.bemerkung}
                  </Typography>
                </Box>
              )}
            </Stack>
          </>
        ) : (
          <>
            {/* Desktop Layout */}
            <Stack direction="row" spacing={4} flexWrap="wrap" sx={{ mt: 2 }}>
              <Box>
                <Typography variant="caption" color="text.secondary">
                  Fahrer
                </Typography>
                <Typography variant="body1" fontWeight={600}>
                  {data.fahrerName}
                </Typography>
              </Box>

              <Box>
                <Typography variant="caption" color="text.secondary">
                  Veranstaltung
                </Typography>
                <Typography variant="body1" fontWeight={600}>
                  {data.veranstaltungName}
                </Typography>
              </Box>

              <Box>
                <Typography variant="caption" color="text.secondary">
                  Datum
                </Typography>
                <Typography variant="body1" fontWeight={600}>
                  {data.abrechnungsdatum}
                </Typography>
              </Box>
            </Stack>

            <Stack direction="row" spacing={4} flexWrap="wrap" sx={{ mt: 2 }}>
              <Box>
                <Typography variant="caption" color="text.secondary">
                  Gesamtkilometer
                </Typography>
                <Typography variant="body1" fontWeight={600}>
                  {data.gesamtKilometer} km
                </Typography>
              </Box>

              <Box>
                <Typography variant="caption" color="text.secondary">
                  Gesamtbetrag
                </Typography>
                <Typography>
                  <Money value={data.gesamtBetrag} variant="body1" sx={{ fontWeight: 600 }} />
                </Typography>
              </Box>

              {data.bemerkung && (
                <Box>
                  <Typography variant="caption" color="text.secondary">
                    Bemerkung
                  </Typography>
                  <Typography variant="body1" fontWeight={600}>
                    {data.bemerkung}
                  </Typography>
                </Box>
              )}
            </Stack>
          </>
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
