import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  TextField,
  Stack,
  MenuItem,
} from "@mui/material";

import { useEffect, useState } from "react";

import { Buchung, BuchungCreate } from "@/api/types/abrechnung";
import { FinanzKategorie, FinanzTyp, kategorieZuTyp } from "@/api/types/finanz";

interface Props {
  open: boolean;
  typ: FinanzTyp;
  initialData?: Buchung;
  onClose: () => void;
  onSave: (data: BuchungCreate) => void | Promise<void>;
}

export default function BuchungDialog({ open, initialData, onClose, onSave }: Props) {
  const [kategorie, setKategorie] = useState<FinanzKategorie | "">("");
  const [betrag, setBetrag] = useState("");
  const [beschreibung, setBeschreibung] = useState("");


  /* =========================================================
     INIT (Neu oder Bearbeiten)
  ========================================================= */

  useEffect(() => {
    if (initialData) {
      setKategorie(initialData.kategorie);
      setBetrag(initialData.betrag.toString());
      setBeschreibung(initialData.beschreibung ?? "");

    } else {
      setKategorie("");
      setBetrag("");
      setBeschreibung("");

    }
  }, [initialData, open]);

  /* =========================================================
     Kategorien filtern (typesafe)
  ========================================================= */

  const gefilterteKategorien = Object.keys(kategorieZuTyp) as FinanzKategorie[];

  /* =========================================================
     SAVE
  ========================================================= */

  const handleSave = async () => {
    if (!kategorie || !betrag) return;

    const payload: BuchungCreate = {
      kategorie,
      betrag: parseFloat(betrag),
      beschreibung,
    };

    await onSave(payload);
  };

  /* =========================================================
     UI
  ========================================================= */

  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="sm">
      <DialogTitle>{initialData ? "Buchung bearbeiten" : "Neue Buchung"}</DialogTitle>

      <DialogContent>
        <Stack spacing={2} mt={1}>
          <TextField
            select
            label="Kategorie"
            value={kategorie}
            onChange={(e) => setKategorie(e.target.value as FinanzKategorie)}
            fullWidth
          >
            {gefilterteKategorien.map((k) => (
              <MenuItem key={k} value={k}>
                {k.replaceAll("_", " ")}
              </MenuItem>
            ))}
          </TextField>

          <TextField
            label="Betrag (€)"
            type="number"
            value={betrag}
            onChange={(e) => setBetrag(e.target.value)}
            fullWidth
          />

          <TextField
            label="Beschreibung"
            value={beschreibung}
            onChange={(e) => setBeschreibung(e.target.value)}
            fullWidth
          />
        </Stack>
      </DialogContent>

      <DialogActions>
        <Button onClick={onClose}>Abbrechen</Button>
        <Button variant="contained" onClick={handleSave}>
          Speichern
        </Button>
      </DialogActions>
    </Dialog>
  );
}
