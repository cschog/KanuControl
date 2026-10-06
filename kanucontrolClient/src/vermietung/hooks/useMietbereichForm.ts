import { useEffect, useState } from "react";

import type { Mietbereich } from "@/vermietung/types/Mietbereich";
import type { MietbereichSave } from "@/vermietung/types/MietbereichSave";

const emptyForm: MietbereichSave = {
  bezeichnung: "",
  beschreibung: "",
  mietbar: true,
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
    if (!form) {
      return null;
    }

    if (!form.bezeichnung.trim()) {
      return null;
    }

    return {
      ...form,
      bezeichnung: form.bezeichnung.trim(),
      beschreibung: form.beschreibung.trim(),
    };
  }

  return {
    form,
    update,
    buildSavePayload,
  };
}
