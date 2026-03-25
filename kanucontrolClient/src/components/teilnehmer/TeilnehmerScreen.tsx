import { useEffect, useState, useCallback } from "react";
import { Box, Button, Typography, Paper, Grid, TextField } from "@mui/material";
import { useAppContext } from "@/context/AppContext";
import { updateTeilnehmerRolle } from "@/api/services/teilnehmerApi";
import { useDebounce } from "@/components/common/reference/hooks";

import {
  getAvailablePersons,
  getTeilnehmer,
  addTeilnehmerBulk,
  removeTeilnehmerBulk,
} from "@/api/services/teilnehmerApi";

import { GenericTable } from "@/components/common/GenericTable";
import { PersonList } from "@/api/types/PersonList";
import { TeilnehmerList } from "@/api/types/TeilnehmerList";

export default function TeilnehmerScreen() {
  const { active } = useAppContext();

  /* ================= DATA ================= */

  const [available, setAvailable] = useState<PersonList[]>([]);
  const [assigned, setAssigned] = useState<TeilnehmerList[]>([]);

  const [selAvailable, setSelAvailable] = useState<PersonList[]>([]);
  const [selAssigned, setSelAssigned] = useState<TeilnehmerList[]>([]);

  /* ================= FILTER ================= */

  const [searchL, setSearchL] = useState("");
  const [fVereinL, setFVereinL] = useState("");

  const [searchR, setSearchR] = useState("");
  const [fVereinR, setFVereinR] = useState("");

  const debounceSearchL = useDebounce(searchL, 300);
  const debounceSearchR = useDebounce(searchR, 300);

  const debounceVereinL = useDebounce(fVereinL, 300);
  const debounceVereinR = useDebounce(fVereinR, 300);

  /* ================= PAGING ================= */

  const [pageL, setPageL] = useState(0);
  const [sizeL, setSizeL] = useState(50);
  const [totalAvailable, setTotalAvailable] = useState(0);

  const [pageR, setPageR] = useState(0);
  const [sizeR, setSizeR] = useState(50);
  const [totalAssigned, setTotalAssigned] = useState(0);

  /* ================= LOAD ================= */

  const load = useCallback(async () => {
    if (!active?.id) return;

    const a = await getAvailablePersons(
      active.id,
      pageL,
      sizeL,
      debounceSearchL || undefined,
      debounceVereinL || undefined,
    );

    setAvailable(a?.content ?? []);
    setTotalAvailable(a?.totalElements ?? 0);

    const b = await getTeilnehmer(
      active.id,
      pageR,
      sizeR,
      debounceSearchR || undefined,
      debounceVereinR || undefined,
    );

    setAssigned(b?.content ?? []);
    setTotalAssigned(b?.totalElements ?? 0);
  }, [
    active?.id,
    pageL,
    sizeL,
    pageR,
    sizeR,
    debounceSearchL,
    debounceSearchR,
    debounceVereinL,
    debounceVereinR,
  ]);

  useEffect(() => {
    load();
  }, [load]);

  /* ================= COUNTS ================= */

  function countDistinct<T>(arr: T[] | undefined, getKey: (x: T) => string | null | undefined) {
    if (!arr) return 0;

    const set = new Set<string>();
    arr.forEach((x) => {
      const k = getKey(x);
      if (k) set.add(k);
    });
    return set.size;
  }

  const availablePersonCount = totalAvailable;
  const availableVereinCount = countDistinct(available, (p) => p.hauptvereinAbk);

  const assignedPersonCount = totalAssigned;
  const assignedVereinCount = countDistinct(assigned, (t) => t.person?.hauptvereinAbk);

  /* ================= ACTIONS ================= */

  const handleAdd = async () => {
    if (!active?.id || selAvailable.length === 0) return;

    await addTeilnehmerBulk(
      active.id,
      selAvailable.map((p) => p.id),
    );

    setSelAvailable([]);
    await load();
  };

  const handleRemove = async () => {
    if (!active?.id || selAssigned.length === 0) return;

    await removeTeilnehmerBulk(
      active.id,
      selAssigned.map((p) => p.personId),
    );

    setSelAssigned([]);
    await load();
  };

  /* ================= ROLE CHANGE (🔥 OPTIMISTIC) ================= */

  const handleRoleChange = async (current: "L" | "M" | null, personId: number) => {
    if (!active?.id) return;
    if (current === "L") return;

    const newRole = current === "M" ? null : "M";

    // console.log("CLICK ROLE CHANGE", current, "->", newRole);

    // optimistic UI
    setAssigned((prev) =>
      prev.map((t) => (t.personId === personId ? { ...t, rolle: newRole } : t)),
    );

    try {
      await updateTeilnehmerRolle(active.id, personId, newRole);
    } catch {
      // rollback
      setAssigned((prev) =>
        prev.map((t) => (t.personId === personId ? { ...t, rolle: current } : t)),
      );
    }
  };

  /* ================= RESET ================= */

  const resetLeftFilter = () => {
    setSearchL("");
    setFVereinL("");
    setPageL(0);
  };

  const resetRightFilter = () => {
    setSearchR("");
    setFVereinR("");
    setPageR(0);
  };

  /* ================= UI ================= */

  if (!active) {
    return <Typography align="center">Keine aktive Veranstaltung</Typography>;
  }

  return (
    <Box>
      <Typography variant="h5" sx={{ mb: 2, fontWeight: 600 }}>
        Teilnehmer — {active.name}
      </Typography>

      <Grid container spacing={2}>
        {/* LEFT */}
        <Grid size={{ xs: 12, md: 5 }}>
          <Paper sx={{ p: 1 }}>
            <Box display="flex" justifyContent="space-between" mb={1}>
              <Typography variant="body2">
                {availablePersonCount} Personen • {availableVereinCount} Vereine
              </Typography>
              <Button size="small" onClick={resetLeftFilter}>
                Reset
              </Button>
            </Box>

            <Grid container spacing={1} sx={{ mb: 1 }}>
              <Grid size={8}>
                <TextField
                  size="small"
                  label="Suche (Name / Vorname)"
                  value={searchL}
                  onChange={(e) => {
                    setSearchL(e.target.value);
                    setPageL(0); // ⭐ wichtig
                  }}
                  fullWidth
                />
              </Grid>
              <Grid size={4}>
                <TextField
                  size="small"
                  label="Verein"
                  value={fVereinL}
                  onChange={(e) => setFVereinL(e.target.value)}
                  fullWidth
                />
              </Grid>
            </Grid>

            <GenericTable<PersonList>
              rows={available}
              columns={[
                { field: "name", headerName: "Name", flex: 1 },
                { field: "vorname", headerName: "Vorname", flex: 1 },
                { field: "hauptvereinAbk", headerName: "Verein", flex: 1 },
              ]}
              checkboxSelection
              onSelectionChange={setSelAvailable}
              paginationMode="server"
              rowCount={totalAvailable}
              page={pageL}
              pageSize={sizeL}
              onPageChange={setPageL}
              onPageSizeChange={(s) => {
                setSizeL(s);
                setPageL(0);
              }}
            />
          </Paper>
        </Grid>

        {/* ACTIONS */}
        <Grid
          size={{ xs: 12, md: 2 }}
          display="flex"
          flexDirection="column"
          gap={2}
          justifyContent="center"
        >
          <Button variant="contained" disabled={!selAvailable.length} onClick={handleAdd}>
            →
          </Button>
          <Button
            variant="contained"
            color="error"
            disabled={!selAssigned.length}
            onClick={handleRemove}
          >
            ←
          </Button>
        </Grid>

        {/* RIGHT */}
        <Grid size={{ xs: 12, md: 5 }}>
          <Paper sx={{ p: 1 }}>
            <Box display="flex" justifyContent="space-between" mb={1}>
              <Typography variant="body2">
                {assignedPersonCount} Teilnehmer • {assignedVereinCount} Vereine
              </Typography>
              <Button size="small" onClick={resetRightFilter}>
                Reset
              </Button>
            </Box>

            <Grid container spacing={1} sx={{ mb: 1 }}>
              <Grid size={8}>
                <TextField
                  size="small"
                  label="Suche (Name / Vorname)"
                  value={searchR}
                  onChange={(e) => {
                    setSearchR(e.target.value);
                    setPageR(0); // ⭐ wichtig
                  }}
                  fullWidth
                />
              </Grid>
              <Grid size={4}>
                <TextField
                  size="small"
                  label="Verein"
                  value={fVereinR}
                  onChange={(e) => setFVereinR(e.target.value)}
                  fullWidth
                />
              </Grid>
            </Grid>

            <GenericTable<TeilnehmerList>
              rows={assigned}
              columns={[
                {
                  field: "name",
                  headerName: "Name",
                  flex: 1,
                  valueGetter: (_v, row) => row.person.name,
                },
                {
                  field: "vorname",
                  headerName: "Vorname",
                  flex: 1,
                  valueGetter: (_v, row) => row.person.vorname,
                },
                {
                  field: "verein",
                  headerName: "Verein",
                  flex: 1,
                  valueGetter: (_v, row) => row.person.hauptvereinAbk,
                },
                {
                  field: "rolle",
                  headerName: "Rolle",
                  flex: 0.6,
                },
              ]}
              // 🔥 HIER HIN!
              onCellClick={(params) => {
                if (params.field !== "rolle") return;

                const value = params.row.rolle as "L" | "M" | null;
                const personId = params.row.personId;

                console.log("CELL CLICK", params);

                if (value !== "L") {
                  handleRoleChange(value ?? null, personId);
                }
              }}
              checkboxSelection
              onSelectionChange={setSelAssigned}
              paginationMode="server"
              rowCount={totalAssigned}
              page={pageR}
              pageSize={sizeR}
              onPageChange={setPageR}
              onPageSizeChange={(s) => {
                setSizeR(s);
                setPageR(0);
              }}
            />
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );
}
