import React, { useEffect, useState } from "react";

import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Box,
  FormControlLabel,
  Checkbox,
} from "@mui/material";

import { PersonBaseForm } from "@/components/person/form/PersonBaseForm";
import { usePersonForm } from "@/components/person/hooks/usePersonForm";

import { PersonSave } from "@/api/types/Person";

/* =========================================================
   PROPS
   ========================================================= */

interface PersonCreateDialogProps {
  open: boolean;

  onClose: () => void;

  onCreate: (person: PersonSave, addToActiveVeranstaltung: boolean) => Promise<void>;

  initialData?: Partial<PersonSave>;

  showAddToVeranstaltung?: boolean;
}

/* =========================================================
   COMPONENT
   ========================================================= */

export const PersonCreateDialog: React.FC<PersonCreateDialogProps> = ({
  open,
  onClose,
  onCreate,
  initialData,
  showAddToVeranstaltung = false,
}) => {
  const { form, update, reset, buildSavePayload } = usePersonForm(undefined);

  const [addToActiveVeranstaltung, setAddToActiveVeranstaltung] = useState(false);

  /* =========================================================
     RESET / PREFILL
     ========================================================= */

  useEffect(() => {
    if (!open) {
      return;
    }

    reset({
      vorname: "",
      name: "",
      sex: "W",
      aktiv: true,

      mitgliedschaften: [],

      ...initialData,

      // Optional bewusst leer lassen
      email: undefined,
      telefon: undefined,
    });

    setAddToActiveVeranstaltung(false);

    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [open, initialData]);

  /* =========================================================
     RENDER
     ========================================================= */

  if (!form) {
    return null;
  }

  return (
    <Dialog open={open} onClose={onClose} maxWidth="xl" fullWidth>
      <DialogTitle>{initialData ? "Person kopieren" : "Neue Person anlegen"}</DialogTitle>

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

        {/* =====================================================
            AKTIVE VERANSTALTUNG
            ===================================================== */}

        {showAddToVeranstaltung && (
          <Box sx={{ mt: 3 }}>
            <FormControlLabel
              control={
                <Checkbox
                  checked={addToActiveVeranstaltung}
                  onChange={(e) => setAddToActiveVeranstaltung(e.target.checked)}
                />
              }
              label="Direkt Teilnehmer der aktiven Veranstaltung werden"
            />
          </Box>
        )}
      </DialogContent>

      <DialogActions>
        <Button onClick={onClose}>Abbrechen</Button>

        <Button
          variant="contained"
          disabled={!form.vorname || !form.name}
          onClick={async () => {
            const payload = buildSavePayload();

            if (!payload) {
              return;
            }
        
            await onCreate(payload, addToActiveVeranstaltung);

            onClose();
          }}
        >
          {initialData ? "Kopieren" : "Anlegen"}
        </Button>
      </DialogActions>
    </Dialog>
  );
};
