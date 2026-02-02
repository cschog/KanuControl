import * as React from "react";
import { DataGrid, GridColDef, GridRowSelectionModel, useGridApiRef } from "@mui/x-data-grid";
import { Box, useMediaQuery, useTheme } from "@mui/material";

export interface WithId {
  id: number;
}

interface GenericTableProps<T extends WithId> {
  rows: T[];
  columns: GridColDef<T>[];

  selectedRow: T | null;
  onSelectRow: (row: T | null) => void;

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
  selectedRow,
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
  /* =========================
     Grid API
     ========================= */

  const apiRef = useGridApiRef();

  /* =========================
     Responsive Höhe
     ========================= */

  const theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down("sm"));
  const isTablet = useMediaQuery(theme.breakpoints.between("sm", "md"));

  const rowHeight = 42;
  const headerHeight = 56;
  const footerHeight = paginationMode === "server" ? 56 : 0;

  const visibleRows = isMobile ? 6 : isTablet ? 8 : 11;

  const calculatedHeight = headerHeight + footerHeight + visibleRows * rowHeight;

  const tableHeight = height ?? calculatedHeight;

  /* =========================
     Selection
     ========================= */

  const handleSelectionChange = (selection: GridRowSelectionModel) => {
    const id = selection.ids.size > 0 ? [...selection.ids][0] : null;
    const row = rows.find((r) => r.id === id) ?? null;
    onSelectRow(row);
  };

  /* =========================
     🔥 Auto-Scroll zur selektierten Zeile
     ========================= */

React.useEffect(() => {
  if (!selectedRow) return;

  const rowIndex = rows.findIndex((r) => r.id === selectedRow.id);
  if (rowIndex === -1) return;

  const api = apiRef.current;
  if (!api) return;

  const renderContext = api.state.virtualization?.renderContext;
  if (!renderContext) return;

  const { firstRowIndex, lastRowIndex } = renderContext;

  if (rowIndex < firstRowIndex || rowIndex > lastRowIndex) {
    api.scrollToIndexes({ rowIndex });
  }
}, [selectedRow, rows, apiRef]);

  /* =========================
     Render
     ========================= */

  return (
    <Box sx={{ width: "100%", height: tableHeight }}>
      <DataGrid
        apiRef={apiRef}
        rows={rows}
        columns={columns}
        getRowId={(row) => row.id}
        density="compact"
        disableMultipleRowSelection
        hideFooter={paginationMode !== "server"}
        paginationMode={paginationMode}
        rowCount={rowCount}
        pageSizeOptions={[10, 20, 50, 100]}
        paginationModel={
          paginationMode === "server" && page != null && pageSize != null
            ? { page, pageSize }
            : undefined
        }
        onPaginationModelChange={(model) => {
          if (model.page !== page) {
            onPageChange?.(model.page);
          }
          if (model.pageSize !== pageSize) {
            onPageSizeChange?.(model.pageSize);
          }
        }}
        initialState={
          initialSortField
            ? {
                sorting: {
                  sortModel: [{ field: initialSortField as string, sort: "asc" }],
                },
              }
            : undefined
        }
        rowSelectionModel={{
          type: "include",
          ids: selectedRow ? new Set([selectedRow.id]) : new Set(),
        }}
        onRowSelectionModelChange={handleSelectionChange}
      />
    </Box>
  );
}
