import { useState, useEffect, useCallback } from "react";
import { Verein } from "@/api/types/Verein";
import { FormFeld } from "@/components/common/FormFeld";
import { EntityEditForm } from "@/components/common/EntityEditForm";
import { FormGrid } from "@/components/common/FormGrid";

interface Props {
  verein?: Verein;
  onSpeichern: (v: Verein) => Promise<void>;
  onAbbruch: () => void;
}

export function VereinEditForm({ verein, onSpeichern, onAbbruch }: Props) {
  const [state, setState] = useState<Verein>({
    name: "",
    abk: "",
    strasse: "",
    plz: "",
    ort: "",
    telefon: "",
    bankName: "",
    iban: "",
  });

  useEffect(() => {
    if (verein) setState(verein);
  }, [verein]);

  const save = useCallback(async () => {
    if (!verein) return false;
    await onSpeichern({ ...state, id: verein.id });
    return true;
  }, [state, verein, onSpeichern]);

  return (
    <EntityEditForm
      title={verein?.id ? "Verein bearbeiten" : "Neuen Verein erstellen"}
      onSave={save}
      onCancel={onAbbruch}
    >
      <FormGrid>
        <FormFeld label="Abkürzung" value={state.abk} onChange={(v) => setState(s => ({ ...s, abk: v }))} />
        <FormFeld label="Vereinsname" value={state.name} onChange={(v) => setState(s => ({ ...s, name: v }))} />
        <FormFeld label="Straße" value={state.strasse} onChange={(v) => setState(s => ({ ...s, strasse: v }))} />
        <FormFeld label="PLZ" value={state.plz} onChange={(v) => setState(s => ({ ...s, plz: v }))} />
        <FormFeld label="Ort" value={state.ort} onChange={(v) => setState(s => ({ ...s, ort: v }))} />
        <FormFeld label="Telefon" value={state.telefon} onChange={(v) => setState(s => ({ ...s, telefon: v }))} />
        <FormFeld label="Bank" value={state.bankName} onChange={(v) => setState(s => ({ ...s, bankName: v }))} />
        <FormFeld label="IBAN" value={state.iban} onChange={(v) => setState(s => ({ ...s, iban: v }))} />
      </FormGrid>
    </EntityEditForm>
  );
}