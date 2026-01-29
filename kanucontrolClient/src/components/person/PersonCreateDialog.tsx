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

import { PersonSave } from "@/api/types/Person";

interface PersonCreateDialogProps {
  open: boolean;
  onClose: () => void;
  onCreate: (person: PersonSave) => Promise<void>;
}

export const PersonCreateDialog: React.FC<PersonCreateDialogProps> = ({
  open,
  onClose,
  onCreate,
}) => {
  console.log("🔥 NEUER PersonCreateDialog", {
    hasPlz: true,
    fields: ["vorname", "name", "sex", "geburtsdatum", "plz", "ort", "strasse"],
  });

  const [draft, setDraft] = useState<PersonSave>({
    vorname: "",
    name: "",
    sex: "W",
    geburtsdatum: undefined,
    plz: "",
    ort: "",
    strasse: "",
    aktiv: true, // 🔑 automatisch TRUE
    mitgliedschaften: [], // 🔑 leer beim Create
  });

  const update = <K extends keyof PersonSave>(key: K, value: PersonSave[K]) =>
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
            onChange={(e) => update("geburtsdatum", e.target.value || undefined)}
            InputLabelProps={{ shrink: true }}
            fullWidth
          />

          <TextField
            label="PLZ"
            value={draft.plz ?? ""}
            onChange={(e) => update("plz", e.target.value)}
            fullWidth
          />

          <TextField
            label="Ort"
            value={draft.ort ?? ""}
            onChange={(e) => update("ort", e.target.value)}
            fullWidth
          />

          <TextField
            label="Straße"
            value={draft.strasse ?? ""}
            onChange={(e) => update("strasse", e.target.value)}
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
