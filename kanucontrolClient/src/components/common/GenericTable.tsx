import * as React from "react";
import { deDE } from "@mui/x-data-grid/locales";
import {
  DataGrid,
  GridColDef,
  GridRowSelectionModel,
  GridSortModel,
  GridCellParams,
  useGridApiRef,
  GridFilterModel,
} from "@mui/x-data-grid";
import { Box, Button, useMediaQuery } from "@mui/material";
import { useTheme } from "@mui/material/styles";
import type { Theme } from "@mui/material/styles";

export interface WithId {
  id: number;
}

interface GenericTableProps<T extends WithId> {
  paginationMode?: "client" | "server";

  rowCount?: number;

  page?: number;
  pageSize?: number;

  filterModel?: GridFilterModel;
  onFilterChange?: (model: GridFilterModel) => void;

  onPageChange?: (page: number) => void;

  onPageSizeChange?: (size: number) => void;

  rows: T[];
  columns: GridColDef<T>[];

  loading?: boolean;
  onLoadMore?: () => void;

  enableCheckboxSelection?: boolean;
  checkboxSelection?: boolean;
  onSelectionChange?: (rows: T[]) => void;

  selectedRowId?: number | null;
  onSelectRow?: (row: T | null) => void;

  onCellClick?: (params: GridCellParams) => void;

  sortField?: string;
  sortDirection?: "asc" | "desc";
  onSortChange?: (field: string, direction: "asc" | "desc") => void;

  initialSortField?: keyof T;
  height?: number;
}

export function GenericTable<T extends WithId>({
  rows,
  columns,
  checkboxSelection,
  onSelectionChange,
  onSelectRow,
  sortField,
  sortDirection,
  onSortChange,
  initialSortField,
  height,
  onCellClick,
  onLoadMore,
  filterModel,
  onFilterChange,
  loading,
}: //rowCount,
GenericTableProps<T>) {
  const theme: Theme = useTheme();

  const isMobile = useMediaQuery(theme.breakpoints.down("sm"));

  const containerRef = React.useRef<HTMLDivElement | null>(null);

  const apiRef = useGridApiRef();

  const rowHeight = 40;
  const headerHeight = 50;

  const tableHeight = height ?? (isMobile ? window.innerHeight - 380 : 650);

  /* ================= Selection ================= */

  const handleSelectionChange = React.useCallback(
    (model: GridRowSelectionModel) => {
      let ids: number[] = [];

      if (model.type === "include") {
        ids = Array.from(model.ids ?? []) as number[];
      }

      if (model.type === "exclude") {
        const excluded = new Set(Array.from(model.ids ?? []) as number[]);
        ids = rows.filter((r) => !excluded.has(r.id)).map((r) => r.id);
      }

      if (checkboxSelection) {
        onSelectionChange?.(rows.filter((r) => ids.includes(r.id)));
        return;
      }

      const id = ids[0];
      const row = rows.find((r) => r.id === id) ?? null;
      onSelectRow?.(row);
    },
    [rows, checkboxSelection, onSelectionChange, onSelectRow],
  );

  /* ================= Sorting ================= */

  const handleSortChange = React.useCallback(
    (model: GridSortModel) => {
      if (!model.length) return;
      const { field, sort } = model[0];
      if (field && sort) {
        onSortChange?.(field, sort);
      }
    },
    [onSortChange],
  );

  /* ================= Render ================= */

 return (
   <Box
     ref={containerRef}
     sx={{
       width: "100%",
     }}
   >
     <Box
       sx={{
         height: tableHeight,
       }}
     >
       <DataGrid
         apiRef={apiRef}
         rows={rows}
         disableVirtualization
         columns={columns}
         getRowId={(row) => row.id}
         //rowBufferPx={200}
         density="compact"
         localeText={deDE.components.MuiDataGrid.defaultProps.localeText}
         checkboxSelection={checkboxSelection}
         disableMultipleRowSelection={!checkboxSelection}
         onRowSelectionModelChange={handleSelectionChange}
         disableRowSelectionOnClick={checkboxSelection}
         sortingMode={onSortChange ? "server" : "client"}
         filterMode="server"
         disableColumnFilter={false}
         filterModel={filterModel}
         onFilterModelChange={onFilterChange}
         sortModel={sortField ? [{ field: sortField, sort: sortDirection ?? "asc" }] : undefined}
         onSortModelChange={handleSortChange}
         onCellClick={onCellClick}
         hideFooter
         rowHeight={rowHeight}
         columnHeaderHeight={headerHeight}
         initialState={
           initialSortField
             ? {
                 sorting: {
                   sortModel: [
                     {
                       field: initialSortField as string,
                       sort: "asc",
                     },
                   ],
                 },
               }
             : undefined
         }
       />
     </Box>

     {onLoadMore && (
       <Box display="flex" justifyContent="center" mt={2}>
         <Button variant="outlined" onClick={() => onLoadMore()} disabled={loading}>
           {loading ? "Lade..." : "Weitere Personen laden"}
         </Button>
       </Box>
     )}
   </Box>
 );
}
