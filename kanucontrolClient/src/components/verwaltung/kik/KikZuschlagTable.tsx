import { Box, Button, IconButton, Stack, Typography } from "@mui/material";
import { useState } from "react";

import { DataGrid, GridColDef } from "@mui/x-data-grid";

import AddIcon from "@mui/icons-material/Add";
import EditIcon from "@mui/icons-material/Edit";
import DeleteIcon from "@mui/icons-material/Delete";

import Money from "@/components/common/Money";

import KikZuschlagDialog from "@/components/verwaltung/kik/KikDialog";

import {
  createKikZuschlag,
  updateKikZuschlag,
  deleteKikZuschlag,
} from "@/api/services/kikZuschlagApi";

import { useKikZuschlag } from "@/hooks/kik/useKikZuschlag";

import { KikDTO } from "@/api/types/Kik";

const KikZuschlagTable = () => {
  const { data = [], isLoading, refetch } = useKikZuschlag();

  const [dialogOpen, setDialogOpen] = useState(false);

  const [editing, setEditing] = useState<KikDTO | null>(null);

  /* =========================================================
     ACTIONS
     ========================================================= */

  const handleCreate = () => {
    setEditing(null);
    setDialogOpen(true);
  };

  const handleEdit = (id: number) => {
    const row = data.find((x) => x.id === id);

    if (!row) return;

    setEditing(row);
    setDialogOpen(true);
  };

  const handleDelete = async (id: number) => {
    if (!window.confirm("KiK-Zuschlag löschen?")) return;

    await deleteKikZuschlag(id);

    await refetch();
  };

  /* =========================================================
     COLUMNS
     ========================================================= */

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

  /* =========================================================
     UI
     ========================================================= */

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
        rows={data}
        loading={isLoading}
        columns={columns}
        disableRowSelectionOnClick
        pageSizeOptions={[10, 25, 50, 100]}
      />

      <KikZuschlagDialog
        open={dialogOpen}
        initialData={editing}
        onClose={() => {
          setDialogOpen(false);
          setEditing(null);
        }}
        onSave={async (dto) => {
          if (editing) {
            await updateKikZuschlag(editing.id, dto);
          } else {
            await createKikZuschlag(dto);
          }

          await refetch();

          setDialogOpen(false);
          setEditing(null);
        }}
      />
    </Box>
  );
};

export default KikZuschlagTable;
