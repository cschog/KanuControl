import React, { useEffect } from "react";
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Box,
} from "@mui/material";

import { PersonBaseForm } from "@/components/person/form/PersonBaseForm";
import { usePersonForm } from "@/components/person/hooks/usePersonForm";
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
  const { form, update, reset, buildSavePayload } = usePersonForm(undefined);

  useEffect(() => {
    if (open) {
      reset({
        vorname: "",
        name: "",
        sex: "W",
        aktiv: true,
        mitgliedschaften: [],
      });
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [open]);

  if (!form) return null;

  return (
    <Dialog open={open} onClose={onClose} maxWidth="xl" fullWidth>
      <DialogTitle>Neue Person anlegen</DialogTitle>

      <DialogContent sx={{ mt: 2 }}>
        <Box
          display="grid"
          gridTemplateColumns={{
            xs: "1fr",
            sm: "repeat(2, 1fr)",
            md: "repeat(4, 1fr)",
          }}
          gap={2}
        >
          <PersonBaseForm form={form} editMode={true} mode="create" onChange={update} />
        </Box>
      </DialogContent>

      <DialogActions>
        <Button onClick={onClose}>Abbrechen</Button>
        <Button
          variant="contained"
          disabled={!form.vorname || !form.name}
          onClick={async () => {
            const payload = buildSavePayload();
            if (!payload) return;

            await onCreate(payload);
            onClose();
          }}
        >
          Anlegen
        </Button>
      </DialogActions>
    </Dialog>
  );
};