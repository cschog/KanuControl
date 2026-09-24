import { ColumnDef } from "@tanstack/react-table";
import { Box, Tooltip } from "@mui/material";
import { PersonList } from "@/api/types/person/Person";

export const personColumnsTanstack: ColumnDef<PersonList>[] = [
  {
    id: "fullName",
    header: "Name",
    accessorFn: (row) => `${row.name ?? ""}, ${row.vorname ?? ""}`,
    cell: ({ row }) => {
      const person = row.original;

      const status = person.dataStatus;

      const marker =
        status === "ERROR"
          ? {
              color: "error.main",
              message: "Fehlende Pflichtangaben",
            }
          : status === "WARNING"
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
            <Tooltip
              title={marker.message}
              arrow
              placement="top"
              slotProps={{
                tooltip: {
                  sx: {
                    fontSize: "0.95rem",
                    lineHeight: 1.4,
                    maxWidth: 360,
                    padding: "10px 14px",
                  },
                },
                arrow: {
                  sx: {
                    fontSize: "1rem",
                  },
                },
              }}
            >
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

          <Box>{`${person.name ?? ""}, ${person.vorname ?? ""}`}</Box>
        </Box>
      );
    },
  },
  {
    accessorKey: "alter",
    header: "Alter",
    cell: (info) => info.getValue<number | null>() ?? "-",
    enableSorting: false,
  },
  {
    accessorKey: "ort",
    header: "Ort",
    cell: (info) => info.getValue<string | null>() ?? "-",
    enableSorting: false,
  },
  {
    accessorKey: "hauptvereinAbk",
    header: "Verein",
    cell: (info) => info.getValue<string | null>() ?? "-",
    enableSorting: false,
  },
];
