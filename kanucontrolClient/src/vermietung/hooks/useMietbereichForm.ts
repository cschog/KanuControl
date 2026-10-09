import { useEffect, useState } from "react";

import type { Mietbereich } from "@/vermietung/types/Mietbereich";
import type { MietbereichSave } from "@/vermietung/types/MietbereichSave";

const emptyForm: MietbereichSave = {
  bezeichnung: "",
  beschreibung: "",
  mietbar: true,
  direktbuchungAktiv: true,
  airbnbAktiv: false,
  bestand: 1,
  mengeneinheit: "",
};

export function useMietbereichForm(mietbereich: Mietbereich | null) {
  const [form, setForm] = useState<MietbereichSave | null>(null);

  useEffect(() => {
    if (!mietbereich) {
      setForm({ ...emptyForm });
      return;
    }

    setForm({
      bezeichnung: mietbereich.bezeichnung ?? "",
      beschreibung: mietbereich.beschreibung ?? "",
      mietbar: mietbereich.mietbar,
      direktbuchungAktiv: mietbereich.direktbuchungAktiv ?? true,
      airbnbAktiv: mietbereich.airbnbAktiv ?? false,
      bestand: mietbereich.bestand ?? 1,
      mengeneinheit: mietbereich.mengeneinheit ?? "",
    });
  }, [mietbereich]);

  function update<K extends keyof MietbereichSave>(key: K, value: MietbereichSave[K]) {
    setForm((current) =>
      current
        ? {
            ...current,
            [key]: value,
          }
        : current,
    );
  }

  function buildSavePayload(): MietbereichSave | null {
    if (!form || !form.bezeichnung.trim()) {
      return null;
    }

    if (!Number.isInteger(form.bestand) || form.bestand < 1) {
      return null;
    }

    return {
      ...form,
      bezeichnung: form.bezeichnung.trim(),
      beschreibung: form.beschreibung.trim(),
      mengeneinheit: form.mengeneinheit.trim(),
    };
  }

  return {
    form,
    update,
    buildSavePayload,
  };
}
