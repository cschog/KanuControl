import React from "react";

import {
  ColumnDef,
  flexRender,
  getCoreRowModel,
  RowSelectionState,
  SortingState,
  useReactTable,
} from "@tanstack/react-table";

import {
  Box,
  Checkbox,
  CircularProgress,
  Paper,
  Table,
  TableBody,
  TableCell,
  TableContainer,
  TableHead,
  TableRow,
  Typography,
  useMediaQuery,
} from "@mui/material";

import { useTheme } from "@mui/material/styles";

export interface WithId {
  id: number;
}

interface GenericTableTanstackProps<T extends WithId> {
  data: T[];
  columns: ColumnDef<T>[];

  loading?: boolean;

  mobileRenderRow?: (row: T) => React.ReactNode;

  selectedRowId?: number | null;

  onSelectRow?: (row: T) => void;
  onRowSelectionChange?: (rows: T[]) => void;

  height?: number;

  enableCheckboxSelection?: boolean;

  resetSelectionTrigger?: number;

  fixedColumnWidths?: boolean;

  // ⭐ SERVER SORTING

  sorting?: SortingState;

  onSortingChange?: (sorting: SortingState) => void;

  // ⭐ INFINITE SCROLL

  onLoadMore?: () => void;

  hasMore?: boolean;
}

