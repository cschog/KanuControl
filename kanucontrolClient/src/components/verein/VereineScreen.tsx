import { useEffect, useState, useCallback } from "react";
import { Box, Grid, Paper } from "@mui/material";

import { MenueHeader } from "@/components/layout/MenueHeader";
import { VereinTable } from "@/components/verein/VereinTable";
import { VereinFormView } from "@/components/verein/VereinFormView";
import { VereinCreateDialog } from "@/components/verein/VereinCreateDialog";
import { VereinCsvImportDialog } from "@/components/verein/import/VereinCsvImportDialog";
import { BottomActionBar } from "@/components/common/BottomActionBar";

import { renderLoadingOrError } from "@/components/common/loadingOnErrorUtils";
import { navigateToStartMenu } from "@/components/layout/navigateToStartMenue";

import { getAllVereine, deleteVerein, createVerein, updateVerein } from "@/api/client/vereinApi";

import type Verein from "@/api/types/VereinFormModel";
import type { VereinSave } from "@/api/types/VereinSave";

/* ========================================================= */

export default function VereinScreen() {
  /* ================= STATE ================= */

  const [data, setData] = useState<Verein[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [selected, setSelected] = useState<Verein | null>(null);

  const [editMode, setEditMode] = useState(false);
  const [editData, setEditData] = useState<Verein | null>(null);

  const [createDialogOpen, setCreateDialogOpen] = useState(false);
  const [csvImportOpen, setCsvImportOpen] = useState(false);

  /* ========================================================= */
  /* LOAD */
  /* ========================================================= */

  const load = useCallback(async () => {
    try {
      setLoading(true);
      const res = await getAllVereine();
      setData(res);
      setError(null);
    } catch {
      setError("Fehler beim Laden der Vereine");
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]); // ✅

  /* ========================================================= */
  /* SELECT */
  /* ========================================================= */

  const handleSelect = (verein: Verein | null) => {
    setSelected(verein);
    setEditMode(false);
    setEditData(null);
  };

  /* ========================================================= */
  /* EDIT */
  /* ========================================================= */

  const handleEdit = () => {
    if (!selected) return;
    setEditData({ ...selected });
    setEditMode(true);
  };

  const handleCancel = () => {
    setEditMode(false);
    setEditData(null);
  };

  const handleSave = async (payload: VereinSave) => {
    if (!selected?.id) return;

    const updated = await updateVerein(selected.id, payload);

    await load();

    setSelected(updated);
    setEditMode(false);
    setEditData(null);
  };

  /* ========================================================= */
  /* DELETE */
  /* ========================================================= */

  const handleDelete = async () => {
    if (!selected?.id) return;

    await deleteVerein(selected.id);

    setSelected(null);
    await load();
  };

  /* ========================================================= */
  /* CREATE */
  /* ========================================================= */

 const handleCreate = async (payload: VereinSave) => {
   const saved = await createVerein(payload);

   setData((prev) => [...prev, saved]); // ⭐ kein reload nötig

   setSelected(saved);
   setEditMode(false);
   setEditData(null);

   setCreateDialogOpen(false);
 };

  /* ========================================================= */
  /* DERIVED */
  /* ========================================================= */

  const disableEdit = !selected || editMode;
  const disableDelete = !selected || editMode;

  /* ========================================================= */
  /* UI */
  /* ========================================================= */

  return (
    <Box>
      <MenueHeader headerText={`${data.length} Vereine`} />

      {renderLoadingOrError({ loading, error })}

      <Grid container spacing={2}>
        {/* LEFT */}
        <Grid size={{ xs: 12, md: 6 }}>
          <Paper sx={{ p: 2 }}>
            <VereinTable data={data} selectedVerein={selected} onSelectVerein={handleSelect} />
          </Paper>
        </Grid>

        {/* RIGHT */}
        <Grid size={{ xs: 12, md: 6 }}>
          <Paper sx={{ p: 2 }}>
            <VereinFormView
              verein={editMode ? editData : selected}
              editMode={editMode}
              onEdit={handleEdit}
              onCancelEdit={handleCancel}
              onSave={handleSave}
              onDelete={handleDelete}
              onBack={navigateToStartMenu}
              onCsvImport={() => setCsvImportOpen(true)}
              disableEdit={disableEdit}
              disableDelete={disableDelete}
            />
          </Paper>
        </Grid>
      </Grid>

      {/* ================= ACTION BAR ================= */}

      {!selected && !editMode && (
        <BottomActionBar
          left={[
            {
              label: "Neuer Verein",
              variant: "outlined",
              onClick: () => setCreateDialogOpen(true),
            },
            {
              label: "Zurück",
              variant: "outlined",
              onClick: navigateToStartMenu,
            },
          ]}
        />
      )}

      {/* ================= DIALOGS ================= */}

      {selected?.id && (
        <VereinCsvImportDialog
          open={csvImportOpen}
          vereinId={selected.id}
          onClose={() => setCsvImportOpen(false)}
        />
      )}

      <VereinCreateDialog
        open={createDialogOpen}
        onClose={() => setCreateDialogOpen(false)}
        onCreate={handleCreate} 
      />
    </Box>
  );
}
