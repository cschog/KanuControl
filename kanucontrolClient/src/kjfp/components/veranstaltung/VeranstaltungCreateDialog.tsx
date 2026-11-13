import { useVeranstaltungNavigationGuard } from "./hook/useVeranstaltungNavigationGuard";
import React, { useEffect, useState } from "react";
import { Dialog, DialogTitle, DialogContent, DialogActions, Button, Alert } from "@mui/material";

import { VeranstaltungSections } from "./form/VeranstaltungSections";
import { useVeranstaltungForm } from "@/kjfp/components/veranstaltung/hook/useVeranstaltungForm";
import { VeranstaltungSave } from "@/kjfp/types/veranstaltung/VeranstaltungSave";
import { VeranstaltungFormModel } from "@/kjfp/types/veranstaltung/VeranstaltungFormModel";

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
  const { form, update, reset, buildSavePayload, isValid, validationErrors, isDirty } = useVeranstaltungForm(null);

  useVeranstaltungNavigationGuard(open && isDirty);
  const [saving, setSaving] = useState(false);
  const [errorAttempt, setErrorAttempt] = useState(0);
  const close = () => { if (saving) return; if (!isDirty || window.confirm("Ungespeicherte Änderungen verwerfen?")) onClose(); };

  // Dialog öffnen → Form reset
  useEffect(() => {
    if (open) {
      reset(initialData);
    }

    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [open]);

  if (!form) return null;

  return (
    <Dialog open={open} onClose={close} maxWidth="lg" fullWidth>
      <DialogTitle>{initialData?.kopieVonVeranstaltungId ? "Veranstaltung mit Planung kopieren" : "Neue Veranstaltung anlegen"}</DialogTitle>

      <DialogContent sx={{ mt: 2 }}>
        <VeranstaltungSections form={form} editMode beitragsstrukturen={beitragsstrukturen} onChange={update} errorAttempt={errorAttempt} />
      </DialogContent>

      {!isValid && <Alert severity="info" sx={{ mx: 3, mt: 2 }}>{validationErrors.join(" ")}</Alert>}

      <DialogActions>
        <Button onClick={close} disabled={saving}>Abbrechen</Button>

        <Button
          variant="contained"
          disabled={saving}
          onClick={async () => {
            const payload = buildSavePayload();
            if (!payload) { setErrorAttempt(n => n + 1); return; }

            setSaving(true);
            try {
              await onCreate(payload);
              onClose();
            } catch {
              setErrorAttempt(n => n + 1);
            } finally {
              setSaving(false);
            }
          }}
        >
          Anlegen
        </Button>
      </DialogActions>
    </Dialog>
  );
};
