// src/components/veranstaltung/VeranstaltungDetailView.tsx
import React from "react";
import { Box, Typography, Divider } from "@mui/material";

import { VeranstaltungDetail } from "@/api/types/VeranstaltungDetail";
import { VeranstaltungFormModel } from "@/api/types/VeranstaltungFormModel";

import { VeranstaltungActionBar } from "@/components/veranstaltung/VeranstaltungActionBar";
import { VeranstaltungBaseForm } from "@/components/veranstaltung/VeranstaltungBaseForm";

interface Props {
  veranstaltung: VeranstaltungDetail | null;
  form: VeranstaltungFormModel | null;

  editMode: boolean;

  onEdit: () => void;
  onCancelEdit: () => void;
  onSave: (data: VeranstaltungFormModel) => Promise<void>;

  onBeenden: () => void;
  onAktivieren: () => void;
  onBack: () => void;

  disableEdit: boolean;
  onChange: <K extends keyof VeranstaltungFormModel>(
    key: K,
    value: VeranstaltungFormModel[K],
  ) => void;
}

export const VeranstaltungDetailView: React.FC<Props> = ({
  veranstaltung,
  form,
  editMode,
  onEdit,
  onCancelEdit,
  onSave,
  onBeenden,
  onAktivieren,
  onBack,
  disableEdit,
  onChange,
}) => {
  if (!veranstaltung || !form) {
    return (
      <Typography align="center" sx={{ mt: 4 }} color="text.secondary">
        Bitte wählen Sie eine Veranstaltung aus.
      </Typography>
    );
  }

  return (
    <>
      {/* ================= HEADER ================= */}
      <Box sx={{ mt: 3 }}>
        <Typography variant="h6">{veranstaltung.name}</Typography>
        <Typography variant="body2" color="text.secondary">
          {veranstaltung.typ} · {veranstaltung.beginnDatum} – {veranstaltung.endeDatum}
        </Typography>
      </Box>

      <Divider sx={{ my: 2 }} />

      {/* ================= FORM ================= */}
      <Box
        display="grid"
        gridTemplateColumns={{
          xs: "1fr",
          sm: "repeat(2, 1fr)",
          lg: "repeat(4, 1fr)",
        }}
        gap={2}
      >
        <VeranstaltungBaseForm form={form} editMode={editMode} onChange={onChange} />
      </Box>

      {/* ================= ACTION BAR ================= */}
      <VeranstaltungActionBar
        aktiv={veranstaltung.aktiv}
        editMode={editMode}
        onEdit={onEdit}
        onCancelEdit={onCancelEdit}
        onSave={() => onSave(form)}
        onBeenden={onBeenden}
        onAktivieren={onAktivieren}
        onBack={onBack}
        disableEdit={disableEdit}
      />
    </>
  );
};
