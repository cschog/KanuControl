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

import { Buchung, BuchungCreate } from "@/kjfp/types/abrechnung";
import { FinanzKategorie, FinanzTyp, kategorieZuTyp } from "@/kjfp/types/finanz";
import { useAppContext } from "@/core/components/context/AppContext";

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
  const [beschreibung, setBeschreibung] = useState("");
  const [kjfpBetrag, setKjfpBetrag] = useState("");
  const { active } = useAppContext();

  /* =========================================================
     INIT (Neu oder Bearbeiten)
  ========================================================= */

  useEffect(() => {
    if (initialData) {
      setKategorie(initialData.kategorie);
      setBetrag(initialData.betrag.toString());
      setBeschreibung(initialData.beschreibung ?? "");
      setKjfpBetrag((initialData.kjfpBetrag ?? initialData.betrag).toString());
    } else {
      setKategorie("");
      setBetrag("");
      setBeschreibung("");
      setKjfpBetrag("");
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
    if (!kategorie || !betrag) return;

    const payload: BuchungCreate = {
      kategorie,
      betrag: parseFloat(betrag),
      kjfpBetrag: active?.kjfpAktiv ? Number(kjfpBetrag || betrag) : 0,
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

          {active?.kjfpAktiv && (
            <TextField
              label="Davon KJFP-Anteil (€)"
              type="number"
              inputProps={{ min: 0, max: Number(betrag || 0), step: "0.01" }}
              value={kjfpBetrag}
              onChange={(e) => setKjfpBetrag(e.target.value)}
              helperText="Der KJFP-Anteil darf den Gesamtbetrag nicht überschreiten."
              fullWidth
            />
          )}

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
