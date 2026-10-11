import { useEffect, useState } from "react";
import {
  Alert,
  Box,
  Button,
  CircularProgress,
  FormControl,
  InputLabel,
  MenuItem,
  Paper,
  Select,
  Tab,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Tabs,
  Tooltip,
  Typography,
  IconButton,
} from "@mui/material";

import type { SelectChangeEvent } from "@mui/material";

import ArrowUpwardIcon from "@mui/icons-material/ArrowUpward";
import ArrowDownwardIcon from "@mui/icons-material/ArrowDownward";
import SaveIcon from "@mui/icons-material/Save";
import RestartAltIcon from "@mui/icons-material/RestartAlt";

import type { Mietobjekt } from "@/vermietung/types/Mietobjekt";
import type { Mietbereich } from "@/vermietung/types/Mietbereich";

import { getMietobjekte, sortiereMietobjekte } from "@/vermietung/api/mietobjektApi";

import { getMietbereiche, sortiereMietbereiche } from "@/vermietung/api/mietbereichApi";

type SortierTab = "MIETOBJEKTE" | "MIETBEREICHE";

export default function SortierungVerwaltung() {
  const [tab, setTab] = useState<SortierTab>("MIETOBJEKTE");

  const [mietobjekte, setMietobjekte] = useState<Mietobjekt[]>([]);
  const [mietbereiche, setMietbereiche] = useState<Mietbereich[]>([]);

  const [mietobjektId, setMietobjektId] = useState<number | "">("");

  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);

  const [originalMietobjekte, setOriginalMietobjekte] = useState<Mietobjekt[]>([]);
  const [originalMietbereiche, setOriginalMietbereiche] = useState<Mietbereich[]>([]);

  useEffect(() => {
    void loadMietobjekte();
  }, []);

  useEffect(() => {
    if (mietobjektId === "") {
      setMietbereiche([]);
      setOriginalMietbereiche([]);
      return;
    }

    void loadMietbereiche(mietobjektId);
  }, [mietobjektId]);

  async function loadMietobjekte() {
    try {
      setLoading(true);
      setError(null);

      const data = await getMietobjekte();
      const sorted = [...data].sort((a, b) => a.sortierung - b.sortierung || a.id - b.id);

      setMietobjekte(sorted);
      setOriginalMietobjekte(sorted);

      if (sorted.length > 0) {
        setMietobjektId((current) =>
          current !== "" && sorted.some((objekt) => objekt.id === current) ? current : sorted[0].id,
        );
      } else {
        setMietobjektId("");
      }
    } catch (err) {
      console.error("Fehler beim Laden der Mietobjekte:", err);
      setError("Die Mietobjekte konnten nicht geladen werden.");
    } finally {
      setLoading(false);
    }
  }

  async function loadMietbereiche(id: number) {
    try {
      setLoading(true);
      setError(null);
      setSuccess(null);

      const data = await getMietbereiche(id);
      const sorted = [...data].sort((a, b) => a.sortierung - b.sortierung || a.id - b.id);

      setMietbereiche(sorted);
      setOriginalMietbereiche(sorted);
    } catch (err) {
      console.error("Fehler beim Laden der Mietbereiche:", err);
      setError("Die Mietbereiche konnten nicht geladen werden.");
    } finally {
      setLoading(false);
    }
  }

  function verschiebeMietobjekt(index: number, richtung: -1 | 1) {
    const zielIndex = index + richtung;

    if (zielIndex < 0 || zielIndex >= mietobjekte.length) {
      return;
    }

    setMietobjekte((current) => {
      const neu = [...current];
      [neu[index], neu[zielIndex]] = [neu[zielIndex], neu[index]];
      return neu;
    });

    setSuccess(null);
  }

  function verschiebeMietbereich(index: number, richtung: -1 | 1) {
    const zielIndex = index + richtung;

    if (zielIndex < 0 || zielIndex >= mietbereiche.length) {
      return;
    }

    setMietbereiche((current) => {
      const neu = [...current];
      [neu[index], neu[zielIndex]] = [neu[zielIndex], neu[index]];
      return neu;
    });

    setSuccess(null);
  }

  function handleMietobjektChange(event: SelectChangeEvent<number | "">) {
    const value = event.target.value;
    setMietobjektId(value === "" ? "" : Number(value));
    setError(null);
    setSuccess(null);
  }

  async function handleSaveMietobjekte() {
    try {
      setSaving(true);
      setError(null);
      setSuccess(null);

      await sortiereMietobjekte(mietobjekte.map((objekt) => objekt.id));

      const gespeichert = mietobjekte.map((objekt, index) => ({
        ...objekt,
        sortierung: index + 1,
      }));

      setMietobjekte(gespeichert);
      setOriginalMietobjekte(gespeichert);

      setSuccess("Die Reihenfolge der Mietobjekte wurde gespeichert.");
    } catch (err) {
      console.error("Fehler beim Speichern der Mietobjekt-Sortierung:", err);
      setError("Die Sortierung der Mietobjekte konnte nicht gespeichert werden.");
    } finally {
      setSaving(false);
    }
  }

  async function handleSaveMietbereiche() {
    if (mietobjektId === "") {
      return;
    }

    try {
      setSaving(true);
      setError(null);
      setSuccess(null);

      await sortiereMietbereiche(
        mietobjektId,
        mietbereiche.map((bereich) => bereich.id),
      );

      const gespeichert = mietbereiche.map((bereich, index) => ({
        ...bereich,
        sortierung: index + 1,
      }));

      setMietbereiche(gespeichert);
      setOriginalMietbereiche(gespeichert);

      setSuccess("Die Reihenfolge der Mietbereiche wurde gespeichert.");
    } catch (err) {
      console.error("Fehler beim Speichern der Mietbereich-Sortierung:", err);
      setError("Die Sortierung der Mietbereiche konnte nicht gespeichert werden.");
    } finally {
      setSaving(false);
    }
  }

  function handleReset() {
    if (tab === "MIETOBJEKTE") {
      setMietobjekte([...originalMietobjekte]);
    } else {
      setMietbereiche([...originalMietbereiche]);
    }

    setError(null);
    setSuccess(null);
  }

  const sortierungGeaendert =
    tab === "MIETOBJEKTE"
      ? mietobjekte.some((objekt, index) => objekt.id !== originalMietobjekte[index]?.id)
      : mietbereiche.some((bereich, index) => bereich.id !== originalMietbereiche[index]?.id);

  if (loading) {
    return (
      <Box sx={{ display: "flex", justifyContent: "center", mt: 4 }}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box>
      <Typography variant="body1" color="text.secondary" sx={{ mb: 2 }}>
        Legen Sie fest, in welcher Reihenfolge Mietobjekte und Mietbereiche angezeigt werden. Die
        Reihenfolge wird dauerhaft gespeichert.
      </Typography>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      {success && (
        <Alert severity="success" sx={{ mb: 2 }}>
          {success}
        </Alert>
      )}

      <Tabs
        value={tab}
        onChange={(_, value: SortierTab) => {
          setTab(value);
          setError(null);
          setSuccess(null);
        }}
        sx={{ mb: 3 }}
      >
        <Tab value="MIETOBJEKTE" label="Mietobjekte" />
        <Tab value="MIETBEREICHE" label="Mietbereiche" />
      </Tabs>

      {tab === "MIETBEREICHE" && (
        <FormControl sx={{ minWidth: 280, mb: 3 }}>
          <InputLabel id="sortierung-mietobjekt-label">Mietobjekt</InputLabel>

          <Select
            labelId="sortierung-mietobjekt-label"
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
      )}

      {tab === "MIETOBJEKTE" ? (
        mietobjekte.length === 0 ? (
          <Alert severity="info">Es sind noch keine Mietobjekte vorhanden.</Alert>
        ) : (
          <SortierTabelle
            titel="Reihenfolge der Mietobjekte"
            hinweis="Mit den Pfeilen verschieben Sie ein Mietobjekt nach oben oder unten."
            namen={mietobjekte.map((objekt) => objekt.bezeichnung)}
            saving={saving}
            geaendert={sortierungGeaendert}
            onMove={verschiebeMietobjekt}
            onSave={handleSaveMietobjekte}
            onReset={handleReset}
          />
        )
      ) : mietobjektId === "" ? (
        <Alert severity="info">Es sind keine Mietobjekte vorhanden.</Alert>
      ) : mietbereiche.length === 0 ? (
        <Alert severity="info">Für dieses Mietobjekt sind keine Mietbereiche vorhanden.</Alert>
      ) : (
        <SortierTabelle
          titel="Reihenfolge der Mietbereiche"
          hinweis="Mit den Pfeilen verschieben Sie einen Mietbereich nach oben oder unten."
          namen={mietbereiche.map((bereich) => bereich.bezeichnung)}
          saving={saving}
          geaendert={sortierungGeaendert}
          onMove={verschiebeMietbereich}
          onSave={handleSaveMietbereiche}
          onReset={handleReset}
        />
      )}
    </Box>
  );
}

