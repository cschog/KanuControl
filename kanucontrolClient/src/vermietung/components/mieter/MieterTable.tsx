import { formatPersonName } from "@/core/utils/personName";
import { useMemo } from "react";
import { Box, Tooltip } from "@mui/material";
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
};
const textCell = (text: string) => text ? <Tooltip title={text}><Box component="span">{text}</Box></Tooltip> : null;
const columns: ColumnDef<Mieter>[] = [
  { id: "name", header: "Name", accessorFn: values.name, cell: info => textCell(info.getValue<string>()) },
  { id: "email", header: "E-Mail", accessorFn: values.email, cell: info => textCell(info.getValue<string>()) },
  { id: "telefon", header: "Telefon", accessorFn: values.telefon, cell: info => textCell(info.getValue<string>()) },
  { id: "festnetz", header: "Festnetz", accessorFn: values.festnetz, cell: info => textCell(info.getValue<string>()) },
  { id: "mietobjekte", header: "Mietobjekte", accessorFn: values.mietobjekte, cell: info => textCell(info.getValue<string>()) },
  { id: "buchungen", header: "Buchungen", accessorFn: values.buchungen, meta: { align: "right" }, cell: ({ row }) => <MieterBuchungenLink mieter={row.original} /> },

];

interface Props {
  data: Mieter[];
  sorting: SortingState;
  onSortingChange: (sorting: SortingState) => void;
  onSelect: (mieter: Mieter) => void;
  emptyState: React.ReactNode;
  showMietobjekte: boolean;
}
export default function MieterTable({ data, sorting, onSortingChange, onSelect, emptyState, showMietobjekte }: Props) {
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
  const visibleColumns = useMemo(() => columns.filter(column => showMietobjekte || column.id !== "mietobjekte"), [showMietobjekte]);
  return <Box sx={{
    "& .MuiTableCell-root": { fontSize: "0.9rem", px: 1, py: 0.35, lineHeight: 1.35 },
    "& .MuiTableRow-root td": { py: 0.35 },
    "& .MuiTableCell-head": { fontSize: "0.85rem" },
    "& .MuiLink-root": { fontSize: "inherit" },
  }}><GenericTableTanstack data={sorted} columns={visibleColumns} sorting={sorting}
    getRowBackgroundColor={row => row.airbnb || row.buchungsquellen.includes("AIRBNB") ? "#FFF3E0" : undefined}
    onSortingChange={onSortingChange} onSelectRow={onSelect} fixedColumnWidths={false} emptyState={emptyState} /></Box>;
}
