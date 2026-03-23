import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  MenuItem,
  TextField,
  Stack,
} from "@mui/material";

import { useState } from "react";

import { BelegCreate } from "@/api/types/abrechnung";

interface Props {
  open: boolean;
  kuerzelListe: string[];
  onClose: () => void;
  onSave: (data: BelegCreate) => void | Promise<void>;
}

export default function BelegDialog({ open, kuerzelListe, onClose, onSave }: Props) {
  const [datum, setDatum] = useState("");
  const [beschreibung, setBeschreibung] = useState("");
  const [kuerzel, setKuerzel] = useState("");

  const handleSave = async () => {
    if (!datum || !kuerzel) return;

    await onSave({
      kuerzel,
      datum,
      beschreibung,
    });

    setDatum("");
    setBeschreibung("");
    setKuerzel("");
  };

  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="sm">
      <DialogTitle>Neuer Beleg</DialogTitle>

      <DialogContent>
        <Stack spacing={2} mt={1}>
          <TextField
            select
            label="Finanzgruppe (Kürzel)"
            value={kuerzel}
            onChange={(e) => setKuerzel(e.target.value)}
            required
            fullWidth
          >
            {kuerzelListe.map((k) => (
              <MenuItem key={k} value={k}>
                {k}
              </MenuItem>
            ))}
          </TextField>

          <TextField
            label="Datum"
            type="date"
            value={datum}
            onChange={(e) => setDatum(e.target.value)}
            InputLabelProps={{ shrink: true }}
            required
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
