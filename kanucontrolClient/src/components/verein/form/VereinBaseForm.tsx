import React from "react";
import { FormFeld } from "@/components/common/FormFeld";

import Verein from "@/api/types/VereinFormModel";

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

      <FormFeld
        label="PLZ"
        value={form.plz ?? ""}
        onChange={(v) => onChange("plz", v)}
        disabled={!editMode}
      />

      <FormFeld
        label="Ort"
        value={form.ort ?? ""}
        onChange={(v) => onChange("ort", v)}
        disabled={!editMode}
      />

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
            label="Kontoinhaber"
            value={
              form.kontoinhaber ? `${form.kontoinhaber.name}, ${form.kontoinhaber.vorname}` : ""
            }
            disabled
          />
        </>
      )}
    </>
  );
};
