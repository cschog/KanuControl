import { useEffect, useState, useCallback } from "react";
import { getApiErrorMessage } from "@/api/utils/apiError";
import { ErrorDialog } from "@/components/common/ErrorDialog";
import { Box, Button, Chip, MenuItem, Typography, Paper, Grid, TextField } from "@mui/material";
import { useTheme } from "@mui/material/styles";
import { useMediaQuery } from "@mui/material";
import ArrowForwardIcon from "@mui/icons-material/ArrowForward";
import ArrowBackIcon from "@mui/icons-material/ArrowBack";
import { useNavigate } from "react-router-dom";
import { SortingState } from "@tanstack/react-table";
import { useAppContext } from "@/context/AppContext";
import { useDebounce } from "@/components/common/reference/hooks";
import { BottomActionBar } from "@/components/layout/BottomActionBar";
import { GenericTableTanstack } from "@/components/common/GenericTableTanstack";
import { teilnehmerAvailableColumns } from "@/components/teilnehmer/teilnehmerAvailableColumns";
import { teilnehmerAssignedColumns } from "@/components/teilnehmer/teilnehmerAssignedColumns";
import {
  getAvailablePersons,
  getTeilnehmer,
  addTeilnehmerBulk,
  removeTeilnehmerBulk,
  updateTeilnehmerRolle,
} from "@/api/services/teilnehmerApi";
import { PersonList } from "@/api/types/person/PersonList";
import { TeilnehmerList } from "@/api/types/TeilnehmerList";

