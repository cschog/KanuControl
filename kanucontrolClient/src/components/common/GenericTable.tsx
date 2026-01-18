// GenericTable.tsx
import * as React from "react";
import {
  DataGrid,
  GridColDef,
  GridRowSelectionModel,
} from "@mui/x-data-grid";
import { Box } from "@mui/material";

export interface WithId {
  id: number;
}

import { GridSortModel } from "@mui/x-data-grid";

interface GenericTableProps<T extends WithId> {
  rows: T[];
  columns: GridColDef<T>[];
  selectedRow: T | null;
  onSelectRow: (row: T | null) => void;

  initialSortField?: keyof T;
  initialSortModel?: GridSortModel;

  height?: number | string;
}

export function GenericTable<T extends WithId>({
  rows,
  columns,
  selectedRow,
  onSelectRow,
  initialSortField,
}: GenericTableProps<T>) {
  const handleSelectionChange = (selection: GridRowSelectionModel) => {
    const id =
      selection.ids.size > 0
        ? [...selection.ids][0]
        : null;

    const row =
      rows.find((r) => r.id === id) ?? null;

    onSelectRow(row);
  };

  return (
    <Box sx={{ width: "100%"}}>
      <DataGrid
        autoHeight
        rows={rows}
        columns={columns}
        getRowId={(row) => row.id}
        density="compact"
        hideFooter
        disableMultipleRowSelection

        initialState={
          initialSortField
            ? {
                sorting: {
                  sortModel: [
                    { field: initialSortField as string, sort: "asc" },
                  ],
                },
              }
            : undefined
        }

        rowSelectionModel={{
          type: "include",
          ids: selectedRow
            ? new Set([selectedRow.id])
            : new Set(),
        }}

        onRowSelectionModelChange={handleSelectionChange}
      />
    </Box>
  );
}