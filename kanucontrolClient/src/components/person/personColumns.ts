import { GridColDef } from "@mui/x-data-grid";
import { PersonList } from "@/api/types/Person";

export type PersonWithId = PersonList & { id: number };

export const personColumns: GridColDef<PersonWithId>[] = [
  {
    field: "fullName",
    headerName: "Name",
    flex: 1,
    valueGetter: (_, row) => `${row.name ?? ""}, ${row.vorname ?? ""}`,
  },
  {
    field: "alter",
    headerName: "Alter",
    flex: 0.3,
    valueFormatter: (value: number | null) => value ?? "-",
  },
  {
    field: "ort",
    headerName: "Ort",
    sortable: false,
    flex: 1,
    valueFormatter: (value: string | null) => value ?? "-",
  },
  {
    field: "hauptvereinAbk",
    headerName: "Verein",
    sortable: false,
    flex: 0.3,
    valueFormatter: (value: string | null) => value ?? "-",
  },
  {
    field: "mitgliedschaftenCount",
    headerName: "Vereine",
    sortable: false,
    width: 90,
    align: "center",
    headerAlign: "center",
    valueFormatter: (value: number | null) => (typeof value === "number" && value > 1 ? value : ""),
  },
];
