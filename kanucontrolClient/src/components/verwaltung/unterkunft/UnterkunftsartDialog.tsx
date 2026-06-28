// src/components/verwaltung/unterkunft/UnterkunftsartDialog.tsx

import { useEffect, useState } from "react";
import { Checkbox, FormControlLabel, Stack } from "@mui/material";

import GenericCrudDialog from "@/components/common/GenericCrudDialog";
import FormFeld from "@/components/common/FormFeld";
import MoneyField from "@/components/common/MoneyField";

import { UnterkunftsartDTO } from "@/api/types/unterkunft/UnterkunftsartDTO";
import { UnterkunftsartCreateUpdateDTO } from "@/api/types/unterkunft/UnterkunftsartCreateUpdateDTO";

interface UnterkunftsartDialogProps {
  open: boolean;
  unterkunftsart?: UnterkunftsartDTO | null;

  onClose: () => void;
  onSave: (dto: UnterkunftsartCreateUpdateDTO) => void;
}


const createEmptyDto = (): UnterkunftsartCreateUpdateDTO => ({
  bezeichnung: "",
  preisProPersonUndNacht: 0,
  bemerkung: "",
  aktiv: true,
});

const UnterkunftsartDialog = ({
  open,
  unterkunftsart,
  onClose,
  onSave,
}: UnterkunftsartDialogProps) => {
  const [form, setForm] = useState<UnterkunftsartCreateUpdateDTO>(
    createEmptyDto(),
  );

  useEffect(() => {
    if (!open) return;

    if (unterkunftsart) {
      setForm({
        bezeichnung: unterkunftsart.bezeichnung,
        preisProPersonUndNacht: unterkunftsart.preisProPersonUndNacht,
        bemerkung: unterkunftsart.bemerkung ?? "",
        aktiv: unterkunftsart.aktiv,
      });
    } else {
      setForm(createEmptyDto());
    }
  }, [open, unterkunftsart]);

  const handleSave = () => {
    onSave(form);
  };

  return (
    <GenericCrudDialog
      open={open}
      title={
        unterkunftsart
          ? "Unterkunftsart bearbeiten"
          : "Unterkunftsart anlegen"
      }
      onClose={onClose}
      onSave={handleSave}
      disableSave={!form.bezeichnung.trim()}
    >
      <Stack spacing={2} sx={{ mt: 1 }}>
        <FormFeld
          label="Bezeichnung"
          value={form.bezeichnung}
          onChange={(value) =>
            setForm((prev) => ({
              ...prev,
              bezeichnung: value,
            }))
          }
          maxLength={100}
          autoFocus
        />

        <MoneyField
          label="Preis pro Person und Nacht"
          value={form.preisProPersonUndNacht}
          onChange={(value) =>
            setForm((prev) => ({
              ...prev,
              preisProPersonUndNacht: Number(value),
            }))
          }
        />

        <FormFeld
          label="Bemerkung"
          value={form.bemerkung}
          onChange={(value) =>
            setForm((prev) => ({
              ...prev,
              bemerkung: value,
            }))
          }
          multiline
          rows={3}
          maxLength={255}
        />

        {/* <FormControlLabel
          control={
            <Checkbox
              checked={form.aktiv}
              onChange={(e) =>
                setForm((prev) => ({
                  ...prev,
                  aktiv: e.target.checked,
                }))
              }
            />
          }
          label="Aktiv"
        /> */}
      </Stack>
    </GenericCrudDialog>
  );
};

export default UnterkunftsartDialog;