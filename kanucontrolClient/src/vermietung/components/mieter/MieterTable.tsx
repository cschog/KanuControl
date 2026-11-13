import { formatPersonName } from "@/core/utils/personName";
import { useMemo } from "react";
import { Box, Chip, Tooltip } from "@mui/material";
import type { ColumnDef, SortingState } from "@tanstack/react-table";
import { GenericTableTanstack } from "@/core/components/table/GenericTableTanstack";
import MieterBuchungenLink from "./MieterBuchungenLink";
import type { Mieter } from "@/vermietung/types/Mieter";

const values = {
  name: (m: Mieter) => `${formatPersonName(m.name, m.vorname)}`,
  email: (m: Mieter) => m.email?.trim() ?? "",
  telefon: (m: Mieter) => m.telefon?.trim() ?? "",
  festnetz: (m: Mieter) => m.telefonFestnetz?.trim() ?? "",
  mietobjekte: (m: Mieter) => (m.mietobjekte ?? []).join(", "),
  buchungen: (m: Mieter) => m.buchungen,
  kanaele: (m: Mieter) => m.buchungsquellen.map(q => q === "AIRBNB" ? "Airbnb" : "Direkt").join(" · "),
};
const textCell = (text: string) => text ? <Tooltip title={text}><Box component="span">{text}</Box></Tooltip> : null;
const columns: ColumnDef<Mieter>[] = [
  { id: "name", header: "Name", accessorFn: values.name, cell: info => textCell(info.getValue<string>()) },
  { id: "email", header: "E-Mail", accessorFn: values.email, cell: info => textCell(info.getValue<string>()) },
  { id: "telefon", header: "Telefon", accessorFn: values.telefon, cell: info => textCell(info.getValue<string>()) },
  { id: "festnetz", header: "Festnetz", accessorFn: values.festnetz, cell: info => textCell(info.getValue<string>()) },
  { id: "mietobjekte", header: "Mietobjekte", accessorFn: values.mietobjekte, cell: info => textCell(info.getValue<string>()) },
  { id: "buchungen", header: "Buchungen", accessorFn: values.buchungen, meta: { align: "right" }, cell: ({ row }) => <MieterBuchungenLink mieter={row.original} /> },
  { id: "kanaele", header: "Buchungskanäle", accessorFn: values.kanaele, cell: ({ row }) => <Box sx={{ display: "flex", gap: 0.5 }}>
    {row.original.buchungsquellen.includes("AIRBNB") && <Chip size="small" label="Airbnb" color="warning" variant="outlined" />}
    {row.original.buchungsquellen.includes("DIREKT") && <Chip size="small" label="Direkt" color="primary" variant="outlined" />}
  </Box> },
];

interface Props {
  data: Mieter[];
  sorting: SortingState;
  onSortingChange: (sorting: SortingState) => void;
  onSelect: (mieter: Mieter) => void;
  emptyState: React.ReactNode;
}
export default function MieterTable({ data, sorting, onSortingChange, onSelect, emptyState }: Props) {
  const sorted = useMemo(() => [...data].sort((a, b) => {
    for (const sort of sorting) {
      const get = values[sort.id as keyof typeof values];
      if (!get) continue;
      const av = get(a), bv = get(b);
      const result = typeof av === "number" && typeof bv === "number" ? av - bv
        : String(av).localeCompare(String(bv), "de", { numeric: true, sensitivity: "base" });
      if (result) return sort.desc ? -result : result;
    }
    return a.id - b.id;
  }), [data, sorting]);
  return <GenericTableTanstack data={sorted} columns={columns} sorting={sorting}
    onSortingChange={onSortingChange} onSelectRow={onSelect} fixedColumnWidths={false} emptyState={emptyState} />;
}
