import { useEffect, useState } from "react";
import { Alert, Box, Button, CircularProgress, Typography } from "@mui/material";

import type { Mietobjekt } from "@/vermietung/types/Mietobjekt";
import type { MietobjektSave } from "@/vermietung/types/MietobjektSave";

import {
  createMietobjekt,
  getMietobjekte,
  getMietobjekt,
  updateMietobjekt,
  deleteMietobjekt,
  setMietobjektAktiv,
} from "@/vermietung/api/mietobjektApi";

import { MietobjektTable } from "@/vermietung/components/mietobjekte/MietobjektTable";
import { MietobjektFormView } from "@/vermietung/components/mietobjekte/MietobjektFormView";

export default function MietobjektePage() {
  const [mietobjekte, setMietobjekte] = useState<Mietobjekt[]>([]);
  const [selectedId, setSelectedId] = useState<number | null>(null);

  const [sorting, setSorting] = useState<{ id: string; desc: boolean }[]>([
    { id: "bezeichnung", desc: false },
  ]);

  const [editMode, setEditMode] = useState(false);
  const [newMode, setNewMode] = useState(false);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const selectedMietobjekt = mietobjekte.find((m) => m.id === selectedId) ?? null;

  async function loadMietobjekte(selectFirst = true) {
    try {
      setLoading(true);
      setError(null);

      const data = await getMietobjekte();

      setMietobjekte(data);

      if (selectFirst && data.length > 0) {
        setSelectedId(data[0].id);
      }

      if (data.length === 0) {
        setSelectedId(null);
      }
    } catch (err) {
      console.error("Fehler beim Laden der Mietobjekte:", err);
      setError("Die Mietobjekte konnten nicht geladen werden.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => {
    void loadMietobjekte();
  }, []);

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

      const updated = await getMietobjekt(selectedId);

      setMietobjekte((current) => current.map((m) => (m.id === updated.id ? updated : m)));

      setEditMode(false);
    } catch (err) {
      console.error("Fehler beim Abbrechen der Bearbeitung:", err);
      setError("Das Mietobjekt konnte nicht neu geladen werden.");
    }
  }

async function handleSave(payload: MietobjektSave) {
  try {
    setError(null);

    if (newMode) {
      const created = await createMietobjekt({
        ...payload,
        aktiv: true,
      });

      await loadMietobjekte(false);

      setSelectedId(created.id);
      setNewMode(false);
      setEditMode(false);

      return;
    }

    if (!selectedId) {
      return;
    }

    const updated = await updateMietobjekt(selectedId, payload);

    await loadMietobjekte(false);

    const refreshed = await getMietobjekt(updated.id);

    setMietobjekte((current) => current.map((m) => (m.id === refreshed.id ? refreshed : m)));

    setSelectedId(refreshed.id);
    setEditMode(false);
  } catch (err) {
    console.error("Fehler beim Speichern des Mietobjekts:", err);
    setError("Das Mietobjekt konnte nicht gespeichert werden.");
  }
}

  async function handleDelete() {
    if (!selectedMietobjekt) {
      return;
    }

    try {
      setError(null);

      await deleteMietobjekt(selectedMietobjekt.id);

      const remaining = mietobjekte.filter((m) => m.id !== selectedMietobjekt.id);

      setMietobjekte(remaining);
      setSelectedId(remaining[0]?.id ?? null);
      setEditMode(false);
    } catch (err) {
      console.error("Fehler beim Löschen des Mietobjekts:", err);
      setError("Das Mietobjekt konnte nicht gelöscht werden.");
    }
  }

  async function handleActivate() {
    if (!selectedId) {
      return;
    }

    try {
      setError(null);

      await setMietobjektAktiv(selectedId);

      await loadMietobjekte(false);

      const updated = await getMietobjekt(selectedId);

      setMietobjekte((current) => current.map((m) => (m.id === updated.id ? updated : m)));

      setSelectedId(updated.id);
    } catch (err) {
      console.error("Fehler beim Aktivieren des Mietobjekts:", err);
      setError("Das Mietobjekt konnte nicht aktiviert werden.");
    }
  }

  if (loading) {
    return (
      <Box
        sx={{
          display: "flex",
          justifyContent: "center",
          mt: 4,
        }}
      >
        <CircularProgress />
      </Box>
    );
  }

  return (
    <Box sx={{ p: 2 }}>
      <Box
        sx={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          mb: 2,
        }}
      >
        <Typography variant="h5">Mietobjekte</Typography>

        <Button variant="contained" onClick={handleNew}>
          Neu
        </Button>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      {!newMode && mietobjekte.length > 0 && (
        <MietobjektTable
          data={mietobjekte}
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
        <MietobjektFormView
          mietobjekt={{
            id: 0,
            bezeichnung: "",
            beschreibung: "",
            strasse: "",
            plz: "",
            ort: "",
            countryCode: "DE",
            aktiv: true,
            mietbar: true,
          }}
          editMode={true}
          onEdit={() => setEditMode(true)}
          onCancelEdit={handleCancelEdit}
          onSave={handleSave}
          onDelete={() => {}}
          onBack={handleCancelEdit}
          onActivate={() => {}}
          disableEdit={false}
          disableDelete={true}
        />
      ) : selectedMietobjekt ? (
        <MietobjektFormView
          mietobjekt={selectedMietobjekt}
          editMode={editMode}
          onEdit={() => setEditMode(true)}
          onCancelEdit={() => setEditMode(false)}
          onSave={handleSave}
          onDelete={handleDelete}
          onBack={() => setSelectedId(null)}
          onActivate={handleActivate}
          disableEdit={false}
          disableDelete={false}
        />
      ) : (
        <Typography color="text.secondary" sx={{ mt: 3 }}>
          Bitte wählen Sie ein Mietobjekt aus.
        </Typography>
      )}
    </Box>
  );
}
