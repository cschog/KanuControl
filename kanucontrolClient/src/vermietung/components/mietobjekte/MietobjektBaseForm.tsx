import React from "react";
import { MenuItem, TextField } from "@mui/material";

import FormFeld from "@/core/components/common/FormFeld";
import PostalCodeAutocomplete from "@/core/components/common/PostalCodeAutocomplete";

import { COUNTRIES } from "@/kjfp/api/enums/CountryCode";

import type { MietobjektSave } from "@/vermietung/types/MietobjektSave";

interface Props {
  form: MietobjektSave;
  editMode: boolean;

  onChange: <K extends keyof MietobjektSave>(key: K, value: MietobjektSave[K]) => void;
}

export const MietobjektBaseForm: React.FC<Props> = ({ form, editMode, onChange }) => {
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

      <FormFeld
        label="Straße"
        value={form.strasse}
        disabled={!editMode}
        onChange={(v) => onChange("strasse", v)}
      />

      <TextField
        select
        fullWidth
        size="small"
        label="Land"
        value={form.countryCode}
        disabled={!editMode}
        onChange={(e) => onChange("countryCode", e.target.value)}
      >
        {COUNTRIES.map((country) => (
          <MenuItem key={country.code} value={country.code}>
            {country.label}
          </MenuItem>
        ))}
      </TextField>

      <PostalCodeAutocomplete
        countryCode={form.countryCode}
        postalCode={form.plz}
        disabled={!editMode}
        onSelect={(item) => {
          onChange("plz", item.postalCode);
          onChange("ort", item.city);
        }}
      />

      <FormFeld
        label="Ort"
        value={form.ort}
        disabled={!editMode}
        onChange={(v) => onChange("ort", v)}
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
