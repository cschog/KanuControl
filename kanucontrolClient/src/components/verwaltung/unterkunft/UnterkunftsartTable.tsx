// src/components/verwaltung/unterkunft/UnterkunftsartTable.tsx

import { useMemo } from "react";
import { ColumnDef } from "@tanstack/react-table";
import { Chip, Stack, IconButton } from "@mui/material";
import Money from "@/components/common/Money";
import EditIcon from "@mui/icons-material/Edit";
import DeleteIcon from "@mui/icons-material/Delete";
import CrudTable from "@/components/common/CrudTable";

import { UnterkunftsartDTO } from "@/api/types/unterkunft/UnterkunftsartDTO";

interface UnterkunftsartTableProps {
    data: UnterkunftsartDTO[];
    loading?: boolean;

    selectedRowId?: number | null;
    onSelectRow?: (row: UnterkunftsartDTO) => void;
    onEdit?: (row: UnterkunftsartDTO) => void;
    onDelete?: (row: UnterkunftsartDTO) => void;
}

const UnterkunftsartTable = ({
    data,
    loading = false,
    selectedRowId,
    onSelectRow,
    onEdit,
    onDelete
}: UnterkunftsartTableProps) => {
    const columns = useMemo<ColumnDef<UnterkunftsartDTO>[]>(
        () => [
            {
                accessorKey: "bezeichnung",
                header: "Bezeichnung",
                size: 220,
            },
            {
                accessorKey: "preisProPersonUndNacht",
                header: "Preis/Nacht",
                size: 140,
                meta: {
                    align: "right",
                },
                cell: ({ getValue }) => (
                    <Money value={Number(getValue())} />
                ),
            },
            {
                accessorKey: "bemerkung",
                header: "Bemerkung",
                size: 320,
            },
        
            {
                id: "actions",
                header: "",
                size: 100,
                enableSorting: false,
                cell: ({ row }) => (
                    <Stack direction="row" spacing={0.5} justifyContent="center">
                        <IconButton
                            size="small"
                            onClick={(e) => {
                                e.stopPropagation();
                                onEdit?.(row.original);
                            }}
                        >
                            <EditIcon fontSize="small" />
                        </IconButton>

                        <IconButton
                            size="small"
                            color="error"
                            onClick={(e) => {
                                e.stopPropagation();
                                onDelete?.(row.original);
                            }}
                        >
                            <DeleteIcon fontSize="small" />
                        </IconButton>
                    </Stack>
                ),
            }
        ],
        [onEdit, onDelete],
    );

   return (

   <CrudTable

      data={data}

      columns={columns}

      loading={loading}

      selectedRowId={selectedRowId}

      onSelectRow={onSelectRow}

      onEdit={onEdit}

      onDelete={onDelete}

      title={(r)=>r.bezeichnung}

      subtitle={(r)=>r.bemerkung}

      money={(r)=>r.preisProPersonUndNacht}

   />

);
};

export default UnterkunftsartTable;