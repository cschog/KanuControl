import { useEffect, useState } from "react";
import {
  Alert,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Stack,
  TextField,
} from "@mui/material";

import type { Zaehlerstand, ZaehlerstandSave } from "@/vermietung/api/zaehlerstandApi";

interface Props {
  open: boolean;
  zaehlerstand: Zaehlerstand | null;
  onClose: () => void;
  onSave: (payload: ZaehlerstandSave) => Promise<void>;
}

function getCurrentDateTime(): string {
  const now = new Date();
  const offset = now.getTimezoneOffset() * 60_000;

  return new Date(now.getTime() - offset).toISOString().slice(0, 16);
}

export default function ZaehlerstandDialog({ open, zaehlerstand, onClose, onSave }: Props) {
  const [ablesedatum, setAblesedatum] = useState("");
  const [wert, setWert] = useState("");
  const [bemerkung, setBemerkung] = useState("");

  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!open) {
      return;
    }

    setAblesedatum(
      zaehlerstand?.ablesedatum ? zaehlerstand.ablesedatum.slice(0, 16) : getCurrentDateTime(),
    );

    setWert(zaehlerstand?.zaehlerstand != null ? String(zaehlerstand.zaehlerstand) : "");

    setBemerkung(zaehlerstand?.bemerkung ?? "");
    setError(null);
  }, [open, zaehlerstand]);

  async function handleSave() {
    if (!ablesedatum) {
      setError("Bitte ein Ablesedatum eingeben.");
      return;
    }

    if (!wert.trim()) {
      setError("Bitte einen Zählerstand eingeben.");
      return;
    }

    const numerischerWert = Number(wert.replace(",", "."));

    if (!Number.isFinite(numerischerWert) || numerischerWert < 0) {
      setError("Der Zählerstand muss eine Zahl größer oder gleich 0 sein.");
      return;
    }

    if (bemerkung.length > 1000) {
      setError("Die Bemerkung darf höchstens 1000 Zeichen lang sein.");
      return;
    }

    setSaving(true);
    setError(null);

    try {
      await onSave({
        ablesedatum,
        zaehlerstand: numerischerWert,
        bemerkung: bemerkung.trim() || null,
      });

      onClose();
    } catch (err) {
      console.error("Fehler beim Speichern des Zählerstands:", err);
      setError("Der Zählerstand konnte nicht gespeichert werden.");
    } finally {
      setSaving(false);
    }
  }

  return (
    <Dialog open={open} onClose={saving ? undefined : onClose} fullWidth maxWidth="sm">
      <DialogTitle>{zaehlerstand ? "Zählerstand bearbeiten" : "Zählerstand erfassen"}</DialogTitle>

      <DialogContent>
        <Stack spacing={2} sx={{ pt: 1 }}>
          {error && <Alert severity="error">{error}</Alert>}

          <TextField
            label="Ablesedatum und Uhrzeit"
            type="datetime-local"
            value={ablesedatum}
            onChange={(event) => setAblesedatum(event.target.value)}
            required
            fullWidth
            slotProps={{
              inputLabel: { shrink: true },
            }}
          />

          <TextField
            label="Zählerstand"
            type="number"
            value={wert}
            onChange={(event) => setWert(event.target.value)}
            required
            fullWidth
            slotProps={{
              htmlInput: {
                min: 0,
                step: 0.001,
              },
            }}
            helperText="Zählerstand gemäß Anzeige des Zählers"
          />

          <TextField
            label="Bemerkung"
            value={bemerkung}
            onChange={(event) => setBemerkung(event.target.value)}
            multiline
            minRows={2}
            maxRows={5}
            fullWidth
            slotProps={{
              htmlInput: {
                maxLength: 1000,
              },
            }}
            helperText={`${bemerkung.length}/1000 Zeichen`}
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