interface SortierTabelleProps {
  titel: string;
  hinweis: string;
  namen: string[];
  saving: boolean;
  geaendert: boolean;
  onMove: (index: number, richtung: -1 | 1) => void;
  onSave: () => void;
  onReset: () => void;
}

function SortierTabelle({
  titel,
  hinweis,
  namen,
  saving,
  geaendert,
  onMove,
  onSave,
  onReset,
}: SortierTabelleProps) {
  return (
    <Box>
      <Typography variant="h6" sx={{ mb: 1 }}>
        {titel}
      </Typography>

      <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
        {hinweis}
      </Typography>

      <TableContainer component={Paper} variant="outlined">
        <Table>
          <TableHead>
            <TableRow>
              <TableCell width={70}>Position</TableCell>
              <TableCell>Bezeichnung</TableCell>
              <TableCell align="right" width={120}>
                Reihenfolge
              </TableCell>
            </TableRow>
          </TableHead>

          <TableBody>
            {namen.map((name, index) => (
              <TableRow key={`${name}-${index}`} hover>
                <TableCell>{index + 1}</TableCell>
                <TableCell>{name}</TableCell>

                <TableCell align="right">
                  <Tooltip title="Nach oben">
                    <span>
                      <IconButton
                        size="small"
                        disabled={index === 0 || saving}
                        onClick={() => onMove(index, -1)}
                        aria-label={`${name} nach oben verschieben`}
                      >
                        <ArrowUpwardIcon fontSize="small" />
                      </IconButton>
                    </span>
                  </Tooltip>

                  <Tooltip title="Nach unten">
                    <span>
                      <IconButton
                        size="small"
                        disabled={index === namen.length - 1 || saving}
                        onClick={() => onMove(index, 1)}
                        aria-label={`${name} nach unten verschieben`}
                      >
                        <ArrowDownwardIcon fontSize="small" />
                      </IconButton>
                    </span>
                  </Tooltip>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </TableContainer>

      <Box
        sx={{
          display: "flex",
          justifyContent: "flex-end",
          gap: 1,
          mt: 2,
        }}
      >
        <Button
          variant="outlined"
          startIcon={<RestartAltIcon />}
          disabled={!geaendert || saving}
          onClick={onReset}
        >
          Zurücksetzen
        </Button>

        <Button
          variant="contained"
          startIcon={<SaveIcon />}
          disabled={!geaendert || saving}
          onClick={onSave}
        >
          {saving ? "Speichern …" : "Speichern"}
        </Button>
      </Box>
    </Box>
  );
}
