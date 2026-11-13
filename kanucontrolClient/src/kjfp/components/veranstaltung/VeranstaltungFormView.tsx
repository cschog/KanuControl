import { useVeranstaltungNavigationGuard } from "./hook/useVeranstaltungNavigationGuard";
import React, { useState } from "react";
import { Alert, Typography } from "@mui/material";
import { VeranstaltungSections } from "./form/VeranstaltungSections";
import { VeranstaltungActionBar } from "./VeranstaltungActionBar";
import { useVeranstaltungForm } from "./hook/useVeranstaltungForm";
import { ErrorDialog } from "@/core/components/common/ErrorDialog";
import ConfirmDeleteDialog from "@/core/components/common/ConfirmDeleteDialog";
import { VeranstaltungDetail } from "@/kjfp/types/veranstaltung/VeranstaltungDetail";
import { VeranstaltungSave } from "@/kjfp/types/veranstaltung/VeranstaltungSave";

interface Props {
  veranstaltung: VeranstaltungDetail | null;
  editMode: boolean;

  onEdit: () => void;
  onCopy: () => void;
  onCancelEdit: () => void;
  onSave: (v: VeranstaltungSave) => Promise<void>;
  onDelete: () => void;
  onBack: () => void;

  onActivate: () => void;

  disableEdit: boolean;
  disableDelete: boolean;

  beitragsstrukturen: BeitragsstrukturDTO[];
}

interface BeitragsstrukturDTO {
  id: number;
  name: string;
}

export const VeranstaltungFormView: React.FC<Props> = ({
  veranstaltung,
  beitragsstrukturen,
  editMode,
  onEdit,
  onCopy,
  onCancelEdit,
  onSave,
  onDelete,
  onBack,
  onActivate,
  disableEdit,
  disableDelete,
}) => {
  const { form, update, buildSavePayload, validationErrors, isDirty } = useVeranstaltungForm(veranstaltung);
  const [errorAttempt, setErrorAttempt] = useState(0);
  const dirty = editMode && isDirty;
  useVeranstaltungNavigationGuard(dirty);
  const leave = (action: () => void) => { if (!dirty || window.confirm("Ungespeicherte Änderungen verwerfen?")) action(); };
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [validationError, setValidationError] = useState<string | null>(null);

  if (!veranstaltung || !form) {
    return (
      <Typography align="center" sx={{ mt: 4 }} color="text.secondary">
        Bitte wählen Sie eine Veranstaltung aus.
      </Typography>
    );
  }

  return (
    <>
      {/* ================= FORM ================= */}
      <VeranstaltungSections key={veranstaltung.id} form={form} editMode={editMode} detailMode beitragsstrukturen={beitragsstrukturen} onChange={update} errorAttempt={errorAttempt} />
      {dirty && <Alert severity="info" sx={{ mt: 2 }}>Ungespeicherte Änderungen. Die PDF-Anmeldung verwendet den zuletzt gespeicherten Stand.</Alert>}


      {/* ================= ACTION BAR ================= */}
      <VeranstaltungActionBar
        aktiv={veranstaltung.aktiv}
        editMode={editMode}
        onEdit={onEdit}
        onCopy={() => leave(onCopy)}
        onCancelEdit={() => leave(onCancelEdit)}
        onSave={async () => {
          const payload = buildSavePayload();
          if (!payload) {
            setErrorAttempt(n => n + 1);
            setValidationError(validationErrors.join(" "));
            return;
          }
          await onSave(payload);
        }}
        onDelete={() => setConfirmOpen(true)}
        onBack={() => leave(onBack)}
        onActivate={onActivate}
        disableEdit={disableEdit}
        disableDelete={disableDelete}
      />

      <ErrorDialog open={!!validationError} title="Veranstaltung noch nicht vollständig" message={validationError ?? ""} onClose={() => setValidationError(null)} />

      {/* ================= DELETE ================= */}
      <ConfirmDeleteDialog
        open={confirmOpen}
        onClose={() => setConfirmOpen(false)}
        onConfirm={onDelete}
        description={`Soll die Veranstaltung „${veranstaltung.name}“ wirklich gelöscht werden?`}
      />
    </>
  );
};
