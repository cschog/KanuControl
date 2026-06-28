// src/components/finanzen/reisekosten/ReisekostenCreateDialog.tsx

import { useState } from "react";
import { ReisekostenPersonAutocomplete } from "@/components/finanzen/reisekosten/ReisekostenPersonAutocomplete";
import { PersonRef } from "@/api/types/person/PersonRef";

import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Stack,
  TextField,
} from "@mui/material";

import { DatePicker } from "@mui/x-date-pickers";

import dayjs, { Dayjs } from "dayjs";

interface Props {
  open: boolean;

  veranstaltungId: number;

  onClose: () => void;

  onSave: (fahrerId: number, abrechnungsdatum: string, bemerkung: string) => void;
}

export default function ReisekostenCreateDialog({ open, veranstaltungId, onClose, onSave }: Props) {
  const [fahrer, setFahrer] = useState<PersonRef | undefined>();

  const [abrechnungsdatum, setAbrechnungsdatum] = useState<Dayjs | null>(dayjs());

  const [bemerkung, setBemerkung] = useState("");

  const handleSave = () => {
    if (!fahrer || !abrechnungsdatum) {
      return;
    }

    onSave(fahrer.id, abrechnungsdatum.format("YYYY-MM-DD"), bemerkung);
  };

  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="sm">
      <DialogTitle>Neue Fahrkostenabrechnung</DialogTitle>

      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          <ReisekostenPersonAutocomplete
            veranstaltungId={veranstaltungId}
            label="Fahrer"
            value={fahrer}
            onChange={setFahrer}
          />

          <DatePicker
            label="Abrechnungsdatum"
            value={abrechnungsdatum}
            onChange={setAbrechnungsdatum}
          />

          <TextField
            label="Bemerkung"
            multiline
            minRows={3}
            value={bemerkung}
            onChange={(e) => setBemerkung(e.target.value)}
          />
        </Stack>
      </DialogContent>

      <DialogActions>
        <Button onClick={onClose}>Abbrechen</Button>

        <Button variant="contained" onClick={handleSave}>
          Anlegen
        </Button>
      </DialogActions>
    </Dialog>
  );
}
