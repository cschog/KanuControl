import React, { useEffect } from "react";
import { Dialog, DialogTitle, DialogContent, DialogActions, Button, Box } from "@mui/material";

import { VeranstaltungBaseForm } from "@/components/veranstaltung/form/VeranstaltungBaseForm";
import { useVeranstaltungForm } from "@/components/veranstaltung/hook/useVeranstaltungForm";
import { VeranstaltungSave } from "@/api/types/veranstaltung/VeranstaltungSave";
import { VeranstaltungFormModel } from "@/api/types/veranstaltung/VeranstaltungFormModel";

interface BeitragsstrukturDTO {
  id: number;
  name: string;
}

interface Props {
  open: boolean;

  onClose: () => void;
  onCreate: (data: VeranstaltungSave) => Promise<void>;

  beitragsstrukturen: BeitragsstrukturDTO[];
  initialData?: Partial<VeranstaltungFormModel>;
}

export const VeranstaltungCreateDialog: React.FC<Props> = ({
  open,
  onClose,
  onCreate,
  beitragsstrukturen,
  initialData,
}) => {
  const { form, update, reset, buildSavePayload, isValid } = useVeranstaltungForm(null);

  // Dialog öffnen → Form reset
  useEffect(() => {
    if (open) {
      reset(initialData);
    }

    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [open]);

  if (!form) return null;

  return (
    <Dialog open={open} onClose={onClose} maxWidth="lg" fullWidth>
      <DialogTitle>Neue Veranstaltung anlegen</DialogTitle>

      <DialogContent sx={{ mt: 2 }}>
        <Box
          display="grid"
          gridTemplateColumns={{
            xs: "1fr",
            sm: "repeat(2, 1fr)",
            lg: "repeat(4, 1fr)",
          }}
          gap={2}
        >
          <VeranstaltungBaseForm
            form={form}
            editMode={true}
            beitragsstrukturen={beitragsstrukturen}
            onChange={update}
          />
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
