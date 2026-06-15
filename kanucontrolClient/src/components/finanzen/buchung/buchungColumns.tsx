import { ColumnDef } from "@tanstack/react-table";
import { Box, IconButton, Stack, Typography } from "@mui/material";
import EditIcon from "@mui/icons-material/Edit";
import DeleteIcon from "@mui/icons-material/Delete";
import Money from "@/components/common/Money";
import { Buchung } from "@/api/types/abrechnung";
import { kategorieZuTyp } from "@/api/types/finanz";

interface Props {
  onEdit: (row: Buchung) => void;
  onDelete: (id: number) => void;
}

export const buchungColumns = ({ onEdit, onDelete }: Props): ColumnDef<Buchung>[] => [
  {
    accessorKey: "kategorie",
    header: "Kategorie",
    size: 200,
    cell: ({ row }) => (
      <Typography
        sx={{
          fontSize: "1.1rem",
          fontWeight: 500,
          color: "text.secondary",
        }}
      >
        {row.original.kategorie.replaceAll("_", " ")}
      </Typography>
    ),
  },
  {
    accessorKey: "beschreibung",
    header: "Beschreibung",
    size: 200,
    cell: ({ row }) => (
      <Typography
        sx={{
          fontSize: "1.1rem",
        }}
      >
        {row.original.beschreibung || "-"}
      </Typography>
    ),
  },
  {
    accessorKey: "betrag",
    header: "Betrag",
    size: 100,
    meta: {
      align: "right",
    },

    cell: ({ row }) => {
      const isKosten = kategorieZuTyp[row.original.kategorie] === "KOSTEN";

      return (
        <Box
          sx={{
            width: "100%",
            textAlign: "right",
          }}
        >
          <Money
            value={isKosten ? -row.original.betrag : row.original.betrag}
            colorize
            variant="h6"
          />
        </Box>
      );
    },
  },

  {
    id: "actions",

    header: "",

    size: 100,
    minSize: 100,
    maxSize: 100,

    enableSorting: false,

    meta: {
      align: "right",
    },

    cell: ({ row }) => {
      if (row.original.systemGenerated) {
        return null;
      }

      return (
        <Stack
          direction="row"
          spacing={0.5}
          justifyContent="flex-end"
          sx={{
            width: "100%",
          }}
        >
          <IconButton size="small" onClick={() => onEdit(row.original)}>
            <EditIcon fontSize="small" />
          </IconButton>

          <IconButton size="small" color="error" onClick={() => onDelete(row.original.id)}>
            <DeleteIcon fontSize="small" />
          </IconButton>
        </Stack>
      );
    },
  },
];
