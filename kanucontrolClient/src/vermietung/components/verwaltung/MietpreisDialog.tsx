import { useEffect, useState } from "react";
import {
  Alert,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  MenuItem,
  Stack,
  TextField,
} from "@mui/material";

import type { Mietpreis } from "@/vermietung/types/Mietpreis";
import type { MietpreisSave } from "@/vermietung/types/MietpreisSave";
import type { Buchungsquelle } from "@/vermietung/enums/Buchungsquelle";

interface MietpreisDialogProps {
  open: boolean;
  mietbereichId: number;
  mietpreis: Mietpreis | null;
  onClose: () => void;
  onSave: (payload: MietpreisSave) => Promise<void>;
}

export default function MietpreisDialog({
  open,
  mietbereichId,
  mietpreis,
  onClose,
  onSave,
}: MietpreisDialogProps) {
  const [buchungsquelle, setBuchungsquelle] = useState<Buchungsquelle>("DIREKT");
  const [gueltigAb, setGueltigAb] = useState("");
  const [preis, setPreis] = useState("");
  const [bemerkung, setBemerkung] = useState("");
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!open) {
      return;
    }

    setBuchungsquelle(mietpreis?.buchungsquelle ?? "DIREKT");
    setGueltigAb(mietpreis?.gueltigAb ?? new Date().toISOString().slice(0, 10));
    setPreis(mietpreis?.preis !== undefined ? String(mietpreis.preis) : "");
    setBemerkung(mietpreis?.bemerkung ?? "");
    setError(null);
  }, [open, mietpreis]);

  async function handleSave() {
    const parsedPreis = Number(preis.replace(",", "."));

    if (!gueltigAb) {
      setError("Bitte ein Gültigkeitsdatum angeben.");
      return;
    }

    if (preis.trim() === "" || !Number.isFinite(parsedPreis) || parsedPreis < 0) {
      setError("Bitte einen gültigen, nicht negativen Preis eingeben.");
      return;
    }

    setSaving(true);
    setError(null);

    try {
      await onSave({
        mietbereichId,
        buchungsquelle,
        gueltigAb,
        preis: parsedPreis,
        bemerkung: bemerkung.trim() || undefined,
      });

      onClose();
    } catch (err) {
      console.error("Fehler beim Speichern des Mietpreises:", err);
      setError("Der Mietpreis konnte nicht gespeichert werden.");
    } finally {
      setSaving(false);
    }
  }

  return (
    <Dialog open={open} onClose={saving ? undefined : onClose} fullWidth maxWidth="sm">
      <DialogTitle>{mietpreis ? "Mietpreis bearbeiten" : "Neuen Mietpreis anlegen"}</DialogTitle>

      <DialogContent>
        <Stack spacing={2} sx={{ pt: 1 }}>
          {error && <Alert severity="error">{error}</Alert>}

          <TextField
            select
            label="Buchungsquelle"
            value={buchungsquelle}
            onChange={(event) => setBuchungsquelle(event.target.value as Buchungsquelle)}
            fullWidth
            required
          >
            <MenuItem value="DIREKT">Direktbuchung</MenuItem>
            <MenuItem value="AIRBNB">Airbnb</MenuItem>
          </TextField>

          <TextField
            label="Gültig ab"
            type="date"
            value={gueltigAb}
            onChange={(event) => setGueltigAb(event.target.value)}
            slotProps={{ inputLabel: { shrink: true } }}
            fullWidth
            required
          />

          <TextField
            label="Preis in Euro"
            type="number"
            value={preis}
            onChange={(event) => setPreis(event.target.value)}
            slotProps={{
              htmlInput: { min: 0, step: "0.01" },
            }}
            fullWidth
            required
          />

          <TextField
            label="Bemerkung"
            value={bemerkung}
            onChange={(event) => setBemerkung(event.target.value)}
            multiline
            minRows={2}
            maxRows={5}
            fullWidth
          />
        </Stack>
      </DialogContent>

      <DialogActions>
        <Button onClick={onClose} disabled={saving}>
          Abbrechen
        </Button>

        <Button variant="contained" onClick={() => void handleSave()} disabled={saving}>
          Speichern
        </Button>
      </DialogActions>
    </Dialog>
  );
}
