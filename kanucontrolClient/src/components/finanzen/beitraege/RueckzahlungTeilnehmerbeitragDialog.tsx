// src/components/finanzen/beitraege/RueckzahlungTeilnehmerbeitragDialog.tsx

import {
  Alert,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Stack,
  TextField,
  Typography,
  FormControl,
  InputLabel,
  Select,
  MenuItem,
} from "@mui/material";

import { useEffect, useState } from "react";

import { OffeneUeberzahlungDTO } from "@/api/types/beitraege";
import { RueckzahlungTeilnehmerbeitragCreate } from "@/api/types/abrechnung";
import { Zahlungsweg } from "@/api/types/beitraege";

interface Props {
  open: boolean;

  ueberzahlung: OffeneUeberzahlungDTO | null;

  onClose: () => void;

  onSave: (data: RueckzahlungTeilnehmerbeitragCreate) => void | Promise<void>;
}

export default function RueckzahlungTeilnehmerbeitragDialog({
  open,
  ueberzahlung,
  onClose,
  onSave,
}: Props) {
  const [betrag, setBetrag] = useState("");
  const [beschreibung, setBeschreibung] = useState("");
  const [saving, setSaving] = useState(false);
  const [zahlungsweg, setZahlungsweg] = useState<Zahlungsweg | "">("");

  /* =========================================================
     RESET
     ========================================================= */

  useEffect(() => {
    if (open) {
      setBetrag("");
      setBeschreibung("");
      setZahlungsweg("");
    }
  }, [open, ueberzahlung]);

  /* =========================================================
     VALIDIERUNG
     ========================================================= */

  const rueckzahlungsbetrag = Number(betrag);

  const isValid =
    ueberzahlung !== null &&
    betrag.trim() !== "" &&
    Number.isFinite(rueckzahlungsbetrag) &&
    rueckzahlungsbetrag > 0 &&
    rueckzahlungsbetrag <= ueberzahlung.offeneUeberzahlung &&
    zahlungsweg !== "";

  /* =========================================================
     SAVE
     ========================================================= */

  const handleSave = async () => {
    if (!isValid || saving || !ueberzahlung) {
      return;
    }

    setSaving(true);

    try {
      await onSave({
        zahlungsnachweisId: ueberzahlung.zahlungsnachweisId,
        betrag: rueckzahlungsbetrag,
        beschreibung: beschreibung.trim() || undefined,
        zahlungsweg,
      });
    } finally {
      setSaving(false);
    }
  };

  if (!ueberzahlung) {
    return null;
  }

  /* =========================================================
     RENDER
     ========================================================= */

  return (
    <Dialog open={open} onClose={saving ? undefined : onClose} fullWidth maxWidth="sm">
      <DialogTitle sx={{ fontWeight: 700 }}>Teilnehmerbeitrag zurückzahlen</DialogTitle>

      <DialogContent>
        <Stack spacing={2} mt={1}>
          <Typography variant="body2" color="text.secondary">
            Für die Rückzahlung wird eine neue Abrechnungsbuchung erstellt und der ursprünglichen
            Zahlung zugeordnet.
          </Typography>

          {/* ================= ZAHLUNG ================= */}

          <Stack spacing={0.5}>
            <Typography variant="body2" color="text.secondary">
              Ursprüngliche Zahlung
            </Typography>

            <Typography fontWeight={600}>
              {new Date(ueberzahlung.datum).toLocaleDateString("de-DE")}
              {" – "}
              {ueberzahlung.urspruenglicherBetrag.toFixed(2)} €
            </Typography>

            {ueberzahlung.bemerkung && (
              <Typography variant="body2" color="text.secondary">
                {ueberzahlung.bemerkung}
              </Typography>
            )}
          </Stack>
          {/* ================= OFFENER BETRAG ================= */}

          <Alert severity="info">
            <Stack spacing={0.5}>
              <Typography>
                Noch offen zur Rückzahlung:{" "}
                <strong>{ueberzahlung.offeneUeberzahlung.toFixed(2)} €</strong>
              </Typography>

              <Typography>
                Konto der Rückzahlung: <strong>{ueberzahlung.finanzGruppeKuerzel ?? "-"}</strong>
              </Typography>
            </Stack>
          </Alert>

          {/* ================= BETRAG ================= */}

          <TextField
            type="number"
            fullWidth
            required
            label="Rückzahlungsbetrag (€)"
            value={betrag}
            disabled={saving}
            onChange={(e) => setBetrag(e.target.value)}
            inputProps={{
              min: 0.01,
              max: ueberzahlung.offeneUeberzahlung,
              step: "0.01",
            }}
            error={
              betrag !== "" &&
              (!Number.isFinite(rueckzahlungsbetrag) ||
                rueckzahlungsbetrag <= 0 ||
                rueckzahlungsbetrag > ueberzahlung.offeneUeberzahlung)
            }
            helperText={
              betrag !== "" && rueckzahlungsbetrag > ueberzahlung.offeneUeberzahlung
                ? `Maximal ${ueberzahlung.offeneUeberzahlung.toFixed(2)} €`
                : undefined
            }
          />

          <FormControl fullWidth required disabled={saving} error={zahlungsweg === ""}>
            <InputLabel>Zahlungsweg</InputLabel>

            <Select
              value={zahlungsweg}
              label="Zahlungsweg"
              onChange={(e) => setZahlungsweg(e.target.value as Zahlungsweg)}
            >
              <MenuItem value="QUITTUNG">Quittung</MenuItem>
              <MenuItem value="UEBERWEISUNG">Überweisung</MenuItem>
            </Select>
          </FormControl>

          {/* ================= BESCHREIBUNG ================= */}

          <TextField
            fullWidth
            label="Beschreibung"
            value={beschreibung}
            disabled={saving}
            onChange={(e) => setBeschreibung(e.target.value)}
            multiline
            minRows={2}
          />
        </Stack>
      </DialogContent>

      <DialogActions>
        <Button onClick={onClose} disabled={saving}>
          Abbrechen
        </Button>

        <Button
          variant="contained"
          color="warning"
          onClick={handleSave}
          disabled={!isValid || saving}
        >
          {saving ? "Speichert..." : "Rückzahlung buchen"}
        </Button>
      </DialogActions>
    </Dialog>
  );
}
