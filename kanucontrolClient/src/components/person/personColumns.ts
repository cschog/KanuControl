// src/components/person/personColumns.ts
import { GridColDef } from "@mui/x-data-grid";
import { Person } from "@/api/types/Person";

export type PersonWithId = Person & { id: number };

export const personColumns: GridColDef<PersonWithId>[] = [
  {
    field: "name",
    headerName: "Name",
    flex: 1,
  },
  {
    field: "vorname",
    headerName: "Vorname",
    flex: 1,
  },
  {
    field: "sex",
    headerName: "Sex",
    flex: 0.3,
  },
  {
    field: "alter",
    headerName: "Alter",
    flex: 0.3,
    valueFormatter: (value) => value ?? "-",
  },
  {
    field: "ort",
    headerName: "Ort",
    flex: 1,
    valueGetter: (_, row) => row.ort ?? "-",
  },
  {
    field: "hauptverein",
    headerName: "Verein",
    flex: 0.6,
    sortable: false,
    valueGetter: (_, row) => {
      const hv = row.mitgliedschaften?.find((m) => m.hauptVerein);
      return hv?.verein?.abk ?? "-";
    },
  }, // ✅ DIESE KLAMMER FEHLTE
  {
    field: "vereinsAnzahl",
    headerName: "#",
    flex: 0.3,
    type: "number",
    sortable: false,
    valueGetter: (_, row) => row.mitgliedschaften?.length ?? 0,
  },
];
