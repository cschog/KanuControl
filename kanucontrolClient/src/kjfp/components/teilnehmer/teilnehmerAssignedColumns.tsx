import { Box, Chip, Tooltip } from "@mui/material";
import { ColumnDef } from "@tanstack/react-table";

import { TeilnehmerList } from "@/kjfp/types/TeilnehmerList";

interface Props {
  onRoleClick: (current: "L" | "M" | null, personId: number) => void;
}

export function teilnehmerAssignedColumns({ onRoleClick }: Props): ColumnDef<TeilnehmerList>[] {
  return [
    {
      id: "fullname",
      header: "Name",
      accessorFn: (row) => `${row.person?.name ?? ""}, ${row.person?.vorname ?? ""}`,
      sortingFn: "text",
      size: 260,

      cell: ({ row }) => {
        const person = row.original.person;

        const marker =
          person?.dataStatus === "ERROR"
            ? {
                color: "error.main",
                message: "Fehlende Pflichtangaben",
              }
            : person?.dataStatus === "WARNING"
              ? {
                  color: "#febf02",
                  message: "Empfohlene Angaben fehlen",
                }
              : null;

        return (
          <Box
            sx={{
              display: "flex",
              alignItems: "center",
              gap: 1,
            }}
          >
            {marker && (
              <Tooltip title={marker.message} arrow placement="top">
                <Box
                  component="span"
                  sx={{
                    width: 9,
                    height: 9,
                    borderRadius: "50%",
                    bgcolor: marker.color,
                    flexShrink: 0,
                  }}
                />
              </Tooltip>
            )}

            <Box>{`${person?.name ?? ""}, ${person?.vorname ?? ""}`}</Box>
          </Box>
        );
      },
    },

    {
      id: "alter",
      header: "Alter",
      accessorFn: (row) => row.alterBeiBeginn ?? null,
      size: 60,
    },

    {
      accessorFn: (row) => row.person?.hauptvereinAbk ?? "",
      id: "verein",
      header: "Verein",
    },

    {
      accessorKey: "rolle",
      header: "Rolle",

      cell: ({ row }) => {
        const rolle = row.original.rolle;

        return (
          <Chip
            clickable={rolle !== "L"}
            size="small"
            variant={rolle ? "filled" : "outlined"}
            label={rolle === "L" ? "L" : rolle === "M" ? "M" : "+"}
            onClick={() => {
              if (rolle !== "L") {
                onRoleClick(rolle ?? null, row.original.personId);
              }
            }}
          />
        );
      },
    },
  ];
}
