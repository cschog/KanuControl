// components/verwaltung/foerdersatz/FoerdersatzTable.tsx

import { Box, Button, IconButton, Stack, Typography } from "@mui/material";
import { useState, useEffect } from "react";

import { DataGrid, GridColDef } from "@mui/x-data-grid";
import { getFoerderdeckel } from "@/api/services/configApi";
import { FoerdersatzDTO } from "@/api/types/Foerdersatz";

import FoerdersatzDialog from "@/components/verwaltung/foerdersatz/FoerdersatzDialog";
import {
  createFoerdersatz,
  deleteFoerdersatz,
  updateFoerdersatz,
} from "@/api/services/foerdersatzApi";

import AddIcon from "@mui/icons-material/Add";
import EditIcon from "@mui/icons-material/Edit";
import DeleteIcon from "@mui/icons-material/Delete";
import { useFoerdersaetze } from "@/hooks/foerdersatz/useFoerdersaetze";

import Money from "@/components/common/Money";

const FoerdersatzTable = () => {
  const { data = [], isLoading, refetch } = useFoerdersaetze();

  const [foerderdeckel, setFoerderdeckel] = useState<number | null>(null);

  const [editing, setEditing] = useState<FoerdersatzDTO | null>(null);

  const [dialogOpen, setDialogOpen] = useState(false);

  const handleCreate = () => {
    setDialogOpen(true);
  };

  const handleEdit = (id: number) => {
    const row = data.find((x) => x.id === id);

    if (!row) return;

    setEditing(row);
    setDialogOpen(true);
  };

  const handleDelete = async (id: number) => {
    if (!window.confirm("Fördersatz löschen?")) return;

    await deleteFoerdersatz(id);

    await refetch();
  };

  useEffect(() => {
    getFoerderdeckel().then(setFoerderdeckel);
  }, []);

  const columns: GridColDef[] = [
    {
      field: "typ",
      headerName: "Typ",
      width: 90,
    },

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

      renderCell: () => (
        <Box
          sx={{
            height: "100%",
            display: "flex",
            alignItems: "center",
            justifyContent: "flex-end",
            width: "100%",
          }}
        >
          {foerderdeckel != null && <Money value={foerderdeckel} />}
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
        initialData={editing}
        onSave={async (dto) => {
          if (editing) {
            await updateFoerdersatz(editing.id, dto);
          } else {
            await createFoerdersatz(dto);
          }

          await refetch();

          setDialogOpen(false);
          setEditing(null);
        }}
        onClose={() => {
          setDialogOpen(false);
          setEditing(null);
        }}
      />
    </Box>
  );
};

export default FoerdersatzTable;
