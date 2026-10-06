import { useEffect, useState } from "react";

import type { Mietobjekt } from "@/vermietung/types/Mietobjekt";
import type { MietobjektSave } from "@/vermietung/types/MietobjektSave";

const emptyForm: MietobjektSave = {
  bezeichnung: "",
  beschreibung: "",
  strasse: "",
  plz: "",
  ort: "",
  countryCode: "DE",
  aktiv: true,
  mietbar: true,
};

export function useMietobjektForm(mietobjekt: Mietobjekt | null) {
  const [form, setForm] = useState<MietobjektSave | null>(null);

  useEffect(() => {
    if (!mietobjekt) {
      setForm({ ...emptyForm });
      return;
    }

    setForm({
      bezeichnung: mietobjekt.bezeichnung ?? "",
      beschreibung: mietobjekt.beschreibung ?? "",
      strasse: mietobjekt.strasse ?? "",
      plz: mietobjekt.plz ?? "",
      ort: mietobjekt.ort ?? "",
      countryCode: mietobjekt.countryCode ?? "DE",
      aktiv: mietobjekt.aktiv,
      mietbar: mietobjekt.mietbar,
    });
  }, [mietobjekt]);

  function update<K extends keyof MietobjektSave>(key: K, value: MietobjektSave[K]) {
    setForm((current) =>
      current
        ? {
            ...current,
            [key]: value,
          }
        : current,
    );
  }

  function buildSavePayload(): MietobjektSave | null {
    if (!form) {
      return null;
    }

    if (!form.bezeichnung.trim()) {
      return null;
    }

    return {
      ...form,
      bezeichnung: form.bezeichnung.trim(),
    };
  }

  return {
    form,
    update,
    buildSavePayload,
  };
}
