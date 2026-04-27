// components/verwaltung/kik/KikZuschlagTable.tsx

import { Box, Button, IconButton, Stack, Typography } from "@mui/material";

import { DataGrid, GridColDef } from "@mui/x-data-grid";

import AddIcon from "@mui/icons-material/Add";
import EditIcon from "@mui/icons-material/Edit";
import DeleteIcon from "@mui/icons-material/Delete";

import Money from "@/components/common/Money";

const rows = [
  {
    id: 1,
    gueltigVon: "01.01.2026",
    gueltigBis: "",
    kikZuschlag: 3,
    beschluss: "KiK Förderung 2026",
  },
];

const KikZuschlagTable = () => {
  const handleCreate = () => {
    console.log("create");
  };

  const handleEdit = (id: number) => {
    console.log("edit", id);
  };

  const handleDelete = (id: number) => {
    console.log("delete", id);
  };

  const columns: GridColDef[] = [
    {
      field: "gueltigVon",
      headerName: "Gültig von",
      width: 120,
    },

    {
      field: "gueltigBis",
      headerName: "Gültig bis",
      width: 120,
      renderCell: (params) => params.value || "unbegrenzt",
    },

    {
      field: "kikZuschlag",
      headerName: "KiK-Zuschlag",
      width: 120,
      align: "right",
      headerAlign: "right",

      renderCell: (params) => (
        <Box
          sx={{
            height: "100%",
            display: "flex",
            alignItems: "center",
            justifyContent: "flex-end",
            width: "100%",
          }}
        >
          <Money value={params.value} />
        </Box>
      ),
    },

    {
      field: "beschluss",
      headerName: "Beschluss",
      flex: 1,
      minWidth: 150,
    },

    {
      field: "actions",
      headerName: "",
      sortable: false,
      filterable: false,
      width: 90,

      renderCell: (params) => (
        <Stack direction="row" spacing={0.5}>
          <IconButton size="small" onClick={() => handleEdit(params.row.id)}>
            <EditIcon fontSize="small" />
          </IconButton>

          <IconButton size="small" color="error" onClick={() => handleDelete(params.row.id)}>
            <DeleteIcon fontSize="small" />
          </IconButton>
        </Stack>
      ),
    },
  ];

  return (
    <Box>
      <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 2 }}>
        <Typography variant="h5">KiK-Zuschlag</Typography>

        <Button variant="contained" startIcon={<AddIcon />} onClick={handleCreate}>
          Neuer KiK-Zuschlag
        </Button>
      </Stack>

      <DataGrid
        autoHeight
        rows={rows}
        columns={columns}
        disableRowSelectionOnClick
        pageSizeOptions={[10, 25, 50, 100]}
      />
    </Box>
  );
};

export default KikZuschlagTable;
