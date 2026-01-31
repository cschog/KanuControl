import React, { useState } from "react";
import { Box, Typography } from "@mui/material";
import Verein from "@/api/types/VereinFormModel";
import { VereinBaseForm } from "./form/VereinBaseForm";
import { VereinActionBar } from "./VereinActionBar";
import { useVereinForm } from "./hooks/useVereinForm";
import { ConfirmDeleteDialog } from "@/components/common/ConfirmDeleteDialog";
import { KontoinhaberSelectDialog } from "@/components/verein/kontoinhaber/KontoinhaberSelectDialog";
import { VereinSave } from "@/api/types/VereinSave";


interface VereinFormViewProps {
  verein: Verein | null;
  editMode: boolean;

  onEdit: () => void;
  onCancelEdit: () => void;
  onSave: (verein: VereinSave) => Promise<void>;
  onDelete: () => void;
  onBack: () => void;

  onCsvImport?: () => void;

  disableEdit: boolean;
  disableDelete: boolean;
}


export const VereinFormView: React.FC<VereinFormViewProps> = ({
  verein,
  editMode,
  onEdit,
  onCancelEdit,
  onSave,
  onDelete,
  onBack,
  onCsvImport,
  disableEdit,
  disableDelete,
}) => {
  const { form, update, buildSavePayload } = useVereinForm(verein);
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [kontoinhaberDialogOpen, setKontoinhaberDialogOpen] = useState(false);
  

  if (!verein || !form) {
    return (
      <Typography align="center" sx={{ mt: 4 }} color="text.secondary">
        Bitte wählen Sie einen Verein aus.
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
          lg: "repeat(4, 1fr)", // 🔑 HIER die Spalten!
        }}
        gap={2}
        sx={{ mt: 2 }}
      >
        <VereinBaseForm form={form} editMode={editMode} mode="edit" onChange={update} />
      </Box>

      {/* ================= KONTOINHABER ================= */}
      <KontoinhaberSelectDialog
        open={kontoinhaberDialogOpen}
        onClose={() => setKontoinhaberDialogOpen(false)}
        onSelect={(person) => update("kontoinhaber", person)}
      />

      {/* ================= ACTION BAR ================= */}
      <VereinActionBar
        editMode={editMode}
        onEdit={onEdit}
        onCancelEdit={onCancelEdit}
        onSave={async () => {
          const payload = buildSavePayload();
          if (payload) await onSave(payload);
        }}
        onDelete={() => setConfirmOpen(true)}
        onBack={onBack}
        onCsvImport={onCsvImport}
        onChangeKontoinhaber={() => setKontoinhaberDialogOpen(true)}
        disableEdit={disableEdit}
        disableDelete={disableDelete}
      />

      {/* ================= DELETE ================= */}
      <ConfirmDeleteDialog
        open={confirmOpen}
        onClose={() => setConfirmOpen(false)}
        onConfirm={onDelete}
        description={`Soll der Verein „${verein.name}“ wirklich gelöscht werden?`}
      />
    </>
  );
};
