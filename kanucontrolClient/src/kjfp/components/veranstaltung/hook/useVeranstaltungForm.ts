import { programErrors } from "../form/programmentwurfHtml";
import { useEffect, useRef, useState } from "react";
import { VeranstaltungFormModel } from "@/kjfp/types/veranstaltung/VeranstaltungFormModel";
import { VeranstaltungSave } from "@/kjfp/types/veranstaltung/VeranstaltungSave";
import { VeranstaltungTyp } from "@/kjfp/api/enums/VeranstaltungTyp";

/* =========================================================
   EMPTY FACTORY (CREATE DEFAULTS)
   ========================================================= */

function emptyVeranstaltung(): VeranstaltungFormModel {
  const today = new Date().toISOString().slice(0, 10);

  return {
    name: "",
    typ: VeranstaltungTyp.FM,
    kjfpAktiv: true,
    fahrtenAktiv: false,

    beginnDatum: today,
    beginnZeit: "10:00",

    endeDatum: today,
    endeZeit: "18:00",

    verein: undefined,
    leiter: undefined,

    /* ===== Detailfelder ===== */

    countryCode: undefined,
    plz: "",
    ort: "",
    unterkunftsart: undefined,
    verpflegungsmodell: undefined,

    beitragsstrukturId: undefined,
  };
}

function tage(form: VeranstaltungFormModel): number | null {
  const parse = (value: string) => {
    if (!/^\d{4}-\d{2}-\d{2}$/.test(value)) return null;
    const timestamp = Date.parse(`${value}T00:00:00Z`);
    return Number.isFinite(timestamp) && new Date(timestamp).toISOString().slice(0, 10) === value ? timestamp : null;
  };
  const beginn = parse(form.beginnDatum), ende = parse(form.endeDatum);
  return beginn === null || ende === null || ende < beginn ? null : (ende - beginn) / 86_400_000 + 1;
}

function mitAutomatischemTyp(form: VeranstaltungFormModel): VeranstaltungFormModel {
  const dauer = tage(form);
  return form.kjfpAktiv ? { ...form, typ: dauer === null ? undefined : dauer <= 4 ? VeranstaltungTyp.FM : VeranstaltungTyp.JEM } : form;
}

/* =========================================================
   HOOK
   ========================================================= */

export function useVeranstaltungForm(initial: VeranstaltungFormModel | null) {
  const baseline = useRef("");
  const [form, setForm] = useState<VeranstaltungFormModel | null>(null);

  /* =========================
     INIT (EDIT)
     ========================= */

  useEffect(() => {
    if (!initial) {
      setForm(null);
      return;
    }

    const loaded = mitAutomatischemTyp({
      ...initial,

      /* ===== Null → Default ===== */

      programmentwurfTitel: initial.programmentwurfTitel ?? "",
      programmentwurfInhalt: initial.programmentwurfInhalt ?? "",
      countryCode: initial.countryCode ?? undefined,

      plz: initial.plz ?? "",
      ort: initial.ort ?? "",
      unterkunftsart: initial.unterkunftsart,
      verpflegungsmodell: initial.verpflegungsmodell,
      beitragsstrukturId: initial.beitragsstrukturId ?? undefined,
    });
    baseline.current = JSON.stringify(loaded);
    setForm(loaded);
  }, [initial]);

  /* =========================
     CREATE RESET
     ========================= */

  const reset = (data?: Partial<VeranstaltungFormModel>) => {
    const loaded = mitAutomatischemTyp({ ...emptyVeranstaltung(), ...data });
    baseline.current = JSON.stringify(loaded);
    setForm(loaded);
  };

  /* =========================
     FIELD UPDATE
     ========================= */

  const update = <K extends keyof VeranstaltungFormModel>(
    key: K,
    value: VeranstaltungFormModel[K],
  ) => {
    setForm((f) => (f ? mitAutomatischemTyp({ ...f, [key]: value }) : f));
  };

  /* =========================
     BUILD SAVE PAYLOAD
     ========================= */

  const buildSavePayload = (): VeranstaltungSave | null => {
    if (!form) return null;

    if (validationErrors.length > 0 || !form.verein) return null;

    return {
      id: form.id,
      kopieVonVeranstaltungId: form.kopieVonVeranstaltungId,
      individuelleGebuehren: form.individuelleGebuehren,
      standardGebuehr: form.standardGebuehr,
      scope: form.scope,
      name: form.name,
      programmentwurfTitel: form.programmentwurfTitel ?? "",
      programmentwurfInhalt: form.programmentwurfInhalt ?? "",
      typ: form.kjfpAktiv ? form.typ : undefined,
      kjfpAktiv: form.kjfpAktiv,
      fahrtenAktiv: form.fahrtenAktiv,
      beschreibung: form.beschreibung,

      beginnDatum: form.beginnDatum,
      beginnZeit: form.kjfpAktiv ? form.beginnZeit : undefined,

      endeDatum: form.endeDatum,
      endeZeit: form.kjfpAktiv ? form.endeZeit : undefined,

      vereinId: form.verein.id,
      leiterId: form.kjfpAktiv ? form.leiter?.id : undefined,

      /* ===== Detailfelder ===== */

      countryCode: form.countryCode,
      plz: form.plz || undefined,
      ort: form.ort || undefined,
      unterkunftsartId: form.unterkunftsart?.id,
      verpflegungsmodellId: form.verpflegungsmodell?.id,

      beitragsstrukturId: form.beitragsstrukturId ?? undefined,
    };
  };

  const dauer = form ? tage(form) : null;
  const isEndTimeValid = !form?.kjfpAktiv || !form.beginnZeit || !form.endeZeit
    || form.beginnDatum !== form.endeDatum || form.endeZeit >= form.beginnZeit;
  const validationErrors: string[] = [];
  if (form) {
    validationErrors.push(...programErrors(form.programmentwurfTitel, form.programmentwurfInhalt));
    if (form.name.trim().length < 2) validationErrors.push("Bitte eine Bezeichnung mit mindestens zwei Zeichen eingeben.");
    if (!form.beginnDatum) validationErrors.push("Bitte das Beginndatum eingeben.");
    if (!form.endeDatum) validationErrors.push("Bitte das Enddatum eingeben.");
    if (form.beginnDatum && form.endeDatum && dauer === null) validationErrors.push("Bitte gültige Datumsangaben eingeben; das Ende darf nicht vor dem Beginn liegen.");
    if (!form.verein) validationErrors.push("Bitte einen Verein auswählen.");
    if (form.kjfpAktiv) {
      if (!form.leiter) validationErrors.push("Für KJFP bitte eine Leitung auswählen.");
      if (!form.beginnZeit) validationErrors.push("Für KJFP bitte die Beginnzeit eingeben.");
      if (!form.endeZeit) validationErrors.push("Für KJFP bitte die Endzeit eingeben.");
    }
    if (!isEndTimeValid) validationErrors.push("Die Endzeit darf nicht vor der Beginnzeit liegen.");
  }
  const isValid = !!form && validationErrors.length === 0;

  /* =========================
     EXPORT
     ========================= */

  return {
    form,
    update,
    reset,
    buildSavePayload,
    isDirty: !!form && JSON.stringify(form) !== baseline.current,
    isValid,
    isEndTimeValid,
    validationErrors,
  };
}
