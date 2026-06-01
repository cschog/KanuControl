import { ColumnDef } from "@tanstack/react-table";

import { IconButton } from "@mui/material";

import EditIcon from "@mui/icons-material/Edit";
import DeleteIcon from "@mui/icons-material/Delete";

import Money from "@/components/common/Money";

import { KostenRow } from "@/api/types/KostenRow";

export const kostenColumns = (
  onEdit: (id: number) => void,
  onDelete: (id: number) => void,
): ColumnDef<KostenRow>[] => [
  {
    accessorKey: "datum",
    header: "Datum",
  },

  {
    accessorKey: "person",
    header: "Person",
  },

  {
    accessorKey: "kategorie",
    header: "Kategorie",
  },

  {
    accessorKey: "kommentar",
    header: "Kommentar",
  },

  {
    accessorKey: "einnahme",
    header: "Einnahme",

    cell: ({ row }) => {
      const value = row.original.einnahme;

      if (!value) return "-";

      return <Money value={value} colorize />;
    },
  },

  {
    accessorKey: "ausgabe",
    header: "Ausgabe",

    cell: ({ row }) => {
      const value = row.original.ausgabe;

      if (!value) return "-";

      return <Money value={-value} colorize />;
    },
  },

  {
    id: "actions",

    header: "",

    enableSorting: false,

    cell: ({ row }) => (
      <>
        <IconButton
          size="small"
          onClick={(e) => {
            e.stopPropagation();
            onEdit(row.original.id);
          }}
        >
          <EditIcon fontSize="small" />
        </IconButton>

        <IconButton
          size="small"
          color="error"
          onClick={(e) => {
            e.stopPropagation();
            onDelete(row.original.id);
          }}
        >
          <DeleteIcon fontSize="small" />
        </IconButton>
      </>
    ),
  },
];
