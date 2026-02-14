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

    // 🔑 KEINE IDs im Form
    verein: undefined,
    leiter: undefined,
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
    setForm(initial ? { ...initial } : null);
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
