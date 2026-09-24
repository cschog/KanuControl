// src/components/person/PersonenScreen.tsx

import { useCallback, useEffect, useState, useRef } from "react"

import { useTheme } from "@mui/material/styles";
import { useMediaQuery } from "@mui/material";
import { GenericTableTanstack } from "@/components/common/GenericTableTanstack";
import { PersonFormView } from "@/components/person/PersonFormView";
import { personColumnsTanstack } from "@/components/person/personColumnsTanstack";
import { deleteMitglied, setHauptverein } from "@/api/services/mitgliedApi";
import { PersonCreateDialog } from "@/components/person/PersonCreateDialog";
import { useAppContext } from "@/context/AppContext";
import { addTeilnehmer } from "@/api/services/teilnehmerApi";
import { BottomActionBar } from "@/components/layout/BottomActionBar";
import { GridFilterModel } from "@mui/x-data-grid";
import { getApiErrorMessage } from "@/api/utils/apiError";
import { ErrorDialog } from "@/components/common/ErrorDialog";
import { VereinAutocomplete } from "@/components/verein/VereinAutocomplete";
import { VereinRef } from "@/api/types/verein/VereinRef";

import {
  getPersonById,
  deletePerson,
  deletePersons,
  updatePerson,
  getPersonsScroll,
  createPerson,
  exportPersonsCsv,
} from "@/api/services/personApi";

import { PersonList, PersonDetail, PersonSave } from "@/api/types/person/Person";
import { useDebounce } from "@/components/common/reference/hooks";
import SearchField from "@/components/common/SearchField";

import {
  Box,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Paper,
  Typography,
  ToggleButton,
  ToggleButtonGroup,
  Tooltip,
} from "@mui/material";

/* ========================================================= */

type Cursor = {
  name: string;
  vorname: string;
  id: number;
} | null;

