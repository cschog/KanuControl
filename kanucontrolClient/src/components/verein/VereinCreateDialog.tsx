import React, { useEffect } from "react";
import { Dialog, DialogTitle, DialogContent, DialogActions, Button, Box } from "@mui/material";

import { VereinBaseForm } from "@/components/verein/form/VereinBaseForm";
import { useVereinForm } from "@/components/verein/hooks/useVereinForm";
import { VereinSave } from "@/api/types/VereinSave";

interface VereinCreateDialogProps {
  open: boolean;
  onClose: () => void;
  onCreate: (verein: VereinSave) => Promise<void>;
}

export const VereinCreateDialog: React.FC<VereinCreateDialogProps> = ({
  open,
  onClose,
  onCreate,
}) => {
  const { form, update, reset, buildSavePayload, isValid } = useVereinForm(null);

  useEffect(() => {
    if (open) reset();
  }, [open, reset]);

  if (!form) return null;

  return (
    <Dialog open={open} onClose={onClose} maxWidth="xl" fullWidth>
      <DialogTitle>Neuen Verein anlegen</DialogTitle>

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
          <VereinBaseForm form={form} editMode={true} mode="create" onChange={update} />
        </Box>
      </DialogContent>

      <DialogActions>
        <Button onClick={onClose}>Abbrechen</Button>
        <Button
          variant="contained"
          disabled={!isValid}
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