export default function TeilnehmerScreen() {
  const { active } = useAppContext();
  const navigate = useNavigate();
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down("md"));

  /* =========================================================
     DATA
     ========================================================= */

  const [available, setAvailable] = useState<PersonList[]>([]);
  const [assigned, setAssigned] = useState<TeilnehmerList[]>([]);
  const [selAvailable, setSelAvailable] = useState<PersonList[]>([]);
  const [selAssigned, setSelAssigned] = useState<TeilnehmerList[]>([]);
  const [mobileMode, setMobileMode] = useState<"available" | "assigned">("available");
  const [errorOpen, setErrorOpen] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  /* =========================================================
     SORTING
     ========================================================= */

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

  /* =========================================================
     FILTER
     ========================================================= */

  const [searchL, setSearchL] = useState("");
  const [searchR, setSearchR] = useState("");
  const [fVereinL, setFVereinL] = useState("");
  const [fVereinR, setFVereinR] = useState("");
  const debounceSearchL = useDebounce(searchL, 300);
  const debounceSearchR = useDebounce(searchR, 300);
  const debounceVereinL = useDebounce(fVereinL, 300);
  const debounceVereinR = useDebounce(fVereinR, 300);

  /* =========================================================
     COUNTS
     ========================================================= */

  const [totalAvailable, setTotalAvailable] = useState(0);
  const [totalAssigned, setTotalAssigned] = useState(0);

  /* =========================================================
     RESET
     ========================================================= */

  const [resetLeftSelection, setResetLeftSelection] = useState(0);
  const [resetRightSelection, setResetRightSelection] = useState(0);

  /* =========================================================
     VEREINE
     ========================================================= */

  const availableVereine = Array.from(
    new Set(available.map((p) => p.hauptvereinAbk).filter((v): v is string => !!v)),
  );

  const assignedVereine = Array.from(
    new Set(assigned.map((t) => t.person?.hauptvereinAbk).filter((v): v is string => !!v)),
  );

  /* =========================================================
     LOAD
     ========================================================= */

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

  /* =========================================================
     DISTINCT COUNTS
     ========================================================= */

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
  const assignedPersonCount = totalAssigned;
  const availableVereinCount = countDistinct(available, (p) => p.hauptvereinAbk);
  const assignedVereinCount = countDistinct(assigned, (t) => t.person?.hauptvereinAbk);

  /* =========================================================
     ACTIONS
     ========================================================= */

  const handleAdd = async () => {
    if (!active?.id || selAvailable.length === 0) return;

    await addTeilnehmerBulk(
      active.id,
      selAvailable.map((p) => p.id),
    );

    setSelAvailable([]);
    setResetLeftSelection((v) => v + 1);

    await load();
  };

  const handleRemove = async () => {
    if (!active?.id || selAssigned.length === 0) return;

    try {
      await removeTeilnehmerBulk(
        active.id,
        selAssigned.map((p) => p.personId),
      );

      setSelAssigned([]);
      setResetRightSelection((v) => v + 1);

      await load();
    } catch (error) {
      setErrorMessage(getApiErrorMessage(error));
      setErrorOpen(true);
    }
  };

  const handleMobileAction = async () => {
    if (mobileMode === "available") {
      await handleAdd();
    } else {
      await handleRemove();
    }
  };

  /* =========================================================
     ROLE CHANGE
     ========================================================= */

  const handleRoleChange = async (current: "L" | "M" | null, personId: number) => {
    if (!active?.id) return;

    if (current === "L") return;

    const newRole = current === "M" ? null : "M";

    setAssigned((prev) =>
      prev.map((t) =>
        t.personId === personId
          ? {
            ...t,
            rolle: newRole,
          }
          : t,
      ),
    );

    try {
      await updateTeilnehmerRolle(active.id, personId, newRole);
    } catch {
      setAssigned((prev) =>
        prev.map((t) =>
          t.personId === personId
            ? {
              ...t,
              rolle: current,
            }
            : t,
        ),
      );
    }
  };

  const assignedColumns = teilnehmerAssignedColumns({
    onRoleClick: handleRoleChange,
  });

  /* =========================================================
     RESET
     ========================================================= */

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

  /* =========================================================
     NO ACTIVE
     ========================================================= */

  if (!active) {
    return <Typography align="center">Keine aktive Veranstaltung</Typography>;
  }

  /* =========================================================
     MOBILE
     ========================================================= */

  if (isMobile) {
    return (
      <Box>
        <Typography
          variant="h5"
          sx={{
            mb: 2,
            fontWeight: 600,
          }}
        >
          Teilnehmer — {active.name}
        </Typography>

        <Box
          sx={{
            display: "flex",
            gap: 1,
            alignItems: "stretch",
          }}
        >
          {/* LISTE */}

          <Box
            sx={{
              flex: 1,
              minWidth: 0,
            }}
          >
            <Paper sx={{ p: 1 }}>
              <Typography
                sx={{
                  mb: 1,
                  fontWeight: 600,
                }}
              >
                {mobileMode === "available"
                  ? `${availablePersonCount} Personen`
                  : `${assignedPersonCount} Teilnehmer`}
              </Typography>

              {/* FILTER */}

              <Box sx={{ mb: 1 }}>
                <TextField
                  size="small"
                  fullWidth
                  label="Suche"
                  value={mobileMode === "available" ? searchL : searchR}
                  onChange={(e) => {
                    if (mobileMode === "available") {
                      setSearchL(e.target.value);
                    } else {
                      setSearchR(e.target.value);
                    }
                  }}
                />
              </Box>

              <Box sx={{ mb: 1 }}>
                <TextField
                  select
                  size="small"
                  fullWidth
                  label="Verein"
                  value={mobileMode === "available" ? fVereinL : fVereinR}
                  onChange={(e) => {
                    if (mobileMode === "available") {
                      setFVereinL(e.target.value);
                    } else {
                      setFVereinR(e.target.value);
                    }
                  }}
                >
                  <MenuItem value="">Alle</MenuItem>

                  {(mobileMode === "available" ? availableVereine : assignedVereine).map((v) => (
                    <MenuItem key={v} value={v}>
                      {v}
                    </MenuItem>
                  ))}
                </TextField>
              </Box>

              <Box display="flex" justifyContent="flex-end" mb={1}>
                <Button
                  size="small"
                  onClick={mobileMode === "available" ? resetLeftFilter : resetRightFilter}
                >
                  Reset
                </Button>
              </Box>

              {/* AVAILABLE */}

              {mobileMode === "available" ? (
                <GenericTableTanstack<PersonList>
                  enableCheckboxSelection
                  data={available}
                  columns={teilnehmerAvailableColumns}
                  resetSelectionTrigger={resetLeftSelection}
                  loading={false}
                  height={window.innerHeight - 390}
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
                />
              ) : (
                /* ASSIGNED */
                <GenericTableTanstack<TeilnehmerList>
                  enableCheckboxSelection
                  data={assigned}
                  columns={assignedColumns}
                  resetSelectionTrigger={resetRightSelection}
                  loading={false}
                  height={window.innerHeight - 390}
                  mobileRenderRow={(row) => (
                    <Box>
                      <Typography fontWeight={600}>
                        {row.person?.name}, {row.person?.vorname}
                      </Typography>

                      <Box
                        sx={{
                          mt: 0.3,
                          display: "flex",
                          alignItems: "center",
                          gap: 1,
                          flexWrap: "wrap",
                        }}
                      >
                        <Typography variant="body2" color="text.secondary">
                          {row.person?.hauptvereinAbk ?? "-"}
                        </Typography>

                        <Typography
                          variant="body2"
                          color="text.secondary"
                          sx={{
                            whiteSpace: "nowrap",
                          }}
                        >
                          {row.alterBeiBeginn ?? "-"} J.
                        </Typography>

                        <Chip
                          size="small"
                          label={row.rolle === "L" ? "L" : row.rolle === "M" ? "M" : "+"}
                          color={row.rolle === "L" ? "secondary" : "default"}
                          variant={row.rolle === "L" ? "filled" : "outlined"}
                          onClick={() => handleRoleChange(row.rolle ?? null, row.personId)}
                          sx={{
                            fontWeight: 700,

                            minWidth: 34,
                            height: 24,
                            cursor: row.rolle === "L" ? "default" : "pointer",

                            opacity: row.rolle === "L" ? 0.9 : 1,
                          }}
                        />
                      </Box>
                    </Box>
                  )}
                  onRowSelectionChange={setSelAssigned}
                />
              )}
            </Paper>
          </Box>

          {/* ACTION RAIL */}

          <Box
            sx={{
              width: 72,
              display: "flex",
              flexDirection: "column",
              gap: 2,
            }}
          >
            <Button
              variant="contained"
              color={mobileMode === "available" ? "primary" : "error"}
              onClick={handleMobileAction}
              disabled={mobileMode === "available" ? !selAvailable.length : !selAssigned.length}
              sx={{
                minWidth: 0,

                width: 72,
                height: 88,

                borderRadius: 3,

                boxShadow: 3,

                opacity:
                  mobileMode === "available"
                    ? selAvailable.length
                      ? 1
                      : 0.5
                    : selAssigned.length
                      ? 1
                      : 0.5,
              }}
            >
              {mobileMode === "available" ? (
                <ArrowForwardIcon
                  sx={{
                    fontSize: 42,
                  }}
                />
              ) : (
                <ArrowBackIcon
                  sx={{
                    fontSize: 42,
                  }}
                />
              )}
            </Button>

            <Button
              variant="outlined"
              onClick={() => setMobileMode((m) => (m === "available" ? "assigned" : "available"))}
              sx={{
                writingMode: "vertical-rl",
                textOrientation: "mixed",

                minWidth: 0,

                py: 2,

                fontWeight: 700,
              }}
            >
              SWITCH
            </Button>
          </Box>
        </Box>

        <BottomActionBar
          left={[
            {
              label: "Zurück",
              onClick: () => navigate("/startmenue"),
              variant: "outlined",
            },
          ]}
        />

        <ErrorDialog
          open={errorOpen}
          title="Teilnehmer kann nicht entfernt werden"
          message={errorMessage}
          onClose={() => setErrorOpen(false)}
        />
      </Box>
    );
  }

  /* =========================================================
     DESKTOP
     ========================================================= */

  return (
    <Box>
      <Typography
        variant="h5"
        sx={{
          mb: 2,
          fontWeight: 600,
        }}
      >
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
                  onChange={(e) => setSearchL(e.target.value)}
                  fullWidth
                />
              </Grid>

              <Grid size={4}>
                <TextField
                  select
                  size="small"
                  label="Verein"
                  value={fVereinL}
                  onChange={(e) => setFVereinL(e.target.value)}
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
              loading={false}
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
          <Button
            variant="contained"
            color="primary"
            disabled={!selAvailable.length}
            onClick={handleAdd}
            sx={{
              minWidth: 0,

              width: 56,
              height: 72,

              alignSelf: "center",

              borderRadius: 3,

              boxShadow: 2,

              opacity: selAvailable.length ? 1 : 0.45,
            }}
          >
            <ArrowForwardIcon
              sx={{
                fontSize: 42,
              }}
            />
          </Button>

          <Button
            variant="contained"
            color="error"
            disabled={!selAssigned.length}
            onClick={handleRemove}
            sx={{
              minWidth: 0,

              width: 56,
              height: 72,

              alignSelf: "center",

              borderRadius: 3,

              boxShadow: 2,

              opacity: selAssigned.length ? 1 : 0.45,
            }}
          >
            <ArrowBackIcon
              sx={{
                fontSize: 42,
              }}
            />
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
                  onChange={(e) => setSearchR(e.target.value)}
                  fullWidth
                />
              </Grid>

              <Grid size={4}>
                <TextField
                  select
                  size="small"
                  label="Verein"
                  value={fVereinR}
                  onChange={(e) => setFVereinR(e.target.value)}
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
              columns={assignedColumns}
              resetSelectionTrigger={resetRightSelection}
              loading={false}
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
      <ErrorDialog
        open={errorOpen}
        title="Teilnehmer kann nicht entfernt werden"
        message={errorMessage}
        onClose={() => setErrorOpen(false)}
      />
    </Box>
  );
}