export default function PersonenScreen() {
  /* ================= STATE ================= */

  const [rows, setRows] = useState<PersonList[]>([]);
  const [loading, setLoading] = useState(false);

  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down("md"));

  const cursorRef = useRef<Cursor>(null);
  const loadingRef = useRef(false);
  const hasMoreRef = useRef(true);
  const [total, setTotal] = useState(0);

  const [editMode, setEditMode] = useState(false);
  const [editData, setEditData] = useState<PersonDetail | null>(null);

  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [selectionMode, setSelectionMode] = useState(false);
  const [selectedIds, setSelectedIds] = useState<Set<number>>(new Set());

  const [selectedPerson, setSelectedPerson] = useState<PersonDetail | null>(null);
  const [loadingDetail, setLoadingDetail] = useState(false);

  const [createOpen, setCreateOpen] = useState(false);
  const [copyOpen, setCopyOpen] = useState(false);
  const [copyData, setCopyData] = useState<Partial<PersonSave>>();

  const { active } = useAppContext();
  const [error, setError] = useState<string | null>(null);

  const [deleteConfirmOpen, setDeleteConfirmOpen] = useState(false);

  /* ================= FILTER ================= */

  const [search, setSearch] = useState("");
  const debounceSearch = useDebounce(search, 300);
  const [aktivFilter, setAktivFilter] = useState<"aktiv" | "alle" | "inaktiv">("aktiv");
  const [vereinFilter, setVereinFilter] = useState<VereinRef | undefined>(undefined);

  const size = 500;

  const [filterModel] = useState<GridFilterModel>({
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

    try {
      const res = await getPersonsScroll(
        cursorRef.current?.name,
        cursorRef.current?.vorname,
        cursorRef.current?.id,
        size,
        {
          search: debounceSearch || undefined,
          ort: ortFilter || undefined,
          vereinId: vereinFilter?.id,
          aktiv: aktivFilter === "aktiv" ? true : aktivFilter === "inaktiv" ? false : undefined,
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

  const handleRowSelectionChange = useCallback((persons: PersonList[]) => {
    const nextIds = new Set(persons.map((person) => person.id));

    setSelectedIds((current) => {
      if (current.size === nextIds.size && [...current].every((id) => nextIds.has(id))) {
        return current;
      }

      return nextIds;
    });
  }, []);

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
    loadRef.current();
  }, [debounceSearch, vereinFilter, filterModel, sorting, aktivFilter]);

  useEffect(() => {
    setSelectedIds(new Set());
  }, [debounceSearch, vereinFilter, aktivFilter, filterModel, sorting]);

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

    try {
      const deletedId = selectedId;

      await deletePerson(deletedId);

      setRows((prev) => prev.filter((p) => p.id !== deletedId));
      setTotal((prev) => Math.max(0, prev - 1));

      setSelectedId(null);
      setSelectedPerson(null);
      setEditMode(false);
      setEditData(null);
    } catch (error: unknown) {
      console.error("Fehler beim Löschen der Person", error);

      setError(getApiErrorMessage(error, "Person konnte nicht gelöscht werden."));
    }
  };

  const handleDeleteSelected = async () => {
    if (selectedIds.size === 0) return;

    setDeleteConfirmOpen(true);
  };

  const executeDeleteSelected = async () => {
    setDeleteConfirmOpen(false);

    try {
      const result = await deletePersons([...selectedIds]);

      if (result.deletedIds.length > 0) {
        setRows((prev) => prev.filter((person) => !result.deletedIds.includes(person.id)));

        setTotal((prev) => Math.max(0, prev - result.deletedIds.length));
      }

      setSelectedIds(new Set(result.errors.map((item) => item.id)));

      if (result.errors.length === 0) {
        setSelectionMode(false);
        setSelectedIds(new Set());
      } else {
        const message = result.errors
          .map((item) => `Person ${item.id}:\n${item.message}`)
          .join("\n\n");

        setError(message);
      }
    } catch (error: unknown) {
      console.error("Fehler beim Löschen der Personen", error);

      setError(
        getApiErrorMessage(error, "Die ausgewählten Personen konnten nicht gelöscht werden."),
      );
    }
  };



  const handleCsvExport = async () => {
    if (selectedIds.size === 0) return;

    try {
      const blob = await exportPersonsCsv([...selectedIds]);

      const url = window.URL.createObjectURL(blob);
      const link = document.createElement("a");

      link.href = url;
      link.download = "personen-export.csv";
      document.body.appendChild(link);
      link.click();
      link.remove();

      window.URL.revokeObjectURL(url);
    } catch (error) {
      console.error("CSV-Export fehlgeschlagen", error);
    }
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
            <Box
              sx={{
                display: "flex",
                gap: isMobile ? 1 : 2,
                mb: 2,
                alignItems: "stretch",
                flexDirection: isMobile ? "column" : "row",
              }}
            >
              {/* SUCHE */}

              <Box
                sx={{
                  flex: 1,
                  minWidth: 0,
                  width: isMobile ? "100%" : undefined,
                }}
              >
                <SearchField value={search} onChange={setSearch} />
              </Box>

              {/* VEREIN */}

              <Box
                sx={{
                  width: isMobile ? "100%" : 220,
                  flexShrink: 0,
                }}
              >
                <VereinAutocomplete value={vereinFilter} onChange={setVereinFilter} />
              </Box>

              {/* STATUS */}

              <ToggleButtonGroup
                value={aktivFilter}
                exclusive
                onChange={(_, value) => {
                  if (value !== null) {
                    setAktivFilter(value);
                  }
                }}
                size="small"
                sx={{
                  flexShrink: 0,
                  width: isMobile ? "100%" : undefined,

                  "& .MuiToggleButton-root": {
                    flex: isMobile ? 1 : undefined,
                  },
                }}
              >
                <ToggleButton value="aktiv">Aktiv</ToggleButton>
                <ToggleButton value="alle">Alle</ToggleButton>
                <ToggleButton value="inaktiv">Inaktiv</ToggleButton>
              </ToggleButtonGroup>
            </Box>

            <GenericTableTanstack
              mobileRenderRow={(row) => {
                const marker =
                  row.dataStatus === "ERROR"
                    ? {
                        color: "error.main",
                        message: "Fehlende Pflichtangaben",
                      }
                    : row.dataStatus === "WARNING"
                      ? {
                          color: "#F2C94C",
                          message: "Empfohlene Angaben fehlen",
                        }
                      : null;

                return (
                  <Box>
                    <Box
                      sx={{
                        display: "flex",
                        alignItems: "center",
                        gap: 1,
                      }}
                    >
                      {marker && (
                        <Tooltip
                          title={marker.message}
                          arrow
                          placement="top"
                          slotProps={{
                            tooltip: {
                              sx: {
                                fontSize: "0.95rem",
                                lineHeight: 1.4,
                                maxWidth: 360,
                                padding: "10px 14px",
                              },
                            },
                            arrow: {
                              sx: {
                                fontSize: "1rem",
                              },
                            },
                          }}
                        >
                          <Box
                            component="span"
                            sx={{
                              width: 10,
                              height: 10,
                              minWidth: 10,
                              borderRadius: "50%",
                              bgcolor: marker.color,
                              flexShrink: 0,
                              cursor: "help",
                            }}
                          />
                        </Tooltip>
                      )}

                      <Typography fontWeight={600}>
                        {row.name}, {row.vorname}
                      </Typography>
                    </Box>

                    <Typography variant="body2" color="text.secondary">
                      {row.alter ?? "-"} Jahre
                      {" • "}
                      {row.hauptvereinAbk ?? ""}
                      {" • "}
                      {row.ort ?? ""}
                    </Typography>
                  </Box>
                );
              }}
              data={rows}
              columns={personColumnsTanstack}
              loading={loading}
              selectedRowId={selectedId}
              selectedRowIds={[...selectedIds]}
              enableCheckboxSelection={selectionMode}
              selectionOnly={selectionMode}
              onRowSelectionChange={handleRowSelectionChange}
              onSelectRow={(row) => {
                if (!selectionMode) {
                  setSelectedId(row.id);
                }
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
          left={
            selectionMode
              ? [
                  {
                    label: "Auswahl beenden",
                    variant: "outlined",
                    onClick: () => {
                      setSelectionMode(false);
                      setSelectedIds(new Set());
                    },
                  },
                  {
                    label: `Alle auswählen (${rows.length})`,
                    variant: "outlined",
                    onClick: () => {
                      setSelectedIds(new Set(rows.map((row) => row.id)));
                    },
                  },
                  {
                    label: `Auswahl löschen (${selectedIds.size})`,
                    variant: "outlined",
                    onClick: () => {
                      setSelectedIds(new Set());
                    },
                  },
                  {
                    label: `CSV exportieren (${selectedIds.size})`,
                    variant: "contained",
                    disabled: selectedIds.size === 0,
                    onClick: handleCsvExport,
                  },
                  {
                    label: `Löschen (${selectedIds.size})`,
                    variant: "outlined",
                    disabled: selectedIds.size === 0,
                    onClick: handleDeleteSelected,
                  },
                ]
              : [
                  {
                    label: "Auswahl",
                    variant: "outlined",
                    onClick: () => {
                      setSelectionMode(true);
                      setSelectedIds(new Set());
                    },
                  },
                  {
                    label: "Neue Person",
                    variant: "outlined",
                    onClick: () => setCreateOpen(true),
                  },
                ]
          }
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

      <Dialog
        open={deleteConfirmOpen}
        onClose={() => setDeleteConfirmOpen(false)}
        maxWidth="xs"
        fullWidth
      >
        <DialogTitle
          sx={{
            fontWeight: 600,
            pb: 1,
          }}
        >
          Personen löschen
        </DialogTitle>

        <DialogContent>
          <Typography>
            Möchtest du wirklich <strong>{selectedIds.size} Personen</strong> löschen?
          </Typography>

          <Typography variant="body2" color="text.secondary" sx={{ mt: 1.5 }}>
            Personen, die noch in Veranstaltungen oder Fahrkostenabrechnungen verwendet werden,
            können nicht gelöscht werden.
          </Typography>
        </DialogContent>

        <DialogActions sx={{ px: 3, pb: 2 }}>
          <Button onClick={() => setDeleteConfirmOpen(false)} variant="outlined">
            Abbrechen
          </Button>

          <Button onClick={executeDeleteSelected} variant="contained" color="error" autoFocus>
            Löschen
          </Button>
        </DialogActions>
      </Dialog>

      <ErrorDialog
        open={error !== null}
        message={error ?? ""}
        title="Löschen nicht möglich"
        onClose={() => setError(null)}
      />
    </Box>
  );
}
