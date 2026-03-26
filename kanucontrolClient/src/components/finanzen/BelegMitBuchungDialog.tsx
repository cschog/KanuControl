import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  TextField,
  Stack,
  MenuItem,
  Box,
} from "@mui/material";
import { useState, useEffect } from "react";

import { kategorieZuTyp, FinanzKategorie } from "@/api/types/finanz";
import { BelegCreate, BuchungCreate } from "@/api/types/abrechnung";

interface Props {
  open: boolean;
  kuerzelListe: string[];
  onClose: () => void;
  onSave: (data: { beleg: BelegCreate; buchung: BuchungCreate }) => Promise<void>;
}

export default function BelegMitBuchungDialog({ open, kuerzelListe, onClose, onSave }: Props) {
  const [kuerzel, setKuerzel] = useState("");
  const [datum, setDatum] = useState("");
  const [beschreibung, setBeschreibung] = useState("");

  const [kategorie, setKategorie] = useState<FinanzKategorie>("VERPFLEGUNG");
  const [betrag, setBetrag] = useState("");
  const [buchungText, setBuchungText] = useState("");

  useEffect(() => {
    if (open) {
      setDatum(new Date().toISOString().slice(0, 10));
    } else {
      setKuerzel("");
      setDatum("");
      setBeschreibung("");
      setKategorie("VERPFLEGUNG");
      setBetrag("");
      setBuchungText("");
    }
  }, [open]);

  const isValid =
    kuerzel.trim() !== "" && datum.trim() !== "" && betrag !== "" && Number(betrag) > 0;

  const handleSave = async () => {
    if (!isValid) return;

    await onSave({
      beleg: {
        kuerzel,
        datum,
        beschreibung,
      },
      buchung: {
        kategorie,
        betrag: Number(betrag),
        datum,
        beschreibung: buchungText,
      },
    });

    onClose();
  };

  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="md">
      <DialogTitle>Beleg + erste Buchung</DialogTitle>

      <DialogContent>
        <Stack spacing={3} mt={1}>
          {/* ================= BELEG ================= */}
          <Stack direction={{ xs: "column", md: "row" }} spacing={2}>
            <Box flex={1}>
              <TextField
                select
                fullWidth
                label="Kürzel"
                value={kuerzel}
                onChange={(e) => setKuerzel(e.target.value)}
                required
              >
                {kuerzelListe.map((k) => (
                  <MenuItem key={k} value={k}>
                    {k}
                  </MenuItem>
                ))}
              </TextField>
            </Box>

            <Box flex={1}>
              <TextField
                type="date"
                fullWidth
                label="Datum"
                InputLabelProps={{ shrink: true }}
                value={datum}
                onChange={(e) => setDatum(e.target.value)}
                required
              />
            </Box>

            <Box flex={1}>
              <TextField
                fullWidth
                label="Beschreibung (Beleg)"
                value={beschreibung}
                onChange={(e) => setBeschreibung(e.target.value)}
              />
            </Box>
          </Stack>

          {/* ================= BUCHUNG ================= */}
          <Stack direction={{ xs: "column", md: "row" }} spacing={2}>
            <Box flex={1}>
              <TextField
                select
                fullWidth
                label="Kategorie"
                value={kategorie}
                onChange={(e) => setKategorie(e.target.value as FinanzKategorie)}
                required
              >
                {Object.keys(kategorieZuTyp).map((k) => {
                  const key = k as FinanzKategorie;

                  return (
                    <MenuItem key={key} value={key}>
                      {key.replaceAll("_", " ")}
                    </MenuItem>
                  );
                })}
              </TextField>
            </Box>

            <Box flex={1}>
              <TextField
                type="number"
                fullWidth
                label="Betrag (€)"
                value={betrag}
                onChange={(e) => setBetrag(e.target.value)}
                required
                inputProps={{ min: 0, step: "0.01" }}
              />
            </Box>

            <Box flex={1}>
              <TextField
                fullWidth
                label="Beschreibung (Buchung)"
                value={buchungText}
                onChange={(e) => setBuchungText(e.target.value)}
              />
            </Box>
          </Stack>
        </Stack>
      </DialogContent>

      <DialogActions>
        <Button onClick={onClose}>Abbrechen</Button>
        <Button variant="contained" onClick={handleSave} disabled={!isValid}>
          Speichern
        </Button>
      </DialogActions>
    </Dialog>
  );
}
