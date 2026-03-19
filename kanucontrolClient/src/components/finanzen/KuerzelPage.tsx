import { useEffect, useState, useCallback, useRef } from "react";
import {
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

import {
  getFinanzgruppen,
  createFinanzgruppe,
  assignTeilnehmerBulk,
} from "@/api/client/finanzgruppenApi";

import { searchTeilnehmer, removeTeilnehmerFromGruppe } from "@/api/client/teilnehmerApi";

/* =========================================================
   TYPES
   ========================================================= */

// 🔹 Für Finanzgruppe-Übersicht (KurzDTO vom Backend)
type TeilnehmerKurz = {
  id: number;
  personId: number;
  vorname: string;
  nachname: string;
};

// 🔹 Für Search (TeilnehmerListDTO vom Backend)
type TeilnehmerSearch = {
  id: number; // Teilnehmer-ID
  personId: number; // WICHTIG für Zuweisung
  person: {
    id: number;
    vorname: string;
    name: string;
  };
  rolle: string | null;
};

type FinanzGruppe = {
  id: number;
  kuerzel: string;
  belegCount: number;
  teilnehmer: TeilnehmerKurz[];
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
    return;
  }, [search]);

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

  /* ================= ADD TEILNEHMER ================= */

  function openDialog(groupId: number) {
    setSelectedGroup(groupId);
    setDialogOpen(true);
    setSearch("");
    setSelectedIds([]);
    setSearchResults([]);
  }

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

  function openConfirm(gruppeId: number, personId: number, name: string) {
    setRemoveTarget({ gruppeId, personId, name });
    setConfirmOpen(true);
  }

  async function confirmRemove() {
    if (!removeTarget) return;

    await removeTeilnehmerFromGruppe(
      veranstaltungId,
      removeTarget.gruppeId,
      removeTarget.personId, // 🔥 HIER ist es wichtig
    );

    setConfirmOpen(false);
    setRemoveTarget(null);
    loadGroups();
  }

  async function handleAssign() {
    if (!selectedGroup || selectedIds.length === 0) return;

    await assignTeilnehmerBulk(
      veranstaltungId,
      selectedGroup,
      selectedIds, // personIds
    );

    setDialogOpen(false);
    loadGroups();
  }

  function toggle(personId: number) {
    setSelectedIds((prev) =>
      prev.includes(personId) ? prev.filter((i) => i !== personId) : [...prev, personId],
    );
  }

  /* ================= UI ================= */

  return (
    <Box>
      <Typography variant="h5" mb={2}>
        Kürzel-Verwaltung
      </Typography>

      {/* CREATE */}
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

      {/* GROUP TABLE */}
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
                  <Button size="small" onClick={() => openDialog(g.id)}>
                    + Teilnehmer
                  </Button>
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
          <Box
            component="form"
            onSubmit={(e) => {
              e.preventDefault();
            }}
          >
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
          </Box>

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
    </Box>
  );
}
