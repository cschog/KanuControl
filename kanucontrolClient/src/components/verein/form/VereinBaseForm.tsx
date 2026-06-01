import React from "react";
import { FormFeld } from "@/components/common/FormFeld";
import { FormFeldDate } from "@/components/common/FormFeldDate";
import { FormControl, InputLabel, Select, MenuItem } from "@mui/material";
import Verein from "@/api/types/VereinFormModel";
import { COUNTRIES, CountryCode } from "@/api/enums/CountryCode";
import { PersonAutocomplete } from "@/components/person/PersonAutocomplete";
import PostalCodeAutocomplete from "@/components/common/PostalCodeAutocomplete";

interface Props {
  form: Verein;
  editMode: boolean;
  mode: "create" | "edit";
  onChange: <K extends keyof Verein>(key: K, value: Verein[K]) => void;
}

export const VereinBaseForm: React.FC<Props> = ({ form, editMode, mode, onChange }) => {
  const showExtended = mode === "edit";

  return (
    <>
      {/* CREATE */}
      <FormFeld
        label="Abkürzung"
        value={form.abk}
        onChange={(v) => onChange("abk", v)}
        disabled={!editMode}
      />

      <FormFeld
        label="Vereinsname"
        value={form.name}
        onChange={(v) => onChange("name", v)}
        disabled={!editMode}
      />

      <PostalCodeAutocomplete
        countryCode={form.countryCode ?? "DE"}
        postalCode={form.plz}
        city={form.ort}
        disabled={!editMode}
        onSelect={(item) => {
          onChange("plz", item.postalCode);
          onChange("ort", item.city);
        }}
      />

      <FormFeld
        label="Ort"
        value={form.ort ?? ""}
        onChange={(v) => onChange("ort", v)}
        disabled={!editMode}
      />

      <FormControl fullWidth size="small">
        <InputLabel>Land</InputLabel>

        <Select
          value={form.countryCode ?? "DE"}
          label="Land"
          size="small"
          disabled={!editMode}
          onChange={(e) => onChange("countryCode", e.target.value as CountryCode)}
        >
          {COUNTRIES.map((c) => (
            <MenuItem key={c.code} value={c.code}>
              {c.label}
            </MenuItem>
          ))}
        </Select>
      </FormControl>

      {/* NUR EDIT */}
      {showExtended && (
        <>
          <FormFeld
            label="Straße"
            value={form.strasse ?? ""}
            onChange={(v) => onChange("strasse", v)}
            disabled={!editMode}
          />

          <FormFeld
            label="Telefon"
            value={form.telefon ?? ""}
            onChange={(v) => onChange("telefon", v)}
            disabled={!editMode}
          />

          <FormFeld
            label="Bank"
            value={form.bankName ?? ""}
            onChange={(v) => onChange("bankName", v)}
            disabled={!editMode}
          />

          <FormFeld
            label="IBAN"
            value={form.iban ?? ""}
            onChange={(v) => onChange("iban", v)}
            disabled={!editMode}
          />

          <FormFeld
            label="BIC"
            value={form.bic ?? ""}
            onChange={(v) => onChange("bic", v)}
            disabled={!editMode}
          />

          <PersonAutocomplete
            label="Kontoinhaber"
            value={form.kontoinhaber}
            disabled={!editMode}
            onChange={(person) => onChange("kontoinhaber", person)}
          />

          <FormFeldDate
            label="Schutzkonzept"
            value={form.schutzkonzept ?? ""}
            onChange={(v) => onChange("schutzkonzept", v || undefined)}
            disabled={!editMode}
          />

          <FormFeldDate
            label="KiK zertifiziert seit"
            value={form.kikZertifiziertSeit ?? ""}
            onChange={(v) => onChange("kikZertifiziertSeit", v || undefined)}
            disabled={!editMode}
          />

          <FormFeldDate
            label="KiK zertifiziert bis"
            value={form.kikZertifiziertBis ?? ""}
            onChange={(v) => onChange("kikZertifiziertBis", v || undefined)}
            disabled={!editMode}
          />
        </>
      )}
    </>
  );
};
