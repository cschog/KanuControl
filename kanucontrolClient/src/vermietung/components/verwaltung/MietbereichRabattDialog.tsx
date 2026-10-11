import { useEffect, useState } from "react";
import {
  Alert,
  Button,
  Checkbox,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControl,
  FormControlLabel,
  InputLabel,
  MenuItem,
  Select,
  Stack,
  TextField,
} from "@mui/material";

import type { SelectChangeEvent } from "@mui/material";

import type { Mietbereich } from "@/vermietung/types/Mietbereich";
import type { MietbereichRabatt } from "@/vermietung/types/MietbereichRabatt";
import type { MietbereichRabattSave } from "@/vermietung/types/MietbereichRabattSave";

import { Rabattart } from "@/vermietung/enums/Rabattart";
import { RabattwertTyp } from "@/vermietung/enums/RabattwertTyp";

interface Props {
  open: boolean;
  mietbereich: Mietbereich | null;
  rabatt: MietbereichRabatt | null;
  onClose: () => void;
  onSave: (payload: MietbereichRabattSave) => Promise<void>;
}

export default function MietbereichRabattDialog({
  open,
  mietbereich,
  rabatt,
  onClose,
  onSave,
}: Props) {
  const [rabattart, setRabattart] = useState<Rabattart>(Rabattart.VEREIN);

  const [rabattwertTyp, setRabattwertTyp] = useState<RabattwertTyp>(RabattwertTyp.PROZENTUAL);

  const [rabattwert, setRabattwert] = useState("0");
  const [aktiv, setAktiv] = useState(true);

  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!open) {
      return;
    }

    setRabattart(rabatt?.rabattart ?? Rabattart.VEREIN);
    setRabattwertTyp(rabatt?.rabattwertTyp ?? RabattwertTyp.PROZENTUAL);
    setRabattwert(String(rabatt?.rabattwert ?? 0));
    setAktiv(rabatt?.aktiv ?? true);
    setError(null);
  }, [open, rabatt]);

  function handleRabattartChange(event: SelectChangeEvent<Rabattart>) {
    setRabattart(event.target.value as Rabattart);
  }

  function handleRabattwertTypChange(event: SelectChangeEvent<RabattwertTyp>) {
    setRabattwertTyp(event.target.value as RabattwertTyp);
  }

  async function handleSave() {
    const wert = Number(rabattwert.replace(",", "."));

    if (rabattwert.trim() === "" || !Number.isFinite(wert) || wert < 0) {
      setError("Bitte einen gültigen, nicht negativen Rabattwert eingeben.");
      return;
    }

    if (rabattwertTyp === RabattwertTyp.PROZENTUAL && wert > 100) {
      setError("Ein prozentualer Rabatt darf maximal 100 % betragen.");
      return;
    }

    setSaving(true);
    setError(null);

    try {
      await onSave({
        rabattart,
        rabattwertTyp,
        rabattwert: wert,
        aktiv,
      });

      onClose();
    } catch (err) {
      console.error("Fehler beim Speichern der Rabattregel:", err);
      setError(
        "Die Rabattregel konnte nicht gespeichert werden. Bitte prüfe, ob diese Rabattart für den Mietbereich bereits existiert.",
      );
    } finally {
      setSaving(false);
    }
  }

  return (
    <Dialog open={open} onClose={saving ? undefined : onClose} fullWidth maxWidth="sm">
      <DialogTitle>{rabatt ? "Rabattregel bearbeiten" : "Rabattregel anlegen"}</DialogTitle>

      <DialogContent>
        <Stack spacing={2} sx={{ pt: 1 }}>
          {mietbereich && <Alert severity="info">Mietbereich: {mietbereich.bezeichnung}</Alert>}

          {error && <Alert severity="error">{error}</Alert>}

          <FormControl fullWidth required>
            <InputLabel id="rabattart-label">Rabattart</InputLabel>

            <Select
              labelId="rabattart-label"
              value={rabattart}
              label="Rabattart"
              onChange={handleRabattartChange}
            >
              <MenuItem value={Rabattart.VEREIN}>Verein</MenuItem>

              <MenuItem value={Rabattart.VERBAND}>Verband</MenuItem>
            </Select>
          </FormControl>

          <FormControl fullWidth required>
            <InputLabel id="rabattwerttyp-label">Rabattwerttyp</InputLabel>

            <Select
              labelId="rabattwerttyp-label"
              value={rabattwertTyp}
              label="Rabattwerttyp"
              onChange={handleRabattwertTypChange}
            >
              <MenuItem value={RabattwertTyp.PROZENTUAL}>Prozentual</MenuItem>

              <MenuItem value={RabattwertTyp.ABSOLUT}>Absoluter Betrag</MenuItem>
            </Select>
          </FormControl>

          <TextField
            label={
              rabattwertTyp === RabattwertTyp.PROZENTUAL
                ? "Rabatt in Prozent"
                : "Rabattbetrag in Euro"
            }
            type="number"
            value={rabattwert}
            onChange={(event) => setRabattwert(event.target.value)}
            slotProps={{
              htmlInput: {
                min: 0,
                max: rabattwertTyp === RabattwertTyp.PROZENTUAL ? 100 : undefined,
                step: 0.01,
              },
            }}
            required
            fullWidth
          />

          <FormControlLabel
            control={
              <Checkbox checked={aktiv} onChange={(event) => setAktiv(event.target.checked)} />
            }
            label="Rabatt aktiv"
          />
        </Stack>
      </DialogContent>

      <DialogActions>
        <Button onClick={onClose} disabled={saving}>
          Abbrechen
        </Button>

        <Button variant="contained" onClick={handleSave} disabled={saving}>
          Speichern
        </Button>
      </DialogActions>
    </Dialog>
  );
}
