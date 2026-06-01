// src/components/finanzen/reisekosten/ReisekostenFahrtabschnittTable.tsx

import { useMemo } from "react";

import { Box, IconButton, Stack, Tooltip } from "@mui/material";

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
        size: 60,
      },

      {
        accessorKey: "vonOrt",
        header: "Von",
        size: 180,
      },

      {
        accessorKey: "nachOrt",
        header: "Nach",
        size: 180,
      },

      {
        accessorKey: "beschreibung",

        header: "Bemerkung",

        size: 250,
      },

      {
        accessorKey: "kilometer",
        header: "km",
        size: 100,
        meta: {
          align: "right",
        },
      },

      {
        id: "mitfahrer",
        header: "Mitfahrer",
        size: 120,

        meta: {
          align: "center",
        },

        cell: ({ row }) => <Box textAlign="center">{row.original.mitfahrerIds?.length ?? 0}</Box>,
      },

      {
        accessorKey: "anhaenger",
        header: "Anhänger",

        cell: ({ row }) => (row.original.anhaenger ? "Ja" : "Nein"),
      },

      {
        id: "actions",
        header: "",
        size: 100,

        cell: ({ row }) => (
          <Stack direction="row" spacing={1}>
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
      mobileRenderRow={(row) => (
        <Box>
          <Box
            sx={{
              fontWeight: 600,
            }}
          >
            {row.vonOrt}
            {" → "}
            {row.nachOrt}
          </Box>

          <Box>{row.kilometer} km</Box>

          <Box>Mitfahrer: {row.mitfahrerIds?.length ?? 0}</Box>

          <Box>Anhänger: {row.anhaenger ? "Ja" : "Nein"}</Box>
        </Box>
      )}
    />
  );
}
