import { useEffect, useState } from "react";
import {
  Alert,
  Box,
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
  Chip,
} from "@mui/material";

import type { SelectChangeEvent } from "@mui/material";

import AddIcon from "@mui/icons-material/Add";
import EditIcon from "@mui/icons-material/Edit";
import DeleteIcon from "@mui/icons-material/Delete";

import type { Mietobjekt } from "@/vermietung/types/Mietobjekt";
import type { Mietbereich } from "@/vermietung/types/Mietbereich";
import type { MietbereichRabatt } from "@/vermietung/types/MietbereichRabatt";
import type { MietbereichRabattSave } from "@/vermietung/types/MietbereichRabattSave";

import { Rabattart } from "@/vermietung/enums/Rabattart";
import { RabattwertTyp } from "@/vermietung/enums/RabattwertTyp";

import { getMietobjekte } from "@/vermietung/api/mietobjektApi";
import { getMietbereiche } from "@/vermietung/api/mietbereichApi";

import {
  getMietbereichRabatte,
  createMietbereichRabatt,
  updateMietbereichRabatt,
  deleteMietbereichRabatt,
} from "@/vermietung/api/mietbereichRabattApi";

import MietbereichRabattDialog from "@/vermietung/components/verwaltung/MietbereichRabattDialog";

