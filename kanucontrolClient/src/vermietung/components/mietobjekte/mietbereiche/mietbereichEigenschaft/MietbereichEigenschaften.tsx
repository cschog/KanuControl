// src/vermietung/components/mietobjekte/mietbereiche/mietbereichEigenschaft/MietbereichEigenschaften.tsx
import { useState } from "react";
import { Alert, Box, Button, Typography } from "@mui/material";

import type { MietbereichEigenschaft } from "@/vermietung/types/MietbereichEigenschaft";
import type { MietbereichEigenschaftSave } from "@/vermietung/types/MietbereichEigenschaftSave";

import { MietbereichEigenschaftTable } from "./MietbereichEigenschaftTable";
import { MietbereichEigenschaftForm } from "./MietbereichEigenschaftForm";

import { useMietbereichEigenschaften } from "@/vermietung/hooks/useMietbereichEigenschaften";

interface Props {
  mietobjektId: number;
  mietbereichId: number;
}

export function MietbereichEigenschaften({ mietobjektId, mietbereichId }: Props) {
  const {
    eigenschaften,
    selectedId,
    setSelectedId,
    sorting,
    setSorting,
    loading,
    error,
    setError,
    create,
    update,
    remove,
    getById,
  } = useMietbereichEigenschaften({
    mietobjektId,
    mietbereichId,
  });

  const [editMode, setEditMode] = useState(false);
  const [newMode, setNewMode] = useState(false);

  const selectedEigenschaft = eigenschaften.find((e) => e.id === selectedId) ?? null;

  const [form, setForm] = useState<MietbereichEigenschaftSave | null>(null);

 function handleNew() {
   setSelectedId(null);
   setNewMode(true);
   setEditMode(true);
   setError(null);

   const nextSortierung =
     eigenschaften.length > 0 ? Math.max(...eigenschaften.map((e) => e.sortierung)) + 1 : 1;

   setForm({
     sortierung: nextSortierung,
     bezeichnung: "",
     typ: "TEXT",
     wert: "",
   });
 }

  function handleSelect(row: MietbereichEigenschaft | null) {
    if (!row) {
      setSelectedId(null);
      setForm(null);
      return;
    }

    setSelectedId(row.id);
    setNewMode(false);
    setEditMode(false);
    setError(null);

    setForm({
      sortierung: row.sortierung,
      bezeichnung: row.bezeichnung,
      typ: row.typ,
      wert: row.wert,
    });
  }

  function handleChange<K extends keyof MietbereichEigenschaftSave>(
    key: K,
    value: MietbereichEigenschaftSave[K],
  ) {
    setForm((current) =>
      current
        ? {
            ...current,
            [key]: value,
          }
        : current,
    );
  }

  async function handleSave() {
    if (!form) {
      return;
    }

    try {
      setError(null);

      if (newMode) {
        const created = await create(form);

        if (created) {
          setNewMode(false);
          setEditMode(false);
        }

        return;
      }

      if (!selectedId) {
        return;
      }

      await update(selectedId, form);

      setEditMode(false);
    } catch {
      // Fehler wird bereits vom Hook gesetzt.
    }
  }

  async function handleCancel() {
    if (newMode) {
      setNewMode(false);
      setEditMode(false);
      setForm(null);
      return;
    }

    if (!selectedId) {
      setEditMode(false);
      return;
    }

    try {
      setError(null);

      const refreshed = await getById(selectedId);

      if (refreshed) {
        setForm({
          sortierung: refreshed.sortierung,
          bezeichnung: refreshed.bezeichnung,
          typ: refreshed.typ,
          wert: refreshed.wert,
        });
      }

      setEditMode(false);
    } catch {
      // Fehler wird hier bewusst nicht überschrieben.
    }
  }

  async function handleDelete() {
    if (!selectedId) {
      return;
    }

    try {
      setError(null);

      await remove(selectedId);

      setForm(null);
      setEditMode(false);
    } catch {
      // Fehler wird bereits vom Hook gesetzt.
    }
  }

  if (loading) {
    return (
      <Box sx={{ mt: 3 }}>
        <Typography color="text.secondary">Eigenschaften werden geladen …</Typography>
      </Box>
    );
  }

  return (
    <Box sx={{ mt: 3 }}>
      <Box
        sx={{
          display: "flex",
          justifyContent: "space-between",
          alignItems: "center",
          mb: 2,
        }}
      >
        <Typography variant="h6">Eigenschaften</Typography>

        <Button variant="contained" onClick={handleNew} disabled={editMode}>
          + Eigenschaft
        </Button>
      </Box>

      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      {!newMode && !selectedEigenschaft && eigenschaften.length > 0 && (
        <MietbereichEigenschaftTable
          data={eigenschaften}
          selectedId={selectedId}
          onSelect={handleSelect}
          sorting={sorting}
          onSortingChange={setSorting}
        />
      )}

      {newMode && form && (
        <EigenschaftForm
          form={form}
          editMode
          onChange={handleChange}
          onSave={handleSave}
          onCancel={handleCancel}
          onDelete={handleDelete}
          disableDelete
        />
      )}

      {!newMode && selectedEigenschaft && form && (
        <EigenschaftForm
          form={form}
          editMode={editMode}
          onChange={handleChange}
          onSave={handleSave}
          onCancel={handleCancel}
          onDelete={handleDelete}
          disableDelete={false}
          onEdit={() => setEditMode(true)}
        />
      )}

      {!newMode && !selectedEigenschaft && eigenschaften.length === 0 && (
        <Typography color="text.secondary">Noch keine Eigenschaften hinterlegt.</Typography>
      )}
    </Box>
  );
}

interface EigenschaftFormProps {
  form: MietbereichEigenschaftSave;
  editMode: boolean;
  onChange: <K extends keyof MietbereichEigenschaftSave>(
    key: K,
    value: MietbereichEigenschaftSave[K],
  ) => void;
  onSave: () => Promise<void>;
  onCancel: () => Promise<void>;
  onDelete: () => Promise<void>;
  onEdit?: () => void;
  disableDelete: boolean;
}

function EigenschaftForm({
  form,
  editMode,
  onChange,
  onSave,
  onCancel,
  onDelete,
  onEdit,
  disableDelete,
}: EigenschaftFormProps) {
  return (
    <>
      <Box
        display="grid"
        gridTemplateColumns={{
          xs: "1fr",
          sm: "repeat(2, 1fr)",
          lg: "repeat(3, 1fr)",
        }}
        gap={2}
        sx={{ mt: 2 }}
      >
        <MietbereichEigenschaftForm form={form} editMode={editMode} onChange={onChange} />
      </Box>

      <Box
        sx={{
          display: "flex",
          gap: 1,
          justifyContent: "flex-end",
          mt: 2,
        }}
      >
        {!editMode && onEdit && (
          <Button variant="outlined" onClick={onEdit}>
            Bearbeiten
          </Button>
        )}

        {editMode && (
          <>
            <Button variant="outlined" onClick={onCancel}>
              Abbrechen
            </Button>

            <Button variant="contained" onClick={onSave}>
              Speichern
            </Button>
          </>
        )}

        {!disableDelete && (
          <Button color="error" variant="outlined" onClick={onDelete}>
            Löschen
          </Button>
        )}
      </Box>
    </>
  );
}
