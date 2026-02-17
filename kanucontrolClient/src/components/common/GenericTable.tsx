import * as React from "react";
import { DataGrid, GridColDef, GridRowSelectionModel, GridRowParams } from "@mui/x-data-grid";
import { Box, useMediaQuery, useTheme } from "@mui/material";

export interface WithId {
  id: number;
}

interface GenericTableProps<T extends WithId> {
  rows: T[];
  columns: GridColDef<T>[];

  /** Multi Select */
  checkboxSelection?: boolean;
  onSelectionChange?: (rows: T[]) => void;

  /** Single Select */
  onSelectRow?: (row: T | null) => void;

  initialSortField?: keyof T;

  paginationMode?: "client" | "server";
  rowCount?: number;
  page?: number;
  pageSize?: number;
  onPageChange?: (page: number) => void;
  onPageSizeChange?: (pageSize: number) => void;

  height?: number;
}

export function GenericTable<T extends WithId>({
  rows,
  columns,
  checkboxSelection,
  onSelectionChange,
  onSelectRow,
  initialSortField,
  paginationMode,
  rowCount,
  page,
  pageSize,
  onPageChange,
  onPageSizeChange,
  height,
}: GenericTableProps<T>) {
  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down("sm"));
  const isTablet = useMediaQuery(theme.breakpoints.between("sm", "md"));

  const rowHeight = 42;
  const headerHeight = 56;
  const footerHeight = paginationMode === "server" ? 56 : 0;

  const visibleRows = isMobile ? 4 : isTablet ? 6 : 8;
  const tableHeight = height ?? headerHeight + footerHeight + visibleRows * rowHeight;

  /* ================= Multi Select ================= */

  const handleSelectionChange = React.useCallback(
    (model: GridRowSelectionModel) => {
      
      if (!checkboxSelection) return;

   let selected: T[] = [];

   if (model.type === "include") {
     const ids = Array.from(model.ids ?? []) as number[];
     selected = rows.filter((r) => ids.includes(r.id));
   }

   if (model.type === "exclude") {
     const excluded = new Set(Array.from(model.ids ?? []) as number[]);
     selected = rows.filter((r) => !excluded.has(r.id));
   }

   onSelectionChange?.(selected);
    },
    [checkboxSelection, rows, onSelectionChange],
  );

  /* ================= Single Select ================= */

  const handleRowClick = React.useCallback(
    (params: GridRowParams<T>) => {
      if (checkboxSelection) return;
      onSelectRow?.(params.row);
    },
    [checkboxSelection, onSelectRow],
  );

  return (
    <Box sx={{ width: "100%", height: tableHeight }}>
      <DataGrid
        rows={rows}
        columns={columns}
        getRowId={(row) => row.id}
        density="compact"
        checkboxSelection={checkboxSelection}
        disableMultipleRowSelection={!checkboxSelection}
        onRowSelectionModelChange={handleSelectionChange}
        onRowClick={handleRowClick}
        disableRowSelectionOnClick={checkboxSelection}
        hideFooter={paginationMode !== "server"}
        paginationMode={paginationMode}
        rowCount={rowCount}
        pageSizeOptions={[8, 20, 50, 100]}
        paginationModel={
          paginationMode === "server" && page != null && pageSize != null
            ? { page, pageSize }
            : undefined
        }
        onPaginationModelChange={(model) => {
          if (model.page !== page) onPageChange?.(model.page);
          if (model.pageSize !== pageSize) onPageSizeChange?.(model.pageSize);
        }}
        rowHeight={rowHeight}
        columnHeaderHeight={headerHeight}
        initialState={
          initialSortField
            ? {
                sorting: {
                  sortModel: [{ field: initialSortField as string, sort: "asc" }],
                },
              }
            : undefined
        }
      />
    </Box>
  );
}
