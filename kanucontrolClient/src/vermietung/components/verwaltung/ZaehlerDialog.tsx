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

import type { Zaehler, ZaehlerSave } from "@/vermietung/api/zaehlerApi";

interface Props {
  open: boolean;
  zaehler: Zaehler | null;
  onClose: () => void;
  onSave: (payload: ZaehlerSave) => Promise<void>;
}

export default function ZaehlerDialog({ open, zaehler, onClose, onSave }: Props) {
  const [bezeichnung, setBezeichnung] = useState("");
  const [verbrauchsart, setVerbrauchsart] = useState<"STROM" | "GAS" | "WASSER">("STROM");
  const [zaehlernummer, setZaehlernummer] = useState("");
  const [mengeneinheit, setMengeneinheit] = useState("kWh");
  const [aktiv, setAktiv] = useState(true);

  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!open) {
      return;
    }

    setBezeichnung(zaehler?.bezeichnung ?? "");
    setVerbrauchsart(zaehler?.verbrauchsart ?? "STROM");
    setZaehlernummer(zaehler?.zaehlernummer ?? "");
    setMengeneinheit(zaehler?.mengeneinheit ?? "kWh");
    setAktiv(zaehler?.aktiv ?? true);
    setError(null);
  }, [open, zaehler]);

  function handleVerbrauchsartChange(event: SelectChangeEvent<"STROM" | "GAS" | "WASSER">) {
    const value = event.target.value as "STROM" | "GAS" | "WASSER";

    setVerbrauchsart(value);

    if (value === "STROM") {
      setMengeneinheit("kWh");
    } else if (value === "GAS") {
      setMengeneinheit("m³");
    } else {
      setMengeneinheit("m³");
    }
  }

  async function handleSave() {
    if (!bezeichnung.trim()) {
      setError("Bitte eine Zählerbezeichnung eingeben.");
      return;
    }

    if (!mengeneinheit.trim()) {
      setError("Bitte eine Mengeneinheit eingeben.");
      return;
    }

    if (bezeichnung.trim().length > 100) {
      setError("Die Bezeichnung darf höchstens 100 Zeichen lang sein.");
      return;
    }

    if (zaehlernummer.trim().length > 100) {
      setError("Die Zählernummer darf höchstens 100 Zeichen lang sein.");
      return;
    }

    if (mengeneinheit.trim().length > 20) {
      setError("Die Mengeneinheit darf höchstens 20 Zeichen lang sein.");
      return;
    }

    setSaving(true);
    setError(null);

    try {
      await onSave({
        bezeichnung: bezeichnung.trim(),
        verbrauchsart,
        zaehlernummer: zaehlernummer.trim() || null,
        mengeneinheit: mengeneinheit.trim(),
        aktiv,
      });

      onClose();
    } catch (err) {
      console.error("Fehler beim Speichern des Zählers:", err);
      setError("Der Zähler konnte nicht gespeichert werden.");
    } finally {
      setSaving(false);
    }
  }

  return (
    <Dialog open={open} onClose={saving ? undefined : onClose} fullWidth maxWidth="sm">
      <DialogTitle>{zaehler ? "Zähler bearbeiten" : "Zähler anlegen"}</DialogTitle>

      <DialogContent>
        <Stack spacing={2} sx={{ pt: 1 }}>
          {error && <Alert severity="error">{error}</Alert>}

          <TextField
            label="Bezeichnung"
            value={bezeichnung}
            onChange={(event) => setBezeichnung(event.target.value)}
            required
            fullWidth
            autoFocus
          />

          <FormControl fullWidth required>
            <InputLabel id="zaehler-verbrauchsart-label">Verbrauchsart</InputLabel>

            <Select
              labelId="zaehler-verbrauchsart-label"
              value={verbrauchsart}
              label="Verbrauchsart"
              onChange={handleVerbrauchsartChange}
            >
              <MenuItem value="STROM">Strom</MenuItem>
              <MenuItem value="GAS">Gas</MenuItem>
              <MenuItem value="WASSER">Wasser</MenuItem>
            </Select>
          </FormControl>

          <TextField
            label="Zählernummer"
            value={zaehlernummer}
            onChange={(event) => setZaehlernummer(event.target.value)}
            fullWidth
            helperText="Optional: Nummer des physischen Zählers"
          />

          <TextField
            label="Mengeneinheit"
            value={mengeneinheit}
            onChange={(event) => setMengeneinheit(event.target.value)}
            placeholder="z. B. kWh oder m³"
            required
            fullWidth
          />

          <FormControlLabel
            control={
              <Checkbox checked={aktiv} onChange={(event) => setAktiv(event.target.checked)} />
            }
            label="Aktiv"
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
