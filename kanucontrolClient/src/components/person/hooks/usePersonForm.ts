import { PersonDetail, PersonSave } from "@/api/types/Person";
import { normalizeGermanDate } from "@/utils/dateUtils";
import { useEntityForm } from "@/components/common/hooks/useEntityForm";

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

function emptyPerson(): PersonSave {
  return {
    vorname: "",
    name: "",
    sex: "W",
    aktiv: true,
    mitgliedschaften: [],
  };
}

export function usePersonForm(initial?: PersonDetail | null) {
  return useEntityForm<PersonDetail, PersonSave>(
    initial,
    mapDetailToSave,
    emptyPerson,
    (form) => ({
      ...form,
      geburtsdatum: normalizeGermanDate(form.geburtsdatum ?? "") ?? undefined,
    }),
    (form) => form.vorname.trim().length > 0 && form.name.trim().length > 0,
  );
}
