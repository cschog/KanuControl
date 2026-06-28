import React from "react";
import {
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  DialogActions,
  Stack,
} from "@mui/material";
import { BtnEditDeleteBack } from "@/components/common/BtnEditDeleteBack";
import { PersonDetail } from "@/api/types/person/Person";

interface ButtonNeuePersonProps {
  onNeuePerson: () => void;
  btnNeuePerson: boolean;

  visible: boolean;
  setVisible: React.Dispatch<React.SetStateAction<boolean>>;

  selectedPerson: PersonDetail | null;

  accept: () => void;
  reject: () => void;

  onÄndernPerson: () => void;
  btnÄndernPerson: boolean;
  btnLöschenPerson: boolean;

  onStartMenue: () => void;
}

export function ButtonNeuePerson({
  onNeuePerson,
  btnNeuePerson,
  visible,
  setVisible,
  selectedPerson,
  accept,
  reject,
  onÄndernPerson,
  btnÄndernPerson,
  btnLöschenPerson,
  onStartMenue,
}: ButtonNeuePersonProps) {
  return (
    <Stack spacing={2}>
      {/* ➕ Neue Person */}
      <Button
        variant="outlined"
        onClick={onNeuePerson}
        disabled={btnNeuePerson}
      >
        Neue Person
      </Button>

      {/* 🗑️ Löschen-Dialog */}
      <Dialog
        open={visible}
        onClose={() => setVisible(false)}
      >
        <DialogTitle>
          {selectedPerson
            ? `Löschen der Person ${selectedPerson.name}?`
            : "Löschen"}
        </DialogTitle>

        <DialogContent>
          <DialogContentText>
            {selectedPerson
              ? `Soll die Person ${selectedPerson.name} wirklich gelöscht werden?`
              : ""}
          </DialogContentText>
        </DialogContent>

        <DialogActions>
          <Button onClick={reject} color="inherit">
            Abbrechen
          </Button>
          <Button onClick={accept} color="error" variant="contained">
            Löschen
          </Button>
        </DialogActions>
      </Dialog>

      {/* ✏️ Ändern / Löschen / Zurück */}
      <BtnEditDeleteBack
        onÄndern={onÄndernPerson}
        btnÄndern={btnÄndernPerson}
        setVisible={setVisible}
        btnLöschen={btnLöschenPerson}
        onStartMenue={onStartMenue}
      />
    </Stack>
  );
}