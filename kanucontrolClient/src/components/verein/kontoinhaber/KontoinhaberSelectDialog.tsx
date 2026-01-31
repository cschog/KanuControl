// src/components/verein/kontoinhaber/KontoinhaberSelectDialog.tsx
import React, { useEffect, useState } from "react";
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Autocomplete,
  TextField,
} from "@mui/material";
import apiClient from "@/api/client/apiClient";
import { PersonRef } from "@/api/types/PersonRef";

interface Props {
  open: boolean;
  onClose: () => void;
  onSelect: (person: PersonRef | undefined) => void;
}

export const KontoinhaberSelectDialog: React.FC<Props> = ({ open, onClose, onSelect }) => {
  const [personen, setPersonen] = useState<PersonRef[]>([]);
  const [value, setValue] = useState<PersonRef | null>(null);

  useEffect(() => {
    if (open) {
      apiClient.get<PersonRef[]>("/person").then((res) => setPersonen(res.data));
      setValue(null);
    }
  }, [open]);

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>Kontoinhaber auswählen</DialogTitle>

      <DialogContent sx={{ mt: 2 }}>
        <Autocomplete
          options={personen}
          value={value}
          onChange={(_, v) => setValue(v)}
          getOptionLabel={(p) => `${p.name}, ${p.vorname}`}
          renderInput={(params) => <TextField {...params} label="Person" size="small" />}
        />
      </DialogContent>

      <DialogActions>
        <Button onClick={onClose}>Abbrechen</Button>
        <Button
          variant="contained"
          disabled={!value}
          onClick={() => {
            onSelect(value ?? undefined);
            onClose();
          }}
        >
          Übernehmen
        </Button>
      </DialogActions>
    </Dialog>
  );
};
