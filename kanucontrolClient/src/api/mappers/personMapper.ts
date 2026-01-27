// src/api/mapper/personMapper.ts

import { PersonDetail } from "@/api/types/PersonDetail";
import { PersonSaveDTO } from "@/api/types/PersonSaveDTO";

export function toPersonSaveDTO(person: PersonDetail): PersonSaveDTO {
  return {
    vorname: person.vorname,
    name: person.name,
    geburtsdatum: person.geburtsdatum,
    sex: person.sex,
    telefon: person.telefon,
    telefonFestnetz: person.telefonFestnetz,
    strasse: person.strasse,
    plz: person.plz,
    ort: person.ort,
    countryCode: person.countryCode,
    bankName: person.bankName,
    iban: person.iban,

    mitgliedschaften:
      person.mitgliedschaften?.map((m) => ({
        vereinId: m.verein.id, // 🔑 NUR ID
        hauptVerein: m.hauptVerein,
        funktion: m.funktion,
      })) ?? [],
  };
}
