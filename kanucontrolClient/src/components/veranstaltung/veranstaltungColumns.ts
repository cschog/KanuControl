// src/components/veranstaltung/veranstaltungColumns.ts
import { GridColDef } from "@mui/x-data-grid";
import { VeranstaltungList } from "@/api/types/VeranstaltungList";

export const veranstaltungColumns: GridColDef<VeranstaltungList>[] = [
  {
    field: "aktiv",
    headerName: "",
    width: 40,
    align: "center",
    renderCell: (p) => (p.value ? "🟢" : "⚪"),
    sortable: false,
  },
  {
    field: "name",
    headerName: "Veranstaltung",
    width: 160,
  },
  {
    field: "typ",
    headerName: "Typ",
    flex: 0.3,
    align: "center",
  },
  {
    field: "beginnDatum",
    headerName: "Beginn",
    flex: 0.6,
  },
  {
    field: "endeDatum",
    headerName: "Ende",
    flex: 0.6,
  },
  {
    field: "vereinAbk",
    headerName: "Verein",
    flex: 0.4,
  },
  {
    field: "leitung",
    headerName: "Leitung",
    width: 130,
    valueGetter: (_value, row) => {
      if (!row.leiterName) return "";
      if (!row.leiterVorname) return row.leiterName;
      return `${row.leiterName}, ${row.leiterVorname}`;
    },
  },
];
