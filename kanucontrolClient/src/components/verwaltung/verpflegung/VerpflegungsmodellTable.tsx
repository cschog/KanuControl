// src/components/verwaltung/unterkunft/UnterkunftsartTable.tsx

import { useMemo } from "react";
import { ColumnDef } from "@tanstack/react-table";
import { Stack, IconButton } from "@mui/material";
import Money from "@/components/common/Money";
import EditIcon from "@mui/icons-material/Edit";
import DeleteIcon from "@mui/icons-material/Delete";
import CrudTable from "@/components/common/CrudTable";

import { VerpflegungsmodellDTO } from "@/api/types/verpflegung/VerpflegungsmodellDTO";

interface VerpflegungsmodellTableProps {
    data: VerpflegungsmodellDTO[];
    loading?: boolean;

    onEdit?: (row: VerpflegungsmodellDTO) => void;
    onDelete?: (row: VerpflegungsmodellDTO) => void;
}

const VerpflegungsmodellTable = ({
    data,
    loading = false,

    onEdit,
    onDelete
}: VerpflegungsmodellTableProps) => {
    const columns = useMemo<ColumnDef<VerpflegungsmodellDTO>[]>(
        () => [
            {
                accessorKey: "bezeichnung",
                header: "Bezeichnung",
                size: 220,
            },
            {
                accessorKey: "preisProPersonUndTag",
                header: "Preis/Tag",
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
            onEdit={onEdit}
            onDelete={onDelete}
            title={(r) => r.bezeichnung}
            subtitle={(r) => r.bemerkung}
            money={(r) => r.preisProPersonUndTag}

        />

    );
};

export default VerpflegungsmodellTable;