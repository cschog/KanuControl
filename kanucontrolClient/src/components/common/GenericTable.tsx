import * as React from "react";
import {
  DataGrid,
  GridColDef,
  
  GridRowSelectionModel,
  GridSortModel,
} from "@mui/x-data-grid";
import { Box, useMediaQuery, useTheme } from "@mui/material";

export interface WithId {
  id: number;
}

interface GenericTableProps<T extends WithId> {
  rows: T[];
  columns: GridColDef<T>[];

  /** Multi Select (Teilnehmer) */
  checkboxSelection?: boolean;
  onSelectionChange?: (rows: T[]) => void;

  /** Single Select (Person) */
  selectedRowId?: number | null;
  onSelectRow?: (row: T | null) => void;

  /** Server Sorting */
  sortField?: string;
  sortDirection?: "asc" | "desc";
  onSortChange?: (field: string, direction: "asc" | "desc") => void;

  /** Paging */
  paginationMode?: "client" | "server";
  rowCount?: number;
  page?: number;
  pageSize?: number;
  onPageChange?: (page: number) => void;
  onPageSizeChange?: (size: number) => void;

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
  paginationMode,
  rowCount,
  page,
  pageSize,
  onPageChange,
  onPageSizeChange,
  initialSortField,
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

  /* ================= Selection ================= */

  const handleSelectionChange = React.useCallback(
    (model: GridRowSelectionModel) => {
      if (!model) return;

      let ids: number[] = [];

      if (model.type === "include") {
        ids = Array.from(model.ids ?? []) as number[];
      }

      if (model.type === "exclude") {
        const excluded = new Set(Array.from(model.ids ?? []) as number[]);
        ids = rows.filter((r) => !excluded.has(r.id)).map((r) => r.id);
      }

      /* ================= Multi ================= */
      if (checkboxSelection) {
        const selectedRows = rows.filter((r) => ids.includes(r.id));
        onSelectionChange?.(selectedRows);
        return;
      }

      /* ================= Single ================= */
      const id = ids[0];
      const row = rows.find((r) => r.id === id) ?? null;
      onSelectRow?.(row);
    },
    [rows, checkboxSelection, onSelectionChange, onSelectRow],
  );

  /* =====================================================
     Server Sorting
     ===================================================== */

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

  /* =====================================================
     Render
     ===================================================== */

  return (
    <Box sx={{ width: "100%", height: tableHeight }}>
      <DataGrid
        rows={rows}
        columns={columns}
        getRowId={(row) => row.id}
        density="compact"
        /* Selection */
        checkboxSelection={checkboxSelection}
        disableMultipleRowSelection={!checkboxSelection}
        onRowSelectionModelChange={handleSelectionChange}
        disableRowSelectionOnClick={checkboxSelection}
        /* Sorting */
        sortingMode={paginationMode === "server" ? "server" : "client"}
        sortModel={sortField ? [{ field: sortField, sort: sortDirection ?? "asc" }] : undefined}
        onSortModelChange={handleSortChange}
        /* Paging */
        paginationMode={paginationMode}
        rowCount={rowCount}
        hideFooter={paginationMode !== "server"}
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
