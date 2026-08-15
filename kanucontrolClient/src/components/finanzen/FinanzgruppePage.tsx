import { useEffect, useState, useCallback, useRef } from "react";
import { GenericTableTanstack } from "@/components/common/GenericTableTanstack";
import { BottomActionBar } from "@/components/layout/BottomActionBar";
import { kuerzelColumns } from "@/components/finanzen/finanzgruppeColumns";
import FinanzGruppeBelege from "@/components/finanzgruppen/FinanzGruppeBelege";
import {
  Alert,
  Box,
  Button,
  Chip,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Paper,
  Stack,
  TextField,
  Typography,
} from "@mui/material";

import { AxiosError } from "axios";

import {
  getFinanzgruppen,
  createFinanzgruppe,
  assignTeilnehmerBulk,
  deleteFinanzGruppe,
  FinanzGruppe,
} from "@/api/services/finanzgruppenApi";

import {
  searchTeilnehmer,
  removeTeilnehmerFromGruppe,
  getTeilnehmerCount,
} from "@/api/services/teilnehmerApi";

/* =========================================================
   TYPES
   ========================================================= */

type TeilnehmerSearch = {
  personId: number;
  vorname: string;
  name: string;
  hauptvereinAbk?: string;
};

type Props = {
  veranstaltungId: number;
};

/* =========================================================
   COMPONENT
   ========================================================= */

