import { useCallback, useEffect, useRef, useState } from "react";
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
  LinearProgress,
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
  deleteZaehlerstand,
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
  const [loadedZaehlerId, setLoadedZaehlerId] = useState<number | null>(null);
  const [selectedZaehlerstand, setSelectedZaehlerstand] = useState<Zaehlerstand | null>(null);
  const [focusedZaehlerstandId, setFocusedZaehlerstandId] = useState<number | null>(null);
  const rowRefs = useRef(new Map<number, HTMLTableRowElement>());
  const focusAfterLoadId = useRef<number | null>(null);

  const [standDialogOpen, setStandDialogOpen] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const loadZaehlerstaende = useCallback(async () => {
    if (!zaehler) {
      setZaehlerstaende([]);
      setLoadedZaehlerId(null);
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
      setLoadedZaehlerId(zaehler.id);
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

  useEffect(() => {
    const id = focusAfterLoadId.current;
    if (id === null || standDialogOpen) return;

    const row = rowRefs.current.get(id);
    if (!row) return;

    focusAfterLoadId.current = null;
    setFocusedZaehlerstandId(id);
    requestAnimationFrame(() => {
      row.scrollIntoView({ block: "nearest" });
      row.querySelector<HTMLButtonElement>('button[aria-label="Zählerstand bearbeiten"]')
        ?.focus({ preventScroll: true });
    });
  }, [zaehlerstaende, standDialogOpen]);

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

    const saved = selectedZaehlerstand
      ? await updateZaehlerstand(zaehler.id, selectedZaehlerstand.id, payload)
      : await createZaehlerstand(zaehler.id, payload);

    focusAfterLoadId.current = saved.id;
    await loadZaehlerstaende();
  }

  async function handleDelete() {
    if (!zaehler || !selectedZaehlerstand) {
      return;
    }

    await deleteZaehlerstand(zaehler.id, selectedZaehlerstand.id);

    setStandDialogOpen(false);
    setSelectedZaehlerstand(null);

    await loadZaehlerstaende();
  }

  if (!zaehler) {
    return null;
  }

  const zeigtAktuellenZaehler = loadedZaehlerId === zaehler.id;

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

          {loading && zeigtAktuellenZaehler && zaehlerstaende.length > 0 && (
            <LinearProgress aria-label="Zählerstände werden aktualisiert" sx={{ mb: 1 }} />
          )}

          {!zeigtAktuellenZaehler && loading ? (
            <Box sx={{ display: "flex", justifyContent: "center", p: 4 }}>
              <CircularProgress />
            </Box>
          ) : !zeigtAktuellenZaehler ? null : zaehlerstaende.length === 0 ? (
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
                      <TableRow
                        key={item.id}
                        hover
                        selected={focusedZaehlerstandId === item.id}
                        ref={(row) => {
                          if (row) rowRefs.current.set(item.id, row);
                          else rowRefs.current.delete(item.id);
                        }}
                      >
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
        onDelete={handleDelete}
      />
    </>
  );
}
