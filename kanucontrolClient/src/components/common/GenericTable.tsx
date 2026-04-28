import * as React from "react";
import {
  DataGrid,
  GridColDef,
  GridRowSelectionModel,
  GridSortModel,
  GridCellParams,
  useGridApiRef,
} from "@mui/x-data-grid";
import { Box, useMediaQuery } from "@mui/material";
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

  onPageChange?: (page: number) => void;

  onPageSizeChange?: (size: number) => void;

  rows: T[];
  columns: GridColDef<T>[];

  loading?: boolean;
  onLoadMore?: () => void;

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
}: GenericTableProps<T>) {
  const theme: Theme = useTheme();
  const isMobile = useMediaQuery(theme.breakpoints.down("sm"));
  const isTablet = useMediaQuery(theme.breakpoints.between("sm", "md"));

  const containerRef = React.useRef<HTMLDivElement | null>(null);
  const apiRef = useGridApiRef();

  const rowHeight = 42;
  const headerHeight = 56;

  const visibleRows = isMobile ? 4 : isTablet ? 6 : 8;
  const tableHeight = height ?? headerHeight + visibleRows * rowHeight;

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

  /* ================= Scroll Handling ================= */

 React.useEffect(() => {
   const el = containerRef.current?.querySelector(
     ".MuiDataGrid-virtualScroller",
   ) as HTMLElement | null;

   if (!el || !onLoadMore) return;

   let lastTrigger = 0;

   const handleScroll = () => {
     const { scrollTop, scrollHeight, clientHeight } = el;

     const isNearBottom = scrollTop + clientHeight >= scrollHeight - 100;

     const now = Date.now();

     if (isNearBottom && now - lastTrigger > 300) {
       lastTrigger = now;
       onLoadMore();
     }
   };

   el.addEventListener("scroll", handleScroll);

   return () => {
     el.removeEventListener("scroll", handleScroll);
   };
 }, [onLoadMore]);

  /* ================= Render ================= */

  return (
    <Box ref={containerRef} sx={{ width: "100%", height: tableHeight, overflow: "hidden" }}>
      <DataGrid
        apiRef={apiRef}
        rows={rows}
        columns={columns}
        getRowId={(row) => row.id}
        rowBufferPx={1000}
        density="compact"
        checkboxSelection={checkboxSelection}
        disableMultipleRowSelection={!checkboxSelection}
        onRowSelectionModelChange={handleSelectionChange}
        disableRowSelectionOnClick={checkboxSelection}
        sortingMode={onSortChange ? "server" : "client"}
        sortModel={sortField ? [{ field: sortField, sort: sortDirection ?? "asc" }] : undefined}
        onSortModelChange={handleSortChange}
        onCellClick={onCellClick}
        rowCount={rows.length} // ⭐ KEY FIX
        paginationMode="server" // ⭐ wichtig!
        hideFooter
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
