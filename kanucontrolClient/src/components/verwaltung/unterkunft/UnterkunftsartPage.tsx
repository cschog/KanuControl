// src/components/verwaltung/unterkunft/UnterkunftsartPage.tsx

import { useEffect, useState } from "react";
import { Alert, Box, CircularProgress } from "@mui/material";
import { getApiErrorMessage } from "@/api/utils/apiError";

import CrudToolbar from "@/components/common/CrudToolbar";

import ConfirmDeleteDialog from "@/components/common/ConfirmDeleteDialog";
import UnterkunftsartTable from "./UnterkunftsartTable";

import { UnterkunftsartDTO } from "@/api/types/unterkunft/UnterkunftsartDTO";
import { UnterkunftsartCreateUpdateDTO } from "@/api/types/unterkunft/UnterkunftsartCreateUpdateDTO";
import {
  getUnterkunftsarten,
  createUnterkunftsart,
  updateUnterkunftsart,
  deleteUnterkunftsart,
} from "@/api/services/unterkunftsartApi";
import UnterkunftsartDialog from "@/components/verwaltung/unterkunft/UnterkunftsartDialog";

const UnterkunftsartPage = () => {
  const [data, setData] = useState<UnterkunftsartDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [dialogOpen, setDialogOpen] = useState(false);

  const [editing, setEditing] = useState<UnterkunftsartDTO | null>(null);

  const [selected, setSelected] = useState<UnterkunftsartDTO | null>(null);

  const [deleteOpen, setDeleteOpen] = useState(false);

  /* =========================================================
       LOAD
       ========================================================= */

  const load = async () => {
    try {
      setLoading(true);

      const result = await getUnterkunftsarten();

      setData(result);
      setError(null);
    } catch (err: unknown) {
      console.error("Fehler beim Laden der Unterkunftsarten", err);
      setError(getApiErrorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  const handleAdd = () => {
    setEditing(null);
    setDialogOpen(true);
  };

  const confirmDelete = async () => {
    if (!selected) {
      return;
    }

    try {
      await deleteUnterkunftsart(selected.id);

      setDeleteOpen(false);
      setSelected(null);

      await load();
    } catch (err: unknown) {
      console.error("Fehler beim Löschen der Unterkunftsart", err);
      setError(getApiErrorMessage(err));
    }
  };

  const handleSave = async (dto: UnterkunftsartCreateUpdateDTO) => {
    try {
      if (editing) {
        await updateUnterkunftsart(editing.id, dto);
      } else {
        await createUnterkunftsart(dto);
      }

      setDialogOpen(false);
      setEditing(null);

      await load();
    } catch (err: unknown) {
      console.error("Fehler beim Speichern der Unterkunftsart", err);
      setError(getApiErrorMessage(err));
    }
  };

  useEffect(() => {
    load();
  }, []);

  /* =========================================================
       RENDER
       ========================================================= */

  if (loading) {
    return (
      <Box sx={{ p: 3 }}>
        <CircularProgress />
      </Box>
    );
  }

  return (
    <>
      {error && (
        <Alert severity="error" sx={{ mb: 2 }}>
          {error}
        </Alert>
      )}

      <CrudToolbar title="Unterkunftsarten" onAdd={handleAdd} />

      <UnterkunftsartTable
        data={data}
        loading={loading}
        onEdit={(row) => {
          setEditing(row);
          setDialogOpen(true);
        }}
        onDelete={(row) => {
          setSelected(row);
          setDeleteOpen(true);
        }}
      />

      <UnterkunftsartDialog
        open={dialogOpen}
        unterkunftsart={editing}
        onClose={() => {
          setDialogOpen(false);
          setEditing(null);
        }}
        onSave={handleSave}
      />
      <ConfirmDeleteDialog
        open={deleteOpen}
        title="Unterkunftsart löschen"
        description={
          <>
            Möchten Sie die Unterkunftsart <strong>"{selected?.bezeichnung}"</strong> wirklich
            löschen?
            <br />
            <br />
            Dieser Vorgang kann nicht rückgängig gemacht werden.
          </>
        }
        onClose={() => setDeleteOpen(false)}
        onConfirm={confirmDelete}
      />
    </>
  );
};

export default UnterkunftsartPage;
