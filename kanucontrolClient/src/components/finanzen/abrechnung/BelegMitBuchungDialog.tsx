import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Stack,
  Typography,
  Radio,
  RadioGroup,
  FormControlLabel,
  List,
  ListItem,
  ListItemText,
  MenuItem,
  Divider,
  TextField,
} from "@mui/material";

import { ReferenzObjekt } from "@/api/enums/ReferenzObjekt";
import { useState, useEffect, useRef } from "react";

import UploadFileIcon from "@mui/icons-material/UploadFile";

import { kategorieZuTyp, FinanzKategorie } from "@/api/types/finanz";
import { BelegCreate, BuchungCreate } from "@/api/types/abrechnung";

interface Props {
  open: boolean;
  kuerzelListe: string[];
  onClose: () => void;
  onSave: (
    data: {
      beleg: BelegCreate;
      buchung: BuchungCreate;
    },
    files: File[],
    referenzObjekt: ReferenzObjekt,
  ) => Promise<void>;
}

const REFERENZ_STORAGE_KEY = "kanucontrol.dokument.referenzObjekt";

export default function BelegMitBuchungDialog({ open, kuerzelListe, onClose, onSave }: Props) {
  const [kuerzel, setKuerzel] = useState("");
  const [datum, setDatum] = useState("");
  const [beschreibung, setBeschreibung] = useState("");

  const [kategorie, setKategorie] = useState<FinanzKategorie>("VERPFLEGUNG");
  const [betrag, setBetrag] = useState("");
  const [buchungText, setBuchungText] = useState("");

  const [aussteller, setAussteller] = useState("");
  const [externeBelegnummer, setExterneBelegnummer] = useState("");

  const [files, setFiles] = useState<File[]>([]);
  const fileInputRef = useRef<HTMLInputElement>(null);

  const [saving, setSaving] = useState(false);

  const [referenzObjekt, setReferenzObjekt] = useState<ReferenzObjekt>(() => {
    const gespeichert = localStorage.getItem(REFERENZ_STORAGE_KEY);

    if (gespeichert && Object.values(ReferenzObjekt).includes(gespeichert as ReferenzObjekt)) {
      return gespeichert as ReferenzObjekt;
    }

    return ReferenzObjekt.DIN_A6;
  });

  useEffect(() => {
    if (open) {
      setDatum(new Date().toISOString().slice(0, 10));
    } else {
      setKuerzel("");
      setDatum("");
      setBeschreibung("");
      setKategorie("VERPFLEGUNG");
      setBetrag("");
      setBuchungText("");
      setAussteller("");
      setExterneBelegnummer("");
      setFiles([]);
    }
  }, [open]);

  const isValid =
    kuerzel.trim() !== "" && datum.trim() !== "" && betrag !== "" && Number(betrag) > 0;

  const handleSave = async () => {
    if (!isValid || saving) {
      return;
    }

    setSaving(true);

    try {
      await onSave(
        {
          beleg: {
            kuerzel,
            datum,
            aussteller,
            externeBelegnummer,
            beschreibung,
          },
          buchung: {
            kategorie,
            betrag: Number(betrag),
            beschreibung: buchungText,
          },
        },
        files,
        referenzObjekt,
      );
    } finally {
      setSaving(false);
    }
  };

  return (
    <Dialog open={open} onClose={saving ? undefined : onClose} fullWidth maxWidth="md">
      <DialogTitle>
        <Typography variant="h6">Beleg + erste Buchung</Typography>
      </DialogTitle>

      <DialogContent>
        <Stack spacing={3} mt={1}>
          {/* ================= DOKUMENTE ================= */}

          <Stack
            direction={{ xs: "column", sm: "row" }}
            justifyContent="space-between"
            alignItems={{ xs: "stretch", sm: "center" }}
            spacing={1}
          >
            <Typography variant="h6">Dokumente</Typography>

            <Stack
              direction={{ xs: "column", sm: "row" }}
              alignItems={{ xs: "flex-start", sm: "center" }}
              spacing={1}
            >
              <RadioGroup
                row
                value={referenzObjekt}
                onChange={(event) => {
                  const value = event.target.value as ReferenzObjekt;

                  setReferenzObjekt(value);

                  localStorage.setItem(REFERENZ_STORAGE_KEY, value);
                }}
              >
                <FormControlLabel
                  value={ReferenzObjekt.DIN_A7}
                  control={<Radio size="small" />}
                  label="A7"
                />

                <FormControlLabel
                  value={ReferenzObjekt.DIN_A6}
                  control={<Radio size="small" />}
                  label="A6"
                />

                <FormControlLabel
                  value={ReferenzObjekt.DIN_A5}
                  control={<Radio size="small" />}
                  label="A5"
                />

                <FormControlLabel
                  value={ReferenzObjekt.DIN_A4}
                  control={<Radio size="small" />}
                  label="A4"
                />
              </RadioGroup>

              <Button
                variant="outlined"
                startIcon={<UploadFileIcon />}
                onClick={() => fileInputRef.current?.click()}
                disabled={saving}
              >
                {files.length === 0
                  ? "Dokumente hinzufügen"
                  : `${files.length} Dokument${files.length === 1 ? "" : "e"} ausgewählt`}
              </Button>

              <input
                hidden
                disabled={saving}
                ref={fileInputRef}
                type="file"
                accept="image/*,.pdf"
                multiple
                onChange={(e) => {
                  const neueDateien = Array.from(e.target.files ?? []);

                  setFiles((old) => [...old, ...neueDateien]);

                  e.target.value = "";
                }}
              />
            </Stack>
          </Stack>

          {files.length > 0 && (
            <List dense disablePadding>
              {files.map((file, index) => (
                <ListItem
                  key={`${file.name}-${index}`}
                  secondaryAction={
                    <Button
                      size="small"
                      color="error"
                      disabled={saving}
                      onClick={() => setFiles((old) => old.filter((_, i) => i !== index))}
                    >
                      Entfernen
                    </Button>
                  }
                >
                  <ListItemText
                    primary={file.name}
                    secondary={`${Math.round(file.size / 1024)} KB`}
                  />
                </ListItem>
              ))}
            </List>
          )}

          <Divider />

          {/* ================= BELEG ================= */}

          <Typography variant="h6">Beleg</Typography>

          <TextField
            select
            fullWidth
            label="Konto"
            value={kuerzel}
            onChange={(e) => setKuerzel(e.target.value)}
            required
          >
            {kuerzelListe.map((k) => (
              <MenuItem key={k} value={k}>
                {k}
              </MenuItem>
            ))}
          </TextField>

          <Stack direction={{ xs: "column", md: "row" }} spacing={2}>
            <TextField
              type="date"
              fullWidth
              label="Datum"
              InputLabelProps={{ shrink: true }}
              value={datum}
              onChange={(e) => setDatum(e.target.value)}
              required
            />

            <TextField
              fullWidth
              label="Aussteller"
              value={aussteller}
              onChange={(e) => setAussteller(e.target.value)}
            />
          </Stack>

          <Stack direction={{ xs: "column", md: "row" }} spacing={2}>
            <TextField
              fullWidth
              label="Externe Belegnummer"
              value={externeBelegnummer}
              onChange={(e) => setExterneBelegnummer(e.target.value)}
            />

            <TextField
              fullWidth
              label="Beschreibung"
              value={beschreibung}
              onChange={(e) => setBeschreibung(e.target.value)}
            />
          </Stack>

          <Divider />

          {/* ================= ERSTE BUCHUNG ================= */}

          <Typography variant="h6">Erste Buchung</Typography>

          <Stack direction={{ xs: "column", md: "row" }} spacing={2}>
            <TextField
              select
              fullWidth
              label="Kategorie"
              value={kategorie}
              onChange={(e) => setKategorie(e.target.value as FinanzKategorie)}
              required
            >
              {Object.keys(kategorieZuTyp).map((k) => {
                const key = k as FinanzKategorie;

                return (
                  <MenuItem key={key} value={key}>
                    {key.replaceAll("_", " ")}
                  </MenuItem>
                );
              })}
            </TextField>

            <TextField
              type="number"
              fullWidth
              label="Betrag (€)"
              value={betrag}
              onChange={(e) => setBetrag(e.target.value)}
              required
              inputProps={{
                min: 0,
                step: "0.01",
              }}
            />

            <TextField
              fullWidth
              label="Beschreibung (Buchung)"
              value={buchungText}
              onChange={(e) => setBuchungText(e.target.value)}
            />
          </Stack>
        </Stack>
      </DialogContent>

      <DialogActions>
        <Button onClick={onClose} disabled={saving}>
          Abbrechen
        </Button>

        <Button variant="contained" onClick={handleSave} disabled={!isValid || saving}>
          {saving ? "Speichert..." : "Speichern"}
        </Button>
      </DialogActions>
    </Dialog>
  );
}
