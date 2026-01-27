import React, { useState } from "react";
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Button,
  MenuItem,
  Stack,
} from "@mui/material";

import { PersonDetail } from "@/api/types/PersonDetail";

interface PersonCreateDialogProps {
  open: boolean;
  onClose: () => void;
  onCreate: (person: PersonDetail) => Promise<void>;
}

export const PersonCreateDialog: React.FC<PersonCreateDialogProps> = ({
  open,
  onClose,
  onCreate,
}) => {
  const [draft, setDraft] = useState<PersonDetail>({
    vorname: "",
    name: "",
    sex: "W",
    geburtsdatum: undefined,
    ort: "",
    aktiv: true, // 🔑 automatisch TRUE
    mitgliedschaften: [], // 🔑 leer beim Create
  });

  const update = <K extends keyof PersonDetail>(key: K, value: PersonDetail[K]) =>
    setDraft((d) => ({ ...d, [key]: value }));

  const handleCreate = async () => {
    await onCreate(draft);
    onClose();
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>Neue Person anlegen</DialogTitle>

      <DialogContent>
        <Stack spacing={2} mt={1}>
          <TextField
            label="Vorname"
            value={draft.vorname}
            onChange={(e) => update("vorname", e.target.value)}
            required
            fullWidth
          />

          <TextField
            label="Name"
            value={draft.name}
            onChange={(e) => update("name", e.target.value)}
            required
            fullWidth
          />

          <TextField
            select
            label="Geschlecht"
            value={draft.sex}
            onChange={(e) => update("sex", e.target.value as "M" | "W" | "D")}
            fullWidth
          >
            <MenuItem value="M">Männlich</MenuItem>
            <MenuItem value="W">Weiblich</MenuItem>
            <MenuItem value="D">Divers</MenuItem>
          </TextField>

          <TextField
            type="date"
            label="Geburtsdatum"
            value={draft.geburtsdatum ?? ""}
            onChange={(e) => update("geburtsdatum", e.target.value ? e.target.value : undefined)}
            InputLabelProps={{ shrink: true }}
            fullWidth
          />

          <TextField
            label="Ort"
            value={draft.ort ?? ""}
            onChange={(e) => update("ort", e.target.value)}
            fullWidth
          />
        </Stack>
      </DialogContent>

      <DialogActions>
        <Button onClick={onClose}>Abbrechen</Button>
        <Button variant="contained" onClick={handleCreate} disabled={!draft.vorname || !draft.name}>
          Anlegen
        </Button>
      </DialogActions>
    </Dialog>
  );
};
