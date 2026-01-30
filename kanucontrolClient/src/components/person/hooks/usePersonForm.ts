import { useEffect, useState } from "react";
import { PersonDetail, PersonSave } from "@/api/types/Person";
import { normalizeGermanDate } from "@/utils/dateUtils";

function mapDetailToSave(detail: PersonDetail): PersonSave {
  return {
    vorname: detail.vorname,
    name: detail.name,
    sex: detail.sex,
    email: detail.email,
    geburtsdatum: detail.geburtsdatum,
    telefon: detail.telefon,
    telefonFestnetz: detail.telefonFestnetz,
    strasse: detail.strasse,
    plz: detail.plz,
    ort: detail.ort,
    countryCode: detail.countryCode,
    bankName: detail.bankName,
    iban: detail.iban,
    aktiv: detail.aktiv,
    mitgliedschaften: detail.mitgliedschaften.map((m) => ({
      vereinId: m.verein.id,
      hauptVerein: m.hauptVerein,
      funktion: m.funktion,
    })),
  };
}

export function usePersonForm(initial?: PersonDetail | null) {
  const [form, setForm] = useState<PersonSave | null>(null);

  useEffect(() => {
    // ❗ Nur reagieren, wenn initial EXPLIZIT gesetzt wurde
    if (initial !== undefined) {
      setForm(initial ? mapDetailToSave(initial) : null);
    }
  }, [initial]);

  console.log("FORM", form);

  const reset = (value?: PersonSave | null) => {
    setForm(value ?? null);
  };

  const update = <K extends keyof PersonSave>(key: K, value: PersonSave[K]) => {
    setForm((f) => (f ? { ...f, [key]: value } : f));
  };

  const buildSavePayload = (): PersonSave | null => {
    if (!form) return null;

    return {
      ...form,
      geburtsdatum: normalizeGermanDate(form.geburtsdatum ?? "") ?? undefined,
    };
  };

  return {
    form,
    update,
    reset,
    buildSavePayload,
  };
}