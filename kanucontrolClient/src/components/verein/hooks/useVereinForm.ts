// src/components/verein/hooks/useVereinForm.ts

import { useEffect, useState, useCallback } from "react";
import Verein from "@/api/types/VereinFormModel";
import { VereinSave } from "@/api/types/VereinSave";
import { PersonRef } from "@/api/types/PersonRef";

/* =========================================================
   EMPTY FACTORY
   ========================================================= */

function emptyVerein(): Verein {
  return {
    id: undefined,
    name: "",
    abk: "",
    strasse: "",
    plz: "",
    ort: "",
    telefon: "",
    bankName: "",
    iban: "",
    kontoinhaber: undefined,
  };
}

/* =========================================================
   HOOK
   ========================================================= */

export function useVereinForm(initial: Verein | null) {
  const [form, setForm] = useState<Verein | null>(null);

  /* ---------------------------------------------------------
     INITIAL LOAD (EDIT MODE)
     --------------------------------------------------------- */

  useEffect(() => {
    setForm(initial ? { ...initial } : null);
  }, [initial]);

  /* ---------------------------------------------------------
     RESET (CREATE MODE)  🔒 STABLE
     --------------------------------------------------------- */

  const reset = useCallback(() => {
    setForm(emptyVerein());
  }, []);

  /* ---------------------------------------------------------
     FIELD UPDATE  🔒 STABLE
     --------------------------------------------------------- */

  const update = useCallback(<K extends keyof Verein>(key: K, value: Verein[K]) => {
    setForm((f) => (f ? { ...f, [key]: value } : f));
  }, []);

  /* ---------------------------------------------------------
     KONTOINHABER
     --------------------------------------------------------- */

  const setKontoinhaber = useCallback((person?: PersonRef) => {
    setForm((f) => (f ? { ...f, kontoinhaber: person } : f));
  }, []);

  /* ---------------------------------------------------------
     BUILD SAVE PAYLOAD
     --------------------------------------------------------- */

  const buildSavePayload = useCallback((): VereinSave | null => {
    if (!form) return null;

    return {
      id: form.id,
      name: form.name.trim(),
      abk: form.abk.trim(),
      strasse: form.strasse?.trim(),
      plz: form.plz?.trim(),
      ort: form.ort?.trim(),
      telefon: form.telefon?.trim(),
      bankName: form.bankName?.trim(),
      iban: form.iban?.trim(),
      kontoinhaberId: form.kontoinhaber?.id,
    };
  }, [form]);

  /* ---------------------------------------------------------
     VALIDATION
     --------------------------------------------------------- */

  const isValid = !!form && form.name.trim().length >= 2 && form.abk.trim().length >= 1;

  /* ---------------------------------------------------------
     RETURN
     --------------------------------------------------------- */

  return {
    form,
    update,
    reset,
    setKontoinhaber,
    buildSavePayload,
    isValid,
  };
}
