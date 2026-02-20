import React from "react";
import { MenuItem, TextField, FormControlLabel, Switch } from "@mui/material";
import { VeranstaltungFormModel } from "@/api/types/VeranstaltungFormModel";
import { VeranstaltungTyp } from "@/api/enums/VeranstaltungTyp";
import { VereinAutocomplete } from "@/components/verein/VereinAutocomplete";
import { PersonAutocomplete } from "@/components/person/PersonAutocomplete";
import { FormFeld } from "@/components/common/FormFeld";
import { FormFeldDatePicker } from "@/components/common/FormFeldDatePicker";
import { FormFeldTimePicker } from "@/components/common/FormFeldTimePicker";
import { COUNTRIES } from "@/api/enums/CountryCode";


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
          <TextField
            select
            fullWidth
            size="small"
            label="Land"
            value={form.laenderCode ?? ""}
            disabled={!editMode}
            onChange={(e) => {
              const value = (e.target.value || undefined) as VeranstaltungFormModel["laenderCode"];
              onChange("laenderCode", value);
            }}
          >
            {COUNTRIES.map((c) => (
              <MenuItem key={c.code} value={c.code}>
                {c.label}
              </MenuItem>
            ))}
          </TextField>

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
                onChange={(e) => {
                  const checked = e.target.checked;

                  onChange("individuelleGebuehren", checked);

                  if (checked) {
                    onChange("standardGebuehr", undefined);
                  }
                }}
              />
            }
            label="Individuelle Gebühren"
          />
          <FormFeld
            label="Teilnehmergebühr"
            value={form.standardGebuehr ?? ""}
            disabled={!editMode || form.individuelleGebuehren}
            onChange={(v) => onChange("standardGebuehr", v ? Number(v) : undefined)}
            type="number"
            helperText={
              form.individuelleGebuehren
                ? "Gebühr wird individuell pro Teilnehmer erfasst"
                : undefined
            }
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
