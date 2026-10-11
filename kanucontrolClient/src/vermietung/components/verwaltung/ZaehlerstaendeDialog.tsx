import { useCallback, useEffect, useState } from "react";
import {
  Alert,
  Box,
  Button,
  CircularProgress,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  IconButton,
  Paper,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Tooltip,
  Typography,
} from "@mui/material";

import AddIcon from "@mui/icons-material/Add";
import EditIcon from "@mui/icons-material/Edit";

import {
  createZaehlerstand,
  getZaehlerstaende,
  updateZaehlerstand,
} from "@/vermietung/api/zaehlerstandApi";

import type { Zaehlerstand, ZaehlerstandSave } from "@/vermietung/api/zaehlerstandApi";

import type { Zaehler } from "@/vermietung/api/zaehlerApi";

import ZaehlerstandDialog from "@/vermietung/components/verwaltung/ZaehlerstandDialog";

interface Props {
  open: boolean;
  zaehler: Zaehler | null;
  onClose: () => void;
}

function formatDatum(value: string): string {
  const date = new Date(value);

  if (Number.isNaN(date.getTime())) {
    return value;
  }

  return new Intl.DateTimeFormat("de-DE", {
    dateStyle: "short",
    timeStyle: "short",
  }).format(date);
}

function formatWert(value: number): string {
  return new Intl.NumberFormat("de-DE", {
    minimumFractionDigits: 0,
    maximumFractionDigits: 3,
  }).format(value);
}

export default function ZaehlerstaendeDialog({ open, zaehler, onClose }: Props) {
  const [zaehlerstaende, setZaehlerstaende] = useState<Zaehlerstand[]>([]);
  const [selectedZaehlerstand, setSelectedZaehlerstand] = useState<Zaehlerstand | null>(null);

  const [standDialogOpen, setStandDialogOpen] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const loadZaehlerstaende = useCallback(async () => {
    if (!zaehler) {
      setZaehlerstaende([]);
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const data = await getZaehlerstaende(zaehler.id);

      const sortiert = [...data].sort(
        (a, b) => new Date(b.ablesedatum).getTime() - new Date(a.ablesedatum).getTime(),
      );

      setZaehlerstaende(sortiert);
    } catch (err) {
      console.error("Fehler beim Laden der Zählerstände:", err);
      setError("Die Zählerstände konnten nicht geladen werden.");
    } finally {
      setLoading(false);
    }
  }, [zaehler]);

  useEffect(() => {
    if (open && zaehler) {
      void loadZaehlerstaende();
    }
  }, [open, zaehler, loadZaehlerstaende]);

  function handleCreate() {
    setSelectedZaehlerstand(null);
    setStandDialogOpen(true);
  }

  function handleEdit(item: Zaehlerstand) {
    setSelectedZaehlerstand(item);
    setStandDialogOpen(true);
  }

  async function handleSave(payload: ZaehlerstandSave) {
    if (!zaehler) {
      return;
    }

    if (selectedZaehlerstand) {
      await updateZaehlerstand(zaehler.id, selectedZaehlerstand.id, payload);
    } else {
      await createZaehlerstand(zaehler.id, payload);
    }

    await loadZaehlerstaende();
  }

  if (!zaehler) {
    return null;
  }

  return (
    <>
      <Dialog open={open} onClose={standDialogOpen ? undefined : onClose} fullWidth maxWidth="lg">
        <DialogTitle>
          <Stack
            direction={{ xs: "column", sm: "row" }}
            spacing={1}
            alignItems={{ xs: "stretch", sm: "center" }}
            justifyContent="space-between"
          >
            <Box>
              <Typography variant="h6">Zählerstände</Typography>

              <Typography variant="body2" color="text.secondary">
                {zaehler.bezeichnung} – {zaehler.mengeneinheit}
              </Typography>
            </Box>

            <Button variant="contained" startIcon={<AddIcon />} onClick={handleCreate}>
              Ablesung erfassen
            </Button>
          </Stack>
        </DialogTitle>

        <DialogContent>
          {error && (
            <Alert severity="error" sx={{ mb: 2 }}>
              {error}
            </Alert>
          )}

          {loading ? (
            <Box sx={{ display: "flex", justifyContent: "center", p: 4 }}>
              <CircularProgress />
            </Box>
          ) : zaehlerstaende.length === 0 ? (
            <Alert severity="info">Für diesen Zähler wurden noch keine Ablesungen erfasst.</Alert>
          ) : (
            <TableContainer component={Paper} variant="outlined">
              <Table>
                <TableHead>
                  <TableRow>
                    <TableCell>Ablesedatum</TableCell>
                    <TableCell align="right">Zählerstand</TableCell>
                    <TableCell align="right">Verbrauch</TableCell>
                    <TableCell>Bemerkung</TableCell>
                    <TableCell align="right">Aktionen</TableCell>
                  </TableRow>
                </TableHead>

                <TableBody>
                  {zaehlerstaende.map((item, index) => {
                    // Die Liste ist absteigend sortiert.
                    // Der nächste Eintrag ist somit die ältere Ablesung.
                    const aeltererStand = zaehlerstaende[index + 1];

                    const verbrauch = aeltererStand
                      ? item.zaehlerstand - aeltererStand.zaehlerstand
                      : null;

                    return (
                      <TableRow key={item.id} hover>
                        <TableCell>{formatDatum(item.ablesedatum)}</TableCell>

                        <TableCell align="right">
                          {formatWert(item.zaehlerstand)} {zaehler.mengeneinheit}
                        </TableCell>

                        <TableCell align="right">
                          {verbrauch === null ? (
                            <Typography variant="body2" color="text.secondary">
                              –
                            </Typography>
                          ) : verbrauch < 0 ? (
                            <Tooltip title="Der Zählerstand ist niedriger als bei der vorherigen Ablesung. Bitte Zählerwechsel oder Zählerrückstellung prüfen.">
                              <Typography variant="body2" color="error">
                                {formatWert(verbrauch)} {zaehler.mengeneinheit}
                              </Typography>
                            </Tooltip>
                          ) : (
                            <>
                              {formatWert(verbrauch)} {zaehler.mengeneinheit}
                            </>
                          )}
                        </TableCell>

                        <TableCell>{item.bemerkung || "–"}</TableCell>

                        <TableCell align="right">
                          <Tooltip title="Zählerstand bearbeiten">
                            <IconButton
                              aria-label="Zählerstand bearbeiten"
                              onClick={() => handleEdit(item)}
                              size="small"
                            >
                              <EditIcon />
                            </IconButton>
                          </Tooltip>
                        </TableCell>
                      </TableRow>
                    );
                  })}
                </TableBody>
              </Table>
            </TableContainer>
          )}
        </DialogContent>

        <DialogActions>
          <Button onClick={onClose}>Schließen</Button>
        </DialogActions>
      </Dialog>

      <ZaehlerstandDialog
        open={standDialogOpen}
        zaehlerstand={selectedZaehlerstand}
        onClose={() => setStandDialogOpen(false)}
        onSave={handleSave}
      />
    </>
  );
}
