import React from "react";
import { MenuItem, TextField } from "@mui/material";

import FormFeld from "@/core/components/common/FormFeld";

import type { MietbereichSave } from "@/vermietung/types/MietbereichSave";

interface Props {
  form: MietbereichSave;
  editMode: boolean;

  onChange: <K extends keyof MietbereichSave>(key: K, value: MietbereichSave[K]) => void;
}

export const MietbereichBaseForm: React.FC<Props> = ({ form, editMode, onChange }) => {
  return (
    <>
      <FormFeld
        label="Bezeichnung"
        value={form.bezeichnung}
        disabled={!editMode}
        onChange={(v) => onChange("bezeichnung", v)}
      />

      <FormFeld
        label="Beschreibung"
        value={form.beschreibung}
        disabled={!editMode}
        onChange={(v) => onChange("beschreibung", v)}
      />

      <TextField
        select
        fullWidth
        size="small"
        label="Mietbar"
        value={form.mietbar ? "ja" : "nein"}
        disabled={!editMode}
        onChange={(e) => onChange("mietbar", e.target.value === "ja")}
      >
        <MenuItem value="ja">Ja</MenuItem>
        <MenuItem value="nein">Nein</MenuItem>
      </TextField>
    </>
  );
};
