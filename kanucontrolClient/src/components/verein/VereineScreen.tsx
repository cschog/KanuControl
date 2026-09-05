import { useEffect, useState, useCallback } from "react";

import { Box, Paper } from "@mui/material";

import { MenueHeader } from "@/components/layout/MenueHeader";
import { VereinTable } from "@/components/verein/VereinTable";
import { VereinFormView } from "@/components/verein/VereinFormView";
import { VereinCreateDialog } from "@/components/verein/VereinCreateDialog";
import { VereinCsvImportDialog } from "@/components/verein/import/VereinCsvImportDialog";
import { BottomActionBar } from "@/components/layout/BottomActionBar";
import { renderLoadingOrError } from "@/components/common/loadingOnErrorUtils";
import { navigateToStartMenu } from "@/components/layout/navigateToStartMenue";

import { ErrorDialog } from "@/components/common/ErrorDialog";
import { getApiErrorMessage } from "@/api/utils/apiError";

import { getAllVereine, deleteVerein, createVerein, updateVerein } from "@/api/services/vereinApi";

import type Verein from "@/api/types/verein/VereinFormModel";
import type { VereinSave } from "@/api/types/verein/VereinSave";

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
    } catch (err: unknown) {
      console.error("Fehler beim Laden der Vereine", err);

      setError(getApiErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    load();
  }, [load]);

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

  /* ========================================================= */
  /* SAVE */
  /* ========================================================= */

  const handleSave = async (payload: VereinSave) => {
    if (!selected?.id) return;

    try {
      const updated = await updateVerein(selected.id, payload);

      await load();

      setSelected(updated);

      setEditMode(false);

      setEditData(null);

      setError(null);
    } catch (err: unknown) {
      console.error("Fehler beim Speichern des Vereins", err);

      setError(getApiErrorMessage(err));
    }
  };

  /* ========================================================= */
  /* DELETE */
  /* ========================================================= */

  const handleDelete = async () => {
    if (!selected?.id) return;

    try {
      await deleteVerein(selected.id);

      await load();

      setSelected(null);

      setError(null);
    } catch (err: unknown) {
      console.error("Fehler beim Löschen des Vereins", err);

      setError(getApiErrorMessage(err));
    }
  };

  /* ========================================================= */
  /* CREATE */
  /* ========================================================= */

  const handleCreate = async (payload: VereinSave) => {
    try {
      const saved = await createVerein(payload);

      setData((prev) => [...prev, saved]);

      setSelected(saved);

      setEditMode(false);

      setEditData(null);

      setCreateDialogOpen(false);

      setError(null);
    } catch (err: unknown) {
      console.error("Fehler beim Anlegen des Vereins", err);

      setError(getApiErrorMessage(err));
    }
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

      {renderLoadingOrError({
        loading,
        error: null,
      })}

      {/* ===================================================== */}
      {/* LIST VIEW */}
      {/* ===================================================== */}

      {!selected ? (
        <Paper sx={{ p: 2 }}>
          <VereinTable data={data} selectedVerein={selected} onSelectVerein={handleSelect} />
        </Paper>
      ) : (
        <Paper sx={{ p: 2 }}>
          <VereinFormView
            verein={editMode ? editData : selected}
            editMode={editMode}
            onEdit={handleEdit}
            onCancelEdit={handleCancel}
            onSave={handleSave}
            onDelete={handleDelete}
            onBack={() => {
              setSelected(null);
              setEditMode(false);
              setEditData(null);
            }}
            onCsvImport={() => setCsvImportOpen(true)}
            disableEdit={disableEdit}
            disableDelete={disableDelete}
          />
        </Paper>
      )}

      {/* ===================================================== */}
      {/* ACTION BAR */}
      {/* ===================================================== */}

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

      {/* ===================================================== */}
      {/* DIALOGS */}
      {/* ===================================================== */}

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

      <ErrorDialog open={!!error} message={error ?? ""} onClose={() => setError(null)} />
    </Box>
  );
}
