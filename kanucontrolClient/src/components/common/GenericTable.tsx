import * as React from "react";
import { DataGrid, GridColDef, useGridApiRef } from "@mui/x-data-grid";
import { Box, useMediaQuery, useTheme } from "@mui/material";

/* ========================================================= */

export interface WithId {
  id: number;
}

interface GenericTableProps<T extends WithId> {
  rows: T[];
  columns: GridColDef<T>[];

  checkboxSelection?: boolean;
  selectedRows?: T[]; // ⭐ MUSS EXISTIEREN
  onSelectionChange?: (rows: T[]) => void;

  selectedRow?: T | null;
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

/* ========================================================= */

export function GenericTable<T extends WithId>({
  rows,
  columns,
  checkboxSelection,
  onSelectionChange,
  initialSortField,
  paginationMode,
  rowCount,
  page,
  pageSize,
  onPageChange,
  onPageSizeChange,
  height,
}: GenericTableProps<T>) {
  const apiRef = useGridApiRef();

  /* ================= Height ================= */

  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down("sm"));
  const isTablet = useMediaQuery(theme.breakpoints.between("sm", "md"));

  const rowHeight = 42;
  const headerHeight = 56;
  const footerHeight = paginationMode === "server" ? 56 : 0;

  const visibleRows = isMobile ? 4 : isTablet ? 6 : 8;
  const tableHeight = height ?? headerHeight + footerHeight + visibleRows * rowHeight;

  /* ================= Selection Handler ================= */

const handleSelectionChange = React.useCallback(() => {
  if (!checkboxSelection) return;
  if (!apiRef.current) return;

  const selectedMap = apiRef.current.getSelectedRows();
  const selected = Array.from(selectedMap.values()) as T[];

  onSelectionChange?.(selected);
}, [apiRef, checkboxSelection, onSelectionChange]);

  /* ================= Render ================= */

  return (
    <Box sx={{ width: "100%", height: tableHeight }}>
      <DataGrid
        apiRef={apiRef}
        rows={rows}
        columns={columns}
        getRowId={(row) => row.id}
        density="compact"
        checkboxSelection={checkboxSelection}
        disableMultipleRowSelection={false}
        onRowSelectionModelChange={handleSelectionChange}
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
