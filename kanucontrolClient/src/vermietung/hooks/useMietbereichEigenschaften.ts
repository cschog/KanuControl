import { useCallback, useEffect, useState } from "react";

import type { MietbereichEigenschaft } from "@/vermietung/types/MietbereichEigenschaft";
import type { MietbereichEigenschaftSave } from "@/vermietung/types/MietbereichEigenschaftSave";

import {
  createMietbereichEigenschaft,
  getMietbereichEigenschaften,
  getMietbereichEigenschaft,
  updateMietbereichEigenschaft,
  deleteMietbereichEigenschaft,
} from "@/vermietung/api/mietbereichEigenschaftApi";

interface Props {
  mietobjektId: number;
  mietbereichId: number | null;
}

export function useMietbereichEigenschaften({ mietobjektId, mietbereichId }: Props) {
  const [eigenschaften, setEigenschaften] = useState<MietbereichEigenschaft[]>([]);

  const [selectedId, setSelectedId] = useState<number | null>(null);

  const [sorting, setSorting] = useState<
    {
      id: string;
      desc: boolean;
    }[]
  >([]);

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const loadEigenschaften = useCallback(async () => {
    if (!mietbereichId) {
      setEigenschaften([]);
      setSelectedId(null);
      return;
    }

    try {
      setLoading(true);
      setError(null);

      const data = await getMietbereichEigenschaften(mietobjektId, mietbereichId);

      setEigenschaften(data);
    } catch (err) {
      console.error("Fehler beim Laden der Mietbereich-Eigenschaften:", err);

      setError("Die Eigenschaften des Mietbereichs konnten nicht geladen werden.");
    } finally {
      setLoading(false);
    }
  }, [mietobjektId, mietbereichId]);

  useEffect(() => {
    void loadEigenschaften();
  }, [loadEigenschaften]);

  const create = useCallback(
    async (payload: MietbereichEigenschaftSave) => {
      if (!mietbereichId) {
        return;
      }

      try {
        setError(null);

        const created = await createMietbereichEigenschaft(mietobjektId, mietbereichId, payload);

        setEigenschaften((current) => [...current, created]);
        setSelectedId(created.id);

        return created;
      } catch (err) {
        console.error("Fehler beim Anlegen der Mietbereich-Eigenschaft:", err);

        setError("Die Mietbereich-Eigenschaft konnte nicht angelegt werden.");

        throw err;
      }
    },
    [mietobjektId, mietbereichId],
  );

  const update = useCallback(
    async (id: number, payload: MietbereichEigenschaftSave) => {
      if (!mietbereichId) {
        return;
      }

      try {
        setError(null);

        const updated = await updateMietbereichEigenschaft(
          mietobjektId,
          mietbereichId,
          id,
          payload,
        );

        setEigenschaften((current) =>
          current.map((eigenschaft) => (eigenschaft.id === updated.id ? updated : eigenschaft)),
        );

        return updated;
      } catch (err) {
        console.error("Fehler beim Aktualisieren der Mietbereich-Eigenschaft:", err);

        setError("Die Mietbereich-Eigenschaft konnte nicht gespeichert werden.");

        throw err;
      }
    },
    [mietobjektId, mietbereichId],
  );

  const remove = useCallback(
    async (id: number) => {
      if (!mietbereichId) {
        return;
      }

      try {
        setError(null);

        await deleteMietbereichEigenschaft(mietobjektId, mietbereichId, id);

        setEigenschaften((current) => current.filter((eigenschaft) => eigenschaft.id !== id));

        setSelectedId((current) => (current === id ? null : current));
      } catch (err) {
        console.error("Fehler beim Löschen der Mietbereich-Eigenschaft:", err);

        setError("Die Mietbereich-Eigenschaft konnte nicht gelöscht werden.");

        throw err;
      }
    },
    [mietobjektId, mietbereichId],
  );

  const reload = useCallback(async () => {
    await loadEigenschaften();
  }, [loadEigenschaften]);

  const getById = useCallback(
    async (id: number) => {
      if (!mietbereichId) {
        return null;
      }

      return getMietbereichEigenschaft(mietobjektId, mietbereichId, id);
    },
    [mietobjektId, mietbereichId],
  );

  return {
    eigenschaften,
    selectedId,
    setSelectedId,

    sorting,
    setSorting,

    loading,
    error,
    setError,

    loadEigenschaften,
    reload,

    create,
    update,
    remove,
    getById,
  };
}
