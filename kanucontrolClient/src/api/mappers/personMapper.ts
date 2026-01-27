// api/mappers/personMapper.ts
import { Person } from "@/api/types/Person";
import { PersonSave } from "@/api/types/PersonSave";

export function toPersonSave(person: Person): PersonSave {
  return {
    id: person.id,
    name: person.name,
    vorname: person.vorname,
    sex: person.sex,

    geburtsdatum: person.geburtsdatum || undefined,
    countryCode: person.countryCode,

    strasse: person.strasse || undefined,
    plz: person.plz || undefined,
    ort: person.ort || undefined,

    telefon: person.telefon || undefined,
    telefonFestnetz: person.telefonFestnetz || undefined,
    bankName: person.bankName || undefined,
    iban: person.iban || undefined,

    aktiv: person.aktiv,
  };
}
