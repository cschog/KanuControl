import { useEffect, useState } from "react";
import {
  Alert,
  Box,
  Button,
  CircularProgress,
  FormControl,
  InputLabel,
  MenuItem,
  Select,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
  Typography,
  IconButton,
  Tooltip,
} from "@mui/material";
import AddIcon from "@mui/icons-material/Add";
import EditIcon from "@mui/icons-material/Edit";

import type { SelectChangeEvent } from "@mui/material";
import type { Mietobjekt } from "@/vermietung/types/Mietobjekt";
import type { Mietbereich } from "@/vermietung/types/Mietbereich";
import type { Mietpreis } from "@/vermietung/types/Mietpreis";
import type { MietpreisSave } from "@/vermietung/types/MietpreisSave";

import { getMietobjekte } from "@/vermietung/api/mietobjektApi";
import { getMietbereiche } from "@/vermietung/api/mietbereichApi";
import { getMietpreise, createMietpreis, updateMietpreis } from "@/vermietung/api/mietpreisApi";

import MietpreisDialog from "@/vermietung/components/verwaltung/MietpreisDialog";

export default function MietpreiseVerwaltung() {
  const [mietobjekte, setMietobjekte] = useState<Mietobjekt[]>([]);
  const [mietbereiche, setMietbereiche] = useState<Mietbereich[]>([]);
  const [mietpreise, setMietpreise] = useState<Mietpreis[]>([]);

  const [mietobjektId, setMietobjektId] = useState<number | "">("");
  const [selectedMietbereich, setSelectedMietbereich] = useState<Mietbereich | null>(null);
  const [selectedMietpreis, setSelectedMietpreis] = useState<Mietpreis | null>(null);

  const [dialogOpen, setDialogOpen] = useState(false);
  const [loading, setLoading] = useState(true);
  const [loadingPreise, setLoadingPreise] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    async function loadMietobjekte() {
      try {
        setError(null);
        const data = await getMietobjekte();

        setMietobjekte(data);

        if (data.length > 0) {
          setMietobjektId(data[0].id);
        }
      } catch (err) {
        console.error("Fehler beim Laden der Mietobjekte:", err);
        setError("Die Mietobjekte konnten nicht geladen werden.");
      } finally {
        setLoading(false);
      }
    }

    void loadMietobjekte();
  }, []);

  useEffect(() => {
    async function loadMietbereiche() {
      setMietbereiche([]);
      setMietpreise([]);
      setSelectedMietbereich(null);

      if (mietobjektId === "") {
        return;
      }

      try {
        setError(null);

        const data = await getMietbereiche(mietobjektId);
        setMietbereiche(data);

        // Preise für alle Mietbereiche laden
        setLoadingPreise(true);

        const preislisten = await Promise.all(
          data.map(async (bereich) => {
            const preise = await getMietpreise(bereich.id);
            return preise;
          }),
        );

        setMietpreise(preislisten.flat());
      } catch (err) {
        console.error("Fehler beim Laden der Mietbereiche oder Mietpreise:", err);
        setError("Die Mietbereiche oder Mietpreise konnten nicht geladen werden.");
      } finally {
        setLoadingPreise(false);
      }
    }

    void loadMietbereiche();
  }, [mietobjektId]);

  const heute = new Date().toISOString().slice(0, 10);

  function ermittleAktuellenPreis(
    mietbereichId: number,
    buchungsquelle: "DIREKT" | "AIRBNB",
  ): Mietpreis | undefined {
    return mietpreise
      .filter(
        (preis) =>
          preis.mietbereichId === mietbereichId &&
          preis.buchungsquelle === buchungsquelle &&
          preis.gueltigAb <= heute,
      )
      .sort((a, b) => b.gueltigAb.localeCompare(a.gueltigAb))[0];
  }

  function handleMietobjektChange(event: SelectChangeEvent<number | "">) {
    const value = event.target.value;
    setMietobjektId(value === "" ? "" : Number(value));
  }

  function handleNew(bereich: Mietbereich, quelle: "DIREKT" | "AIRBNB") {
    setSelectedMietbereich(bereich);

    // Der Dialog wird mit der gewünschten Buchungsquelle vorbelegt,
    // sobald diese beim Öffnen übergeben wird.
    setSelectedMietpreis({
      id: 0,
      mietbereichId: bereich.id,
      mietbereichBezeichnung: bereich.bezeichnung,
      buchungsquelle: quelle,
      gueltigAb: heute,
      preis: 0,
    });

    setDialogOpen(true);
  }

  function handleEdit(preis: Mietpreis) {
    const bereich = mietbereiche.find((m) => m.id === preis.mietbereichId) ?? null;

    setSelectedMietbereich(bereich);
    setSelectedMietpreis(preis);
    setDialogOpen(true);
  }

  async function handleSave(payload: MietpreisSave) {
    if (selectedMietpreis && selectedMietpreis.id > 0) {
      await updateMietpreis(selectedMietpreis.id, payload);
    } else {
      await createMietpreis(payload);
    }

    if (mietobjektId !== "") {
      const bereiche = await getMietbereiche(mietobjektId);
      const preislisten = await Promise.all(bereiche.map((bereich) => getMietpreise(bereich.id)));

      setMietbereiche(bereiche);
      setMietpreise(preislisten.flat());
    }
  }

  function formatPreis(preis: Mietpreis | undefined) {
    if (!preis) {
      return "–";
    }

    return preis.preis.toLocaleString("de-DE", {
      style: "currency",
      currency: "EUR",
    });
  }

  if (loading) {
    return (
      <Box sx={{ display: "flex", justifyContent: "center", mt: 4 }}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box>
      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      <FormControl sx={{ minWidth: 280, mb: 3 }}>
        <InputLabel id="mietobjekt-label">Mietobjekt</InputLabel>
        <Select
          labelId="mietobjekt-label"
          value={mietobjektId}
          label="Mietobjekt"
          onChange={handleMietobjektChange}
        >
          {mietobjekte.map((objekt) => (
            <MenuItem key={objekt.id} value={objekt.id}>
              {objekt.bezeichnung}
            </MenuItem>
          ))}
        </Select>
      </FormControl>

      {mietobjekte.length === 0 ? (
        <Alert severity="info">Es sind noch keine Mietobjekte vorhanden.</Alert>
      ) : loadingPreise ? (
        <Box sx={{ display: "flex", justifyContent: "center", mt: 3 }}>
          <CircularProgress />
        </Box>
      ) : mietbereiche.length === 0 ? (
        <Alert severity="info">Für dieses Mietobjekt sind keine Mietbereiche vorhanden.</Alert>
      ) : (
        <>
          <Typography variant="h6" sx={{ mb: 2 }}>
            Aktuelle Mietpreise
          </Typography>

          <TableContainer component={Paper} variant="outlined">
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Mietbereich</TableCell>
                  <TableCell align="right">Direktbuchung</TableCell>
                  <TableCell align="right">Airbnb</TableCell>
                  <TableCell align="right">Aktionen</TableCell>
                </TableRow>
              </TableHead>

              <TableBody>
                {mietbereiche.map((bereich) => {
                  const direktPreis = ermittleAktuellenPreis(bereich.id, "DIREKT");
                  const airbnbPreis = ermittleAktuellenPreis(bereich.id, "AIRBNB");

                  return (
                    <TableRow key={bereich.id} hover>
                      <TableCell>{bereich.bezeichnung}</TableCell>

                      <TableCell align="right">
                        <Box
                          sx={{
                            display: "flex",
                            justifyContent: "flex-end",
                            alignItems: "center",
                            gap: 1,
                          }}
                        >
                          {formatPreis(direktPreis)}

                          <Tooltip
                            title={direktPreis ? "Direktpreis bearbeiten" : "Direktpreis anlegen"}
                          >
                            <IconButton
                              size="small"
                              onClick={() =>
                                direktPreis ? handleEdit(direktPreis) : handleNew(bereich, "DIREKT")
                              }
                            >
                              {direktPreis ? (
                                <EditIcon fontSize="small" />
                              ) : (
                                <AddIcon fontSize="small" />
                              )}
                            </IconButton>
                          </Tooltip>
                        </Box>
                      </TableCell>

                      <TableCell align="right">
                        <Box
                          sx={{
                            display: "flex",
                            justifyContent: "flex-end",
                            alignItems: "center",
                            gap: 1,
                          }}
                        >
                          {formatPreis(airbnbPreis)}

                          <Tooltip
                            title={airbnbPreis ? "Airbnb-Preis bearbeiten" : "Airbnb-Preis anlegen"}
                          >
                            <IconButton
                              size="small"
                              onClick={() =>
                                airbnbPreis ? handleEdit(airbnbPreis) : handleNew(bereich, "AIRBNB")
                              }
                            >
                              {airbnbPreis ? (
                                <EditIcon fontSize="small" />
                              ) : (
                                <AddIcon fontSize="small" />
                              )}
                            </IconButton>
                          </Tooltip>
                        </Box>
                      </TableCell>

                      <TableCell align="right">
                        <Button
                          size="small"
                          onClick={() => {
                            setSelectedMietbereich(bereich);
                            setSelectedMietpreis(null);
                            setDialogOpen(true);
                          }}
                        >
                          Neuer Preis
                        </Button>
                      </TableCell>
                    </TableRow>
                  );
                })}
              </TableBody>
            </Table>
          </TableContainer>
        </>
      )}

      {selectedMietbereich && (
        <MietpreisDialog
          open={dialogOpen}
          mietbereichId={selectedMietbereich.id}
          mietpreis={selectedMietpreis}
          onClose={() => setDialogOpen(false)}
          onSave={handleSave}
        />
      )}
    </Box>
  );
}
