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

import type {
  Verbrauchskosten,
  Verbrauchsart,
  Verbrauchsabrechnungsart,
} from "@/vermietung/types/Verbrauchskosten";
import type { VerbrauchskostenSave } from "@/vermietung/types/VerbrauchskostenSave";

interface Props {
  open: boolean;
  verbrauchskosten: Verbrauchskosten | null;
  onClose: () => void;
  onSave: (payload: VerbrauchskostenSave) => Promise<void>;
}

export default function VerbrauchskostenDialog({ open, verbrauchskosten, onClose, onSave }: Props) {
  const [bezeichnung, setBezeichnung] = useState("");
  const [verbrauchsart, setVerbrauchsart] = useState<Verbrauchsart>("STROM");
  const [abrechnungsart, setAbrechnungsart] = useState<Verbrauchsabrechnungsart>("PAUSCHALE");
  const [mengeneinheit, setMengeneinheit] = useState("");
  const [preis, setPreis] = useState("0");
  const [aktiv, setAktiv] = useState(true);

  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!open) {
      return;
    }

    setBezeichnung(verbrauchskosten?.bezeichnung ?? "");
    setVerbrauchsart(verbrauchskosten?.verbrauchsart ?? "STROM");
    setAbrechnungsart(verbrauchskosten?.abrechnungsart ?? "PAUSCHALE");
    setMengeneinheit(verbrauchskosten?.mengeneinheit ?? "");
    setPreis(String(verbrauchskosten?.preis ?? 0));
    setAktiv(verbrauchskosten?.aktiv ?? true);
    setError(null);
  }, [open, verbrauchskosten]);

  function handleVerbrauchsartChange(event: SelectChangeEvent<Verbrauchsart>) {
    setVerbrauchsart(event.target.value as Verbrauchsart);
  }

  function handleAbrechnungsartChange(event: SelectChangeEvent<Verbrauchsabrechnungsart>) {
    const value = event.target.value as Verbrauchsabrechnungsart;

    setAbrechnungsart(value);

    if (value !== "ZAEHLERSTAND") {
      setMengeneinheit("");
    }
  }

  async function handleSave() {
    const preisWert = Number(preis.replace(",", "."));

    if (!bezeichnung.trim()) {
      setError("Bitte eine Bezeichnung eingeben.");
      return;
    }

    if (!verbrauchsart) {
      setError("Bitte eine Verbrauchsart auswählen.");
      return;
    }

    if (preis.trim() === "" || !Number.isFinite(preisWert) || preisWert < 0) {
      setError("Bitte einen gültigen, nicht negativen Preis eingeben.");
      return;
    }

    if (abrechnungsart === "ZAEHLERSTAND" && !mengeneinheit.trim()) {
      setError("Bei der Abrechnung nach Zählerstand ist eine Mengeneinheit erforderlich.");
      return;
    }

    setSaving(true);
    setError(null);

    try {
      await onSave({
        bezeichnung: bezeichnung.trim(),
        verbrauchsart,
        abrechnungsart,
        mengeneinheit: abrechnungsart === "ZAEHLERSTAND" ? mengeneinheit.trim() : null,
        preis: preisWert,
        aktiv,
      });

      onClose();
    } catch (err) {
      console.error("Fehler beim Speichern der Verbrauchskosten:", err);
      setError("Die Verbrauchskosten konnten nicht gespeichert werden.");
    } finally {
      setSaving(false);
    }
  }

  return (
    <Dialog open={open} onClose={saving ? undefined : onClose} fullWidth maxWidth="sm">
      <DialogTitle>
        {verbrauchskosten ? "Verbrauchskosten bearbeiten" : "Verbrauchskosten anlegen"}
      </DialogTitle>

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
            <InputLabel id="verbrauchsart-label">Verbrauchsart</InputLabel>
            <Select
              labelId="verbrauchsart-label"
              value={verbrauchsart}
              label="Verbrauchsart"
              onChange={handleVerbrauchsartChange}
            >
              <MenuItem value="STROM">Strom</MenuItem>
              <MenuItem value="GAS">Gas</MenuItem>
              <MenuItem value="WASSER">Wasser</MenuItem>
            </Select>
          </FormControl>

          <FormControl fullWidth required>
            <InputLabel id="abrechnungsart-label">Abrechnungsart</InputLabel>
            <Select
              labelId="abrechnungsart-label"
              value={abrechnungsart}
              label="Abrechnungsart"
              onChange={handleAbrechnungsartChange}
            >
              <MenuItem value="PAUSCHALE">Pauschale pro Buchung</MenuItem>
              <MenuItem value="TAGESPREIS">Preis pro Tag</MenuItem>
              <MenuItem value="ZAEHLERSTAND">Abrechnung nach Zählerstand</MenuItem>
            </Select>
          </FormControl>

          {abrechnungsart === "ZAEHLERSTAND" && (
            <TextField
              label="Mengeneinheit"
              value={mengeneinheit}
              onChange={(event) => setMengeneinheit(event.target.value)}
              placeholder="z. B. kWh oder m³"
              required
              fullWidth
            />
          )}

          <TextField
            label={
              abrechnungsart === "PAUSCHALE"
                ? "Pauschalpreis (€)"
                : abrechnungsart === "TAGESPREIS"
                  ? "Preis pro Tag (€)"
                  : "Preis pro Einheit (€)"
            }
            type="number"
            value={preis}
            onChange={(event) => setPreis(event.target.value)}
            slotProps={{
              htmlInput: { min: 0, step: 0.01 },
            }}
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
