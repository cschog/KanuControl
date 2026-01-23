// components/verein/vereinColumns.ts
import { GridColDef } from "@mui/x-data-grid";
import  Verein  from "@/api/types/VereinFormModel";

export interface VereinWithId extends Verein {
  id: number;
}

export const vereinColumns: GridColDef<VereinWithId>[] = [
  { field: "abk", headerName: "Abk.", flex: 0.5 },
  { field: "name", headerName: "Verein", flex: 1 },
  { field: "strasse", headerName: "Straße", flex: 1 },
  { field: "plz", headerName: "PLZ.", flex: 0.5 },
  { field: "ort", headerName: "Ort", flex: 1 },
];
