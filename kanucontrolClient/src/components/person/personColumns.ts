// src/components/person/personColumns.ts
import { GridColDef } from "@mui/x-data-grid";
import { PersonList } from "@/api/types/Person";

export type PersonWithId = PersonList & { id: number };

export const personColumns: GridColDef<PersonWithId>[] = [
  { field: "name", headerName: "Name", flex: 1 },
  { field: "vorname", headerName: "Vorname", flex: 1 },
  {
    field: "alter",
    headerName: "Alter",
    flex: 0.3,
    valueFormatter: (v) => v ?? "-",
  },
  {
    field: "ort",
    headerName: "Ort",
    flex: 1,
    valueFormatter: (v) => v ?? "-",
  },
  {
    field: "hauptvereinAbk",
    headerName: "Verein",
    flex: 0.3,
    valueFormatter: (v) => v ?? "-",
  },
  {
    field: "mitgliedschaftenCount",
    headerName: "#",
    flex: 0.1,
    type: "number",
  },
];
