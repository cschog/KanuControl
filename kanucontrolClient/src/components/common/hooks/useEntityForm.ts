import { useEffect, useState, useCallback } from "react";

export function useEntityForm<TDetail, TSave>(
  initial: TDetail | null | undefined,
  mapDetailToSave: (detail: TDetail) => TSave,
  emptyFactory: () => TSave,
  buildPayload: (form: TSave) => TSave,
  validate: (form: TSave) => boolean,
) {
  const [form, setForm] = useState<TSave | null>(null);

  // 🔑 Initialisierung (READ / EDIT)
  useEffect(() => {
    if (initial !== undefined) {
      setForm(initial ? mapDetailToSave(initial) : null);
    }
  }, [initial, mapDetailToSave]);

  // 🔑 CREATE
  const reset = useCallback(
    (value?: TSave | null) => {
      setForm(value ?? emptyFactory());
    },
    [emptyFactory],
  );

  // 🔑 Feld-Update
  const update = <K extends keyof TSave>(key: K, value: TSave[K]) => {
    setForm((f) => (f ? { ...f, [key]: value } : f));
  };

  // 🔑 Save-Payload
  const buildSavePayload = (): TSave | null => {
    if (!form) return null;
    return buildPayload(form);
  };

  const isValid = !!form && validate(form);

  return {
    form,
    update,
    reset,
    buildSavePayload,
    isValid,
  };
}
