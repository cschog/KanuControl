// src/components/veranstaltung/form/VeranstaltungBaseForm.tsx
import React from "react";
import { TextField, MenuItem } from "@mui/material";
import { VeranstaltungFormModel } from "@/api/types/VeranstaltungFormModel";
import { VeranstaltungTyp } from "@/api/enums/VeranstaltungTyp";

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
      <TextField
        label="Bezeichnung"
        value={form.name}
        disabled={!editMode}
        onChange={(e) => onChange("name", e.target.value)}
        fullWidth
      />

      {/* ================= Typ ================= */}
      <TextField
        select
        label="Typ"
        value={form.typ}
        disabled={!editMode}
        onChange={(e) => onChange("typ", e.target.value as VeranstaltungTyp)}
        fullWidth
      >
        <MenuItem value={VeranstaltungTyp.JEM}>JEM</MenuItem>
        <MenuItem value={VeranstaltungTyp.FM}>FM</MenuItem>
        <MenuItem value={VeranstaltungTyp.BM}>BM</MenuItem>
        <MenuItem value={VeranstaltungTyp.GV}>GV</MenuItem>
      </TextField>

      {/* ================= Beginn ================= */}
      <TextField
        type="date"
        label="Beginn Datum"
        value={form.beginnDatum ?? ""}
        disabled={!editMode}
        InputLabelProps={{ shrink: true }}
        onChange={(e) => onChange("beginnDatum", e.target.value)}
      />

      <TextField
        type="time"
        label="Beginn Zeit"
        value={form.beginnZeit ?? ""}
        disabled={!editMode}
        InputLabelProps={{ shrink: true }}
        onChange={(e) => onChange("beginnZeit", e.target.value)}
      />

      {/* ================= Ende ================= */}
      <TextField
        type="date"
        label="Ende Datum"
        value={form.endeDatum ?? ""}
        disabled={!editMode}
        InputLabelProps={{ shrink: true }}
        onChange={(e) => onChange("endeDatum", e.target.value)}
      />

      <TextField
        type="time"
        label="Ende Zeit"
        value={form.endeZeit ?? ""}
        disabled={!editMode}
        InputLabelProps={{ shrink: true }}
        onChange={(e) => onChange("endeZeit", e.target.value)}
      />
    </>
  );
};
