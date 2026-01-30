import React from "react";
import { TextField, MenuItem } from "@mui/material";
import { PersonSave } from "@/api/types/Person";
import { Sex } from "@/api/enums/Sex";
import { CountryCode } from "@/api/enums/CountryCode";
import { Checkbox, FormControlLabel } from "@mui/material";

interface PersonBaseFormProps {
  form: PersonSave;
  editMode: boolean;
  mode: "create" | "edit";
  onChange: <K extends keyof PersonSave>(key: K, value: PersonSave[K]) => void;
}


export const PersonBaseForm: React.FC<PersonBaseFormProps> = ({ form, editMode, mode, onChange }) => {
  const showExtended = mode === "edit";

  return (
    <>
      {/* Pflichtfelder – CREATE + EDIT */}
      <TextField
        label="Vorname"
        margin="normal"
        value={form.vorname}
        onChange={(e) => onChange("vorname", e.target.value)}
        disabled={!editMode}
        required
      />

      <TextField
        label="Name"
        margin="normal"
        value={form.name}
        onChange={(e) => onChange("name", e.target.value)}
        disabled={!editMode}
        required
      />

      <TextField
        select
        label="Geschlecht"
        margin="normal"
        value={form.sex}
        onChange={(e) => onChange("sex", e.target.value as Sex)}
        disabled={!editMode}
      >
        <MenuItem value="M">Männlich</MenuItem>
        <MenuItem value="W">Weiblich</MenuItem>
        <MenuItem value="D">Divers</MenuItem>
      </TextField>

      <TextField
        type="date"
        label="Geburtsdatum"
        margin="normal"
        value={form.geburtsdatum ?? ""}
        onChange={(e) => onChange("geburtsdatum", e.target.value || undefined)}
        disabled={!editMode}
        InputLabelProps={{ shrink: true }}
      />

      <TextField
        label="Straße"
        margin="normal"
        value={form.strasse ?? ""}
        onChange={(e) => onChange("strasse", e.target.value || undefined)}
        disabled={!editMode}
      />

      <TextField
        label="PLZ"
        margin="normal"
        value={form.plz ?? ""}
        onChange={(e) => onChange("plz", e.target.value || undefined)}
        disabled={!editMode}
      />

      <TextField
        label="Ort"
        margin="normal"
        value={form.ort ?? ""}
        onChange={(e) => onChange("ort", e.target.value || undefined)}
        disabled={!editMode}
      />

      {/* NUR IM EDIT */}
      {showExtended && (
        <>
          <TextField
            label="E-Mail"
            value={form.email ?? ""}
            onChange={(e) => onChange("email", e.target.value || undefined)}
            disabled={!editMode}
          />

          <TextField
            label="Telefon"
            value={form.telefon ?? ""}
            onChange={(e) => onChange("telefon", e.target.value || undefined)}
            disabled={!editMode}
          />

          <TextField
            label="Telefon (Festnetz)"
            value={form.telefonFestnetz ?? ""}
            onChange={(e) => onChange("telefonFestnetz", e.target.value || undefined)}
            disabled={!editMode}
          />
          <TextField
            select
            label="Land"
            value={form.countryCode ?? ""}
            onChange={(e) =>
              onChange(
                "countryCode",
                e.target.value === "" ? undefined : (e.target.value as CountryCode),
              )
            }
            disabled={!editMode}
            fullWidth
          >
            <MenuItem value="">–</MenuItem>
            <MenuItem value="DE">Deutschland</MenuItem>
            <MenuItem value="NL">Niederlande</MenuItem>
            <MenuItem value="BE">Belgien</MenuItem>
          </TextField>

          <TextField
            label="Bank"
            value={form.bankName ?? ""}
            onChange={(e) => onChange("bankName", e.target.value || undefined)}
            disabled={!editMode}
          />

          <TextField
            label="IBAN"
            value={form.iban ?? ""}
            onChange={(e) => onChange("iban", e.target.value || undefined)}
            disabled={!editMode}
          />
          <FormControlLabel
            control={
              <Checkbox
                checked={form.aktiv ?? true}
                onChange={(e) => onChange("aktiv", e.target.checked)}
                disabled={!editMode}
              />
            }
            label="Aktiv"
          />
        </>
      )}
    </>
  );
};