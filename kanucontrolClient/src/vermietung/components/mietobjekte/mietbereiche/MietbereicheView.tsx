import { useCallback, useEffect, useState } from "react";
import { Alert, Box, Button, Typography } from "@mui/material";

import type { Mietbereich } from "@/vermietung/types/Mietbereich";
import type { MietbereichSave } from "@/vermietung/types/MietbereichSave";

import {
  createMietbereich,
  getMietbereiche,
  getMietbereich,
  updateMietbereich,
  deleteMietbereich,
} from "@/vermietung/api/mietbereichApi";

import { MietbereichTable } from "./MietbereichTable";
import { MietbereichFormView } from "./MietbereichFormView";

interface Props {
  mietobjektId: number;
  mietobjektBezeichnung: string;
}

export function MietbereicheView({ mietobjektId, mietobjektBezeichnung }: Props) {
  const [mietbereiche, setMietbereiche] = useState<Mietbereich[]>([]);
  const [selectedId, setSelectedId] = useState<number | null>(null);

  const [sorting, setSorting] = useState<
    {
      id: string;
      desc: boolean;
    }[]
  >([]);

  const [editMode, setEditMode] = useState(false);
  const [newMode, setNewMode] = useState(false);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const selectedMietbereich = mietbereiche.find((m) => m.id === selectedId) ?? null;

  const loadMietbereiche = useCallback(async () => {
    try {
      setLoading(true);
      setError(null);

      const data = await getMietbereiche(mietobjektId);

      setMietbereiche(data);
    } catch (err) {
      console.error("Fehler beim Laden der Mietbereiche:", err);
      setError("Die Mietbereiche konnten nicht geladen werden.");
    } finally {
      setLoading(false);
    }
  }, [mietobjektId]);

  useEffect(() => {
    void loadMietbereiche();
  }, [loadMietbereiche]);

  function handleNew() {
    setSelectedId(null);
    setNewMode(true);
    setEditMode(true);
  }

  async function handleCancelEdit() {
    if (newMode) {
      setNewMode(false);
      setEditMode(false);
      return;
    }

    if (!selectedId) {
      setEditMode(false);
      return;
    }

    try {
      setError(null);

      const updated = await getMietbereich(mietobjektId, selectedId);

      setMietbereiche((current) => current.map((m) => (m.id === updated.id ? updated : m)));

      setEditMode(false);
    } catch (err) {
      console.error("Fehler beim Abbrechen der Bearbeitung:", err);
      setError("Der Mietbereich konnte nicht neu geladen werden.");
    }
  }

  async function handleSave(payload: MietbereichSave) {
    try {
      setError(null);

      if (newMode) {
        const created = await createMietbereich(mietobjektId, payload);

        await loadMietbereiche();

        setSelectedId(created.id);
        setNewMode(false);
        setEditMode(false);

        return;
      }

      if (!selectedId) {
        return;
      }

      const updated = await updateMietbereich(mietobjektId, selectedId, payload);

      await loadMietbereiche();

      const refreshed = await getMietbereich(mietobjektId, updated.id);

      setMietbereiche((current) => current.map((m) => (m.id === refreshed.id ? refreshed : m)));

      setSelectedId(refreshed.id);
      setEditMode(false);
    } catch (err) {
      console.error("Fehler beim Speichern des Mietbereichs:", err);
      setError("Der Mietbereich konnte nicht gespeichert werden.");
    }
  }

  async function handleDelete() {
    if (!selectedMietbereich) {
      return;
    }

    try {
      setError(null);

      await deleteMietbereich(mietobjektId, selectedMietbereich.id);

      const remaining = mietbereiche.filter((m) => m.id !== selectedMietbereich.id);

      setMietbereiche(remaining);
      setSelectedId(remaining[0]?.id ?? null);
      setEditMode(false);
    } catch (err) {
      console.error("Fehler beim Löschen des Mietbereichs:", err);
      setError("Der Mietbereich konnte nicht gelöscht werden.");
    }
  }

  if (loading) {
    return (
      <Box sx={{ mt: 4 }}>
        <Typography color="text.secondary">Mietbereiche werden geladen …</Typography>
      </Box>
    );
  }

  return (
    <Box sx={{ mt: 4 }}>
      <Box
        sx={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          mb: 2,
        }}
      >
        <Typography variant="h6">Mietbereiche für {mietobjektBezeichnung}</Typography>

        <Button variant="contained" onClick={handleNew} disabled={editMode}>
          Neu
        </Button>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      {!newMode && !selectedMietbereich && mietbereiche.length > 0 && (
        <MietbereichTable
          data={mietbereiche}
          selectedId={selectedId}
          onSelect={(row) => {
            setSelectedId(row?.id ?? null);
            setEditMode(false);
          }}
          sorting={sorting}
          onSortingChange={setSorting}
        />
      )}

      {newMode ? (
        <MietbereichFormView
          mietbereich={{
            id: 0,
            mietobjektId,
            bezeichnung: "",
            beschreibung: "",
            mietbar: true,
          }}
          editMode
          onEdit={() => setEditMode(true)}
          onCancelEdit={handleCancelEdit}
          onSave={handleSave}
          onDelete={() => {}}
          onBack={handleCancelEdit}
          disableDelete
        />
      ) : selectedMietbereich ? (
        <MietbereichFormView
          mietbereich={selectedMietbereich}
          editMode={editMode}
          onEdit={() => setEditMode(true)}
          onCancelEdit={handleCancelEdit}
          onSave={handleSave}
          onDelete={handleDelete}
          onBack={() => setSelectedId(null)}
          disableDelete={false}
        />
      ) : mietbereiche.length === 0 ? (
        <Typography color="text.secondary" sx={{ mt: 2 }}>
          Für dieses Mietobjekt sind noch keine Mietbereiche angelegt.
        </Typography>
      ) : null}
    </Box>
  );
}
