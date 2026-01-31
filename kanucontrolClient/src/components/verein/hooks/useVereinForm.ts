// src/components/verein/hooks/useVereinForm.ts
import { useEffect, useState } from "react";
import Verein from "@/api/types/VereinFormModel";
import { VereinSave } from "@/api/types/VereinSave";
import { PersonRef } from "@/api/types/PersonRef";

function emptyVerein(): Verein {
  return {
    name: "",
    abk: "",
    plz: "",
    ort: "",
    kontoinhaber: undefined,
  };
}

export function useVereinForm(initial: Verein | null) {
  const [form, setForm] = useState<Verein | null>(null);

  useEffect(() => {
    setForm(initial ? { ...initial } : null);
  }, [initial]);

  const reset = () => setForm(emptyVerein());

  const update = <K extends keyof Verein>(key: K, value: Verein[K]) => {
    setForm((f) => (f ? { ...f, [key]: value } : f));
  };

  const setKontoinhaber = (person?: PersonRef) => {
    setForm((f) => (f ? { ...f, kontoinhaber: person } : f));
  };

  const buildSavePayload = (): VereinSave | null => {
    if (!form) return null;

    return {
      id: form.id,
      name: form.name,
      abk: form.abk,
      strasse: form.strasse,
      plz: form.plz,
      ort: form.ort,
      telefon: form.telefon,
      bankName: form.bankName,
      iban: form.iban,
      kontoinhaberId: form.kontoinhaber?.id, // 🔑 HIER
    };
  };

  const isValid = !!form && form.name.trim().length >= 2 && form.abk.trim().length >= 1;

  return {
    form,
    update,
    reset,
    setKontoinhaber,
    buildSavePayload,
    isValid,
  };
}
