import { useEffect, useState, useCallback, useRef } from "react";
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
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableRow,
  TextField,
  Typography,
} from "@mui/material";

import { AxiosError } from "axios";

import {
  getFinanzgruppen,
  createFinanzgruppe,
  assignTeilnehmerBulk,
  deleteFinanzGruppe,
  FinanzGruppe
} from "@/api/client/finanzgruppenApi";

import { searchTeilnehmer, removeTeilnehmerFromGruppe } from "@/api/services/teilnehmerApi";

/* =========================================================
   TYPES
   ========================================================= */

type TeilnehmerSearch = {
  id: number;
  personId: number;
  person: {
    id: number;
    vorname: string;
    name: string;
  };
  rolle: string | null;
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

  /* ================= LOAD GROUPS ================= */

  const loadGroups = useCallback(async () => {
    const data = await getFinanzgruppen(veranstaltungId);
    setGroups(data);
  }, [veranstaltungId]);

  useEffect(() => {
    loadGroups();
  }, [loadGroups]);

  useEffect(() => {
    if (!search.trim()) {
      setSearchResults([]);
      return;
    }

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

  function openDialog(groupId: number) {
    setSelectedGroup(groupId);
    setDialogOpen(true);
    setSearch("");
    setSelectedIds([]);
    setSearchResults([]);
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
    loadGroups();
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
    loadGroups();
  }

  /* ================= UI ================= */

  return (
    <Box>
      <Typography variant="h5" mb={2}>
        Kürzel-Verwaltung
      </Typography>

      <Paper sx={{ p: 2, mb: 3 }}>
        <Stack direction="row" spacing={2}>
          <TextField
            size="small"
            placeholder="Neues Kürzel"
            value={newKuerzel}
            onChange={(e) => setNewKuerzel(e.target.value)}
          />
          <Button variant="contained" onClick={handleCreate}>
            Hinzufügen
          </Button>
        </Stack>
      </Paper>

      <Paper sx={{ p: 2 }}>
        <Table size="small">
          <TableHead>
            <TableRow>
              <TableCell>Kürzel</TableCell>
              <TableCell>Teilnehmer</TableCell>
              <TableCell>Belege</TableCell>
              <TableCell />
            </TableRow>
          </TableHead>

          <TableBody>
            {groups.map((g) => (
              <TableRow key={g.id}>
                <TableCell>{g.kuerzel}</TableCell>

                <TableCell>
                  {g.teilnehmer.map((t) => (
                    <Chip
                      key={t.id}
                      size="small"
                      label={`${t.vorname} ${t.nachname}`}
                      onDelete={() => openConfirm(g.id, t.personId, `${t.vorname} ${t.nachname}`)}
                      sx={{ mr: 1, mb: 1 }}
                    />
                  ))}
                </TableCell>

                <TableCell>{g.belegCount}</TableCell>

                <TableCell>
                  <Stack direction="row" spacing={1}>
                    <Button size="small" onClick={() => openDialog(g.id)}>
                      + Teilnehmer
                    </Button>

                    <Button size="small" color="error" onClick={() => openDeleteDialog(g)}>
                      Löschen
                    </Button>
                  </Stack>
                </TableCell>
              </TableRow>
            ))}
          </TableBody>
        </Table>
      </Paper>

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
              {t.person.vorname} {t.person.name}
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
            Möchten Sie <strong>{removeTarget?.name}</strong> wirklich aus diesem Kürzel entfernen?
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
        <DialogTitle>Kürzel löschen</DialogTitle>

        <DialogContent>
          {deleteError ? (
            <Alert severity="error">{deleteError}</Alert>
          ) : (
            <Typography>
              Möchten Sie das Kürzel <strong>{deleteTarget?.kuerzel}</strong> wirklich löschen?
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
    </Box>
  );
}
