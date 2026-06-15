import React from "react";
import { MenuItem, TextField, FormControlLabel, Switch } from "@mui/material";

import { VeranstaltungFormModel } from "@/api/types/VeranstaltungFormModel";
import { VeranstaltungTyp } from "@/api/enums/VeranstaltungTyp";
// import { VeranstaltungScope } from "@/api/enums/VeranstaltungScope";

import { VereinAutocomplete } from "@/components/verein/VereinAutocomplete";
import { PersonAutocomplete } from "@/components/person/PersonAutocomplete";

import { FormFeld } from "@/components/common/FormFeld";
import { FormFeldDatePicker } from "@/components/common/FormFeldDatePicker";
import { FormFeldTimePicker } from "@/components/common/FormFeldTimePicker";

import { COUNTRIES } from "@/api/enums/CountryCode";
import PostalCodeAutocomplete from "@/components/common/PostalCodeAutocomplete";

/* =========================================================
   TYPES
   ========================================================= */

interface BeitragsstrukturDTO {
  id: number;
  name: string;
}

interface Props {
  form: VeranstaltungFormModel;

  editMode: boolean;

  detailMode?: boolean;

  beitragsstrukturen: BeitragsstrukturDTO[];

  onChange: <K extends keyof VeranstaltungFormModel>(
    key: K,
    value: VeranstaltungFormModel[K],
  ) => void;
}

/* =========================================================
   COMPONENT
   ========================================================= */

