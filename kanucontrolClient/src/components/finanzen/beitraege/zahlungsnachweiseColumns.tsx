import { ColumnDef } from "@tanstack/react-table";
import { IconButton, Stack, Tooltip } from "@mui/material";

import EditIcon from "@mui/icons-material/Edit";
import DeleteIcon from "@mui/icons-material/Delete";

import { ZahlungsnachweisListDTO } from "@/api/types/beitraege";
import Money from "@/components/common/Money";

interface Props {
  onEdit: (id: number) => void;
  onDelete: (id: number) => void;
}

export const zahlungsnachweiseColumns = ({
  onEdit,
  onDelete,
}: Props): ColumnDef<ZahlungsnachweisListDTO>[] => [
  {
    accessorKey: "datum",
    header: "Datum",
    size: 100,
  },

  {
    accessorKey: "betrag",
    header: "Betrag",
    size: 80,
    meta: {
      align: "right",
    },
    cell: ({ row }) => (
      <Money value={row.original.rueckzahlung ? -row.original.betrag : row.original.betrag} />
    ),
  },

  {
    id: "teilnehmer",
    header: "Teilnehmer",
    size: 250,
    enableSorting: false,
    cell: ({ row }) => {
      if (row.original.rueckzahlung) {
        return "Rückzahlung";
      }

      const teilnehmer = row.original.teilnehmer ?? [];

      if (teilnehmer.length === 0) {
        return null;
      }

      return teilnehmer.map((tn) => `${tn.nachname}, ${tn.vorname}`).join("; ");
    },
  },

  {
    accessorKey: "anzahlDokumente",
    header: "Dok.",
    size: 70,
    meta: {
      align: "right",
    },
  },

  {
    accessorKey: "bemerkung",
    header: "Bemerkung",
  },

  {
    id: "actions",
    header: "",
    size: 90,
    enableSorting: false,
    cell: ({ row }) => (
      <Stack direction="row" spacing={0.5}>
        <Tooltip title="Bearbeiten">
          <IconButton size="small" onClick={() => onEdit(row.original.id)}>
            <EditIcon fontSize="small" />
          </IconButton>
        </Tooltip>

        <Tooltip title="Löschen">
          <IconButton size="small" color="error" onClick={() => onDelete(row.original.id)}>
            <DeleteIcon fontSize="small" />
          </IconButton>
        </Tooltip>
      </Stack>
    ),
  },
];
