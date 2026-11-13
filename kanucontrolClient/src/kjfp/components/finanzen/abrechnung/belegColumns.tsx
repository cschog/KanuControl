import { ColumnDef } from "@tanstack/react-table";
import { Typography } from "@mui/material";

import { AbrechnungBeleg } from "@/kjfp/types/abrechnung";
import Money from "@/core/components/common/Money";
import { formatGermanDate } from "@/core/utils/dateUtils";
//import { finanzKategorieLabel, kategorieZuTyp } from "@/api/types/finanz";
import { kategorieZuTyp } from "@/kjfp/types/finanz";

import AttachFileIcon from "@mui/icons-material/AttachFile";
import { Chip } from "@mui/material";

export const belegColumns: ColumnDef<AbrechnungBeleg>[] = [
  {
    accessorKey: "belegnummer",
    header: "Beleg",
    size: 110,
  },

  {
    id: "art",
    header: "Art",
    size: 150,
    accessorFn: (beleg) => {
      const typen = new Set(beleg.positionen.map((position) => kategorieZuTyp[position.kategorie]));
      return typen.size > 1 ? "Einnahmen / Ausgaben" : typen.has("EINNAHME") ? "Einnahme" : typen.has("KOSTEN") ? "Ausgabe" : "-";
    },
  },

  {
    accessorKey: "aussteller",
    header: "Aussteller",
    size: 180,
    cell: ({ row }) => <Typography noWrap>{row.original.aussteller || "-"}</Typography>,
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
    id: "dokumente",
    header: "📎",
    size: 70,
    enableSorting: false,
    cell: ({ row }) =>
      row.original.dokumente.length > 0 ? (
        <Chip icon={<AttachFileIcon />} label={row.original.dokumente.length} size="small" />
      ) : (
        "-"
      ),
  },

  ...(["EINNAHME", "KOSTEN"] as const).map((typ): ColumnDef<AbrechnungBeleg> => ({
    id: typ === "EINNAHME" ? "einnahmen" : "ausgaben",
    header: typ === "EINNAHME" ? "Einnahmen" : "Ausgaben",
    size: 120,
    enableSorting: false,
    meta: { align: "right" },
    cell: ({ row }) => <Money
      value={row.original.positionen.filter((p) => kategorieZuTyp[p.kategorie] === typ)
        .reduce((sum, p) => sum + p.betrag, 0)}
      color={typ === "EINNAHME" ? "success.main" : "error.main"}
    />,
  })),
];
