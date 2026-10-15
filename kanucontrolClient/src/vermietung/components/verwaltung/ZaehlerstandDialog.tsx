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
  onDelete: () => Promise<void>;
}

function getCurrentDateTime(): string {
  const now = new Date();
  const offset = now.getTimezoneOffset() * 60_000;

  return new Date(now.getTime() - offset).toISOString().slice(0, 16);
}

export default function ZaehlerstandDialog({
  open,
  zaehlerstand,
  onClose,
  onSave,
  onDelete,
}: Props) {
  const [ablesedatum, setAblesedatum] = useState("");
  const [wert, setWert] = useState("");
  const [bemerkung, setBemerkung] = useState("");

  const [saving, setSaving] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const busy = saving || deleting;

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

  async function handleDelete() {
    if (!zaehlerstand) {
      return;
    }

    const bestaetigt = window.confirm(
      `Möchtest du diesen Zählerstand wirklich löschen?\n\n` +
        `Ablesedatum: ${zaehlerstand.ablesedatum.replace("T", " ")}\n` +
        `Zählerstand: ${zaehlerstand.zaehlerstand}\n\n` +
        "Das Löschen kann nicht rückgängig gemacht werden.",
    );

    if (!bestaetigt) {
      return;
    }

    setDeleting(true);
    setError(null);

    try {
      await onDelete();
      onClose();
    } catch (err) {
      console.error("Fehler beim Löschen des Zählerstands:", err);
      setError("Der Zählerstand konnte nicht gelöscht werden.");
    } finally {
      setDeleting(false);
    }
  }

  return (
    <Dialog open={open} onClose={busy ? undefined : onClose} fullWidth maxWidth="sm">
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
            disabled={busy}
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
            disabled={busy}
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
            disabled={busy}
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
        {zaehlerstand && (
          <Button color="error" onClick={handleDelete} disabled={busy} sx={{ mr: "auto" }}>
            {deleting ? "Wird gelöscht …" : "Löschen"}
          </Button>
        )}

        <Button onClick={onClose} disabled={busy}>
          Abbrechen
        </Button>

        <Button variant="contained" onClick={handleSave} disabled={busy}>
          {saving ? "Wird gespeichert …" : "Speichern"}
        </Button>
      </DialogActions>
    </Dialog>
  );
}
