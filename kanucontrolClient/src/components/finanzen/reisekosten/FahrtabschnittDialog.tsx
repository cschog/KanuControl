// src/components/finanzen/reisekosten/FahrtabschnittDialog.tsx

import { useEffect, useState } from "react";

import {
  Button,
  Checkbox,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  FormControlLabel,
  Paper,
  Stack,
  TextField,
} from "@mui/material";

import { PersonRef } from "@/api/types/PersonRef";

import { FahrtabschnittRequest } from "@/api/types/Reisekostenabrechnung";

interface Props {
  open: boolean;
  veranstaltungId: number;
  mitfahrerPool: PersonRef[];
  initialData?: FahrtabschnittRequest | null;
  onClose: () => void;
  onSave: (dto: FahrtabschnittRequest) => void;
}

export default function FahrtabschnittDialog({
  open,
  mitfahrerPool,
  initialData,
  onClose,
  onSave,
}: Props) {
  const [beschreibung, setBeschreibung] = useState("");

  const [vonOrt, setVonOrt] = useState("");
  const [nachOrt, setNachOrt] = useState("");
  const [kilometer, setKilometer] = useState<number | "">("");
  const [anhaenger, setAnhaenger] = useState(false);

 const [mitfahrerIds, setMitfahrerIds] = useState<number[]>([]);

  useEffect(() => {
    if (!initialData) {
      setBeschreibung("");
      setVonOrt("");
      setNachOrt("");
      setKilometer("");
      setAnhaenger(false);

      setMitfahrerIds([]);

      return;
    }

    setBeschreibung(initialData.beschreibung ?? "");
    setVonOrt(initialData.vonOrt ?? "");
    setNachOrt(initialData.nachOrt ?? "");
    setKilometer(initialData.kilometer ?? "");
    setAnhaenger(initialData.anhaenger);

    setMitfahrerIds(initialData.mitfahrerIds ?? []);
  }, [initialData, open]);

  const handleSave = () => {
    onSave({
      id: initialData?.id ?? -Date.now(),
      reihenfolge: initialData?.reihenfolge ?? 1,
      beschreibung,
      vonPlz: "",
      vonOrt,
      vonCountryCode: null,
      nachPlz: "",
      nachOrt,
      nachCountryCode: null,
      kilometer: Number(kilometer),
      anhaenger,
      mitfahrerIds,
    });

    onClose();
  };

  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="md">
      <DialogTitle>Fahrtabschnitt</DialogTitle>

      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          <TextField
            label="Beschreibung"
            value={beschreibung}
            onChange={(e) => setBeschreibung(e.target.value)}
          />

          <TextField label="Von Ort" value={vonOrt} onChange={(e) => setVonOrt(e.target.value)} />

          <TextField
            label="Nach Ort"
            value={nachOrt}
            onChange={(e) => setNachOrt(e.target.value)}
          />

          <TextField
            label="Kilometer"
            type="number"
            value={kilometer}
            onChange={(e) => setKilometer(e.target.value === "" ? "" : Number(e.target.value))}
          />

          <FormControlLabel
            control={
              <Checkbox checked={anhaenger} onChange={(e) => setAnhaenger(e.target.checked)} />
            }
            label="Anhänger mitgeführt"
          />

          <Paper sx={{ p: 2 }}>
            <Stack spacing={1}>
              {mitfahrerPool.map((person) => (
                <FormControlLabel
                  key={person.id}
                  control={
                    <Checkbox
                      checked={mitfahrerIds.includes(person.id)}
                      onChange={(e) => {
                        if (e.target.checked) {
                          setMitfahrerIds((current) => [...current, person.id]);
                        } else {
                          setMitfahrerIds((current) => current.filter((id) => id !== person.id));
                        }
                      }}
                    />
                  }
                  label={`${person.vorname} ${person.name}`}
                />
              ))}
            </Stack>
          </Paper>
        </Stack>
      </DialogContent>

      <DialogActions>
        <Button onClick={onClose}>Abbrechen</Button>

        <Button
          variant="contained"
          onClick={handleSave}
          disabled={!vonOrt || !nachOrt || kilometer === ""}
        >
          Speichern
        </Button>
      </DialogActions>
    </Dialog>
  );
}
