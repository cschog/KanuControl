import React from "react";
import { Box, Typography } from "@mui/material";

import { MietbereichBaseForm } from "./MietbereichBaseForm";
import { MietbereichActionBar } from "./MietbereichActionBar";

import { useMietbereichForm } from "@/vermietung/hooks/useMietbereichForm";

import type { Mietbereich } from "@/vermietung/types/Mietbereich";
import type { MietbereichSave } from "@/vermietung/types/MietbereichSave";

interface Props {
  mietbereich: Mietbereich | null;
  editMode: boolean;

  onEdit: () => void;
  onCancelEdit: () => void;
  onSave: (payload: MietbereichSave) => Promise<void>;

  onDelete: () => void;
  onBack: () => void;

  disableDelete: boolean;
}

export const MietbereichFormView: React.FC<Props> = ({
  mietbereich,
  editMode,
  onEdit,
  onCancelEdit,
  onSave,
  onDelete,
  onBack,
  disableDelete,
}) => {
  const { form, update, buildSavePayload } = useMietbereichForm(mietbereich);

  if (!mietbereich || !form) {
    return (
      <Typography align="center" sx={{ mt: 4 }} color="text.secondary">
        Bitte wählen Sie einen Mietbereich aus.
      </Typography>
    );
  }

  return (
    <>
      <Box
        display="grid"
        gridTemplateColumns={{
          xs: "1fr",
          sm: "repeat(2, 1fr)",
          lg: "repeat(3, 1fr)",
        }}
        gap={2}
        sx={{ mt: 2 }}
      >
        <MietbereichBaseForm form={form} editMode={editMode} onChange={update} />
      </Box>

      <MietbereichActionBar
        editMode={editMode}
        onEdit={onEdit}
        onCancelEdit={onCancelEdit}
        onSave={async () => {
          const payload = buildSavePayload();

          if (payload) {
            await onSave(payload);
          }
        }}
        onDelete={onDelete}
        onBack={onBack}
        disableDelete={disableDelete}
      />
    </>
  );
};
