// src/hooks/useCrudForm.ts

import { useState } from "react";

export function useCrudForm<T>(createEmpty: () => T) {
  const [form, setForm] = useState<T>(createEmpty());

  const updateField = <K extends keyof T>(
    field: K,
    value: T[K],
  ) => {
    setForm((prev) => ({
      ...prev,
      [field]: value,
    }));
  };

  const resetForm = () => {
    setForm(createEmpty());
  };

  const loadForm = (value: T) => {
    setForm(value);
  };

  return {
    form,
    setForm,
    updateField,
    resetForm,
    loadForm,
  };
}