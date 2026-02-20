import { useEffect, useState } from "react";
import { VeranstaltungFormModel } from "@/api/types/VeranstaltungFormModel";
import { VeranstaltungSave } from "@/api/types/VeranstaltungSave";
import { VeranstaltungTyp } from "@/api/enums/VeranstaltungTyp";


/* =========================================================
   EMPTY FACTORY (CREATE DEFAULTS)
   ========================================================= */

function emptyVeranstaltung(): VeranstaltungFormModel {
  const today = new Date().toISOString().slice(0, 10);

  return {
    name: "",
    typ: VeranstaltungTyp.JEM,

    beginnDatum: today,
    beginnZeit: "10:00",

    endeDatum: today,
    endeZeit: "18:00",

    verein: undefined,
    leiter: undefined,

    /* ===== Detailfelder ===== */

    laenderCode: undefined,
    plz: "",
    ort: "",
    artDerUnterkunft: "",
    artDerVerpflegung: "",

    individuelleGebuehren: false,
    standardGebuehr: undefined,

    geplanteTeilnehmerMaennlich: undefined,
    geplanteTeilnehmerWeiblich: undefined,
    geplanteTeilnehmerDivers: undefined,

    geplanteMitarbeiterMaennlich: undefined,
    geplanteMitarbeiterWeiblich: undefined,
    geplanteMitarbeiterDivers: undefined,
  };
}

/* =========================================================
   HOOK
   ========================================================= */

export function useVeranstaltungForm(initial: VeranstaltungFormModel | null) {
  const [form, setForm] = useState<VeranstaltungFormModel | null>(null);

  /* =========================
     INIT (EDIT)
     ========================= */

  useEffect(() => {
    if (!initial) {
      setForm(null);
      return;
    }

   setForm({
     ...initial,

     /* ===== Null → Default ===== */

     laenderCode: initial.laenderCode ?? undefined,

     plz: initial.plz ?? "",
     ort: initial.ort ?? "",
     artDerUnterkunft: initial.artDerUnterkunft ?? "",
     artDerVerpflegung: initial.artDerVerpflegung ?? "",

     individuelleGebuehren: initial.individuelleGebuehren ?? false,
     standardGebuehr: initial.standardGebuehr ?? undefined,

     geplanteTeilnehmerMaennlich: initial.geplanteTeilnehmerMaennlich ?? undefined,
     geplanteTeilnehmerWeiblich: initial.geplanteTeilnehmerWeiblich ?? undefined,
     geplanteTeilnehmerDivers: initial.geplanteTeilnehmerDivers ?? undefined,

     geplanteMitarbeiterMaennlich: initial.geplanteMitarbeiterMaennlich ?? undefined,
     geplanteMitarbeiterWeiblich: initial.geplanteMitarbeiterWeiblich ?? undefined,
     geplanteMitarbeiterDivers: initial.geplanteMitarbeiterDivers ?? undefined,
   });
  }, [initial]);

  /* =========================
     CREATE RESET
     ========================= */

  const reset = () => setForm(emptyVeranstaltung());

  /* =========================
     FIELD UPDATE
     ========================= */

  const update = <K extends keyof VeranstaltungFormModel>(
    key: K,
    value: VeranstaltungFormModel[K],
  ) => {
    setForm((f) => (f ? { ...f, [key]: value } : f));
  };

  /* =========================
     BUILD SAVE PAYLOAD
     ========================= */

  const buildSavePayload = (): VeranstaltungSave | null => {
    if (!form) return null;

    if (
      !form.typ ||
      !form.beginnDatum ||
      !form.beginnZeit ||
      !form.endeDatum ||
      !form.endeZeit ||
      !form.verein ||
      !form.leiter
    ) {
      return null;
    }

    return {
      id: form.id,
      name: form.name,
      typ: form.typ,

      beginnDatum: form.beginnDatum,
      beginnZeit: form.beginnZeit,

      endeDatum: form.endeDatum,
      endeZeit: form.endeZeit,

      vereinId: form.verein.id,
      leiterId: form.leiter.id,

      /* ===== Detailfelder ===== */

      laenderCode: form.laenderCode,
      plz: form.plz || undefined,
      ort: form.ort || undefined,
      artDerUnterkunft: form.artDerUnterkunft || undefined,
      artDerVerpflegung: form.artDerVerpflegung || undefined,

      individuelleGebuehren: form.individuelleGebuehren ?? false,
      standardGebuehr: form.individuelleGebuehren ? undefined : form.standardGebuehr ?? undefined,

      geplanteTeilnehmerMaennlich: form.geplanteTeilnehmerMaennlich ?? undefined,
      geplanteTeilnehmerWeiblich: form.geplanteTeilnehmerWeiblich ?? undefined,
      geplanteTeilnehmerDivers: form.geplanteTeilnehmerDivers ?? undefined,

      geplanteMitarbeiterMaennlich: form.geplanteMitarbeiterMaennlich ?? undefined,
      geplanteMitarbeiterWeiblich: form.geplanteMitarbeiterWeiblich ?? undefined,
      geplanteMitarbeiterDivers: form.geplanteMitarbeiterDivers ?? undefined,
    };
  };

  /* =========================
     VALIDATION (MINIMAL)
     ========================= */

  const isValid =
    !!form &&
    form.name.trim().length >= 2 &&
    !!form.typ &&
    !!form.beginnDatum &&
    !!form.endeDatum &&
    !!form.verein &&
    !!form.leiter;

  return {
    form,
    update,
    reset,
    buildSavePayload,
    isValid,
  };
}