export function GenericTableTanstack<T extends WithId>({
  data,
  columns,
  loading = false,
  selectedRowId,
  onSelectRow,
  onRowSelectionChange,
  height,
  sorting = [],
  onSortingChange,
  onLoadMore,
  hasMore = false,
  mobileRenderRow,
  resetSelectionTrigger,
  fixedColumnWidths = true,
  enableCheckboxSelection = false,
}: GenericTableTanstackProps<T>) {
  const theme = useTheme();

  const isMobile = useMediaQuery(theme.breakpoints.down("sm"));

  const loadMoreRef = React.useRef<HTMLDivElement | null>(null);

  /* =========================================================
     ROW SELECTION
     ========================================================= */

  const [rowSelection, setRowSelection] = React.useState<RowSelectionState>({});

  React.useEffect(() => {
    setRowSelection({});
  }, [resetSelectionTrigger]);

  /* =========================================================
     CHECKBOX COLUMN
     ========================================================= */

  const checkboxColumn: ColumnDef<T> = {
    id: "select",
    size: 52,

    header: ({ table }) => (
      <Checkbox
        size="small"
        sx={{ p: 0.3 }}
        checked={table.getIsAllRowsSelected()}
        indeterminate={table.getIsSomeRowsSelected()}
        onChange={table.getToggleAllRowsSelectedHandler()}
      />
    ),

    cell: ({ row }) => (
      <Checkbox
        size="small"
        sx={{ p: 0.3 }}
        checked={row.getIsSelected()}
        disabled={!row.getCanSelect()}
        onChange={row.getToggleSelectedHandler()}
        onClick={(e) => e.stopPropagation()}
      />
    ),
  };

  /* =========================================================
     FINAL COLUMNS
     ========================================================= */

  const finalColumns = [...(enableCheckboxSelection ? [checkboxColumn] : []), ...columns];

  /* =========================================================
     TABLE
     ========================================================= */

  const table = useReactTable({
    data,

    columns: finalColumns,

    state: {
      sorting,
      rowSelection,
    },

    enableRowSelection: true,

    onRowSelectionChange: setRowSelection,

    manualSorting: true,

    onSortingChange: (updater) => {
      const nextSorting = typeof updater === "function" ? updater(sorting) : updater;
      onSortingChange?.(nextSorting);
    },

    getCoreRowModel: getCoreRowModel(),
  });

  /* =========================================================
   ROW SELECTION EFFECT
   ========================================================= */

  React.useEffect(() => {
    if (!onRowSelectionChange) return;

    const selectedRows = table.getSelectedRowModel().rows.map((r) => r.original);

    onRowSelectionChange(selectedRows);
  }, [rowSelection, table, onRowSelectionChange]);

  /* =========================================================
     HEIGHT
     ========================================================= */

  const tableHeight = height ?? (isMobile ? undefined : 650);

  /* =========================================================
     INFINITE SCROLL
     ========================================================= */

  React.useEffect(() => {
    if (!hasMore) return;

    if (!onLoadMore) return;

    const el = loadMoreRef.current;

    if (!el) return;

    const observer = new IntersectionObserver(
      (entries) => {
        const first = entries[0];

        if (first.isIntersecting && !loading) {
          onLoadMore();
        }
      },
      {
        threshold: 0.5,
      },
    );

    observer.observe(el);

    return () => {
      observer.disconnect();
    };
  }, [hasMore, loading, onLoadMore]);

  /* =========================================================
     RENDER
     ========================================================= */

  return (
    <Paper
      sx={{
        width: "100%",
        overflow: "hidden",
      }}
    >
      <TableContainer
        sx={{
          maxHeight: tableHeight,
          height: "auto",
          overflowX: "auto",
        }}
      >
        {/* =====================================================
           MOBILE
           ===================================================== */}

        {isMobile && mobileRenderRow ? (
          <Box>
            {table.getRowModel().rows.map((row) => {
              const selected = row.original.id === selectedRowId;

              return (
                <Paper
                  key={row.id}
                  variant="outlined"
                  onClick={() => onSelectRow?.(row.original)}
                  sx={{
                    p: 1.2,
                    mb: 1,

                    cursor: "pointer",

                    borderColor: selected ? "primary.main" : "divider",

                    bgcolor: selected ? "action.selected" : undefined,
                  }}
                >
                  <Box display="flex" alignItems="center" gap={1}>
                    {/* =========================
                MOBILE CHECKBOX
               ========================= */}

                    {enableCheckboxSelection && (
                      <Checkbox
                        size="small"
                        checked={row.getIsSelected()}
                        disabled={!row.getCanSelect()}
                        onChange={row.getToggleSelectedHandler()}
                        onClick={(e) => e.stopPropagation()}
                        sx={{
                          p: 0.3,
                          alignSelf: "flex-start",
                          mt: 0.2,
                        }}
                      />
                    )}

                    {/* =========================
                MOBILE CONTENT
               ========================= */}

                    <Box flex={1}>{mobileRenderRow(row.original)}</Box>
                  </Box>
                </Paper>
              );
            })}
          </Box>
        ) : (
          /* ===================================================
             DESKTOP
             =================================================== */
          <Table
            stickyHeader
            size="small"
            sx={{
              tableLayout: fixedColumnWidths ? "fixed" : "auto",
              ...(fixedColumnWidths ? { minWidth: table.getTotalSize() } : { width: "100%" }),
            }}
          >
            <TableHead>
              {table.getHeaderGroups().map((headerGroup) => (
                <TableRow key={headerGroup.id}>
                  {headerGroup.headers.map((header) => {
                    const sorted = header.column.getIsSorted();

                    return (
                      <TableCell
                        key={header.id}
                        align={
                          (header.column.columnDef.meta as { align?: string })?.align === "right"
                            ? "right"
                            : header.id === "select"
                            ? "center"
                            : "left"
                        }
                        onClick={header.column.getToggleSortingHandler()}
                        padding="normal"
                        sx={{
                          ...(fixedColumnWidths && {
                            width: header.getSize(),
                            minWidth: header.getSize(),
                            maxWidth: header.getSize(),
                          }),

                          whiteSpace: "nowrap",

                          overflow: "hidden",
                          textOverflow: "ellipsis",

                          cursor: header.column.getCanSort() ? "pointer" : "default",

                          py: 0.5,
                          px: 1,

                          fontWeight: 700,

                          fontSize: {
                            xs: "0.9rem",
                            md: "1.2rem",
                          },

                          userSelect: "none",
                        }}
                      >
                        <Box
                          display="flex"
                          alignItems="center"
                          justifyContent={
                            (
                              header.column.columnDef.meta as {
                                align?: string;
                              }
                            )?.align === "right"
                              ? "flex-end"
                              : "flex-start"
                          }
                          gap={1}
                        >
                          {flexRender(header.column.columnDef.header, header.getContext())}

                          {sorted === "asc" ? "▲" : sorted === "desc" ? "▼" : ""}
                        </Box>
                      </TableCell>
                    );
                  })}
                </TableRow>
              ))}
            </TableHead>

            <TableBody>
              {table.getRowModel().rows.map((row) => {
                const selected = row.original.id === selectedRowId;

                return (
                  <TableRow
                    key={row.id}
                    hover
                    selected={selected}
                    onClick={() => onSelectRow?.(row.original)}
                    sx={{
                      cursor: "pointer",

                      "& td": {
                        py: 0.6,
                      },
                    }}
                  >
                    {row.getVisibleCells().map((cell) => (
                      <TableCell
                        key={cell.id}
                        align={
                          (cell.column.columnDef.meta as { align?: string })?.align === "right"
                            ? "right"
                            : cell.column.id === "select"
                            ? "center"
                            : "left"
                        }
                        size="small"
                        padding="normal"
                        sx={{
                          ...(fixedColumnWidths && {
                            width: cell.column.getSize(),
                            minWidth: cell.column.getSize(),
                            maxWidth: cell.column.getSize(),
                          }),

                          py: 0.5,
                          px: 1,

                          fontSize: {
                            xs: "0.82rem",
                            md: "1.2rem",
                          },

                          overflow: "hidden",
                          textOverflow: "ellipsis",

                          whiteSpace: "nowrap",
                          lineHeight: 1.2,
                        }}
                      >
                        {flexRender(cell.column.columnDef.cell, cell.getContext())}
                      </TableCell>
                    ))}
                  </TableRow>
                );
              })}
            </TableBody>
          </Table>
        )}
      </TableContainer>

      {/* =====================================================
         LOADING
         ===================================================== */}

      {loading && (
        <Box display="flex" justifyContent="center" p={2}>
          <CircularProgress size={24} />
        </Box>
      )}

      {/* =====================================================
         EMPTY
         ===================================================== */}

      {!loading && data.length === 0 && (
        <Box p={3}>
          <Typography align="center" color="text.secondary">
            Keine Daten vorhanden
          </Typography>
        </Box>
      )}

      {/* =====================================================
         INFINITE SCROLL
         ===================================================== */}

      {hasMore && (
        <Box ref={loadMoreRef} display="flex" justifyContent="center" p={2}>
          {loading && <CircularProgress size={24} />}
        </Box>
      )}
    </Paper>
  );
}
