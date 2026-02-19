import React, { useState } from "react";
import { Box, Typography } from "@mui/material";
import { VeranstaltungBaseForm } from "./form/VeranstaltungBaseForm";
import { VeranstaltungActionBar } from "./VeranstaltungActionBar";
import { useVeranstaltungForm } from "./hook/useVeranstaltungForm";
import { ConfirmDeleteDialog } from "@/components/common/ConfirmDeleteDialog";
import { VeranstaltungDetail } from "@/api/types/VeranstaltungDetail";
import { VeranstaltungSave } from "@/api/types/VeranstaltungSave";

interface Props {
  veranstaltung: VeranstaltungDetail | null;
  editMode: boolean;

  onEdit: () => void;
  onCancelEdit: () => void;
  onSave: (v: VeranstaltungSave) => Promise<void>;
  onDelete: () => void;
  onBack: () => void;

  onActivate: () => void;

  disableEdit: boolean;
  disableDelete: boolean;
}

export const VeranstaltungFormView: React.FC<Props> = ({
  veranstaltung,
  editMode,
  onEdit,
  onCancelEdit,
  onSave,
  onDelete,
  onBack,
  onActivate,
  disableEdit,
  disableDelete,
}) => {
  const { form, update, buildSavePayload } = useVeranstaltungForm(veranstaltung);
  const [confirmOpen, setConfirmOpen] = useState(false);

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
      <Box
        display="grid"
        gridTemplateColumns={{
          xs: "1fr",
          sm: "repeat(2, 1fr)",
          lg: "repeat(4, 1fr)",
        }}
        gap={2}
        sx={{ mt: 2 }}
      >
        <VeranstaltungBaseForm
          form={form}
          editMode={editMode}
          detailMode={true}
          onChange={update}
        />
      </Box>

      {/* ================= ACTION BAR ================= */}
      <VeranstaltungActionBar
        aktiv={veranstaltung.aktiv}
        editMode={editMode}
        onEdit={onEdit}
        onCancelEdit={onCancelEdit}
        onSave={async () => {
          const payload = buildSavePayload();
          if (payload) await onSave(payload);
        }}
        onDelete={() => setConfirmOpen(true)}
        onBack={onBack}
        onActivate={onActivate}
        disableEdit={disableEdit}
        disableDelete={disableDelete}
      />

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