export const VeranstaltungBaseForm: React.FC<Props> = ({
  form,
  editMode,
  detailMode = false,
  beitragsstrukturen,
  onChange,
}) => {
  return (
    <>
      {/* ================= NAME ================= */}

      <FormFeld
        label="Bezeichnung"
        value={form.name}
        disabled={!editMode}
        onChange={(v) => onChange("name", v)}
      />

      {/* ================= TYP ================= */}

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

      {/* ================= LEITUNG ================= */}

      <PersonAutocomplete
        label="Leitung"
        value={form.leiter}
        disabled={!editMode}
        onChange={(v) => onChange("leiter", v)}
      />

      {/* ================= VEREIN ================= */}

      <VereinAutocomplete
        value={form.verein}
        disabled={!editMode}
        onChange={(v) => onChange("verein", v)}
      />

      {/* ================= SCOPE ================= */}

      {/* <TextField
        select
        fullWidth
        size="small"
        label="Scope"
        value={form.scope ?? ""}
        disabled={!editMode}
        onChange={(e) => onChange("scope", e.target.value as VeranstaltungScope)}
      >
        <MenuItem value={VeranstaltungScope.VERBAND}>Verband</MenuItem>

        <MenuItem value={VeranstaltungScope.VEREIN}>Verein</MenuItem>
      </TextField> */}

      {/* ================= BEGINN ================= */}

      <FormFeldDatePicker
        label="Beginn Datum"
        value={form.beginnDatum}
        disabled={!editMode}
        onChange={(v) => {
          const date = v ?? "";

          onChange("beginnDatum", date);

          // Wenn Ende identisch oder leer → mitziehen
          if (!form.endeDatum || form.endeDatum === form.beginnDatum) {
            onChange("endeDatum", date);
          }
        }}
      />

      <FormFeldTimePicker
        label="Beginn Zeit"
        value={form.beginnZeit}
        disabled={!editMode}
        onChange={(v) => onChange("beginnZeit", v ?? "")}
      />

      {/* ================= ENDE ================= */}

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
        error={form.beginnDatum === form.endeDatum && form.endeZeit < form.beginnZeit}
        helperText={
          form.beginnDatum === form.endeDatum && form.endeZeit < form.beginnZeit
            ? "Ende darf nicht vor Beginn liegen"
            : undefined
        }
      />

      {/* =====================================================
         DETAIL MODE
         ===================================================== */}

      {detailMode && (
        <>
          {/* ================= UNTERKUNFT ================= */}

          <FormFeld
            label="Art der Unterkunft"
            value={form.artDerUnterkunft ?? ""}
            disabled={!editMode}
            onChange={(v) => onChange("artDerUnterkunft", v || undefined)}
          />

          {/* ================= VERPFLEGUNG ================= */}

          <FormFeld
            label="Art der Verpflegung"
            value={form.artDerVerpflegung ?? ""}
            disabled={!editMode}
            onChange={(v) => onChange("artDerVerpflegung", v || undefined)}
          />

          {/* ================= LAND ================= */}

          <TextField
            select
            fullWidth
            size="small"
            label="Land"
            value={form.countryCode ?? ""}
            disabled={!editMode}
            onChange={(e) => {
              const value = (e.target.value || undefined) as VeranstaltungFormModel["countryCode"];

              onChange("countryCode", value);
            }}
          >
            {COUNTRIES.map((c) => (
              <MenuItem key={c.code} value={c.code}>
                {c.label}
              </MenuItem>
            ))}
          </TextField>

          {/* ================= PLZ ================= */}

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
          {/* ================= Ort ================= */}

          <FormFeld
            label="Ort"
            value={form.ort ?? ""}
            onChange={(v) => onChange("ort", v)}
            disabled={!editMode}
          />

          {/* ================= INDIVIDUELLE GEBÜHREN ================= */}

          <FormControlLabel
            control={
              <Switch
                checked={form.individuelleGebuehren ?? false}
                disabled={!editMode}
                onChange={(e) => {
                  const checked = e.target.checked;

                  onChange("individuelleGebuehren", checked);

                  // Standardgebühr entfernen
                  if (checked) {
                    onChange("standardGebuehr", undefined);
                  }
                }}
              />
            }
            label="Individuelle Gebühren"
          />

          {/* ================= BEITRAGSSTRUKTUR ================= */}

          <TextField
            select
            fullWidth
            size="small"
            label="Beitragsstruktur"
            value={form.beitragsstrukturId ?? ""}
            disabled={!editMode}
            onChange={(e) =>
              onChange("beitragsstrukturId", e.target.value ? Number(e.target.value) : undefined)
            }
          >
            <MenuItem value="">Keine</MenuItem>

            {beitragsstrukturen.map((s) => (
              <MenuItem key={s.id} value={s.id}>
                {s.name}
              </MenuItem>
            ))}
          </TextField>

          {/* ================= STANDARDGEBÜHR ================= */}

          <FormFeld
            label="Teilnehmergebühr (Euro)"
            value={form.standardGebuehr ?? ""}
            disabled={!editMode || form.individuelleGebuehren}
            onChange={(v) => onChange("standardGebuehr", v ? Number(v) : undefined)}
            type="number"
            helperText={
              form.individuelleGebuehren
                ? "Gebühr wird individuell pro Teilnehmer berechnet"
                : undefined
            }
          />

          {/* ================= PLANUNG ================= */}

          <FormFeld
            label="Plan: geförderte TN männlich"
            value={form.geplanteTeilnehmerMaennlich ?? ""}
            disabled={!editMode}
            onChange={(v) => onChange("geplanteTeilnehmerMaennlich", Number(v))}
            type="number"
          />

          <FormFeld
            label="Plan: geförderte TN weiblich"
            value={form.geplanteTeilnehmerWeiblich ?? ""}
            disabled={!editMode}
            onChange={(v) => onChange("geplanteTeilnehmerWeiblich", Number(v))}
            type="number"
          />

          <FormFeld
            label="Plan: geförderte TN divers"
            value={form.geplanteTeilnehmerDivers ?? ""}
            disabled={!editMode}
            onChange={(v) => onChange("geplanteTeilnehmerDivers", Number(v))}
            type="number"
          />

          <FormFeld
            label="Plan: Mitarbeiter männlich"
            value={form.geplanteMitarbeiterMaennlich ?? ""}
            disabled={!editMode}
            onChange={(v) =>
              onChange("geplanteMitarbeiterMaennlich", v === "" ? undefined : Number(v))
            }
            type="number"
          />

          <FormFeld
            label="Plan: Mitarbeiter weiblich"
            value={form.geplanteMitarbeiterWeiblich ?? ""}
            disabled={!editMode}
            onChange={(v) =>
              onChange("geplanteMitarbeiterWeiblich", v === "" ? undefined : Number(v))
            }
            type="number"
          />

          <FormFeld
            label="Plan: Mitarbeiter divers"
            value={form.geplanteMitarbeiterDivers ?? ""}
            disabled={!editMode}
            onChange={(v) =>
              onChange("geplanteMitarbeiterDivers", v === "" ? undefined : Number(v))
            }
            type="number"
          />
        </>
      )}
    </>
  );
};
