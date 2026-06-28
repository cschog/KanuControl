// src/components/common/CrudTable.tsx

import { ReactNode } from "react";
import {
  Stack,
  Typography,
  IconButton,
} from "@mui/material";

import EditIcon from "@mui/icons-material/Edit";
import DeleteIcon from "@mui/icons-material/Delete";

import Money from "@/components/common/Money";
import {
  GenericTableTanstack,
  WithId,
} from "@/components/common/GenericTableTanstack";

import { ColumnDef } from "@tanstack/react-table";

interface CrudTableProps<T extends WithId> {
  data: T[];
  columns: ColumnDef<T>[];

  loading?: boolean;

  selectedRowId?: number |null;
  onSelectRow?: (row:T)=>void;

  onEdit?: (row:T)=>void;
  onDelete?: (row:T)=>void;

  title:(row:T)=>ReactNode;

  subtitle?:(row:T)=>ReactNode;

  money?:(row:T)=>number|null|undefined;

  extra?:(row:T)=>ReactNode;
}

export default function CrudTable<T extends WithId>({
  data,
  columns,
  loading=false,
  selectedRowId,
  onSelectRow,
  onEdit,
  onDelete,
  title,
  subtitle,
  money,
  extra,
}:CrudTableProps<T>) {

  return (
    <GenericTableTanstack
      data={data}
      columns={columns}
      loading={loading}
      selectedRowId={selectedRowId}
      onSelectRow={onSelectRow}

      mobileRenderRow={(row)=>(
        <Stack spacing={1}>

          <Stack
            direction="row"
            justifyContent="space-between"
            alignItems="center"
          >

            <Typography
              variant="subtitle1"
              fontWeight={600}
            >
              {title(row)}
            </Typography>

            {money && (
              <Money value={money(row) ?? 0}/>
            )}

          </Stack>

          {subtitle && (
            <Typography
              variant="body2"
              color="text.secondary"
            >
              {subtitle(row)}
            </Typography>
          )}

          {extra?.(row)}

          {(onEdit || onDelete) && (

            <Stack
              direction="row"
              spacing={1}
              justifyContent="flex-end"
            >

              {onEdit && (
                <IconButton
                  size="small"
                  onClick={(e)=>{
                    e.stopPropagation();
                    onEdit(row);
                  }}
                >
                  <EditIcon fontSize="small"/>
                </IconButton>
              )}

              {onDelete && (
                <IconButton
                  size="small"
                  color="error"
                  onClick={(e)=>{
                    e.stopPropagation();
                    onDelete(row);
                  }}
                >
                  <DeleteIcon fontSize="small"/>
                </IconButton>
              )}

            </Stack>
          )}

        </Stack>
      )}
    />
  );
}