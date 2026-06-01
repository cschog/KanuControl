// MitfahrerMehrereAbschnitteDialog.tsx

import { useState } from "react";

import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  MenuItem,
  Stack,
  TextField,
} from "@mui/material";

import { PersonRef } from "@/api/types/PersonRef";

import { ReisekostenMitfahrerAutocomplete } from "./ReisekostenMitfahrerAutocomplete";

interface Abschnitt {
  reihenfolge: number;
  vonOrt: string;
  nachOrt: string;
}

interface Props {
  open: boolean;

  veranstaltungId: number;

  abschnitte: Abschnitt[];

  onClose: () => void;

  onSave: (person: PersonRef, startReihenfolge: number, endReihenfolge: number) => void;
}

export default function MitfahrerMehrereAbschnitteDialog({
  open,
  veranstaltungId,
  abschnitte,
  onClose,
  onSave,
}: Props) {
  const [person, setPerson] = useState<PersonRef>();

  const [start, setStart] = useState<number>();

  const [ende, setEnde] = useState<number>();

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>Mitfahrer für mehrere Abschnitte</DialogTitle>

      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          <ReisekostenMitfahrerAutocomplete
            veranstaltungId={veranstaltungId}
            value={person}
            onChange={setPerson}
          />

          <TextField
            select
            label="Von Abschnitt"
            value={start ?? ""}
            onChange={(e) => setStart(Number(e.target.value))}
          >
            {abschnitte.map((a) => (
              <MenuItem key={a.reihenfolge} value={a.reihenfolge}>
                {a.reihenfolge} - {a.vonOrt} → {a.nachOrt}
              </MenuItem>
            ))}
          </TextField>

          <TextField
            select
            label="Bis Abschnitt"
            value={ende ?? ""}
            onChange={(e) => setEnde(Number(e.target.value))}
          >
            {abschnitte.map((a) => (
              <MenuItem key={a.reihenfolge} value={a.reihenfolge}>
                {a.reihenfolge} - {a.vonOrt} → {a.nachOrt}
              </MenuItem>
            ))}
          </TextField>
        </Stack>
      </DialogContent>

      <DialogActions>
        <Button onClick={onClose}>Abbrechen</Button>

        <Button
          variant="contained"
          disabled={!person || start == null || ende == null}
          onClick={() => {
            if (person && start != null && ende != null) {
              onSave(person, start, ende);
            }
          }}
        >
          Übernehmen
        </Button>
      </DialogActions>
    </Dialog>
  );
}
