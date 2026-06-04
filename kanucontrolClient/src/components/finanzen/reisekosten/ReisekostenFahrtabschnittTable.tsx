// src/components/finanzen/reisekosten/ReisekostenFahrtabschnittTable.tsx

import { useMemo } from "react";

import { Box, IconButton, Stack, Tooltip, Typography } from "@mui/material";

import EditIcon from "@mui/icons-material/Edit";
import DeleteIcon from "@mui/icons-material/Delete";

import { ColumnDef } from "@tanstack/react-table";

import { GenericTableTanstack } from "@/components/common/GenericTableTanstack";

import { FahrtabschnittRequest } from "@/api/types/Reisekostenabrechnung";

interface Props {
  data: FahrtabschnittRequest[];

  onEdit: (abschnitt: FahrtabschnittRequest) => void;

  onDelete: (abschnitt: FahrtabschnittRequest) => void;
}

export default function ReisekostenFahrtabschnittTable({ data, onEdit, onDelete }: Props) {
  const columns = useMemo<ColumnDef<FahrtabschnittRequest>[]>(
    () => [
      {
        accessorKey: "reihenfolge",
        header: "#",
        size: 50,
      },

      {
        accessorKey: "vonOrt",
        header: "Von",
      },

      {
        accessorKey: "nachOrt",
        header: "Nach",
      },

      {
        accessorKey: "beschreibung",
        header: "Bemerkung",
      },

      {
        accessorKey: "kilometer",
        header: "km",
        size: 80,
        meta: {
          align: "right",
        },
      },

      {
        id: "mitfahrer",
        header: "Mitfahrer",
        size: 90,
        meta: {
          align: "center",
        },
        cell: ({ row }) => <Box textAlign="center">{row.original.mitfahrerIds?.length ?? 0}</Box>,
      },

      {
        accessorKey: "anhaenger",
        header: "Hänger",
        cell: ({ row }) => (row.original.anhaenger ? "Ja" : "Nein"),
      },

      {
        id: "actions",
        header: "",
        size: 70,

        cell: ({ row }) => (
          <Stack direction="row" spacing={0}>
            <Tooltip title="Bearbeiten">
              <IconButton size="small" onClick={() => onEdit(row.original)}>
                <EditIcon fontSize="small" />
              </IconButton>
            </Tooltip>

            <Tooltip title="Löschen">
              <IconButton size="small" onClick={() => onDelete(row.original)}>
                <DeleteIcon fontSize="small" />
              </IconButton>
            </Tooltip>
          </Stack>
        ),
      },
    ],
    [onEdit, onDelete],
  );

  return (
    <GenericTableTanstack<FahrtabschnittRequest>
      data={data}
      columns={columns}
      loading={false}
      height={450}
      fixedColumnWidths={false}
      mobileRenderRow={(row) => (
        <Box display="flex" justifyContent="space-between" alignItems="center">
          <Box>
            <Typography fontWeight={600}>
              {row.vonOrt} → {row.nachOrt}
            </Typography>

            <Typography variant="body2">
              {row.kilometer} km · {row.mitfahrerIds?.length ?? 0} Mitfahrer
            </Typography>

            <Typography variant="body2">Anhänger: {row.anhaenger ? "Ja" : "Nein"}</Typography>
          </Box>

          <Stack direction="row" spacing={0.5}>
            <IconButton size="small" onClick={() => onEdit(row)}>
              <EditIcon fontSize="small" />
            </IconButton>

            <IconButton size="small" color="error" onClick={() => onDelete(row)}>
              <DeleteIcon fontSize="small" />
            </IconButton>
          </Stack>
        </Box>
      )}
    />
  );
}
