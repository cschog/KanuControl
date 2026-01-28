// src/api/mapper/personMapper.ts

import { PersonDetail, PersonSave } from "@/api/types/Person";
import { MitgliedSaveInPerson } from "@/api/types/Mitglied";

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

    mitgliedschaften:
      person.mitgliedschaften?.map<MitgliedSaveInPerson>((m) => ({
        vereinId: m.verein.id,
        hauptVerein: m.hauptVerein,
        funktion: m.funktion,
      })) ?? [],
  };
}