export default function KuerzelPage({ veranstaltungId }: Props) {
  const [groups, setGroups] = useState<FinanzGruppe[]>([]);
  const [newKuerzel, setNewKuerzel] = useState("");

  const [dialogOpen, setDialogOpen] = useState(false);
  const [selectedGroup, setSelectedGroup] = useState<number | null>(null);
  const [search, setSearch] = useState("");
  const [searchResults, setSearchResults] = useState<TeilnehmerSearch[]>([]);
  const [selectedIds, setSelectedIds] = useState<number[]>([]);

  const [confirmOpen, setConfirmOpen] = useState(false);
  const [removeTarget, setRemoveTarget] = useState<{
    gruppeId: number;
    personId: number;
    name: string;
  } | null>(null);

  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState<FinanzGruppe | null>(null);
  const [deleteError, setDeleteError] = useState<string | null>(null);

  const inputRef = useRef<HTMLInputElement>(null);
  const [gesamtTeilnehmer, setGesamtTeilnehmer] = useState(0);
  const zugeordnet = groups.reduce((sum, g) => sum + g.teilnehmer.length, 0);

  /* ================= LOAD GROUPS ================= */

  const loadGroups = useCallback(async () => {
    const data = await getFinanzgruppen(veranstaltungId);
    setGroups(data);
  }, [veranstaltungId]);

  const loadTeilnehmerCount = useCallback(async () => {
    const count = await getTeilnehmerCount(veranstaltungId);
    setGesamtTeilnehmer(count);
  }, [veranstaltungId]);

 useEffect(() => {
   loadGroups();
   loadTeilnehmerCount();
 }, [loadGroups, loadTeilnehmerCount]);

  useEffect(() => {
    const timeout = setTimeout(async () => {
      const results = await searchTeilnehmer(veranstaltungId, search);
      setSearchResults(results);
    }, 300);

    return () => clearTimeout(timeout);
  }, [search, veranstaltungId]);

  useEffect(() => {
    if (dialogOpen) {
      setTimeout(() => {
        inputRef.current?.focus();
      }, 50);
    }
  }, [dialogOpen]);

  /* ================= CREATE ================= */

  async function handleCreate() {
    if (!newKuerzel.trim()) return;

    await createFinanzgruppe(veranstaltungId, newKuerzel);
    setNewKuerzel("");
    loadGroups();
  }

  /* ================= DELETE KÜRZEL ================= */

  function openDeleteDialog(gruppe: FinanzGruppe) {
    setDeleteTarget(gruppe);
    setDeleteError(null);
    setDeleteDialogOpen(true);
  }

  async function handleConfirmDelete() {
    if (!deleteTarget) return;

    try {
      await deleteFinanzGruppe(veranstaltungId, deleteTarget.id);

      setDeleteDialogOpen(false);
      setDeleteTarget(null);
      loadGroups();
    } catch (error: unknown) {
      if (error instanceof AxiosError) {
        if (error.response?.status === 409) {
          setDeleteError(error.response.data?.message ?? "Löschen nicht möglich.");
        } else {
          setDeleteError("Fehler beim Löschen.");
        }
      } else {
        setDeleteError("Unbekannter Fehler.");
      }
    }
  }

  /* ================= ADD TEILNEHMER ================= */

  async function openDialog(groupId: number) {
    setSelectedGroup(groupId);
    setDialogOpen(true);
    setSearch("");
    setSelectedIds([]);

    const results = await searchTeilnehmer(veranstaltungId, "");
    setSearchResults(results);
  }

  function toggle(personId: number) {
    setSelectedIds((prev) =>
      prev.includes(personId) ? prev.filter((i) => i !== personId) : [...prev, personId],
    );
  }

  async function handleAssign() {
    if (!selectedGroup || selectedIds.length === 0) return;

    await assignTeilnehmerBulk(veranstaltungId, selectedGroup, selectedIds);

    setDialogOpen(false);
    await loadGroups();
  }

  /* ================= REMOVE TEILNEHMER ================= */

  function openConfirm(gruppeId: number, personId: number, name: string) {
    setRemoveTarget({ gruppeId, personId, name });
    setConfirmOpen(true);
  }

  async function confirmRemove() {
    if (!removeTarget) return;

    await removeTeilnehmerFromGruppe(veranstaltungId, removeTarget.gruppeId, removeTarget.personId);

    setConfirmOpen(false);
    setRemoveTarget(null);
   await loadGroups();
    
  }

  const columns = kuerzelColumns({
    onAddTeilnehmer: openDialog,

    onDelete: openDeleteDialog,

    onRemoveTeilnehmer: openConfirm,
  });

  /* ================= UI ================= */

  return (
    <Box>
      <Stack direction="row" justifyContent="space-between" alignItems="center" mb={2}>
        <Typography variant="h5">Konten-Verwaltung</Typography>

        <Typography
          color={zugeordnet === gesamtTeilnehmer ? "success.main" : "warning.main"}
          fontWeight={700}
        >
          {zugeordnet} / {gesamtTeilnehmer} Teilnehmer zugeordnet
        </Typography>
      </Stack>

      <Paper sx={{ p: 2, mb: 3 }}>
        <Stack direction="row" spacing={2}>
          <TextField
            size="small"
            placeholder="Neues Konto"
            value={newKuerzel}
            onChange={(e) => setNewKuerzel(e.target.value)}
          />
          <Button variant="contained" onClick={handleCreate}>
            Hinzufügen
          </Button>
        </Stack>
      </Paper>

      <GenericTableTanstack<FinanzGruppe>
        data={groups}
        columns={columns}
        loading={false}
        height={500}
        detailPanel={(gruppe) => (
          <FinanzGruppeBelege veranstaltungId={veranstaltungId} finanzGruppeId={gruppe.id} />
        )}
        mobileRenderRow={(row) => (
          <Box>
            <Box
              sx={{
                display: "flex",
                justifyContent: "space-between",
                alignItems: "baseline",
                mb: 1,
              }}
            >
              <Typography fontWeight={700}>{row.kuerzel}</Typography>

              <Typography
                variant="body2"
                color="text.secondary"
                sx={{
                  whiteSpace: "nowrap",
                }}
              >
                {row.belegCount} Belege
              </Typography>
            </Box>

            <Stack direction="row" spacing={0.5} useFlexGap flexWrap="wrap">
              {row.teilnehmer.map((t) => (
                <Chip
                  key={t.id}
                  size="small"
                  label={`${t.vorname} ${t.nachname}`}
                  onDelete={
                    row.system
                      ? undefined
                      : () => openConfirm(row.id, t.personId, `${t.vorname} ${t.nachname}`)
                  }
                />
              ))}
            </Stack>

            <Stack direction="row" spacing={1} sx={{ mt: 1 }}>
              {!row.system && (
                <>
                  <Button size="small" variant="outlined" onClick={() => openDialog(row.id)}>
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
                </>
              )}
            </Stack>
          </Box>
        )}
      />

      {/* ADD DIALOG */}
      <Dialog open={dialogOpen} onClose={() => setDialogOpen(false)} fullWidth>
        <DialogTitle>Teilnehmer hinzufügen</DialogTitle>

        <DialogContent>
          <Stack direction="row" spacing={2} mb={2}>
            <TextField
              size="small"
              fullWidth
              inputRef={inputRef}
              placeholder="Name suchen..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
          </Stack>

          {searchResults.map((t) => (
            <Box
              key={t.personId}
              sx={{
                cursor: "pointer",
                p: 1,
                bgcolor: selectedIds.includes(t.personId) ? "action.selected" : "transparent",
              }}
              onClick={() => toggle(t.personId)}
            >
              <Typography>
                {t.name}, {t.vorname}
              </Typography>
            </Box>
          ))}
        </DialogContent>

        <DialogActions>
          <Button onClick={() => setDialogOpen(false)}>Abbrechen</Button>
          <Button variant="contained" onClick={handleAssign} disabled={selectedIds.length === 0}>
            Zuweisen
          </Button>
        </DialogActions>
      </Dialog>

      {/* REMOVE TEILNEHMER */}
      <Dialog open={confirmOpen} onClose={() => setConfirmOpen(false)}>
        <DialogTitle>Teilnehmer entfernen</DialogTitle>
        <DialogContent>
          <Typography>
            Möchten Sie <strong>{removeTarget?.name}</strong> wirklich aus dieser Finanzgruppe
            entfernen?
          </Typography>
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setConfirmOpen(false)}>Abbrechen</Button>
          <Button color="error" variant="contained" onClick={confirmRemove}>
            Entfernen
          </Button>
        </DialogActions>
      </Dialog>

      {/* DELETE KÜRZEL */}
      <Dialog open={deleteDialogOpen} onClose={() => setDeleteDialogOpen(false)}>
        <DialogTitle>Konto löschen</DialogTitle>

        <DialogContent>
          {deleteError ? (
            <Alert severity="error">{deleteError}</Alert>
          ) : (
            <Typography>
              Möchten Sie das Konto <strong>{deleteTarget?.kuerzel}</strong> wirklich
              löschen?
            </Typography>
          )}
        </DialogContent>

        <DialogActions>
          <Button onClick={() => setDeleteDialogOpen(false)}>Abbrechen</Button>

          {!deleteError && (
            <Button color="error" variant="contained" onClick={handleConfirmDelete}>
              Löschen
            </Button>
          )}
        </DialogActions>
      </Dialog>
      <BottomActionBar
        left={[
          {
            label: "Zurück",
            variant: "outlined",
            onClick: () => window.history.back(),
          },
        ]}
      />
    </Box>
  );
}
