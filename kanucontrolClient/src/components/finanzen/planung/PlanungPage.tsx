import {
  Alert,
  Box,
  Button,
  Card,
  CardContent,
  Typography,
  Stack,
  Divider,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions
} from "@mui/material";
import InfoOutlinedIcon from "@mui/icons-material/InfoOutlined";
import PlanungspositionenTable from "@/components/simulation/PlanungspositionenTable";
import { useEffect, useState, useCallback } from "react";
import {
  getPlanung,
  einreichen,
  wiederOeffnen,
} from "@/api/services/planungApi";
import { PlanungDetail } from "@/api/types/planung";
import { kategorieZuTyp } from "@/api/types/finanz";
import FinanzSummary from "@/components/common/FinanzSummary";


interface Props {
  veranstaltungId: number;
  onOpenSimulation: () => void;
}

export default function PlanungPage({
  veranstaltungId,
  onOpenSimulation,
}: Props) {
  const [planung, setPlanung] = useState<PlanungDetail | null>(null);
  const [confirmOpen, setConfirmOpen] = useState(false);

  const load = useCallback(async () => {
    const data = await getPlanung(veranstaltungId);
    setPlanung(data);
  }, [veranstaltungId]);

  useEffect(() => {
    load();
  }, [load]);

  if (!planung) {
    return (
      <Box p={3}>
        <Typography
          variant="h5"
          gutterBottom
        >
          Finanzplanung
        </Typography>

        <Card>
          <CardContent>

            <Box
              display="flex"
              alignItems="center"
              gap={2}
              mb={2}
            >
              <InfoOutlinedIcon
                color="info"
                fontSize="large"
              />

              <Typography variant="h6">
                Es liegt noch keine Planung vor.
              </Typography>
            </Box>

            <Typography
              color="text.secondary"
              paragraph
            >
              Erstellen Sie zunächst eine Simulation und
              speichern Sie diese. Anschließend kann die
              Planung hier eingesehen und eingereicht werden.
            </Typography>

            <Button
              variant="contained"
              onClick={onOpenSimulation}
            >
              Zur Simulation
            </Button>

          </CardContent>
        </Card>
      </Box>
    );
  }

  const kosten = planung.positionen.filter((p) => kategorieZuTyp[p.kategorie] === "KOSTEN");
  const einnahmen = planung.positionen.filter((p) => kategorieZuTyp[p.kategorie] === "EINNAHME");

  const sumKosten = kosten.reduce((a, b) => a + b.betrag, 0);
  const sumEinnahmen = einnahmen.reduce((a, b) => a + b.betrag, 0);
  const saldo = sumEinnahmen - sumKosten;


  const kjfpZuschuss = planung.positionen
    .filter(p => p.kategorie === "KJFP_ZUSCHUSS")
    .reduce((sum, p) => sum + p.betrag, 0);


  return (
        <>
      
      {planung.eingereicht && (
        <Alert severity="warning" sx={{ mb: 2 }}>
          Diese Planung wurde eingereicht und ist gesperrt.
          <Button
            size="small"
            sx={{ ml: 2 }}
            onClick={async () => {
              await wiederOeffnen(veranstaltungId);
              load();
            }}
          >
            Planung wieder öffnen
          </Button>
        </Alert>
      )}

      <FinanzSummary
        kosten={sumKosten}
        einnahmen={sumEinnahmen}
        eigenanteil={saldo}
        kjfpZuschuss={kjfpZuschuss}
      />

      <PlanungspositionenTable
        positionen={planung.positionen}
      />

      <Divider sx={{ my: 3 }} />



      {!planung.eingereicht && (
        <Box
          display="flex"
          justifyContent="center"
          mt={3}
        >
          <Button
            variant="contained"
            color="warning"
            size="large"
            onClick={async () => {
              try {
                await einreichen(veranstaltungId);
                setConfirmOpen(false);
                load();
              } catch (e) {
                console.error(e);
                alert("Planung konnte nicht eingereicht werden.");
              }
            }}
          >
            Planung einreichen
          </Button>
        </Box>
      )}



      <Dialog open={confirmOpen} onClose={() => setConfirmOpen(false)}>
        <DialogTitle>Planung wirklich einreichen?</DialogTitle>
        <DialogContent>
          <Typography>
            Nach dem Einreichen können keine Änderungen mehr vorgenommen werden.
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setConfirmOpen(false)}>Abbrechen</Button>
          <Button
            color="warning"
            variant="contained"
            onClick={async () => {
              await einreichen(veranstaltungId);
              setConfirmOpen(false);
              load();
            }}
          >
            Ja, einreichen
          </Button>
        </DialogActions>
      </Dialog>
   </>
  );
}
