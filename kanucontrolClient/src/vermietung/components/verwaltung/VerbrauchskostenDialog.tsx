import { useEffect, useState } from "react";
import { isAxiosError } from "axios";
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
  const [preise, setPreise] = useState<{gueltigVon: string; gueltigBis: string; preis: string}[]>([]);
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
    setPreise((verbrauchskosten?.preise ?? [{ gueltigVon: "0001-01-01", gueltigBis: null, preis: verbrauchskosten?.preis ?? 0 }])
      .map(p => ({ ...p, gueltigBis: p.gueltigBis ?? "", preis: String(p.preis) })));
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


    if (!bezeichnung.trim()) {
      setError("Bitte eine Bezeichnung eingeben.");
      return;
    }

    if (!verbrauchsart) {
      setError("Bitte eine Verbrauchsart auswählen.");
      return;
    }

    if (abrechnungsart === "ZAEHLERSTAND" && !mengeneinheit.trim()) {
      setError("Bei der Abrechnung nach Zählerstand ist eine Mengeneinheit erforderlich.");
      return;
    }

    if (!preise.length || preise.some(p => !/^\d{4}-\d{2}-\d{2}$/.test(p.gueltigVon)
      || (p.gueltigBis && p.gueltigBis < p.gueltigVon) || !p.preis.trim()
      || !Number.isFinite(Number(p.preis.replace(",", "."))) || Number(p.preis.replace(",", ".")) < 0)) {
      setError("Bitte gültige Preise und Zeiträume eingeben."); return;
    }
    if (new Set(preise.map(p => p.gueltigVon)).size !== preise.length) {
      setError("Jeder Gültigkeitsbeginn darf nur einmal vorkommen."); return;
    }
    setSaving(true);
    setError(null);

    try {
      await onSave({
        bezeichnung: bezeichnung.trim(),
        verbrauchsart,
        abrechnungsart,
        mengeneinheit: abrechnungsart === "ZAEHLERSTAND" ? mengeneinheit.trim() : null,
        preis: Number(preise[0].preis.replace(",", ".")),
        preise: preise.map(p => ({ gueltigVon: p.gueltigVon, gueltigBis: p.gueltigBis || null, preis: Number(p.preis.replace(",", ".")) })),
        aktiv,
      });

      onClose();
    } catch (err) {
      console.error("Fehler beim Speichern der Verbrauchskosten:", err);
      setError(isAxiosError<{ message?: string }>(err) && err.response?.data?.message
        ? err.response.data.message : "Die Verbrauchskosten konnten nicht gespeichert werden.");
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

          <Alert severity="info">Für die gesamte Buchung gilt der Preis am Anreisetag. Ein neuer Beginn begrenzt den vorherigen Preis automatisch.</Alert>
          {preise.map((p, index) => (
            <Stack key={index} spacing={1} sx={{ border: 1, borderColor: "divider", p: 2, borderRadius: 1 }}>
              <TextField label="Gültig von" type="date" value={p.gueltigVon} required
                slotProps={{ inputLabel: { shrink: true } }}
                onChange={e => setPreise(ps => ps.map((v, i) => i === index ? {...v, gueltigVon: e.target.value} : v))} />
              <TextField label="Gültig bis" type="date" value={p.gueltigBis} helperText={p.gueltigBis ? "Einschließlich dieses Tages" : "Unbegrenzt"}
                slotProps={{ inputLabel: { shrink: true } }}
                onChange={e => setPreise(ps => ps.map((v, i) => i === index ? {...v, gueltigBis: e.target.value} : v))} />
              <TextField label="Preis (€)" type="number" value={p.preis} required
                slotProps={{ htmlInput: { min: 0, step: 0.01 } }}
                onChange={e => setPreise(ps => ps.map((v, i) => i === index ? {...v, preis: e.target.value} : v))} />
              <Button disabled={saving || preise.length === 1} onClick={() => setPreise(ps => ps.filter((_, i) => i !== index))}>Preis entfernen</Button>
            </Stack>
          ))}
          <Button disabled={saving} onClick={() => setPreise(ps => [...ps, { gueltigVon: "", gueltigBis: "", preis: "0" }])}>Preis hinzufügen (auch rückwirkend)</Button>

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
