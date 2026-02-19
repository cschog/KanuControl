import React from "react";
import { MenuItem, TextField, FormControlLabel, Switch } from "@mui/material";
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
  detailMode?: boolean; // ⭐ NEU
  onChange: <K extends keyof VeranstaltungFormModel>(
    key: K,
    value: VeranstaltungFormModel[K],
  ) => void;
}

export const VeranstaltungBaseForm: React.FC<Props> = ({
  form,
  editMode,
  detailMode = false, // ⭐ Default
  onChange,
}) => {
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
        label="Leitung"
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
        onChange={(v) => onChange("beginnDatum", v ?? "")}
      />

      <FormFeldTimePicker
        label="Beginn Zeit"
        value={form.beginnZeit}
        disabled={!editMode}
        onChange={(v) => onChange("beginnZeit", v ?? "")}
      />

      {/* Ende */}
      <FormFeldDatePicker
        label="Ende Datum"
        value={form.endeDatum}
        disabled={!editMode}
        onChange={(v) => onChange("endeDatum", v ?? "")}
      />

      <FormFeldTimePicker
        label="Ende Zeit"
        value={form.endeZeit}
        disabled={!editMode}
        onChange={(v) => onChange("endeZeit", v ?? "")}
      />
      {detailMode && (
        <>
          {/* ================= Unterkunft ================= */}
          <FormFeld
            label="Art der Unterkunft"
            value={form.artDerUnterkunft ?? ""}
            disabled={!editMode}
            onChange={(v) => onChange("artDerUnterkunft", v || undefined)}
          />

          {/* ================= Verpflegung ================= */}
          <FormFeld
            label="Art der Verpflegung"
            value={form.artDerVerpflegung ?? ""}
            disabled={!editMode}
            onChange={(v) => onChange("artDerVerpflegung", v || undefined)}
          />

          {/* ================= Ort ================= */}
          <FormFeld
            label="PLZ"
            value={form.plz ?? ""}
            disabled={!editMode}
            onChange={(v) => onChange("plz", v || undefined)}
          />

          <FormFeld
            label="Ort"
            value={form.ort ?? ""}
            disabled={!editMode}
            onChange={(v) => onChange("ort", v || undefined)}
          />

          {/* ================= Individuelle Gebühren ================= */}
          <FormControlLabel
            control={
              <Switch
                checked={form.individuelleGebuehren ?? false}
                disabled={!editMode}
                onChange={(e) => onChange("individuelleGebuehren", e.target.checked)}
              />
            }
            label="Individuelle Gebühren"
          />
          <FormFeld
            label="Standardgebühr"
            value={form.standardGebuehr ?? ""}
            disabled={!editMode}
            onChange={(v) => onChange("standardGebuehr", Number(v))}
            type="number"
          />

          {/* ================= Planung ================= */}
          <FormFeld
            label="Geplante TN männlich"
            value={form.geplanteTeilnehmerMaennlich ?? ""}
            disabled={!editMode}
            onChange={(v) => onChange("geplanteTeilnehmerMaennlich", Number(v))}
            type="number"
          />

          <FormFeld
            label="Geplante TN weiblich"
            value={form.geplanteTeilnehmerWeiblich ?? ""}
            disabled={!editMode}
            onChange={(v) => onChange("geplanteTeilnehmerWeiblich", Number(v))}
            type="number"
          />

          <FormFeld
            label="Geplante TN divers"
            value={form.geplanteTeilnehmerDivers ?? ""}
            disabled={!editMode}
            onChange={(v) => onChange("geplanteTeilnehmerDivers", Number(v))}
            type="number"
          />
        </>
      )}
    </>
  );
};
