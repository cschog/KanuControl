import { useEffect, useState, useRef } from "react";
import { Box, Paper, Typography, TextField, Button } from "@mui/material";
import { GenericTableTanstack } from "@/components/common/GenericTableTanstack";
import { PersonFormView } from "@/components/person/PersonFormView";
//import { personColumns, personColumnsMobile } from "@/components/person/personColumns";
import { personColumnsTanstack } from "@/components/person/personColumnsTanstack";
import { deleteMitglied, setHauptverein } from "@/api/services/mitgliedApi";
import { PersonCreateDialog } from "@/components/person/PersonCreateDialog";
import { useAppContext } from "@/context/AppContext";
import { addTeilnehmer } from "@/api/services/teilnehmerApi";
import { useNavigate } from "react-router-dom";
import { BottomActionBar } from "@/components/layout/BottomActionBar";
import { GridFilterModel } from "@mui/x-data-grid";

import {
  getPersonById,
  deletePerson,
  updatePerson,
  getPersonsScroll,
  createPerson,
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

  const navigate = useNavigate();

  const [rows, setRows] = useState<PersonList[]>([]);
  const [loading, setLoading] = useState(false);

  const cursorRef = useRef<Cursor>(null);
  const loadingRef = useRef(false);
  const hasMoreRef = useRef(true);
  const [total, setTotal] = useState(0);

  const [editMode, setEditMode] = useState(false);
  const [editData, setEditData] = useState<PersonDetail | null>(null);

  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [selectedPerson, setSelectedPerson] = useState<PersonDetail | null>(null);
  const [loadingDetail, setLoadingDetail] = useState(false);

  const [createOpen, setCreateOpen] = useState(false);
  const [copyOpen, setCopyOpen] = useState(false);
  const [copyData, setCopyData] = useState<Partial<PersonSave>>();

  const { active } = useAppContext();

  /* ================= FILTER ================= */

  const [search, setSearch] = useState("");
  const debounceSearch = useDebounce(search, 300);

  const size = 25;

  const [filterModel, setFilterModel] = useState<GridFilterModel>({
    items: [],
  });

  const [sorting, setSorting] = useState<{ id: string; desc: boolean }[]>([
    {
      id: "fullName",
      desc: false,
    },
  ]);

  const ortFilter = filterModel.items.find((i) => i.field === "ort")?.value;

  /* ========================================================= */
  /* 🔄 LOAD (Cursor-based) */
  /* ========================================================= */

  const load = async () => {
    if (loadingRef.current || !hasMoreRef.current) return;

    loadingRef.current = true;
    setLoading(true);

    console.log("filterModel", filterModel);
    console.log("ortFilter", ortFilter);

    try {
      const res = await getPersonsScroll(
        cursorRef.current?.name,
        cursorRef.current?.vorname,
        cursorRef.current?.id,
        size,
        {
          search: debounceSearch || undefined,
          ort: ortFilter || undefined,

          sortField: sorting[0]?.id,
          sortDirection: sorting[0]?.desc ? "desc" : "asc",
        },
      );

      const newRows = res.content;

      hasMoreRef.current = res.hasMore;

      if (!newRows.length) {
        return;
      }

      const isFirstPage = cursorRef.current === null;

      setRows((prev) => {
        if (isFirstPage) {
          return newRows;
        }

        const existing = new Set(prev.map((r) => r.id));

        const filtered = newRows.filter((r) => !existing.has(r.id));

        console.log(
          "PREV",
          prev.length,
          "NEW",
          newRows.length,
          "FILTERED",
          filtered.length,
          "LAST ID",
          newRows[newRows.length - 1]?.id,
        );

        console.log("RESULT LENGTH", prev.length + filtered.length);

        return [...prev, ...filtered];
      });

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
    hasMoreRef.current = true;

    loadRef.current(); // ⭐ initial load
  }, [debounceSearch, filterModel, sorting]);

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

  const handleCopy = () => {
    if (!selectedPerson) {
      return;
    }

    setCopyData({
      name: selectedPerson.name,

      strasse: selectedPerson.strasse,

      plz: selectedPerson.plz,
      ort: selectedPerson.ort,

      mitgliedschaften: selectedPerson.mitgliedschaften.map((m) => ({
        vereinId: m.verein.id,

        funktion: m.funktion,

        hauptVerein: m.hauptVerein,
      })),
    });

    setCopyOpen(true);
  };

  const handleSave = async (data: PersonSave) => {
    if (!selectedId) return;

    const updated = await updatePerson(selectedId, data);
    setSelectedPerson(updated);

    setEditMode(false);

    cursorRef.current = null;
    setRows([]);
    hasMoreRef.current = true;
    loadRef.current();
  };

  const handleDelete = async () => {
    if (!selectedId) return;

    const deletedId = selectedId;

    // ⭐ DB DELETE
    await deletePerson(deletedId);

    // ⭐ lokal aus Tabelle entfernen
    setRows((prev) => prev.filter((p) => p.id !== deletedId));

    // ⭐ Counter aktualisieren
    setTotal((prev) => Math.max(0, prev - 1));

    // ⭐ Detail schließen
    setSelectedId(null);

    setSelectedPerson(null);

    setEditMode(false);

    setEditData(null);
  };

  const resetFilter = () => {

    setSearch("");

    setFilterModel({
      items: [],
    });
  };

  useEffect(() => {
    setEditMode(false);
    setEditData(null);
  }, [selectedId]);

  /* ========================================================= */
  /* UI */
  /* ========================================================= */

  return (
    <Box>
      <Typography
        sx={{
          mb: 2,

          fontWeight: 600,

          fontSize: {
            xs: "1.5rem",
            md: "2rem",
          },
        }}
      >
        {total} Personen
      </Typography>

      <>
        {/* ===================================================== */}
        {/* LIST VIEW */}
        {/* ===================================================== */}

        <Box
          sx={{
            display: selectedPerson ? "none" : "block",
          }}
        >
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

            <GenericTableTanstack
              mobileRenderRow={(row) => (
                <Box>
                  <Typography fontWeight={600}>
                    {row.name}, {row.vorname}
                  </Typography>

                  <Typography variant="body2" color="text.secondary">
                    {row.alter ?? "-"} Jahre
                    {" • "}
                    {row.hauptvereinAbk ?? ""}
                    {" • "}
                    {row.ort ?? ""}
                  </Typography>
                </Box>
              )}
              data={rows}
              columns={personColumnsTanstack}
              loading={loading}
              selectedRowId={selectedId}
              onSelectRow={(row) => {
                setSelectedId(row.id);
              }}
              hasMore={hasMoreRef.current}
              onLoadMore={() => loadRef.current()}
              sorting={sorting}
              onSortingChange={(s) => {
                cursorRef.current = null;

                setRows([]);

                hasMoreRef.current = true;

                setSorting(s);
              }}
            />

            {loading && (
              <Box textAlign="center" p={1}>
                ⏳ Lade mehr...
              </Box>
            )}
          </Paper>
        </Box>

        {/* ===================================================== */}
        {/* DETAIL VIEW */}
        {/* ===================================================== */}

        <Box
          sx={{
            display: selectedPerson ? "block" : "none",
          }}
        >
          <Paper sx={{ p: 2 }}>
            {loadingDetail ? (
              <Typography>Lade Details...</Typography>
            ) : (
              <PersonFormView
                personDetail={editMode ? editData : selectedPerson}
                editMode={editMode}
                onEdit={handleEdit}
                onCopy={handleCopy}
                onCancelEdit={() => {
                  setEditMode(false);

                  setEditData(null);
                }}
                onSpeichern={handleSave}
                onDeletePerson={handleDelete}
                onDeleteMitglied={async (mitgliedId) => {
                  await deleteMitglied(mitgliedId);

                  if (selectedId) {
                    const fresh = await getPersonById(selectedId);

                    setSelectedPerson(fresh);

                    setEditData(fresh);
                  }

                  cursorRef.current = null;

                  setRows([]);

                  hasMoreRef.current = true;

                  await loadRef.current();
                }}
                onSetHauptverein={async (mitgliedId) => {
                  await setHauptverein(mitgliedId);

                  if (selectedId) {
                    const fresh = await getPersonById(selectedId);

                    setSelectedPerson(fresh);

                    setEditData(fresh);
                  }
                }}
                onBack={() => {
                  setSelectedId(null);

                  setSelectedPerson(null);

                  setEditMode(false);

                  setEditData(null);
                }}
                onReloadPerson={async () => {
                  if (selectedId) {
                    const fresh = await getPersonById(selectedId);

                    setSelectedPerson(fresh);

                    setEditData(fresh);
                  }
                }}
                btnÄndernPerson={false}
                btnLöschenPerson={false}
              />
            )}
          </Paper>
        </Box>
      </>

      {!selectedPerson && (
        <BottomActionBar
          left={[
            {
              label: "Neue Person",
              variant: "outlined",
              onClick: () => setCreateOpen(true),
            },
            {
              label: "Zurück",
              variant: "outlined",
              onClick: () => navigate("/startmenue"),
            },
          ]}
        />
      )}
      <PersonCreateDialog
        open={copyOpen}
        onClose={() => setCopyOpen(false)}
        initialData={copyData}
        showAddToVeranstaltung={true}
        onCreate={async (person, addToActiveVeranstaltung) => {
          const created = await createPerson(person);

          // Direkt Teilnehmer hinzufügen
          if (addToActiveVeranstaltung && active?.id) {
            await addTeilnehmer(active.id, created.id);
          }

          // Tabelle neu laden
          cursorRef.current = null;

          setRows([]);

          hasMoreRef.current = true;

          await loadRef.current();

          // Neue Person selektieren
          // setSelectedId(created.id);

          setCopyOpen(false);
        }}
      />

      <PersonCreateDialog
        open={createOpen}
        onClose={() => setCreateOpen(false)}
        showAddToVeranstaltung={true}
        onCreate={async (person, addToActiveVeranstaltung) => {
          const created = await createPerson(person);

          if (addToActiveVeranstaltung && active?.id) {
            await addTeilnehmer(active.id, created.id);
          }

          cursorRef.current = null;
          setRows([]);

          hasMoreRef.current = true;

          await loadRef.current();

          // setSelectedId(created.id);

          setCreateOpen(false);
        }}
      />
    </Box>
  );
}
