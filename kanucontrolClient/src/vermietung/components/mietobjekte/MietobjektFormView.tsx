import React, { useEffect, useState } from "react";
import { Box, Button, Typography } from "@mui/material";

import { MietobjektBaseForm } from "./MietobjektBaseForm";
import { MietobjektActionBar } from "./MietobjektActionBar";

import { useMietobjektForm } from "@/vermietung/hooks/useMietobjektForm";

import type { Mietobjekt } from "@/vermietung/types/Mietobjekt";
import type { MietobjektSave } from "@/vermietung/types/MietobjektSave";
import { MietbereicheView } from "./mietbereiche/MietbereicheView";

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
  const [view, setView] = useState<"details" | "mietbereiche">("mietbereiche");

  useEffect(() => {
    setView("mietbereiche");
  }, [mietobjekt?.id]);

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
        sx={{
          display: "flex",
          gap: 1,
          mb: 2,
          mt: 2,
        }}
      >
        <Button
          variant={view === "details" ? "contained" : "outlined"}
          onClick={() => setView("details")}
        >
          Objekt
        </Button>

        <Button
          variant={view === "mietbereiche" ? "contained" : "outlined"}
          onClick={() => setView("mietbereiche")}
          disabled={mietobjekt.id <= 0}
        >
          Mietbereiche
        </Button>
      </Box>

      {view === "details" && (
        <Box
          display="grid"
          gridTemplateColumns={{
            xs: "1fr",
            sm: "repeat(2, 1fr)",
            lg: "repeat(4, 1fr)",
          }}
          gap={2}
        >
          <MietobjektBaseForm form={form} editMode={editMode} onChange={update} />
        </Box>
      )}

      {view === "mietbereiche" && mietobjekt.id > 0 && (
        <MietbereicheView
          mietobjektId={mietobjekt.id}
          mietobjektBezeichnung={mietobjekt.bezeichnung}
        />
      )}

      {/* ================= ACTION BAR ================= */}

      {view === "details" && (
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
      )}
    </>
  );
};
