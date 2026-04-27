// components/verwaltung/foerdersatz/FoerdersatzTable.tsx

import { Box, Button, IconButton, Stack, Typography } from "@mui/material";
import { useState } from "react";

import { DataGrid, GridColDef } from "@mui/x-data-grid";

import FoerdersatzDialog from "@/components/verwaltung/foerdersatz/FoerdersatzDialog";

import AddIcon from "@mui/icons-material/Add";
import EditIcon from "@mui/icons-material/Edit";
import DeleteIcon from "@mui/icons-material/Delete";
import { useFoerdersaetze } from "@/hooks/foerdersatz/useFoerdersaetze";

import Money from "@/components/common/Money";

const FoerdersatzTable = () => {

  const { data = [], isLoading } = useFoerdersaetze();

  const [dialogOpen, setDialogOpen] = useState(false);

 const handleCreate = () => {
   setDialogOpen(true);
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
      field: "foerdersatz",
      headerName: "Fördersatz",
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
      field: "foerderdeckel",
      headerName: "Deckel",
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
      <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mb: 1 }}>
        <Typography variant="h5">Fördersätze</Typography>

        <Button variant="contained" startIcon={<AddIcon />} onClick={handleCreate}>
          Neuer Fördersatz
        </Button>
      </Stack>

      <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>
        Gilt für FM und JEM.
      </Typography>

      <DataGrid
        autoHeight
        rows={data}
        loading={isLoading}
        columns={columns}
        disableRowSelectionOnClick
        pageSizeOptions={[10, 25, 50, 100]}
      />
      <FoerdersatzDialog
        open={dialogOpen}
        onClose={() => setDialogOpen(false)}
        onSave={(dto) => {
          console.log(dto);
          setDialogOpen(false);
        }}
      />
    </Box>
  );
};

export default FoerdersatzTable;
