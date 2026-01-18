// src/components/person/personColumns.ts
import { GridColDef } from "@mui/x-data-grid";

export const personColumns: GridColDef[] = [
  { field: "vorname", headerName: "Vorname", flex: 1 },
  { field: "name", headerName: "Nachname", flex: 1 },
  { field: "sex", headerName: "Sex", flex: 0.25 },
  { field: "geburtsdatum", headerName: "Geburtsd.", flex: 0.5 },
  { field: "strasse", headerName: "Straße", flex: 1 },
  { field: "plz", headerName: "PLZ", flex: 0.5 },
  { field: "ort", headerName: "Ort", flex: 1 },
];