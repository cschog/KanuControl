import { Box, Tooltip } from "@mui/material";
import { ColumnDef } from "@tanstack/react-table";
import { PersonList } from "@/api/types/person/PersonList";

export const teilnehmerAvailableColumns: ColumnDef<PersonList>[] = [
  {
    id: "fullname",
    header: "Name",
    accessorFn: (row) => `${row.name ?? ""}, ${row.vorname ?? ""}`,
    sortingFn: "text",
    size: 260,

    cell: ({ row }) => {
      const person = row.original;

      const marker =
        person.dataStatus === "ERROR"
          ? {
              color: "error.main",
              message: "Fehlende Pflichtangaben",
            }
          : person.dataStatus === "WARNING"
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

          <Box>{`${person.name ?? ""}, ${person.vorname ?? ""}`}</Box>
        </Box>
      );
    },
  },

  {
    id: "alter",
    header: "Alter",
    accessorKey: "alter",
    size: 60,
  },

  {
    accessorKey: "hauptvereinAbk",
    header: "Verein",
    size: 90,
  },
];