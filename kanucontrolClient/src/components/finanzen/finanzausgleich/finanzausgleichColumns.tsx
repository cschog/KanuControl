import { ColumnDef } from "@tanstack/react-table";
import { Typography } from "@mui/material";

import Money from "@/components/common/Money";
import { FinanzausgleichRow } from "./finanzausgleichTypes";

export const finanzausgleichColumns: ColumnDef<FinanzausgleichRow>[] = [
  {
    accessorKey: "kuerzel",
    header: "Konto",
    size: 120,
    meta: {
      align: "left",
    },
    cell: ({ row }) => (
      <Typography component="span" fontWeight={700} fontSize="1.5rem">
        {row.original.kuerzel}
      </Typography>
    ),
  },

  {
    accessorKey: "teilnehmerBeitraegeSoll",
    header: "Soll-Beiträge",
    size: 180,
    meta: {
      align: "right",
    },
    cell: ({ row }) => <Money value={row.original.teilnehmerBeitraegeSoll} />,
  },

  {
    accessorKey: "teilnehmerBeitraegeUeberweisung",
    header: "Überweisung",
    size: 180,
    meta: {
      align: "right",
    },
    cell: ({ row }) => <Money value={row.original.teilnehmerBeitraegeUeberweisung} />,
  },

  {
    accessorKey: "teilnehmerBeitraegeQuittung",
    header: "Quittung",
    size: 180,
    meta: {
      align: "right",
    },
    cell: ({ row }) => <Money value={row.original.teilnehmerBeitraegeQuittung} />,
  },

  {
    accessorKey: "ausgaben",
    header: "Ausgaben",
    size: 180,
    meta: {
      align: "right",
    },
    cell: ({ row }) => <Money value={row.original.ausgaben} />,
  },

  {
    accessorKey: "fahrkosten",
    header: "Fahrtkosten",
    size: 180,
    meta: {
      align: "right",
    },
    cell: ({ row }) => <Money value={row.original.fahrkosten} />,
  },

  {
    accessorKey: "erstattungVomVK",
    header: "Erstattung vom VK",
    size: 220,
    meta: {
      align: "right",
    },
    cell: ({ row }) => (
      <Typography component="div" fontWeight={700}>
        <Money value={row.original.erstattungVomVK} />
      </Typography>
    ),
  },
];
