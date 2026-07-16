import { useEffect, useState } from "react";
import { VeranstaltungFormModel } from "@/api/types/veranstaltung/VeranstaltungFormModel";
import { VeranstaltungSave } from "@/api/types/veranstaltung/VeranstaltungSave";
import { VeranstaltungTyp } from "@/api/enums/VeranstaltungTyp";
import { VeranstaltungScope } from "@/api/enums/VeranstaltungScope";


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

    countryCode: undefined,
    plz: "",
    ort: "",
    unterkunftsart: undefined,
    verpflegungsmodell: undefined,

    beitragsstrukturId: undefined,
  
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

      countryCode: initial.countryCode ?? undefined,

      plz: initial.plz ?? "",
      ort: initial.ort ?? "",
      unterkunftsart: initial.unterkunftsart,
      verpflegungsmodell: initial.verpflegungsmodell,
      beitragsstrukturId: initial.beitragsstrukturId ?? undefined,
    });
  }, [initial]);

  /* =========================
     CREATE RESET
     ========================= */

  const reset = (data?: Partial<VeranstaltungFormModel>) => {
    setForm({
      ...emptyVeranstaltung(),
      ...data,
    });
  };

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

      countryCode: form.countryCode,
      plz: form.plz || undefined,
      ort: form.ort || undefined,
      unterkunftsartId: form.unterkunftsart?.id,
      verpflegungsmodellId: form.verpflegungsmodell?.id,

      beitragsstrukturId: form.beitragsstrukturId ?? undefined,
    };
  };

  /* =========================
     VALIDATION (MINIMAL)
     ========================= */

  /* =========================
     VALIDATION
     ========================= */

  const isEndTimeValid = (() => {
    if (!form || !form.beginnDatum || !form.endeDatum || !form.beginnZeit || !form.endeZeit) {
      return true;
    }

    // Unterschiedliche Tage -> erlaubt
    if (form.beginnDatum !== form.endeDatum) {
      return true;
    }

    // Gleicher Tag -> Ende >= Beginn
    return form.endeZeit >= form.beginnZeit;
  })();

  const isValid =
    !!form &&
    form.name.trim().length >= 2 &&
    !!form.typ &&
    !!form.beginnDatum &&
    !!form.endeDatum &&
    !!form.verein &&
    !!form.leiter &&
    isEndTimeValid;

  /* =========================
     EXPORT
     ========================= */

  return {
    form,
    update,
    reset,
    buildSavePayload,
    isValid,
    isEndTimeValid,
  };
}
