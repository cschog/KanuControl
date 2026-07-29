import { ColumnDef } from "@tanstack/react-table";
import { Typography } from "@mui/material";

import { AbrechnungBeleg } from "@/api/types/abrechnung";
import Money from "@/components/common/Money";
import { formatGermanDate } from "@/utils/dateUtils";
import { finanzKategorieLabel, kategorieZuTyp } from "@/api/types/finanz";

export const belegColumns: ColumnDef<AbrechnungBeleg>[] = [
  {
    accessorKey: "belegnummer",
    header: "Beleg",
    size: 110,
  },

  {
    id: "kategorie",
    header: "Kategorie",
    size: 180,
    enableSorting: false,
    cell: ({ row }) => {
      const positionen = row.original.positionen;
      if (positionen.length !== 1) {
        return "-";
      }
      return finanzKategorieLabel[positionen[0].kategorie];
    },
  },

  {
    accessorKey: "datum",
    header: "Datum",
    size: 120,
    cell: ({ row }) => formatGermanDate(row.original.datum),
  },

  {
    accessorKey: "beschreibung",
    header: "Beschreibung",
    cell: ({ row }) => <Typography noWrap>{row.original.beschreibung || "-"}</Typography>,
  },

  {
    id: "positionen",
    header: "Pos.",
    size: 70,
    enableSorting: false,
    cell: ({ row }) => row.original.positionen.length,
  },

  {
    id: "summe",
    header: "Summe",
    size: 120,
    enableSorting: false,
    meta: {
      align: "right",
    },

    cell: ({ row }) => {
      const summe = row.original.positionen.reduce((sum, p) => sum + p.betrag, 0);

      const typ =
        row.original.positionen.length > 0
          ? kategorieZuTyp[row.original.positionen[0].kategorie]
          : undefined;

      return (
        <Money
          value={summe}
          color={typ === "EINNAHME" ? "success.main" : typ === "KOSTEN" ? "error.main" : undefined}
        />
      );
    },
  },
];
