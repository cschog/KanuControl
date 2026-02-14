import React from "react";
import { MenuItem, TextField } from "@mui/material";
import { VeranstaltungFormModel } from "@/api/types/VeranstaltungFormModel";
import { VeranstaltungTyp } from "@/api/enums/VeranstaltungTyp";
import { VereinAutocomplete } from "@/components/verein/VereinAutocomplete";
import { PersonAutocomplete } from "@/components/person/PersonAutocomplete";
import { FormFeld } from "@/components/common/FormFeld";
import { FormFeldDatePicker } from "@/components/common/FormFeldDatePicker";
import { FormFeldTimePicker } from "@/components/common/FormFeldTimePicker";


interface Props {
  form: VeranstaltungFormModel;
  editMode: boolean;
  onChange: <K extends keyof VeranstaltungFormModel>(
    key: K,
    value: VeranstaltungFormModel[K],
  ) => void;
}

export const VeranstaltungBaseForm: React.FC<Props> = ({ form, editMode, onChange }) => {
  return (
    <>
      {/* ================= Name ================= */}
      <FormFeld
        label="Bezeichnung"
        value={form.name}
        disabled={!editMode}
        onChange={(v) => onChange("name", v)}
      />

      {/* ================= Typ ================= */}
      <TextField
        select
        fullWidth
        size="small"
        label="Typ"
        value={form.typ ?? ""}
        disabled={!editMode}
        onChange={(e) => onChange("typ", e.target.value as VeranstaltungTyp)}
      >
        <MenuItem value={VeranstaltungTyp.JEM}>JEM</MenuItem>
        <MenuItem value={VeranstaltungTyp.FM}>FM</MenuItem>
        <MenuItem value={VeranstaltungTyp.BM}>BM</MenuItem>
        <MenuItem value={VeranstaltungTyp.GV}>GV</MenuItem>
      </TextField>

      {/* ================= Leiter ================= */}
      <PersonAutocomplete
        value={form.leiter}
        disabled={!editMode}
        onChange={(v) => onChange("leiter", v)}
      />

      {/* ================= Verein ================= */}
      <VereinAutocomplete
        value={form.verein}
        disabled={!editMode}
        onChange={(v) => onChange("verein", v)}
      />

      {/* Beginn */}
      <FormFeldDatePicker
        label="Beginn Datum"
        value={form.beginnDatum}
        disabled={!editMode}
        onChange={(v) => onChange("beginnDatum", v)}
      />

      <FormFeldTimePicker
        label="Beginn Zeit"
        value={form.beginnZeit}
        disabled={!editMode}
        onChange={(v) => onChange("beginnZeit", v)}
      />

      {/* Ende */}
      <FormFeldDatePicker
        label="Ende Datum"
        value={form.endeDatum}
        disabled={!editMode}
        onChange={(v) => onChange("endeDatum", v)}
      />

      <FormFeldTimePicker
        label="Ende Zeit"
        value={form.endeZeit}
        disabled={!editMode}
        onChange={(v) => onChange("endeZeit", v)}
      />
    </>
  );
};
