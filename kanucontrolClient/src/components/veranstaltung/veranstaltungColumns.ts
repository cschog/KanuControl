// src/components/veranstaltung/veranstaltungColumns.ts
import { GridColDef } from "@mui/x-data-grid";
import { VeranstaltungList } from "@/api/types/VeranstaltungList";

export const veranstaltungColumns: GridColDef<VeranstaltungList>[] = [
  {
    field: "aktiv",
    headerName: "",
    width: 60,
    align: "center",
    renderCell: (p) => (p.value ? "🟢" : "⚪"),
    sortable: false,
  },
  {
    field: "name",
    headerName: "Veranstaltung",
    flex: 2,
  },
  {
    field: "typ",
    headerName: "Typ",
    width: 120,
  },
  {
    field: "beginnDatum",
    headerName: "Beginn",
    width: 120,
  },
  {
    field: "endeDatum",
    headerName: "Ende",
    width: 120,
  },
  {
    field: "vereinName",
    headerName: "Verein",
    flex: 1.5,
  },
  {
    field: "leiterName",
    headerName: "Leiter",
    flex: 1.5,
  },
];
