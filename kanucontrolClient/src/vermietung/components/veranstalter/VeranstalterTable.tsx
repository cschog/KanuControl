import { useMemo } from "react";
import { Box, Link, ListItemButton, Stack, Tooltip, Typography, useMediaQuery, useTheme } from "@mui/material";
import { Link as RouterLink } from "react-router-dom";
import type { ColumnDef, SortingState } from "@tanstack/react-table";
import { GenericTableTanstack } from "@/core/components/table/GenericTableTanstack";
import type { Veranstalter } from "@/vermietung/api/veranstalterApi";

function BuchungenLink({ veranstalter }: { veranstalter: Veranstalter }) {
  return <Link component={RouterLink} to={`/vermietung/buchung?veranstalterId=${veranstalter.id}`}
    onClick={event => event.stopPropagation()} underline="hover"
    aria-label={`Buchungen von ${veranstalter.name} anzeigen`}>
    {veranstalter.buchungen} {veranstalter.buchungen === 1 ? "Buchung" : "Buchungen"}
  </Link>;
}
const textCell = (text: string) => text ? <Tooltip title={text}><Box component="span">{text}</Box></Tooltip> : null;
const columns: ColumnDef<Veranstalter>[] = [
  { accessorKey: "name", header: "Name", cell: info => textCell(info.getValue<string>()) },
  { accessorKey: "abk", header: "Abk", cell: info => textCell(info.getValue<string>()) },
  { accessorKey: "countryCode", header: "Länderkennzeichen", cell: info => textCell(info.getValue<string>()) },
  { accessorKey: "plz", header: "PLZ", cell: info => textCell(info.getValue<string>() ?? "") },
  { accessorKey: "ort", header: "Ort", cell: info => textCell(info.getValue<string>() ?? "") },
  { accessorKey: "buchungen", header: "Buchungen", meta: { align: "right" }, cell: ({ row }) => <BuchungenLink veranstalter={row.original} /> },
];
interface Props {
  data: Veranstalter[];
  sorting: SortingState;
  onSortingChange: (sorting: SortingState) => void;
  onSelect: (veranstalter: Veranstalter) => void;
}
export default function VeranstalterTable({ data, sorting, onSortingChange, onSelect }: Props) {
  const isMobile = useMediaQuery(useTheme().breakpoints.down("md"));
  const sorted = useMemo(() => [...data].sort((a, b) => {
    for (const sort of sorting) {
      const key = sort.id as keyof Veranstalter;
      const av = a[key] ?? "", bv = b[key] ?? "";
      const result = typeof av === "number" && typeof bv === "number" ? av - bv
        : String(av).localeCompare(String(bv), "de", { numeric: true, sensitivity: "base" });
      if (result) return sort.desc ? -result : result;
    }
    return a.id - b.id;
  }), [data, sorting]);
  const emptyState = <Typography sx={{ p: 2 }} color="text.secondary">Keine Veranstalter gefunden.</Typography>;
  return isMobile ? <>
    {sorted.length === 0 && emptyState}
    {sorted.map(item => <ListItemButton key={item.id} onClick={() => onSelect(item)}
      sx={{ display: "block", p: 1.5, minWidth: 0, borderBottom: 1, borderColor: "divider" }}>
      <Stack spacing={0.5}>
        <Typography fontWeight={600}>{item.name}</Typography>
        <Typography variant="body2" color="text.secondary">{[item.abk, item.countryCode, item.plz, item.ort].filter(Boolean).join(" · ")}</Typography>
        <Typography variant="caption"><BuchungenLink veranstalter={item} /></Typography>
      </Stack>
    </ListItemButton>)}
  </> : <GenericTableTanstack data={sorted} columns={columns} sorting={sorting} onSortingChange={onSortingChange}
    onSelectRow={onSelect} fixedColumnWidths={false} emptyState={emptyState} />;
}
