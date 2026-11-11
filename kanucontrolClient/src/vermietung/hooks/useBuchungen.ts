import type { AblesungBestaetigung } from "@/vermietung/types/VerbrauchskostenVorschau";
import { useCallback, useEffect, useState } from "react";

import type { Buchung } from "@/vermietung/types/Buchung";
import type { BuchungSave } from "@/vermietung/types/BuchungSave";
import { getApiErrorMessage } from "@/kjfp/api/utils/apiError";

import type { Buchungsstatus } from "@/vermietung/enums/Buchungsstatus";

import {
  createBuchung,
  getBuchungen,
  getBuchung,
  updateBuchung,
  deleteBuchung,
  brauchtAbrechnungsLoeschbestaetigung,
  aendereBuchungsstatus,
  aendereBuchungsnummer,
} from "@/vermietung/api/buchungApi";

export function useBuchungen() {
  const [buchungen, setBuchungen] = useState<Buchung[]>([]);
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [sorting, setSorting] = useState<{ id: string; desc: boolean }[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const changeStatus = useCallback(async (id: number, status: Buchungsstatus, ablesungenBestaetigt: AblesungBestaetigung[] = [], sendEmail = false, fingerprint?: string) => {
    try {
      setError(null);

      const updated = await aendereBuchungsstatus(id, status, ablesungenBestaetigt, sendEmail, fingerprint);

      setBuchungen((current) =>
        current.map((buchung) => (buchung.id === updated.id ? updated : buchung)),
      );

      return updated;
    } catch (err: unknown) {
      console.error("Fehler beim Ändern des Buchungsstatus:", err);
      setError(getApiErrorMessage(err));
      try {
        const latest = await getBuchungen();
        setBuchungen(latest);
      } catch (refreshError) {
        console.error("Statusänderung konnte nach einem Fehler nicht aktualisiert werden:", refreshError);
      }
      throw err;
    }
  }, []);

  const changeBuchungsnummer = useCallback(async (id: number, nummer: string, bisherigeNummer: string) => {
    const result = await aendereBuchungsnummer(id, nummer, bisherigeNummer);
    setBuchungen(current => current.map(b => b.id === result.id ? { ...b, buchungsnummer: result.buchungsnummer } : b));
    return result;
  }, []);

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

  const remove = useCallback(async (id: number, archivierteAbrechnungenBestaetigt = false) => {
    try {
      setError(null);

      await deleteBuchung(id, archivierteAbrechnungenBestaetigt);

      setBuchungen((current) => current.filter((buchung) => buchung.id !== id));

      setSelectedId((current) => (current === id ? null : current));
    } catch (err) {
      if (!brauchtAbrechnungsLoeschbestaetigung(err)) {
        console.error("Fehler beim Löschen der Buchung:", err);
        setError(getApiErrorMessage(err));
      }
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
   changeStatus,
   changeBuchungsnummer,
   remove,
   getById,
 };
}
