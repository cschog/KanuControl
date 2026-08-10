import { ColumnDef } from "@tanstack/react-table";

import { Box, Chip, Typography } from "@mui/material";

import Money from "@/components/common/Money";

import { TeilnehmerListDTO } from "@/api/types/beitraege";
import { fontSize } from "@/theme/ui";

export const beitraegeColumns = (): ColumnDef<TeilnehmerListDTO>[] => [
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

    cell: ({ row }) => <Money value={row.original.sollBeitrag ?? 0} align="right" />,
  },

  {
    accessorKey: "gezahlterBetrag",

    header: "Gezahlt",

    size: 140,

    meta: {
      align: "right",
    },

    cell: ({ row }) => <Money value={row.original.gezahlterBetrag ?? 0} align="right" />,
  },

  {
    accessorKey: "zahlungsstatus",
    header: "Status",
    size: 120,

    meta: {
      align: "center",
    },

    cell: ({ row }) => {
      const status = row.original.zahlungsstatus;

      if (status === "GRUEN") {
        return <Chip size="small" label="Bezahlt" color="success" />;
      }

      if (status === "GELB") {
        return <Chip size="small" label="Teilweise" color="warning" />;
      }

      return <Chip size="small" label="Offen" color="error" />;
    },
  },
];
