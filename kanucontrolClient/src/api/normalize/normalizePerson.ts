// api/normalize/normalizePerson.ts
import { PersonSave } from "@/api/types/PersonSave";

export type PersonMode = "create" | "update";

export function normalizePersonPayload(raw: PersonSave, mode: PersonMode): PersonSave {
  const p: PersonSave = { ...raw };

  if (mode === "create") {
    delete p.id;
  }

  if (mode === "update" && p.id == null) {
    throw new Error("normalizePersonPayload(update): id is required");
  }

  const emptyToUndefined = <T>(v: T | "" | null | undefined): T | undefined =>
    v === "" || v === null ? undefined : v;

  p.geburtsdatum = emptyToUndefined(p.geburtsdatum);
  p.countryCode = emptyToUndefined(p.countryCode);
  p.iban = emptyToUndefined(p.iban);
  p.bankName = emptyToUndefined(p.bankName);
  p.telefon = emptyToUndefined(p.telefon);
  p.telefonFestnetz = emptyToUndefined(p.telefonFestnetz);
  p.strasse = emptyToUndefined(p.strasse);
  p.plz = emptyToUndefined(p.plz);
  p.ort = emptyToUndefined(p.ort);

  if (!p.name || !p.vorname || !p.sex) {
    throw new Error("normalizePersonPayload: missing required fields");
  }

  return p;
}
