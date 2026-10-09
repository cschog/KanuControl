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

      <TextField
        fullWidth
        size="small"
        type="number"
        label="Bestand"
        value={form.bestand}
        disabled={!editMode}
        slotProps={{
          htmlInput: { min: 1, step: 1 },
        }}
        onChange={(e) => onChange("bestand", Number(e.target.value))}
        helperText="Anzahl verfügbarer Einheiten"
      />

      <FormFeld
        label="Mengeneinheit"
        value={form.mengeneinheit}
        disabled={!editMode}
        onChange={(v) => onChange("mengeneinheit", v)}
      />

      <TextField
        select
        fullWidth
        size="small"
        label="Direktbuchung aktiv"
        value={form.direktbuchungAktiv ? "ja" : "nein"}
        disabled={!editMode}
        onChange={(e) => onChange("direktbuchungAktiv", e.target.value === "ja")}
      >
        <MenuItem value="ja">Ja</MenuItem>
        <MenuItem value="nein">Nein</MenuItem>
      </TextField>

      <TextField
        select
        fullWidth
        size="small"
        label="Airbnb aktiv"
        value={form.airbnbAktiv ? "ja" : "nein"}
        disabled={!editMode}
        onChange={(e) => onChange("airbnbAktiv", e.target.value === "ja")}
      >
        <MenuItem value="ja">Ja</MenuItem>
        <MenuItem value="nein">Nein</MenuItem>
      </TextField>
    </>
  );
};
