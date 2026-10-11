import { useCallback, useEffect, useState } from "react";
import {
  Alert,
  Box,
  Button,
  Chip,
  CircularProgress,
  FormControl,
  InputLabel,
  MenuItem,
  Select,
  Stack,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Paper,
} from "@mui/material";

import type { SelectChangeEvent } from "@mui/material";

import AddIcon from "@mui/icons-material/Add";
import EditIcon from "@mui/icons-material/Edit";
import AssessmentIcon from "@mui/icons-material/Assessment";

import { getMietobjekte } from "@/vermietung/api/mietobjektApi";
import type { Mietobjekt } from "@/vermietung/types/Mietobjekt";

import { getZaehler, createZaehler, updateZaehler } from "@/vermietung/api/zaehlerApi";

import type { Zaehler, ZaehlerSave } from "@/vermietung/api/zaehlerApi";

import ZaehlerDialog from "@/vermietung/components/verwaltung/ZaehlerDialog";
import ZaehlerstaendeDialog from "@/vermietung/components/verwaltung/ZaehlerstaendeDialog";

export default function ZaehlerVerwaltung() {
  const [mietobjekte, setMietobjekte] = useState<Mietobjekt[]>([]);
  const [selectedMietobjektId, setSelectedMietobjektId] = useState<number | "">("");

  const [zaehler, setZaehler] = useState<Zaehler[]>([]);
  const [selectedZaehler, setSelectedZaehler] = useState<Zaehler | null>(null);

  const [dialogOpen, setDialogOpen] = useState(false);
  const [staendeDialogOpen, setStaendeDialogOpen] = useState(false);

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    const loadMietobjekte = async () => {
      try {
        setError(null);

        const data = await getMietobjekte();

        setMietobjekte(data);

        if (data.length > 0) {
          setSelectedMietobjektId(data[0].id);
        }
      } catch (err) {
        console.error("Fehler beim Laden der Mietobjekte:", err);
        setError("Die Mietobjekte konnten nicht geladen werden.");
      }
    };

    void loadMietobjekte();
  }, []);

  const loadZaehler = useCallback(async () => {
    if (selectedMietobjektId === "") {
      setZaehler([]);
      return;
    }

    setLoading(true);
    setError(null);

    try {
      const data = await getZaehler(selectedMietobjektId);
      setZaehler(data);
    } catch (err) {
      console.error("Fehler beim Laden der Zähler:", err);
      setError("Die Zähler konnten nicht geladen werden.");
    } finally {
      setLoading(false);
    }
  }, [selectedMietobjektId]);

  useEffect(() => {
    void loadZaehler();
  }, [loadZaehler]);

  function handleMietobjektChange(event: SelectChangeEvent<number | "">) {
    const value = event.target.value;

    setSelectedMietobjektId(value === "" ? "" : Number(value));
    setSelectedZaehler(null);
  }

  function handleCreate() {
    setSelectedZaehler(null);
    setDialogOpen(true);
  }

  function handleEdit(item: Zaehler) {
    setSelectedZaehler(item);
    setDialogOpen(true);
  }

  function handleShowStaende(item: Zaehler) {
    setSelectedZaehler(item);
    setStaendeDialogOpen(true);
  }

  async function handleSave(payload: ZaehlerSave) {
    if (selectedMietobjektId === "") {
      return;
    }

    if (selectedZaehler && dialogOpen) {
      await updateZaehler(selectedMietobjektId, selectedZaehler.id, payload);
    } else {
      await createZaehler(selectedMietobjektId, payload);
    }

    await loadZaehler();
  }

  const verbrauchsartLabel: Record<Zaehler["verbrauchsart"], string> = {
    STROM: "Strom",
    GAS: "Gas",
    WASSER: "Wasser",
  };

  return (
    <Box>
      <Stack
        direction={{ xs: "column", sm: "row" }}
        spacing={2}
        alignItems={{ xs: "stretch", sm: "center" }}
        justifyContent="space-between"
        sx={{ mb: 3 }}
      >
        <FormControl sx={{ minWidth: 260 }}>
          <InputLabel id="zaehler-mietobjekt-label">Mietobjekt</InputLabel>

          <Select
            labelId="zaehler-mietobjekt-label"
            value={selectedMietobjektId}
            label="Mietobjekt"
            onChange={handleMietobjektChange}
          >
            {mietobjekte.map((mietobjekt) => (
              <MenuItem key={mietobjekt.id} value={mietobjekt.id}>
                {mietobjekt.bezeichnung}
              </MenuItem>
            ))}
          </Select>
        </FormControl>

        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={handleCreate}
          disabled={selectedMietobjektId === ""}
        >
          Zähler anlegen
        </Button>
      </Stack>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      {loading ? (
        <Box sx={{ display: "flex", justifyContent: "center", p: 4 }}>
          <CircularProgress />
        </Box>
      ) : selectedMietobjektId === "" ? (
        <Alert severity="info">Bitte wähle zunächst ein Mietobjekt aus.</Alert>
      ) : zaehler.length === 0 ? (
        <Alert severity="info">Für dieses Mietobjekt sind noch keine Zähler angelegt.</Alert>
      ) : (
        <TableContainer component={Paper} variant="outlined">
          <Table>
            <TableHead>
              <TableRow>
                <TableCell>Bezeichnung</TableCell>
                <TableCell>Verbrauchsart</TableCell>
                <TableCell>Zählernummer</TableCell>
                <TableCell>Einheit</TableCell>
                <TableCell>Status</TableCell>
                <TableCell align="right">Aktionen</TableCell>
              </TableRow>
            </TableHead>

            <TableBody>
              {zaehler.map((item) => (
                <TableRow key={item.id} hover>
                  <TableCell>{item.bezeichnung}</TableCell>

                  <TableCell>{verbrauchsartLabel[item.verbrauchsart]}</TableCell>

                  <TableCell>{item.zaehlernummer || "–"}</TableCell>

                  <TableCell>{item.mengeneinheit}</TableCell>

                  <TableCell>
                    <Chip
                      label={item.aktiv ? "Aktiv" : "Inaktiv"}
                      color={item.aktiv ? "success" : "default"}
                      size="small"
                    />
                  </TableCell>

                  <TableCell align="right">
                    <Stack direction="row" spacing={1} justifyContent="flex-end">
                      <Button
                        size="small"
                        startIcon={<AssessmentIcon />}
                        onClick={() => handleShowStaende(item)}
                      >
                        Zählerstände
                      </Button>

                      <Button
                        size="small"
                        startIcon={<EditIcon />}
                        onClick={() => handleEdit(item)}
                      >
                        Bearbeiten
                      </Button>
                    </Stack>
                  </TableCell>
                </TableRow>
              ))}
            </TableBody>
          </Table>
        </TableContainer>
      )}

      <ZaehlerDialog
        open={dialogOpen}
        zaehler={selectedZaehler}
        onClose={() => setDialogOpen(false)}
        onSave={handleSave}
      />

      <ZaehlerstaendeDialog
        open={staendeDialogOpen}
        zaehler={selectedZaehler}
        onClose={() => setStaendeDialogOpen(false)}
      />
    </Box>
  );
}
