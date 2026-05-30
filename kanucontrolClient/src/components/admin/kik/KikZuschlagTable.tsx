import { Box, Button, IconButton, Stack, Typography } from "@mui/material";

import { useState, useMemo, useCallback } from "react";

import AddIcon from "@mui/icons-material/Add";
import EditIcon from "@mui/icons-material/Edit";
import DeleteIcon from "@mui/icons-material/Delete";

import Money from "@/components/common/Money";

import KikZuschlagDialog from "@/components/admin/kik/KikDialog";

import {
  createKikZuschlag,
  updateKikZuschlag,
  deleteKikZuschlag,
} from "@/api/services/kikZuschlagApi";

import { useKikZuschlag } from "@/hooks/kik/useKikZuschlag";

import { KikDTO } from "@/api/types/Kik";

import { GenericTableTanstack } from "@/components/common/GenericTableTanstack";

import { ColumnDef } from "@tanstack/react-table";

/* =========================================================
   COMPONENT
   ========================================================= */

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

  const handleEdit = useCallback((row: KikDTO) => {
    setEditing(row);

    setDialogOpen(true);
  }, []);

  const handleDelete = useCallback(
    async (id: number) => {
      if (!window.confirm("KiK-Zuschlag löschen?")) {
        return;
      }

      await deleteKikZuschlag(id);

      await refetch();
    },
    [refetch],
  );

  /* =========================================================
     COLUMNS
     ========================================================= */

  const columns = useMemo<ColumnDef<KikDTO>[]>(
    () => [
      {
        accessorKey: "gueltigVon",

        header: "Gültig von",

        size: 140,
      },

      {
        accessorKey: "gueltigBis",

        header: "Gültig bis",

        size: 140,

        cell: ({ row }) => row.original.gueltigBis || "unbegrenzt",
      },

      {
        accessorKey: "kikZuschlag",

        header: "KiK-Zuschlag",

        size: 160,

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
            <Money value={row.original.kikZuschlag} />
          </Box>
        ),
      },

      {
        accessorKey: "beschluss",

        header: "Beschluss",

        size: 260,
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
    [handleDelete, handleEdit],
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
        direction="row"
        justifyContent="flex-end"
        sx={{
          mb: 2,
        }}
      >
        <Button variant="contained" startIcon={<AddIcon />} onClick={handleCreate}>
          Neuer KiK-Zuschlag
        </Button>
      </Stack>

      {/* =====================================================
          TABLE
          ===================================================== */}

      <GenericTableTanstack<KikDTO>
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
              <Typography
                variant="body2"
                color="text.secondary"
                sx={{
                  whiteSpace: "nowrap",
                }}
              >
                {row.gueltigVon}
                {" • "}
                {row.gueltigBis || "unbegrenzt"}
              </Typography>

              <Typography
                sx={{
                  whiteSpace: "nowrap",
                }}
              >
                <Money value={row.kikZuschlag} />
              </Typography>
            </Box>

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
