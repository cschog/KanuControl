import React from "react";
import { Box, Typography } from "@mui/material";

import { MietobjektBaseForm } from "./MietobjektBaseForm";
import { MietobjektActionBar } from "./MietobjektActionBar";

import { useMietobjektForm } from "@/vermietung/hooks/useMietobjektForm";

import type { Mietobjekt } from "@/vermietung/types/Mietobjekt";
import type { MietobjektSave } from "@/vermietung/types/MietobjektSave";


interface Props {
  mietobjekt: Mietobjekt | null;
  editMode: boolean;

  onEdit: () => void;
  onCancelEdit: () => void;
  onSave: (payload: MietobjektSave) => Promise<void>;

  onDelete: () => void;
  onBack: () => void;
  onActivate: () => void;

  disableEdit: boolean;
  disableDelete: boolean;
}

export const MietobjektFormView: React.FC<Props> = ({
  mietobjekt,
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
  const { form, update, buildSavePayload } = useMietobjektForm(mietobjekt);

  if (!mietobjekt || !form) {
    return (
      <Typography align="center" sx={{ mt: 4 }} color="text.secondary">
        Bitte wählen Sie ein Mietobjekt aus.
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
        <MietobjektBaseForm form={form} editMode={editMode} onChange={update} />
      </Box>

      {/* ================= ACTION BAR ================= */}

      <MietobjektActionBar
        aktiv={mietobjekt.aktiv}
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
        onActivate={onActivate}
        disableEdit={disableEdit}
        disableDelete={disableDelete}
      />
    </>
  );
};
