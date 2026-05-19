import { ColumnDef } from "@tanstack/react-table";
import { Chip } from "@mui/material";

import { TeilnehmerList } from "@/api/types/TeilnehmerList";

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
    },
    {
      accessorFn: (row) => row.person?.hauptvereinAbk ?? "",
      id: "verein",
      header: "Verein",
    },

    /* ======================================== */
    /* ROLLE */
    /* ======================================== */

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
            label={rolle === "L" ? "Leitung" : rolle === "M" ? "Mitarbeiter" : "+"}
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
