// src/components/verwaltung/verpflegung/VerpflegungsmodellPage.tsx

import { useEffect, useState } from "react";
import { Alert, Box, CircularProgress } from "@mui/material";

import CrudToolbar from "@/components/common/CrudToolbar";

import ConfirmDeleteDialog from "@/components/common/ConfirmDeleteDialog";
import VerpflegungsmodellTable from "@/components/verwaltung/verpflegung/VerpflegungsmodellTable";
import { getApiErrorMessage } from "@/api/utils/apiError";
import {
  getVerpflegungsmodelle,
  createVerpflegungsmodell,
  updateVerpflegungsmodell,
  deleteVerpflegungsmodell,
} from "@/api/services/verpflegungsmodellApi";
import VerpflegungsmodellDialog from "@/components/verwaltung/verpflegung/VerpflegungsmodellDialog";
import { VerpflegungsmodellDTO } from "@/api/types/verpflegung/VerpflegungsmodellDTO";
import { VerpflegungsmodellCreateUpdateDTO } from "@/api/types/verpflegung/VerpflegungsmodellCreateUpdateDTO";

const VerpflegungsmodellPage = () => {
  const [data, setData] = useState<VerpflegungsmodellDTO[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [dialogOpen, setDialogOpen] = useState(false);

  const [editing, setEditing] = useState<VerpflegungsmodellDTO | null>(null);

  const [selected, setSelected] = useState<VerpflegungsmodellDTO | null>(null);

  const [deleteOpen, setDeleteOpen] = useState(false);

  /* =========================================================
       LOAD
       ========================================================= */

  const load = async () => {
    try {
      setLoading(true);

      const result = await getVerpflegungsmodelle();

      setData(result);
      setError(null);
    } catch (err: unknown) {
      console.error("Fehler beim Laden der Verpflegungsmodelle", err);
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
      await deleteVerpflegungsmodell(selected.id);

      setDeleteOpen(false);
      setSelected(null);

      await load();
    } catch (err: unknown) {
      console.error("Fehler beim Löschen des Verpflegungsmodells", err);
      setError(getApiErrorMessage(err));
    }
  };

  const handleSave = async (dto: VerpflegungsmodellCreateUpdateDTO) => {
    try {
      if (editing) {
        await updateVerpflegungsmodell(editing.id, dto);
      } else {
        await createVerpflegungsmodell(dto);
      }

      setDialogOpen(false);
      setEditing(null);

      await load();
    } catch (err: unknown) {
      console.error("Fehler beim Speichern des Verpflegungsmodells", err);
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

     <CrudToolbar title="Verpflegungsmodelle" onAdd={handleAdd} />

     <VerpflegungsmodellTable
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

     <VerpflegungsmodellDialog
       open={dialogOpen}
       verpflegungsmodell={editing}
       onClose={() => {
         setDialogOpen(false);
         setEditing(null);
       }}
       onSave={handleSave}
     />
     <ConfirmDeleteDialog
       open={deleteOpen}
       title="Verpflegungsmodell löschen"
       description={
         <>
           Möchten Sie das Verpflegungsmodell <strong>"{selected?.bezeichnung}"</strong> wirklich
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

export default VerpflegungsmodellPage;
