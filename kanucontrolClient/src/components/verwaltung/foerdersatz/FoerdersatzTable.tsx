import { Box, Button, IconButton, Stack, Typography, Chip } from "@mui/material";

import { useState, useEffect, useCallback, useMemo } from "react";

import AddIcon from "@mui/icons-material/Add";
import EditIcon from "@mui/icons-material/Edit";
import DeleteIcon from "@mui/icons-material/Delete";

import { GenericTableTanstack } from "@/components/common/GenericTableTanstack";

import FoerdersatzDialog from "@/components/verwaltung/foerdersatz/FoerdersatzDialog";

import {
  createFoerdersatz,
  deleteFoerdersatz,
  updateFoerdersatz,
} from "@/api/services/foerdersatzApi";

import { getFoerderdeckel } from "@/api/services/configApi";

import { useFoerdersaetze } from "@/hooks/foerdersatz/useFoerdersaetze";

import { FoerdersatzDTO } from "@/api/types/Foerdersatz";

import Money from "@/components/common/Money";

import { ColumnDef } from "@tanstack/react-table";

/* =========================================================
   COMPONENT
   ========================================================= */

const FoerdersatzTable = () => {
  const { data = [], isLoading, refetch } = useFoerdersaetze();

  const [foerderdeckel, setFoerderdeckel] = useState<number | null>(null);

  const [editing, setEditing] = useState<FoerdersatzDTO | null>(null);

  const [dialogOpen, setDialogOpen] = useState(false);

  /* =========================================================
     LOAD
     ========================================================= */

  useEffect(() => {
    getFoerderdeckel().then(setFoerderdeckel);
  }, []);

  /* =========================================================
     ACTIONS
     ========================================================= */

  const handleCreate = () => {
    setEditing(null);

    setDialogOpen(true);
  };

  const handleEdit = (row: FoerdersatzDTO) => {
    setEditing(row);

    setDialogOpen(true);
  };

  const handleDelete = useCallback(
    async (id: number) => {
      if (!window.confirm("Fördersatz löschen?")) {
        return;
      }

      await deleteFoerdersatz(id);

      await refetch();
    },
    [refetch],
  );

  /* =========================================================
     COLUMNS
     ========================================================= */

  const columns = useMemo<ColumnDef<FoerdersatzDTO>[]>(
    () => [
      {
        accessorKey: "typ",

        header: "Typ",

        size: 90,

        cell: ({ row }) => (
          <Chip size="small" label={row.original.typ} color="primary" variant="outlined" />
        ),
      },

      {
        accessorKey: "gueltigVon",

        header: "Gültig von",

        size: 120,
      },

      {
        accessorKey: "gueltigBis",

        header: "Gültig bis",

        size: 120,

        cell: ({ row }) => row.original.gueltigBis || "unbegrenzt",
      },

      {
        accessorKey: "foerdersatz",

        header: "Fördersatz",

        size: 140,

        meta: {
          align: "right",
        },

        cell: ({ row }) => (
          <Box
            sx={{
              display: "flex",
              justifyContent: "flex-end",
              width: "100%",
            }}
          >
            <Money value={row.original.foerdersatz} />
          </Box>
        ),
      },

      {
        id: "deckel",

        header: "Deckel",

        size: 120,

        meta: {
          align: "right",
        },

        cell: () => (
          <Box
            sx={{
              display: "flex",
              justifyContent: "flex-end",
              width: "100%",
            }}
          >
            {foerderdeckel != null && <Money value={foerderdeckel} />}
          </Box>
        ),
      },

      {
        accessorKey: "beschluss",

        header: "Beschluss",

        size: 240,
      },

      {
        id: "actions",

        header: "",

        size: 90,

        enableSorting: false,

        cell: ({ row }) => (
          <Stack direction="row" spacing={0.5}>
            <IconButton size="small" onClick={() => handleEdit(row.original)}>
              <EditIcon fontSize="small" />
            </IconButton>

            <IconButton size="small" color="error" onClick={() => handleDelete(row.original.id)}>
              <DeleteIcon fontSize="small" />
            </IconButton>
          </Stack>
        ),
      },
    ],
    [foerderdeckel, handleDelete],
  );

  /* =========================================================
     UI
     ========================================================= */

  return (
    <Box>
      {/* =====================================================
          HEADER
          ===================================================== */}

      <Stack
        direction={{
          xs: "column",
          sm: "row",
        }}
        justifyContent="space-between"
        alignItems={{
          xs: "stretch",
          sm: "center",
        }}
        spacing={1.5}
        sx={{
          mb: 2,
        }}
      >
        <Typography
          variant="h5"
          sx={{
            fontSize: {
              xs: "1.2rem",
              md: "1.5rem",
            },
            fontWeight: 600,
          }}
        >
          Fördersätze
        </Typography>

        <Button variant="contained" startIcon={<AddIcon />} onClick={handleCreate}>
          Neuer Fördersatz
        </Button>
      </Stack>

      {/* =====================================================
          TABLE
          ===================================================== */}

      <GenericTableTanstack<FoerdersatzDTO>
        data={data}
        columns={columns}
        loading={isLoading}
        height={600}
        mobileRenderRow={(row) => (
          <Box>
            <Box
              sx={{
                display: "flex",
                justifyContent: "space-between",
                alignItems: "center",
                gap: 1,
              }}
            >
              <Chip size="small" label={row.typ} color="primary" variant="outlined" />

              <Typography
                sx={{
                  fontWeight: 700,
                  color: "primary.main",
                  whiteSpace: "nowrap",
                }}
              >
                <Money value={row.foerdersatz} />
              </Typography>
            </Box>

            <Typography
              variant="body2"
              color="text.secondary"
              sx={{
                mt: 0.5,
              }}
            >
              {row.gueltigVon}
              {" bis "}
              {row.gueltigBis || "unbegrenzt"}
            </Typography>

            <Stack
              direction="row"
              spacing={1}
              sx={{
                mt: 1,
              }}
            >
              <Button size="small" variant="outlined" onClick={() => handleEdit(row)}>
                Bearbeiten
              </Button>

              <Button
                size="small"
                variant="outlined"
                color="error"
                onClick={() => handleDelete(row.id)}
              >
                Löschen
              </Button>
            </Stack>
          </Box>
        )}
      />

      {/* =====================================================
          DIALOG
          ===================================================== */}

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