export default function RabatteVerwaltung() {
  const [mietobjekte, setMietobjekte] = useState<Mietobjekt[]>([]);
  const [mietbereiche, setMietbereiche] = useState<Mietbereich[]>([]);
  const [rabatte, setRabatte] = useState<MietbereichRabatt[]>([]);

  const [mietobjektId, setMietobjektId] = useState<number | "">("");
  const [selectedMietbereich, setSelectedMietbereich] = useState<Mietbereich | null>(null);
  const [selectedRabatt, setSelectedRabatt] = useState<MietbereichRabatt | null>(null);

  const [dialogOpen, setDialogOpen] = useState(false);
  const [loading, setLoading] = useState(true);
  const [loadingRabatte, setLoadingRabatte] = useState(false);
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

  async function loadRabatte(id: number) {
    setLoadingRabatte(true);
    setError(null);

    try {
      const bereiche = await getMietbereiche(id);

      const rabattlisten = await Promise.all(
        bereiche.map((bereich) => getMietbereichRabatte(id, bereich.id)),
      );

      setMietbereiche(bereiche);
      setRabatte(rabattlisten.flat());
    } catch (err) {
      console.error("Fehler beim Laden der Mietbereiche oder Rabatte:", err);
      setMietbereiche([]);
      setRabatte([]);
      setError("Die Mietbereiche oder Rabattregeln konnten nicht geladen werden.");
    } finally {
      setLoadingRabatte(false);
    }
  }

  useEffect(() => {
    setMietbereiche([]);
    setRabatte([]);

    if (mietobjektId === "") {
      return;
    }

    void loadRabatte(mietobjektId);
  }, [mietobjektId]);

  function handleMietobjektChange(event: SelectChangeEvent<number | "">) {
    const value = event.target.value;

    setMietobjektId(value === "" ? "" : Number(value));
  }

  function handleNew(bereich: Mietbereich) {
    setSelectedMietbereich(bereich);
    setSelectedRabatt(null);
    setDialogOpen(true);
  }

  function handleEdit(bereich: Mietbereich, rabatt: MietbereichRabatt) {
    setSelectedMietbereich(bereich);
    setSelectedRabatt(rabatt);
    setDialogOpen(true);
  }

  async function handleSave(payload: MietbereichRabattSave) {
    if (mietobjektId === "" || !selectedMietbereich) {
      throw new Error("Es wurde kein Mietbereich ausgewählt.");
    }

    if (selectedRabatt) {
      await updateMietbereichRabatt(
        mietobjektId,
        selectedMietbereich.id,
        selectedRabatt.id,
        payload,
      );
    } else {
      await createMietbereichRabatt(mietobjektId, selectedMietbereich.id, payload);
    }

    await loadRabatte(mietobjektId);
  }

  async function handleDelete(bereich: Mietbereich, rabatt: MietbereichRabatt) {
    if (mietobjektId === "") {
      return;
    }

    const bestaetigt = window.confirm(
      `Soll der ${rabatt.rabattart === Rabattart.VEREIN ? "Vereinsrabatt" : "Verbandsrabatt"} für „${bereich.bezeichnung}“ wirklich gelöscht werden?`,
    );

    if (!bestaetigt) {
      return;
    }

    try {
      setError(null);

      await deleteMietbereichRabatt(mietobjektId, bereich.id, rabatt.id);

      await loadRabatte(mietobjektId);
    } catch (err) {
      console.error("Fehler beim Löschen der Rabattregel:", err);
      setError("Die Rabattregel konnte nicht gelöscht werden.");
    }
  }

  function formatRabatt(rabatt: MietbereichRabatt): string {
    if (rabatt.rabattwertTyp === RabattwertTyp.PROZENTUAL) {
      return `${rabatt.rabattwert.toLocaleString("de-DE")} %`;
    }

    return rabatt.rabattwert.toLocaleString("de-DE", {
      style: "currency",
      currency: "EUR",
    });
  }

  function bezeichnungRabattart(rabattart: Rabattart): string {
    switch (rabattart) {
      case Rabattart.VEREIN:
        return "Verein";
      case Rabattart.VERBAND:
        return "Verband";
    }
  }

  function bezeichnungRabattwertTyp(rabattwertTyp: RabattwertTyp): string {
    switch (rabattwertTyp) {
      case RabattwertTyp.PROZENTUAL:
        return "Prozentual";
      case RabattwertTyp.ABSOLUT:
        return "Absolut";
    }
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
        <InputLabel id="rabatte-mietobjekt-label">Mietobjekt</InputLabel>

        <Select
          labelId="rabatte-mietobjekt-label"
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
      ) : loadingRabatte ? (
        <Box sx={{ display: "flex", justifyContent: "center", mt: 3 }}>
          <CircularProgress />
        </Box>
      ) : mietbereiche.length === 0 ? (
        <Alert severity="info">Für dieses Mietobjekt sind keine Mietbereiche vorhanden.</Alert>
      ) : (
        <>
          <Typography variant="h6" sx={{ mb: 2 }}>
            Rabattregeln je Mietbereich
          </Typography>

          <TableContainer component={Paper} variant="outlined">
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Mietbereich</TableCell>
                  <TableCell>Rabattart</TableCell>
                  <TableCell>Berechnung</TableCell>
                  <TableCell align="right">Rabatt</TableCell>
                  <TableCell align="center">Status</TableCell>
                  <TableCell align="right">Aktionen</TableCell>
                </TableRow>
              </TableHead>

              <TableBody>
                {mietbereiche.map((bereich) => {
                  const bereichsRabatte = rabatte.filter(
                    (rabatt) => rabatt.mietbereichId === bereich.id,
                  );

                  if (bereichsRabatte.length === 0) {
                    return (
                      <TableRow key={bereich.id} hover>
                        <TableCell>{bereich.bezeichnung}</TableCell>
                        <TableCell colSpan={4}>
                          <Typography variant="body2" color="text.secondary">
                            Keine Rabattregeln konfiguriert
                          </Typography>
                        </TableCell>
                        <TableCell align="right">
                          <Tooltip title="Rabattregel hinzufügen">
                            <IconButton size="small" onClick={() => handleNew(bereich)}>
                              <AddIcon fontSize="small" />
                            </IconButton>
                          </Tooltip>
                        </TableCell>
                      </TableRow>
                    );
                  }

                  return bereichsRabatte.map((rabatt, index) => (
                    <TableRow key={rabatt.id} hover>
                      <TableCell>{index === 0 ? bereich.bezeichnung : ""}</TableCell>

                      <TableCell>{bezeichnungRabattart(rabatt.rabattart)}</TableCell>

                      <TableCell>{bezeichnungRabattwertTyp(rabatt.rabattwertTyp)}</TableCell>

                      <TableCell align="right">{formatRabatt(rabatt)}</TableCell>

                      <TableCell align="center">
                        <Chip
                          size="small"
                          label={rabatt.aktiv ? "Aktiv" : "Inaktiv"}
                          color={rabatt.aktiv ? "success" : "default"}
                        />
                      </TableCell>

                      <TableCell align="right">
                        <Tooltip title="Rabatt bearbeiten">
                          <IconButton size="small" onClick={() => handleEdit(bereich, rabatt)}>
                            <EditIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>

                        <Tooltip title="Rabatt löschen">
                          <IconButton
                            size="small"
                            color="error"
                            onClick={() => void handleDelete(bereich, rabatt)}
                          >
                            <DeleteIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>

                        <Tooltip title="Weitere Rabattregel hinzufügen">
                          <IconButton size="small" onClick={() => handleNew(bereich)}>
                            <AddIcon fontSize="small" />
                          </IconButton>
                        </Tooltip>
                      </TableCell>
                    </TableRow>
                  ));
                })}
              </TableBody>
            </Table>
          </TableContainer>
        </>
      )}

      <MietbereichRabattDialog
        open={dialogOpen}
        mietbereich={selectedMietbereich}
        rabatt={selectedRabatt}
        onClose={() => setDialogOpen(false)}
        onSave={handleSave}
      />
    </Box>
  );
}
