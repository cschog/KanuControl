import { useCallback, useEffect, useMemo, useState } from "react";

import { Box, Button, Chip, Stack, Typography } from "@mui/material";

import { AxiosError } from "axios";

import { GenericTableTanstack } from "@/components/common/GenericTableTanstack";
import { kuerzelColumns } from "@/components/finanzen/konto/finanzgruppeColumns";


import KontoCreateForm from "./KontoCreateForm";
import TeilnehmerZuordnenDialog from "./TeilnehmerZuordnenDialog";
import TeilnehmerEntfernenDialog from "./TeilnehmerEntfernenDialog";
import KontoLoeschenDialog from "./KontoLoeschenDialog";
import { Teilnehmer } from "@/api/types/teilnehmer";

import {
  getFinanzgruppen,
  createFinanzgruppe,
  assignTeilnehmerBulk,
  removeTeilnehmerFromFinanzgruppe,
  deleteFinanzGruppe,
  FinanzGruppe,
} from "@/api/services/finanzgruppenApi";

import { searchTeilnehmer, getTeilnehmerCount } from "@/api/services/teilnehmerApi";

/* =========================================================
   TYPES
   ========================================================= */

type Props = {
  veranstaltungId: number;
};

type RemoveTarget = {
  gruppeId: number;
  personId: number;
  name: string;
};

/* =========================================================
   COMPONENT
   ========================================================= */

