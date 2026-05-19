import { useEffect, useState, useCallback } from "react";
import { Box, Button, MenuItem, Typography, Paper, Grid, TextField } from "@mui/material";
import { useAppContext } from "@/context/AppContext";
import { updateTeilnehmerRolle } from "@/api/services/teilnehmerApi";
import { useDebounce } from "@/components/common/reference/hooks";
import { BottomActionBar } from "@/components/layout/BottomActionBar";
import { useNavigate } from "react-router-dom";
import { SortingState } from "@tanstack/react-table";

import {
  getAvailablePersons,
  getTeilnehmer,
  addTeilnehmerBulk,
  removeTeilnehmerBulk,
} from "@/api/services/teilnehmerApi";

import { GenericTableTanstack } from "@/components/common/GenericTableTanstack";
import { teilnehmerAvailableColumns } from "@/components/teilnehmer/teilnehmerAvailableColumns";
import { teilnehmerAssignedColumns } from "@/components/teilnehmer/teilnehmerAssignedColumns";
import { PersonList } from "@/api/types/PersonList";
import { TeilnehmerList } from "@/api/types/TeilnehmerList";

export default function TeilnehmerScreen() {
  const { active } = useAppContext();

  const navigate = useNavigate();

  /* ================= DATA ================= */

  const [available, setAvailable] = useState<PersonList[]>([]);
  const [assigned, setAssigned] = useState<TeilnehmerList[]>([]);

  const [selAvailable, setSelAvailable] = useState<PersonList[]>([]);
  const [selAssigned, setSelAssigned] = useState<TeilnehmerList[]>([]);

  const [sortingL, setSortingL] = useState<SortingState>([
    {
      id: "fullname",
      desc: false,
    },
  ]);

  const [sortingR, setSortingR] = useState<SortingState>([
    {
      id: "fullname",
      desc: false,
    },
  ]);

  /* ================= FILTER ================= */

  const [searchL, setSearchL] = useState("");
  const [fVereinL, setFVereinL] = useState("");

  const [searchR, setSearchR] = useState("");
  const [fVereinR, setFVereinR] = useState("");

  const debounceSearchL = useDebounce(searchL, 300);
  const debounceSearchR = useDebounce(searchR, 300);

  const debounceVereinL = useDebounce(fVereinL, 300);
  const debounceVereinR = useDebounce(fVereinR, 300);

  const availableVereine = Array.from(
    new Set(available.map((p) => p.hauptvereinAbk).filter((v): v is string => !!v)),
  );

  const assignedVereine = Array.from(
    new Set(assigned.map((t) => t.person?.hauptvereinAbk).filter((v): v is string => !!v)),
  );

  /* ================= PAGING ================= */

  const [totalAvailable, setTotalAvailable] = useState(0);
  const [totalAssigned, setTotalAssigned] = useState(0);

  /* ========= Reset ========= */

  const [resetLeftSelection, setResetLeftSelection] = useState(0);
  const [resetRightSelection, setResetRightSelection] = useState(0);

  /* ================= LOAD ================= */

  const mapSortField = (field: string) => {
    switch (field) {
      case "fullname":
        return "name";

      default:
        return field;
    }
  };

  const load = useCallback(async () => {
    if (!active?.id) return;

   const a = await getAvailablePersons(
     active.id,
     0,
     1000,
     debounceSearchL || undefined,
     debounceVereinL || undefined,
     mapSortField(sortingL[0]?.id ?? "fullname"),
     sortingL[0]?.desc ? "desc" : "asc",
   );

    setAvailable(a?.content ?? []);
    setTotalAvailable(a?.totalElements ?? 0);

   const b = await getTeilnehmer(
     active.id,
     0,
     1000,
     debounceSearchR || undefined,
     debounceVereinR || undefined,
     mapSortField(sortingR[0]?.id ?? "fullname"),
     sortingR[0]?.desc ? "desc" : "asc",
   );

    setAssigned(b?.content ?? []);
    setTotalAssigned(b?.totalElements ?? 0);
  }, [
    active?.id,
    debounceSearchL,
    debounceSearchR,
    debounceVereinL,
    debounceVereinR,
    sortingL,
    sortingR,
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

  const assignedColumns = teilnehmerAssignedColumns({
    onRoleClick: handleRoleChange,
  });

  /* ================= RESET ================= */

  const resetLeftFilter = () => {
    setSearchL("");
    setFVereinL("");

    setSelAvailable([]);

    setResetLeftSelection((v) => v + 1);
  };

  const resetRightFilter = () => {
    setSearchR("");
    setFVereinR("");

    setSelAssigned([]);

    setResetRightSelection((v) => v + 1);
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
              <Typography
                variant="subtitle2"
                sx={{
                  fontWeight: 600,
                  fontSize: {
                    xs: "0.95rem",
                    md: "1.2rem",
                  },
                }}
              >
                {availablePersonCount} Personen • {availableVereinCount} Verein(e)
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
                  }}
                  fullWidth
                />
              </Grid>
              <Grid size={4}>
                <TextField
                  select
                  size="small"
                  label="Verein"
                  value={fVereinL}
                  onChange={(e) => {
                    setFVereinL(e.target.value);
                  }}
                  fullWidth
                >
                  <MenuItem value="">Alle</MenuItem>

                  {availableVereine.map((v) => (
                    <MenuItem key={v} value={v}>
                      {v}
                    </MenuItem>
                  ))}
                </TextField>
              </Grid>
            </Grid>

            <GenericTableTanstack<PersonList>
              enableCheckboxSelection
              data={available}
              columns={teilnehmerAvailableColumns}
              resetSelectionTrigger={resetLeftSelection}
              selectedRowId={undefined}
              loading={false}
              mobileRenderRow={(row) => (
                <Box>
                  <Typography fontWeight={600}>
                    {row.name}, {row.vorname}
                  </Typography>

                  <Typography variant="body2" color="text.secondary">
                    {row.hauptvereinAbk ?? "-"}
                    {" • "}
                    {row.alter ?? "-"} Jahre
                  </Typography>
                </Box>
              )}
              onRowSelectionChange={setSelAvailable}
              sorting={sortingL}
              onSortingChange={setSortingL}
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
              <Typography
                variant="subtitle2"
                sx={{
                  fontWeight: 600,
                  fontSize: {
                    xs: "0.95rem",
                    md: "1.2rem",
                  },
                }}
              >
                {assignedPersonCount} Teilnehmer • {assignedVereinCount} Verein(e)
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
                  }}
                  fullWidth
                />
              </Grid>
              <Grid size={4}>
                <TextField
                  select
                  size="small"
                  label="Verein"
                  value={fVereinR}
                  onChange={(e) => {
                    setFVereinR(e.target.value);
                  }}
                  fullWidth
                >
                  <MenuItem value="">Alle</MenuItem>

                  {assignedVereine.map((v) => (
                    <MenuItem key={v} value={v}>
                      {v}
                    </MenuItem>
                  ))}
                </TextField>
              </Grid>
            </Grid>

            <GenericTableTanstack<TeilnehmerList>
              enableCheckboxSelection
              data={assigned}
              resetSelectionTrigger={resetRightSelection}
              columns={assignedColumns}
              selectedRowId={undefined}
              loading={false}
              mobileRenderRow={(row) => (
                <Box>
                  <Typography fontWeight={600}>
                    {row.person?.name}, {row.person?.vorname}
                  </Typography>

                  <Typography variant="body2" color="text.secondary">
                    {row.person?.hauptvereinAbk ?? "-"}
                    {" • "}
                    {row.rolle === "L"
                      ? "Leitung"
                      : row.rolle === "M"
                      ? "Mitarbeiter"
                      : "Teilnehmer"}
                  </Typography>
                </Box>
              )}
              onRowSelectionChange={setSelAssigned}
              sorting={sortingR}
              onSortingChange={setSortingR}
            />
          </Paper>
        </Grid>
      </Grid>
      <BottomActionBar
        left={[
          {
            label: "Zurück",
            onClick: () => navigate("/startmenue"),
            variant: "outlined",
          },
        ]}
      />
    </Box>
  );
}
