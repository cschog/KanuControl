import {
  Alert,
  Box,
  Button,
  Typography,
  Stack,
  Divider,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Table,
  TableHead,
  TableRow,
  TableCell,
  TableBody,
  Paper,
} from "@mui/material";
import { useEffect, useState, useCallback } from "react";
import {
  getPlanung,
  addPosition,
  updatePosition,
  deletePosition,
  einreichen,
  wiederOeffnen,
} from "@/api/services/planungApi";
import FinanzPositionDialog from "@/components/finanzen/FinanzPositionDialog";
import { PlanungDetail, PlanungPosition, PlanungPositionCreate } from "@/api/types/planung";
import { kategorieZuTyp } from "@/api/types/finanz";


interface Props {
  veranstaltungId: number;
}

export default function PlanungPage({ veranstaltungId }: Props) {
  const [planung, setPlanung] = useState<PlanungDetail | null>(null);
  const [dialogOpen, setDialogOpen] = useState(false);
  const [dialogTyp, setDialogTyp] = useState<"KOSTEN" | "EINNAHME">("KOSTEN");
  const [editing, setEditing] = useState<PlanungPosition | undefined>();
  const [confirmOpen, setConfirmOpen] = useState(false);

  const load = useCallback(async () => {
    const data = await getPlanung(veranstaltungId);
    setPlanung(data);
  }, [veranstaltungId]);

  useEffect(() => {
    load();
  }, [load]);

  if (!planung) return null;

  const kosten = planung.positionen.filter((p) => kategorieZuTyp[p.kategorie] === "KOSTEN");
  const einnahmen = planung.positionen.filter((p) => kategorieZuTyp[p.kategorie] === "EINNAHME");

  const sumKosten = kosten.reduce((a, b) => a + b.betrag, 0);
  const sumEinnahmen = einnahmen.reduce((a, b) => a + b.betrag, 0);
  const saldo = sumEinnahmen - sumKosten;

  const handleSave = async (data: PlanungPositionCreate) => {
    if (editing) {
      await updatePosition(veranstaltungId, editing.id, data);
    } else {
      await addPosition(veranstaltungId, data);
    }
    setDialogOpen(false);
    setEditing(undefined);
    load();
  };

  const handleDelete = async (id: number) => {
    await deletePosition(veranstaltungId, id);
    load();
  };

  const renderTable = (title: string, data: PlanungPosition[], typ: "KOSTEN" | "EINNAHME") => (
    <Box mb={4}>
      <Stack direction="row" justifyContent="space-between" alignItems="center" mb={1}>
        <Typography variant="h6">{title}</Typography>
        {!planung.eingereicht && (
          <Button
            size="small"
            variant="contained"
            onClick={() => {
              setDialogTyp(typ);
              setDialogOpen(true);
            }}
          >
            + {typ === "KOSTEN" ? "Ausgabe" : "Einnahme"}
          </Button>
        )}
      </Stack>

      <Paper variant="outlined">
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>Kategorie</TableCell>
              <TableCell align="right">Betrag (€)</TableCell>
              {!planung.eingereicht && <TableCell width={160}>Aktionen</TableCell>}
            </TableRow>
          </TableHead>
          <TableBody>
            {data.map((p) => (
              <TableRow key={p.id}>
                <TableCell>{p.kategorie.replaceAll("_", " ")}</TableCell>
                <TableCell align="right">{p.betrag.toFixed(2)}</TableCell>
                {!planung.eingereicht && (
                  <TableCell>
                    <Stack direction="row" spacing={1}>
                      <Button
                        size="small"
                        onClick={() => {
                          setEditing(p);
                          setDialogTyp(typ);
                          setDialogOpen(true);
                        }}
                      >
                        Bearbeiten
                      </Button>
                      <Button size="small" color="error" onClick={() => handleDelete(p.id)}>
                        Löschen
                      </Button>
                    </Stack>
                  </TableCell>
                )}
              </TableRow>
            ))}
            {data.length === 0 && (
              <TableRow>
                <TableCell colSpan={3}>
                  <Typography variant="body2" color="text.secondary">
                    Keine Einträge vorhanden
                  </Typography>
                </TableCell>
              </TableRow>
            )}
          </TableBody>
        </Table>
      </Paper>
    </Box>
  );

  return (
    <Box p={3}>
      <Typography variant="h5" gutterBottom>
        Finanzplanung
      </Typography>

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

      <Stack direction="row" spacing={4} mb={3}>
        <Typography>
          💰 Einnahmen: <strong>{sumEinnahmen.toFixed(2)} €</strong>
        </Typography>
        <Typography>
          💸 Ausgaben: <strong>{sumKosten.toFixed(2)} €</strong>
        </Typography>
        <Typography>
          📊 Saldo:{" "}
          <strong style={{ color: saldo >= 0 ? "green" : "red" }}>{saldo.toFixed(2)} €</strong>
        </Typography>
      </Stack>

      <Divider sx={{ mb: 3 }} />

      {renderTable("Einnahmen", einnahmen, "EINNAHME")}
      {renderTable("Ausgaben", kosten, "KOSTEN")}

      {!planung.eingereicht && (
        <Box textAlign="right">
          <Button variant="outlined" color="warning" onClick={() => setConfirmOpen(true)}>
            Planung einreichen
          </Button>
        </Box>
      )}

      <FinanzPositionDialog
        open={dialogOpen}
        typ={dialogTyp}
        initialData={editing}
        onClose={() => setDialogOpen(false)}
        onSave={handleSave}
      />

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
    </Box>
  );
}
