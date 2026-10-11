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
  Chip,
} from "@mui/material";

import type { SelectChangeEvent } from "@mui/material";

import AddIcon from "@mui/icons-material/Add";
import EditIcon from "@mui/icons-material/Edit";

import type { Mietobjekt } from "@/vermietung/types/Mietobjekt";
import type { Verbrauchskosten } from "@/vermietung/types/Verbrauchskosten";
import type { VerbrauchskostenSave } from "@/vermietung/types/VerbrauchskostenSave";

import { getMietobjekte } from "@/vermietung/api/mietobjektApi";
import {
  getVerbrauchskosten,
  createVerbrauchskosten,
  updateVerbrauchskosten,
} from "@/vermietung/api/verbrauchskostenApi";

import VerbrauchskostenDialog from "@/vermietung/components/verwaltung/VerbrauchskostenDialog";

export default function VerbrauchskostenVerwaltung() {
  const [mietobjekte, setMietobjekte] = useState<Mietobjekt[]>([]);
  const [verbrauchskosten, setVerbrauchskosten] = useState<Verbrauchskosten[]>([]);

  const [mietobjektId, setMietobjektId] = useState<number | "">("");
  const [selected, setSelected] = useState<Verbrauchskosten | null>(null);

  const [dialogOpen, setDialogOpen] = useState(false);
  const [loading, setLoading] = useState(true);
  const [loadingDaten, setLoadingDaten] = useState(false);
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

  async function loadVerbrauchskosten(id: number) {
    setLoadingDaten(true);
    setError(null);

    try {
      const data = await getVerbrauchskosten(id);
      setVerbrauchskosten(data);
    } catch (err) {
      console.error("Fehler beim Laden der Verbrauchskosten:", err);
      setVerbrauchskosten([]);
      setError("Die Verbrauchskosten konnten nicht geladen werden.");
    } finally {
      setLoadingDaten(false);
    }
  }

  useEffect(() => {
    setVerbrauchskosten([]);

    if (mietobjektId === "") {
      return;
    }

    void loadVerbrauchskosten(mietobjektId);
  }, [mietobjektId]);

  function handleMietobjektChange(event: SelectChangeEvent<number | "">) {
    const value = event.target.value;
    setMietobjektId(value === "" ? "" : Number(value));
  }

  function handleNew() {
    setSelected(null);
    setDialogOpen(true);
  }

  function handleEdit(item: Verbrauchskosten) {
    setSelected(item);
    setDialogOpen(true);
  }

  async function handleSave(payload: VerbrauchskostenSave) {
    if (mietobjektId === "") {
      throw new Error("Es wurde kein Mietobjekt ausgewählt.");
    }

    if (selected) {
      await updateVerbrauchskosten(mietobjektId, selected.id, payload);
    } else {
      await createVerbrauchskosten(mietobjektId, payload);
    }

    await loadVerbrauchskosten(mietobjektId);
  }

  function formatPreis(preis: number) {
    return preis.toLocaleString("de-DE", {
      style: "currency",
      currency: "EUR",
    });
  }

  function formatAbrechnungsart(item: Verbrauchskosten) {
    switch (item.abrechnungsart) {
      case "PAUSCHALE":
        return "Pauschale pro Buchung";
      case "TAGESPREIS":
        return "Preis pro Tag";
      case "ZAEHLERSTAND":
        return "Nach Zählerstand";
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

      <Box
        sx={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          gap: 2,
          flexWrap: "wrap",
          mb: 3,
        }}
      >
        <FormControl sx={{ minWidth: 280 }}>
          <InputLabel id="verbrauchskosten-mietobjekt-label">Mietobjekt</InputLabel>

          <Select
            labelId="verbrauchskosten-mietobjekt-label"
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

        <Button
          variant="contained"
          startIcon={<AddIcon />}
          onClick={handleNew}
          disabled={mietobjektId === ""}
        >
          Verbrauchskosten anlegen
        </Button>
      </Box>

      {mietobjekte.length === 0 ? (
        <Alert severity="info">Es sind noch keine Mietobjekte vorhanden.</Alert>
      ) : loadingDaten ? (
        <Box sx={{ display: "flex", justifyContent: "center", mt: 3 }}>
          <CircularProgress />
        </Box>
      ) : verbrauchskosten.length === 0 ? (
        <Alert severity="info">
          Für dieses Mietobjekt sind noch keine Verbrauchskosten hinterlegt.
        </Alert>
      ) : (
        <>
          <Typography variant="h6" sx={{ mb: 2 }}>
            Verbrauchskosten
          </Typography>

          <TableContainer component={Paper} variant="outlined">
            <Table>
              <TableHead>
                <TableRow>
                  <TableCell>Bezeichnung</TableCell>
                  <TableCell>Abrechnungsart</TableCell>
                  <TableCell>Mengeneinheit</TableCell>
                  <TableCell align="right">Preis</TableCell>
                  <TableCell align="center">Status</TableCell>
                  <TableCell align="right">Aktionen</TableCell>
                </TableRow>
              </TableHead>

              <TableBody>
                {verbrauchskosten.map((item) => (
                  <TableRow key={item.id} hover>
                    <TableCell>{item.bezeichnung}</TableCell>

                    <TableCell>{formatAbrechnungsart(item)}</TableCell>

                    <TableCell>{item.mengeneinheit ?? "–"}</TableCell>

                    <TableCell align="right">
                      {formatPreis(item.preis)}
                      {item.abrechnungsart === "TAGESPREIS" && " / Tag"}
                      {item.abrechnungsart === "ZAEHLERSTAND" &&
                        item.mengeneinheit &&
                        ` / ${item.mengeneinheit}`}
                    </TableCell>

                    <TableCell align="center">
                      <Chip
                        size="small"
                        label={item.aktiv ? "Aktiv" : "Inaktiv"}
                        color={item.aktiv ? "success" : "default"}
                      />
                    </TableCell>

                    <TableCell align="right">
                      <Tooltip title="Verbrauchskosten bearbeiten">
                        <IconButton size="small" onClick={() => handleEdit(item)}>
                          <EditIcon fontSize="small" />
                        </IconButton>
                      </Tooltip>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          </TableContainer>
        </>
      )}

      <VerbrauchskostenDialog
        open={dialogOpen}
        verbrauchskosten={selected}
        onClose={() => setDialogOpen(false)}
        onSave={handleSave}
      />
    </Box>
  );
}
