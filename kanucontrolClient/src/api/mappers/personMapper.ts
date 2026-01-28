import { PersonDetail, PersonSave } from "@/api/types/Person";
import { MitgliedSaveInPerson } from "@/api/types/Mitglied";

/**
 * 🔁 PersonDetail → PersonSave
 * Wird für EDIT, RELOAD, CANCEL verwendet
 */
export function toPersonSaveDTO(person: PersonDetail): PersonSave {
  return {
    vorname: person.vorname,
    name: person.name,
    sex: person.sex,
    geburtsdatum: person.geburtsdatum,

    telefon: person.telefon,
    telefonFestnetz: person.telefonFestnetz,
    strasse: person.strasse,
    plz: person.plz,
    ort: person.ort,
    countryCode: person.countryCode,

    bankName: person.bankName,
    iban: person.iban,

    aktiv: person.aktiv,

    mitgliedschaften:
      person.mitgliedschaften?.map<MitgliedSaveInPerson>((m) => ({
        vereinId: m.verein.id,
        hauptVerein: m.hauptVerein,
        funktion: m.funktion,
      })) ?? [],
  };
}
