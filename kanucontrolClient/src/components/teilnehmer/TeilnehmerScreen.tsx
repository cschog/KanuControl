import { useEffect, useState, useCallback } from "react";
import { Box, Button, Typography, Paper, Grid, TextField } from "@mui/material";
import { useAppContext } from "@/context/AppContext";

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

  const [available, setAvailable] = useState<PersonList[]>([]);
  const [assigned, setAssigned] = useState<TeilnehmerList[]>([]);

  const [selAvailable, setSelAvailable] = useState<PersonList[]>([]);
  const [selAssigned, setSelAssigned] = useState<TeilnehmerList[]>([]);

  /* ================= FILTER ================= */

  const [fNameL, setFNameL] = useState("");
  const [fVornameL, setFVornameL] = useState("");
  const [fVereinL, setFVereinL] = useState("");

  const [fNameR, setFNameR] = useState("");
  const [fVornameR, setFVornameR] = useState("");
  const [fVereinR, setFVereinR] = useState("");

  /* ================= LOAD ================= */

  const load = useCallback(async () => {
    if (!active?.id) return;

    const a = await getAvailablePersons(active.id, 0, 50, fNameL, fVornameL, fVereinL);

    const b = await getTeilnehmer(active.id, 0, 50, fNameR, fVornameR, fVereinR);

    setAvailable(a.content);
    setAssigned(b.content);
  }, [active?.id, fNameL, fVornameL, fVereinL, fNameR, fVornameR, fVereinR]);

  useEffect(() => {
    load();
  }, [load]);

  /* ================= ACTIONS ================= */

const handleAdd = async () => {
  console.log(
    "ADD IDs:",
    selAvailable.map((p) => p.id),
  ); // 🔍 Debug

  if (!selAvailable.length) return;

  await addTeilnehmerBulk(
    active!.id,
    selAvailable.map((p) => p.id),
  );

  setSelAvailable([]);
  await load();
};

  const handleRemove = async () => {
    await removeTeilnehmerBulk(
      active!.id,
      selAssigned.map((p) => p.personId),
    );
    setSelAssigned([]);
    await load();
  };

  if (!active) {
    return <Typography align="center">Keine aktive Veranstaltung</Typography>;
  }

  /* ================= UI ================= */

  return (
    <Box>
      <Typography variant="h5" sx={{ mb: 2, fontWeight: 600 }}>
        Teilnehmer — {active.name}
      </Typography>

      <Grid container spacing={2}>
        {/* ================= LEFT ================= */}
        <Grid size={{ xs: 12, md: 5 }}>
          <Paper sx={{ p: 1 }}>
            {/* FILTER */}
            <Grid container spacing={1} sx={{ mb: 1 }}>
              <Grid size={4}>
                <TextField
                  size="small"
                  label="Name"
                  value={fNameL}
                  onChange={(e) => setFNameL(e.target.value)}
                  fullWidth
                />
              </Grid>
              <Grid size={4}>
                <TextField
                  size="small"
                  label="Vorname"
                  value={fVornameL}
                  onChange={(e) => setFVornameL(e.target.value)}
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
              selectedRows={selAvailable}
              columns={[
                { field: "name", headerName: "Name", flex: 1 },
                { field: "vorname", headerName: "Vorname", flex: 1 },
                { field: "hauptvereinAbk", headerName: "Verein", flex: 1 },
              ]}
              checkboxSelection
              onSelectionChange={setSelAvailable}
            />
          </Paper>
        </Grid>

        {/* ================= CENTER ================= */}
        <Grid
          size={{ xs: 12, md: 2 }}
          display="flex"
          flexDirection="column"
          justifyContent="center"
          alignItems="center"
          gap={2}
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

        {/* ================= RIGHT ================= */}
        <Grid size={{ xs: 12, md: 5 }}>
          <Paper sx={{ p: 1 }}>
            {/* FILTER */}
            <Grid container spacing={1} sx={{ mb: 1 }}>
              <Grid size={4}>
                <TextField
                  size="small"
                  label="Name"
                  value={fNameR}
                  onChange={(e) => setFNameR(e.target.value)}
                  fullWidth
                />
              </Grid>
              <Grid size={4}>
                <TextField
                  size="small"
                  label="Vorname"
                  value={fVornameR}
                  onChange={(e) => setFVornameR(e.target.value)}
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
              selectedRows={selAssigned}
              columns={[
                {
                  field: "name",
                  headerName: "Name",
                  flex: 1,
                  valueGetter: (_v, row: TeilnehmerList) => row.person.name,
                },
                {
                  field: "vorname",
                  headerName: "Vorname",
                  flex: 1,
                  valueGetter: (_v, row: TeilnehmerList) => row.person.vorname,
                },
                {
                  field: "verein",
                  headerName: "Verein",
                  flex: 1,
                  valueGetter: (_v, row: TeilnehmerList) => row.person.hauptvereinAbk,
                },
                {
                  field: "rolle",
                  headerName: "Rolle",
                  flex: 1,
                },
              ]}
              checkboxSelection
              onSelectionChange={setSelAssigned}
            />
          </Paper>
        </Grid>
      </Grid>
    </Box>
  );
}
