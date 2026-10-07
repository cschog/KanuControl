import React from "react";
import { MenuItem, TextField } from "@mui/material";

import FormFeld from "@/core/components/common/FormFeld";

import type { MietbereichEigenschaftSave } from "@/vermietung/types/MietbereichEigenschaftSave";
import type { MietbereichEigenschaftTyp } from "@/vermietung/enums/MietbereichEigenschaftTyp";

interface Props {
  form: MietbereichEigenschaftSave;
  editMode: boolean;

  onChange: <K extends keyof MietbereichEigenschaftSave>(
    key: K,
    value: MietbereichEigenschaftSave[K],
  ) => void;
}

const typOptions: {
  value: MietbereichEigenschaftTyp;
  label: string;
}[] = [
  { value: "TEXT", label: "Text" },
  { value: "ZAHL", label: "Zahl" },
  { value: "FLAECHE", label: "Fläche" },
  { value: "HOEHE", label: "Höhe" },
  { value: "LAENGE", label: "Länge" },
  { value: "BOOLEAN", label: "Ja / Nein" },
];

export const MietbereichEigenschaftForm: React.FC<Props> = ({ form, editMode, onChange }) => {
  const isNumber =
    form.typ === "ZAHL" || form.typ === "FLAECHE" || form.typ === "HOEHE" || form.typ === "LAENGE";

  const unit =
    form.typ === "FLAECHE" ? "m²" : form.typ === "HOEHE" || form.typ === "LAENGE" ? "m" : "";

  return (
    <>
      <FormFeld
        label="Bezeichnung"
        value={form.bezeichnung}
        disabled={!editMode}
        onChange={(v) => onChange("bezeichnung", v)}
      />

      <TextField
        select
        fullWidth
        size="small"
        label="Typ"
        value={form.typ}
        disabled={!editMode}
        onChange={(e) => onChange("typ", e.target.value as MietbereichEigenschaftTyp)}
      >
        {typOptions.map((option) => (
          <MenuItem key={option.value} value={option.value}>
            {option.label}
          </MenuItem>
        ))}
      </TextField>

      {form.typ === "BOOLEAN" ? (
        <TextField
          select
          fullWidth
          size="small"
          label="Wert"
          value={form.wert}
          disabled={!editMode}
          onChange={(e) => onChange("wert", e.target.value)}
        >
          <MenuItem value="true">Ja</MenuItem>
          <MenuItem value="false">Nein</MenuItem>
        </TextField>
      ) : (
        <TextField
          fullWidth
          size="small"
          label="Wert"
          value={form.wert}
          disabled={!editMode}
          type={isNumber ? "number" : "text"}
          slotProps={{
            input: {
              endAdornment: unit || undefined,
            },
          }}
          onChange={(e) => onChange("wert", e.target.value)}
        />
      )}
    </>
  );
};
