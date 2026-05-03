import { Box, Card, CardContent, Chip, Divider, Grid, Stack, Typography } from "@mui/material";

import { DataGrid, GridColDef } from "@mui/x-data-grid";

import Money from "@/components/common/Money";

type Props = {
  veranstaltungId: number;
};

type BeitragRow = {
  id: number;
  name: string;
  rolle: string;

  soll: number;
  bezahlt: number;
  status: "OFFEN" | "TEILWEISE" | "BEZAHLT";
  bezahltAm?: string;
};

const rows: BeitragRow[] = [
  {
    id: 1,
    name: "Hannah Rzehak",
    rolle: "Leitung",
  
    soll: 0,
    bezahlt: 0,
    status: "BEZAHLT",
  },
  {
    id: 2,
    name: "Pascal Rzehak",
    rolle: "Teilnehmer",
    
    soll: 400,
    bezahlt: 400,
    status: "BEZAHLT",
    bezahltAm: "12.03.2026",
  },
  {
    id: 3,
    name: "Ida Rzehak",
    rolle: "Teilnehmer",
   
    soll: 100,
    bezahlt: 100,
    status: "BEZAHLT",
    bezahltAm: "12.03.2026",
  },
  {
    id: 4,
    name: "Emma Sieren",
    rolle: "Teilnehmer",
    soll: 200,
    bezahlt: 0,
    status: "OFFEN",
  },
  {
    id: 5,
    name: "Andreas Molitor",
    rolle: "Teilnehmer",

    soll: 400,
    bezahlt: 200,
    status: "TEILWEISE",
  },
];

const BeitraegePage = ({ veranstaltungId }: Props) => {
    void veranstaltungId;
  /* =========================================================
     KPI
     ========================================================= */

  const gesamtSoll = rows.reduce((a, b) => a + b.soll, 0);

  const gesamtBezahlt = rows.reduce((a, b) => a + b.bezahlt, 0);

  const offen = gesamtSoll - gesamtBezahlt;

  const offeneTeilnehmer = rows.filter((r) => r.status !== "BEZAHLT").length;

  /* =========================================================
     GRID
     ========================================================= */

  const columns: GridColDef[] = [
    {
      field: "name",
      headerName: "Teilnehmer",
      flex: 1.4,
    },
    {
      field: "rolle",
      headerName: "Rolle",
      width: 120,
    },
    
    {
      field: "soll",
      headerName: "Soll",
      width: 140,
      align: "right",
      headerAlign: "right",

      renderCell: (params) => <Money value={params.value} />,
    },
    {
      field: "bezahlt",
      headerName: "Bezahlt",
      width: 140,
      align: "right",
      headerAlign: "right",

      renderCell: (params) => <Money value={params.value} />,
    },
    {
      field: "status",
      headerName: "Status",
      width: 150,

      renderCell: (params) => {
        if (params.value === "BEZAHLT") {
          return <Chip label="Bezahlt" color="success" size="small" />;
        }

        if (params.value === "TEILWEISE") {
          return <Chip label="Teilweise" color="warning" size="small" />;
        }

        return <Chip label="Offen" color="error" size="small" />;
      },
    },
    {
      field: "bezahltAm",
      headerName: "Bezahlt am",
      width: 140,
    },
  ];

  /* ========================================================= */

  return (
    <Box sx={{ mt: 2 }}>
      {/* =====================================================
          KPI
          ===================================================== */}

      <Grid container spacing={3}>
        <Grid size={{ xs: 12, md: 3 }}>
          <Card sx={{ borderLeft: 6, borderColor: "primary.main" }}>
            <CardContent>
              <Typography variant="subtitle2">Beiträge Soll</Typography>

              <Money value={gesamtSoll} variant="h5" colorize />
            </CardContent>
          </Card>
        </Grid>

        <Grid size={{ xs: 12, md: 3 }}>
          <Card sx={{ borderLeft: 6, borderColor: "success.main" }}>
            <CardContent>
              <Typography variant="subtitle2">Bereits bezahlt</Typography>

              <Money value={gesamtBezahlt} variant="h5" colorize />
            </CardContent>
          </Card>
        </Grid>

        <Grid size={{ xs: 12, md: 3 }}>
          <Card sx={{ borderLeft: 6, borderColor: "error.main" }}>
            <CardContent>
              <Typography variant="subtitle2">Offen</Typography>

              <Money value={offen} variant="h5" colorize />
            </CardContent>
          </Card>
        </Grid>

        <Grid size={{ xs: 12, md: 3 }}>
          <Card sx={{ borderLeft: 6, borderColor: "warning.main" }}>
            <CardContent>
              <Typography variant="subtitle2">Offene Teilnehmer</Typography>

              <Typography variant="h4">{offeneTeilnehmer}</Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* =====================================================
          TABELLE
          ===================================================== */}

      <Card sx={{ mt: 3 }}>
        <CardContent>
          <Stack direction="row" justifyContent="space-between" alignItems="center">
            <Typography variant="h6">Teilnehmerbeiträge</Typography>

            <Typography variant="body2" color="text.secondary">
              {rows.length} Teilnehmer
            </Typography>
          </Stack>

          <Divider sx={{ my: 2 }} />

          <Box sx={{ height: 650 }}>
            <DataGrid
              rows={rows}
              columns={columns}
              disableRowSelectionOnClick
              pageSizeOptions={[25, 50, 100]}
            />
          </Box>
        </CardContent>
      </Card>
    </Box>
  );
};

export default BeitraegePage;
