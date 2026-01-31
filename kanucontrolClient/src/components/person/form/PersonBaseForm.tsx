import React from "react";
import { TextField, MenuItem, Checkbox, FormControlLabel } from "@mui/material";
import { FormFeld } from "@/components/common/FormFeld";
import { FormFeldDate } from "@/components/common/FormFeldDate";
import { PersonSave } from "@/api/types/Person";
import { Sex } from "@/api/enums/Sex";
import { CountryCode } from "@/api/enums/CountryCode";

interface Props {
  form: PersonSave;
  editMode: boolean;
  mode: "create" | "edit";
  onChange: <K extends keyof PersonSave>(key: K, value: PersonSave[K]) => void;
}

export const PersonBaseForm: React.FC<Props> = ({ form, editMode, mode, onChange }) => {
  const showExtended = mode === "edit";

  return (
    <>
      {/* ================= CREATE + EDIT ================= */}

      <FormFeld
        label="Vorname"
        value={form.vorname}
        onChange={(v) => onChange("vorname", v)}
        disabled={!editMode}
      />

      <FormFeld
        label="Name"
        value={form.name}
        onChange={(v) => onChange("name", v)}
        disabled={!editMode}
      />

      <TextField
        select
        fullWidth
        size="small"
        label="Geschlecht"
        value={form.sex}
        onChange={(e) => onChange("sex", e.target.value as Sex)}
        disabled={!editMode}
      >
        <MenuItem value="M">Männlich</MenuItem>
        <MenuItem value="W">Weiblich</MenuItem>
        <MenuItem value="D">Divers</MenuItem>
      </TextField>

      <FormFeldDate
        label="Geburtsdatum"
        value={form.geburtsdatum ?? ""}
        onChange={(v) => onChange("geburtsdatum", v || undefined)}
        disabled={!editMode}
      />

      <FormFeld
        label="Straße"
        value={form.strasse}
        onChange={(v) => onChange("strasse", v || undefined)}
        disabled={!editMode}
      />

      <FormFeld
        label="PLZ"
        value={form.plz}
        onChange={(v) => onChange("plz", v || undefined)}
        disabled={!editMode}
      />

      <FormFeld
        label="Ort"
        value={form.ort}
        onChange={(v) => onChange("ort", v || undefined)}
        disabled={!editMode}
      />

      {/* ================= NUR EDIT ================= */}
      {showExtended && (
        <>
          <FormFeld
            label="E-Mail"
            value={form.email}
            onChange={(v) => onChange("email", v || undefined)}
            disabled={!editMode}
          />

          <FormFeld
            label="Telefon"
            value={form.telefon}
            onChange={(v) => onChange("telefon", v || undefined)}
            disabled={!editMode}
          />

          <FormFeld
            label="Telefon (Festnetz)"
            value={form.telefonFestnetz}
            onChange={(v) => onChange("telefonFestnetz", v || undefined)}
            disabled={!editMode}
          />

          <TextField
            select
            fullWidth
            size="small"
            label="Land"
            value={form.countryCode ?? ""}
            onChange={(e) =>
              onChange(
                "countryCode",
                e.target.value === "" ? undefined : (e.target.value as CountryCode),
              )
            }
            disabled={!editMode}
          >
            <MenuItem value="">–</MenuItem>
            <MenuItem value="DE">Deutschland</MenuItem>
            <MenuItem value="NL">Niederlande</MenuItem>
            <MenuItem value="BE">Belgien</MenuItem>
          </TextField>

          <FormFeld
            label="Bank"
            value={form.bankName}
            onChange={(v) => onChange("bankName", v || undefined)}
            disabled={!editMode}
          />

          <FormFeld
            label="IBAN"
            value={form.iban}
            onChange={(v) => onChange("iban", v || undefined)}
            disabled={!editMode}
          />

          <FormControlLabel
            label="Aktiv"
            control={
              <Checkbox
                checked={form.aktiv ?? true}
                onChange={(e) => onChange("aktiv", e.target.checked)}
                disabled={!editMode}
              />
            }
          />
        </>
      )}
    </>
  );
};
