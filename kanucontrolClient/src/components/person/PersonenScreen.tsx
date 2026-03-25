import { useEffect, useState, useRef } from "react";
import { Box, Grid, Paper, Typography, TextField, Button } from "@mui/material";

import { GenericTable } from "@/components/common/GenericTable";
import { PersonFormView } from "@/components/person/PersonFormView";
import { personColumns } from "@/components/person/personColumns";

import {
  getPersonById,
  deletePerson,
  updatePerson,
  getPersonsScroll,
} from "@/api/services/personApi";

import { PersonList, PersonDetail, PersonSave } from "@/api/types/Person";
import { useDebounce } from "@/components/common/reference/hooks";

/* ========================================================= */

type Cursor = {
  name: string;
  vorname: string;
  id: number;
} | null;

export default function PersonenScreen() {
  /* ================= STATE ================= */

  const [rows, setRows] = useState<PersonList[]>([]);
  const [hasMore, setHasMore] = useState(true);
  const [loading, setLoading] = useState(false);

  const cursorRef = useRef<Cursor>(null);
  const loadingRef = useRef(false);
  const [total, setTotal] = useState(0);

  const [editMode, setEditMode] = useState(false);
  const [editData, setEditData] = useState<PersonDetail | null>(null);

  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [selectedPerson, setSelectedPerson] = useState<PersonDetail | null>(null);
  const [loadingDetail, setLoadingDetail] = useState(false);

  /* ================= FILTER ================= */

  const [search, setSearch] = useState("");
  const debounceSearch = useDebounce(search, 300);

  const size = 25;

  /* ========================================================= */
  /* 🔄 LOAD (Cursor-based) */
  /* ========================================================= */

  const load = async () => {
    if (loadingRef.current || !hasMore) return;

    loadingRef.current = true;
    setLoading(true);

    try {
      const res = await getPersonsScroll(
        cursorRef.current?.name,
        cursorRef.current?.vorname,
        cursorRef.current?.id,
        size,
        {
          search: debounceSearch || undefined,
        },
      );

      const newRows = res.content; // ⭐ WICHTIG

      if (!newRows.length) {
        setHasMore(false);
        return;
      }

      setRows((prev) => {
        const existing = new Set(prev.map((r) => r.id));
        const filtered = newRows.filter((r) => !existing.has(r.id));
        return [...prev, ...filtered];
      });

      const isFirstPage = cursorRef.current === null;

      if (isFirstPage) {
        setTotal(res.total);
      }

      // ⭐ Cursor setzen
      const last = newRows[newRows.length - 1];

      cursorRef.current = {
        name: last.name,
        vorname: last.vorname,
        id: last.id,
      };

      // ⭐ total setzen (optional optimiert, siehe unten)
    } finally {
      loadingRef.current = false;
      setLoading(false);
    }
  };

  /* ========================================================= */
  /* 🔁 loadRef (STABIL!) */
  /* ========================================================= */

  const loadRef = useRef(load);

  useEffect(() => {
    loadRef.current = load;
  });

  /* ========================================================= */
  /* 🔄 INITIAL + SEARCH RESET */
  /* ========================================================= */

  useEffect(() => {
    cursorRef.current = null;
    setRows([]);
    setHasMore(true);

    loadRef.current(); // ⭐ initial load
  }, [debounceSearch]);

  /* ========================================================= */
  /* 🔄 DETAIL */
  /* ========================================================= */

  useEffect(() => {
    if (!selectedId) {
      setSelectedPerson(null);
      return;
    }

    const run = async () => {
      setLoadingDetail(true);
      try {
        const d = await getPersonById(selectedId);
        setSelectedPerson(d);
      } finally {
        setLoadingDetail(false);
      }
    };

    run();
  }, [selectedId]);

  /* ========================================================= */
  /* ACTIONS */
  /* ========================================================= */

  const handleEdit = () => {
    if (!selectedPerson) return;

    setEditData({ ...selectedPerson }); // ⭐ HIER!
    setEditMode(true);
  };

  const handleSave = async (data: PersonSave) => {
    if (!selectedId) return;

    const updated = await updatePerson(selectedId, data);
    setSelectedPerson(updated);

    setEditMode(false);

    cursorRef.current = null;
    setRows([]);
    setHasMore(true);
    loadRef.current();
  };

  const handleDelete = async () => {
    if (!selectedId) return;

    await deletePerson(selectedId);

    setSelectedId(null);
    setSelectedPerson(null);

    // ⭐ ALLES resetten
    cursorRef.current = null;
    setRows([]);
    setHasMore(true);
    setEditMode(false);
    setEditData(null);

    load(); // ⭐ neu laden
  };

const resetFilter = () => {
  // ⭐ alles sauber zurücksetzen
  cursorRef.current = null;
  setRows([]);
  setHasMore(true);

  setSearch(""); // UI

  loadRef.current(); // ⭐ DIREKT laden
};

  useEffect(() => {
    setEditMode(false); 
  }, [selectedId]);

  /* ========================================================= */
  /* UI */
  /* ========================================================= */

  return (
    <Box>
      <Typography variant="h5" sx={{ mb: 2, fontWeight: 600 }}>
        {total} Personen
      </Typography>

      <Grid container spacing={2}>
        {/* LEFT */}
        <Grid size={{ xs: 12, md: 6 }}>
          <Paper sx={{ p: 2 }}>
            <Box display="flex" gap={1} mb={2}>
              <TextField
                size="small"
                label="Suche"
                value={search}
                onChange={(e) => setSearch(e.target.value)}
                fullWidth
              />
              <Button onClick={resetFilter}>Reset</Button>
            </Box>

            <GenericTable<PersonList>
              rows={rows}
              columns={personColumns}
              loading={loading}
              onLoadMore={() => loadRef.current()}
              selectedRowId={selectedId}
              onSelectRow={(row) => {
                // console.log("selected:", row); // ⭐ DEBUG
                setSelectedId(row?.id ?? null);
              }}
            />

            {loading && (
              <Box textAlign="center" p={1}>
                ⏳ Lade mehr...
              </Box>
            )}
          </Paper>
        </Grid>

        {/* RIGHT */}
        <Grid size={{ xs: 12, md: 6 }}>
          <Paper sx={{ p: 2 }}>
            {loadingDetail ? (
              <Typography>Lade Details...</Typography>
            ) : !selectedPerson ? (
              <Typography color="text.secondary">Bitte Person auswählen</Typography>
            ) : (
              <PersonFormView
                personDetail={editMode ? editData : selectedPerson} // ⭐ SWITCH
                editMode={editMode}
                onEdit={handleEdit}
                onCancelEdit={() => {
                  setEditMode(false);
                  setEditData(null);
                }}
                onSpeichern={handleSave}
                onDeletePerson={handleDelete}
                onDeleteMitglied={async () => {}}
                onSetHauptverein={async () => {}}
                onBack={() => setSelectedId(null)}
                onReloadPerson={async () => {
                  if (selectedId) {
                    const fresh = await getPersonById(selectedId);
                    setSelectedPerson(fresh);
                  }
                }}
                btnÄndernPerson={false}
                btnLöschenPerson={false}
              />
            )}
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );
}
