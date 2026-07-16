import { ColumnDef } from "@tanstack/react-table";

import { Box, Checkbox, Chip, TextField, Typography } from "@mui/material";

import Money from "@/components/common/Money";

import { TeilnehmerListDTO } from "@/api/types/beitraege";
import { fontSize } from "@/theme/ui";

interface Props {
  onBezahltChange: (id: number, checked: boolean) => void;

  setData: React.Dispatch<React.SetStateAction<TeilnehmerListDTO[]>>;
}

export const beitraegeColumns = ({
  onBezahltChange,
  setData,
}: Props): ColumnDef<TeilnehmerListDTO>[] => [
    {
      id: "teilnehmer",

      header: "Teilnehmer",

      size: 240,

      cell: ({ row }) => (
        <Box>
          <Typography
            sx={{
              fontSize: fontSize.sectionTitle,
              fontWeight: 600,
            }}
          >
            {row.original.person.name}
          </Typography>

          <Typography
            color="text.secondary"
            sx={{
              fontSize: fontSize.sectionTitle,
            }}
          >
            {row.original.person.vorname}
          </Typography>
        </Box>
      ),
    },

    {
      accessorKey: "alterBeiBeginn",
      header: "Alter",
      size: 70,
      meta: {
        align: "right",
      },

      cell: ({ row }) => (
        <Box
          sx={{
            width: "100%",
            textAlign: "right",
          }}
        >
          {row.original.alterBeiBeginn ?? "-"}
        </Box>
      ),
    },

    {
      id: "verein",
      header: "Verein",
      size: 90,
      cell: ({ row }) => row.original.person.hauptvereinAbk ?? "-",
    },

    {
      accessorKey: "rolle",
      header: "Rolle",
      size: 120,
      cell: ({ row }) => (
        <>
          {row.original.rolle === "L" && <Chip size="small" label="Leiter" color="secondary" />}

          {row.original.rolle === "M" && <Chip size="small" label="Mitarbeiter" />}
        </>
      ),
    },

    {
      id: "beitrag",
      header: "Beitrag",
      size: 140,
      meta: {
        align: "right",
      },

      cell: ({ row }) => (
        <Money
          value={row.original.effektiverBeitrag ?? 0}
          align="right"
        />
      ),
    },

    {
      accessorKey: "bezahlt",

      header: "Bezahlt",

      size: 90,

      meta: {
        align: "center",
      },

      cell: ({ row }) => (
        <Checkbox
          checked={row.original.bezahlt}
          onChange={(e) => onBezahltChange(row.original.id, e.target.checked)}
        />
      ),
    },

    {
      accessorKey: "bezahltAm",

      header: "Bezahlt am",

      size: 120,
    },
  ];
