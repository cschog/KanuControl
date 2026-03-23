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

export default function BuchungDialog({ open, typ, initialData, onClose, onSave }: Props) {
  const [kategorie, setKategorie] = useState<FinanzKategorie | "">("");
  const [betrag, setBetrag] = useState("");
  const [datum, setDatum] = useState("");
  const [beschreibung, setBeschreibung] = useState("");
  const [teilnehmerId, setTeilnehmerId] = useState<number | "">("");

  /* =========================================================
     INIT (Neu oder Bearbeiten)
  ========================================================= */

  useEffect(() => {
    if (initialData) {
      setKategorie(initialData.kategorie);
      setBetrag(initialData.betrag.toString());
      setDatum(initialData.datum);
      setBeschreibung(initialData.beschreibung ?? "");
      setTeilnehmerId(initialData.teilnehmerId);
    } else {
      setKategorie("");
      setBetrag("");
      setDatum("");
      setBeschreibung("");
      setTeilnehmerId("");
    }
  }, [initialData, open]);

  /* =========================================================
     Kategorien filtern (typesafe)
  ========================================================= */

  const gefilterteKategorien = (Object.keys(kategorieZuTyp) as FinanzKategorie[]).filter(
    (k) => kategorieZuTyp[k] === typ,
  );

  /* =========================================================
     SAVE
  ========================================================= */

  const handleSave = async () => {
    if (!kategorie || !betrag || !datum || !teilnehmerId) return;

    const payload: BuchungCreate = {
      kategorie,
      betrag: parseFloat(betrag),
      datum,
      beschreibung,
      teilnehmerId: Number(teilnehmerId),
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
            label="Datum"
            type="date"
            value={datum}
            onChange={(e) => setDatum(e.target.value)}
            InputLabelProps={{
              shrink: true,
            }}
            fullWidth
          />

          <TextField
            label="Beschreibung"
            value={beschreibung}
            onChange={(e) => setBeschreibung(e.target.value)}
            fullWidth
          />

          <TextField
            label="Teilnehmer ID"
            type="number"
            value={teilnehmerId}
            onChange={(e) => setTeilnehmerId(Number(e.target.value))}
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
