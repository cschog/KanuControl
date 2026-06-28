import { useState } from "react";

import { Button, Dialog, DialogActions, DialogContent, DialogTitle } from "@mui/material";

import { PersonRef } from "@/api/types/person/PersonRef";
import { PersonAutocomplete } from "@/components/person/PersonAutocomplete";

interface Props {
  open: boolean;
  onSelect: (p: PersonRef) => void;
  onClose: () => void;
}

export function KontoinhaberDialog({ open, onSelect, onClose }: Props) {
  const [person, setPerson] = useState<PersonRef>();

  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="sm">
      <DialogTitle>Kontoinhaber auswählen</DialogTitle>

      <DialogContent>
        <PersonAutocomplete label="Kontoinhaber" value={person} onChange={setPerson} />
      </DialogContent>

      <DialogActions>
        <Button onClick={onClose}>Abbrechen</Button>

        <Button
          variant="contained"
          disabled={!person}
          onClick={() => {
            if (!person) {
              return;
            }

            onSelect(person);
            onClose();
          }}
        >
          Übernehmen
        </Button>
      </DialogActions>
    </Dialog>
  );
}
