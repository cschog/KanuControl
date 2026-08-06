import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Divider,
  Button,
  TextField,
  Stack,
  MenuItem,
  Typography,
  List,
  ListItem,
  ListItemText,
  Box,
} from "@mui/material";
import { useState, useEffect, useRef } from "react";

// import PhotoCameraIcon from "@mui/icons-material/PhotoCamera";
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
  ) => Promise<void>;
}

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
          <Typography variant="h6">Dokumente</Typography>

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

          <Typography variant="body2" color="text.secondary">
            PDF, Fotos oder Scans können direkt hinzugefügt werden.
          </Typography>

          <input
            hidden
            disabled={saving}
            ref={fileInputRef}
            type="file"
            multiple
            accept="image/*,.pdf"
            capture="environment"
            onChange={(e) => {
              const neueDateien = Array.from(e.target.files ?? []);

              setFiles((old) => [...old, ...neueDateien]);

              e.target.value = "";
            }}
          />

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

          <Typography variant="h6">Beleg</Typography>
          {/* ================= BELEG ================= */}

          <TextField
            select
            fullWidth
            label="Finanzgruppe"
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

          <Typography
            variant="h6"
            sx={{
              mt: 1,
            }}
          >
            Erste Buchung
          </Typography>

          {/* ================= BUCHUNG ================= */}
          <Stack direction={{ xs: "column", md: "row" }} spacing={2}>
            <Box flex={1}>
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
            </Box>

            <Box flex={1}>
              <TextField
                type="number"
                fullWidth
                label="Betrag (€)"
                value={betrag}
                onChange={(e) => setBetrag(e.target.value)}
                required
                inputProps={{ min: 0, step: "0.01" }}
              />
            </Box>

            <Box flex={1}>
              <TextField
                fullWidth
                label="Beschreibung (Buchung)"
                value={buchungText}
                onChange={(e) => setBuchungText(e.target.value)}
              />
            </Box>
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