export default function KontoPage({ veranstaltungId }: Props) {
  /* ================= DATA ================= */

  const [groups, setGroups] = useState<FinanzGruppe[]>([]);
  const [gesamtTeilnehmer, setGesamtTeilnehmer] = useState(0);

  /* ================= CREATE ================= */

  const [newKuerzel, setNewKuerzel] = useState("");

  /* ================= ASSIGN ================= */

  const [dialogOpen, setDialogOpen] = useState(false);
  const [selectedGroup, setSelectedGroup] = useState<number | null>(null);
  const [search, setSearch] = useState("");
  const [searchResults, setSearchResults] = useState<Teilnehmer[]>([]);
  const [selectedIds, setSelectedIds] = useState<number[]>([]);

  /* ================= REMOVE ================= */

  const [removeTarget, setRemoveTarget] = useState<RemoveTarget | null>(null);

  /* ================= DELETE ================= */

  const [deleteTarget, setDeleteTarget] = useState<FinanzGruppe | null>(null);
  const [deleteError, setDeleteError] = useState<string | null>(null);

  /* =========================================================
     CALCULATIONS
     ========================================================= */

  const zugeordnet = useMemo(
    () => groups.reduce((sum, group) => sum + group.teilnehmer.length, 0),
    [groups],
  );

  /* =========================================================
     LOAD
     ========================================================= */

  const loadGroups = useCallback(async () => {
    const data = await getFinanzgruppen(veranstaltungId);

    setGroups(data);
  }, [veranstaltungId]);

  const loadTeilnehmerCount = useCallback(async () => {
    const count = await getTeilnehmerCount(veranstaltungId);

    setGesamtTeilnehmer(count);
  }, [veranstaltungId]);

  useEffect(() => {
    Promise.all([loadGroups(), loadTeilnehmerCount()]);
  }, [loadGroups, loadTeilnehmerCount]);

  /* =========================================================
     SEARCH
     ========================================================= */

  useEffect(() => {
    if (!dialogOpen) {
      return;
    }

    const timeout = window.setTimeout(async () => {
      const results = await searchTeilnehmer(veranstaltungId, search);

      setSearchResults(results);
    }, 300);

    return () => {
      window.clearTimeout(timeout);
    };
  }, [dialogOpen, search, veranstaltungId]);

  /* =========================================================
     CREATE
     ========================================================= */

  const handleCreate = useCallback(async () => {
    const kuerzel = newKuerzel.trim();

    if (!kuerzel) {
      return;
    }

    await createFinanzgruppe(veranstaltungId, kuerzel);

    setNewKuerzel("");

    await loadGroups();
  }, [newKuerzel, veranstaltungId, loadGroups]);

  /* =========================================================
     ASSIGN
     ========================================================= */

  const openAssignDialog = useCallback((groupId: number) => {
    setSelectedGroup(groupId);
    setDialogOpen(true);

    setSearch("");
    setSearchResults([]);
    setSelectedIds([]);
  }, []);

  const closeAssignDialog = useCallback(() => {
    setDialogOpen(false);

    setSelectedGroup(null);
    setSearch("");
    setSearchResults([]);
    setSelectedIds([]);
  }, []);

  const toggleParticipant = useCallback((personId: number) => {
    setSelectedIds((previous) =>
      previous.includes(personId)
        ? previous.filter((id) => id !== personId)
        : [...previous, personId],
    );
  }, []);

  const handleAssign = useCallback(async () => {
    if (!selectedGroup || selectedIds.length === 0) {
      return;
    }

    await assignTeilnehmerBulk(veranstaltungId, selectedGroup, selectedIds);

    closeAssignDialog();

    await loadGroups();
  }, [selectedGroup, selectedIds, veranstaltungId, closeAssignDialog, loadGroups]);

  /* =========================================================
     REMOVE
     ========================================================= */

  const openRemoveDialog = useCallback((gruppeId: number, personId: number, name: string) => {
    setRemoveTarget({
      gruppeId,
      personId,
      name,
    });
  }, []);

  const closeRemoveDialog = useCallback(() => {
    setRemoveTarget(null);
  }, []);

  const confirmRemove = useCallback(async () => {
    if (!removeTarget) {
      return;
    }

    await removeTeilnehmerFromFinanzgruppe(
      veranstaltungId,
      removeTarget.gruppeId,
      removeTarget.personId,
    );

    closeRemoveDialog();

    await loadGroups();
  }, [removeTarget, veranstaltungId, closeRemoveDialog, loadGroups]);

  /* =========================================================
     DELETE
     ========================================================= */

  const openDeleteDialog = useCallback((gruppe: FinanzGruppe) => {
    setDeleteTarget(gruppe);
    setDeleteError(null);
  }, []);

  const closeDeleteDialog = useCallback(() => {
    setDeleteTarget(null);
    setDeleteError(null);
  }, []);

  const confirmDelete = useCallback(async () => {
    if (!deleteTarget) {
      return;
    }

    try {
      await deleteFinanzGruppe(veranstaltungId, deleteTarget.id);

      closeDeleteDialog();

      await loadGroups();
    } catch (error: unknown) {
      if (error instanceof AxiosError && error.response?.status === 409) {
        setDeleteError(error.response.data?.message ?? "Löschen nicht möglich.");

        return;
      }

      setDeleteError("Beim Löschen ist ein Fehler aufgetreten.");
    }
  }, [deleteTarget, veranstaltungId, closeDeleteDialog, loadGroups]);

  /* =========================================================
     TABLE
     ========================================================= */

  const columns = useMemo(
    () =>
      kuerzelColumns({
        onAddTeilnehmer: openAssignDialog,

        onDelete: openDeleteDialog,

        onRemoveTeilnehmer: openRemoveDialog,
      }),
    [openAssignDialog, openDeleteDialog, openRemoveDialog],
  );

  /* =========================================================
     UI
     ========================================================= */

  return (
    <Box
      sx={{
        minHeight: {
          md: "calc(100vh - 140px)",
        },

        display: "flex",
        flexDirection: "column",
      }}
    >
      {/* HEADER */}

      <Stack direction="row" justifyContent="space-between" alignItems="center" mb={2}>
        <Typography variant="h5">Konten-Verwaltung</Typography>

        <Typography
          color={zugeordnet === gesamtTeilnehmer ? "success.main" : "warning.main"}
          fontWeight={700}
        >
          {zugeordnet} / {gesamtTeilnehmer} Teilnehmer zugeordnet
        </Typography>
      </Stack>

      {/* CREATE */}

      <KontoCreateForm value={newKuerzel} onChange={setNewKuerzel} onCreate={handleCreate} />

      {/* TABLE */}

      <Box
        sx={{
          flex: 1,
          minHeight: 0,
        }}
      >
        <GenericTableTanstack<FinanzGruppe>
          data={groups}
          columns={columns}
          loading={false}
          mobileRenderRow={(row) => (
            <Box>
              <Typography fontWeight={700} sx={{ mb: 1 }}>
                {row.kuerzel}
              </Typography>

              <Stack direction="row" spacing={0.5} useFlexGap flexWrap="wrap">
                {row.teilnehmer.map((teilnehmer) => (
                  <Chip
                    key={teilnehmer.id}
                    size="small"
                    label={`${teilnehmer.vorname} ${teilnehmer.nachname}`}
                    onDelete={
                      row.system
                        ? undefined
                        : () =>
                            openRemoveDialog(
                              row.id,
                              teilnehmer.personId,
                              `${teilnehmer.vorname} ${teilnehmer.nachname}`,
                            )
                    }
                  />
                ))}
              </Stack>

              {!row.system && (
                <Stack direction="row" spacing={1} sx={{ mt: 1 }}>
                  <Button size="small" variant="outlined" onClick={() => openAssignDialog(row.id)}>
                    + Teilnehmer
                  </Button>

                  <Button
                    size="small"
                    color="error"
                    variant="outlined"
                    onClick={() => openDeleteDialog(row)}
                  >
                    Löschen
                  </Button>
                </Stack>
              )}
            </Box>
          )}
        />
      </Box>

      {/* DIALOGS */}

      <TeilnehmerZuordnenDialog
        open={dialogOpen}
        search={search}
        results={searchResults}
        selectedIds={selectedIds}
        onClose={closeAssignDialog}
        onSearchChange={setSearch}
        onToggle={toggleParticipant}
        onAssign={handleAssign}
      />

      <TeilnehmerEntfernenDialog
        open={!!removeTarget}
        name={removeTarget?.name}
        onClose={closeRemoveDialog}
        onConfirm={confirmRemove}
      />

      <KontoLoeschenDialog
        open={!!deleteTarget}
        kuerzel={deleteTarget?.kuerzel}
        error={deleteError}
        onClose={closeDeleteDialog}
        onConfirm={confirmDelete}
      />

  
    </Box>
  );
}
