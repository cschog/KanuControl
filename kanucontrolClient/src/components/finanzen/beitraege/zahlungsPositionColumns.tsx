import { ColumnDef } from "@tanstack/react-table";

import { IconButton } from "@mui/material";
import DeleteIcon from "@mui/icons-material/Delete";

import Money from "@/components/common/Money";

import { ZahlungsPositionDTO } from "@/api/types/beitraege";

interface Props {
  onDelete: (index: number) => void;
}

export const zahlungsPositionColumns = ({ onDelete }: Props): ColumnDef<ZahlungsPositionDTO>[] => [
  {
    id: "teilnehmerName",
    header: "Teilnehmer",
    size: 300,

    accessorFn: (row) => `${row.nachname ?? ""}, ${row.vorname ?? ""}`,

    sortingFn: "text",
  },

  {
    accessorKey: "betrag",
    header: "Betrag",
    size: 120,

    meta: {
      align: "right",
    },

    cell: ({ row }) => <Money value={row.original.betrag ?? 0} align="right" />,
  },

  {
    id: "delete",
    header: "",
    size: 60,

    meta: {
      align: "center",
    },

    cell: ({ row }) => (
      <IconButton size="small" color="error" onClick={() => onDelete(row.index)}>
        <DeleteIcon fontSize="small" />
      </IconButton>
    ),
  },
];
