import { useCallback, useEffect, useState } from "react";

import type { Buchung } from "@/vermietung/types/Buchung";
import type { BuchungSave } from "@/vermietung/types/BuchungSave";
import { getApiErrorMessage } from "@/kjfp/api/utils/apiError";

import {
  createBuchung,
  getBuchungen,
  getBuchung,
  updateBuchung,
  deleteBuchung,
} from "@/vermietung/api/buchungApi";

export function useBuchungen() {
  const [buchungen, setBuchungen] = useState<Buchung[]>([]);
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [sorting, setSorting] = useState<{ id: string; desc: boolean }[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const loadBuchungen = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);

      const data = await getBuchungen();
      setBuchungen(data);
    } catch (err) {
      console.error("Fehler beim Laden der Buchungen:", err);
      setError("Die Buchungen konnten nicht geladen werden.");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    void loadBuchungen();
  }, [loadBuchungen]);

  const create = useCallback(async (payload: BuchungSave) => {
    try {
      setError(null);

      const created = await createBuchung(payload);

      setBuchungen((current) => [...current, created]);
      setSelectedId(created.id);

      return created;
    } catch (err: unknown) {
      console.error("Fehler beim Anlegen der Buchung:", err);
      setError(getApiErrorMessage(err));
      throw err;
    }
  }, []);

  const update = useCallback(async (id: number, payload: BuchungSave) => {
    try {
      setError(null);

      const updated = await updateBuchung(id, payload);

      setBuchungen((current) =>
        current.map((buchung) => (buchung.id === updated.id ? updated : buchung)),
      );

      return updated;
    } catch (err: unknown) {
      console.error("Fehler beim Aktualisieren der Buchung:", err);
      setError(getApiErrorMessage(err));
      throw err;
    }
  }, []);

  const remove = useCallback(async (id: number) => {
    try {
      setError(null);

      await deleteBuchung(id);

      setBuchungen((current) => current.filter((buchung) => buchung.id !== id));

      setSelectedId((current) => (current === id ? null : current));
    } catch (err) {
      console.error("Fehler beim Löschen der Buchung:", err);
      setError("Die Buchung konnte nicht gelöscht werden.");
      throw err;
    }
  }, []);

  const reload = useCallback(async () => {
    await loadBuchungen();
  }, [loadBuchungen]);

  const getById = useCallback(async (id: number) => getBuchung(id), []);

  return {
    buchungen,
    selectedId,
    setSelectedId,
    sorting,
    setSorting,
    loading,
    error,
    setError,
    loadBuchungen,
    reload,
    create,
    update,
    remove,
    getById,
  };
}
