import { ColumnDef } from "@tanstack/react-table";
import { COUNTRIES } from "@/kjfp/api/enums/CountryCode";
import { Box, Tooltip } from "@mui/material";

import Verein from "@/core/api/types/verein/VereinFormModel";

export interface VereinWithId extends Verein {
  id: number;
}

export const vereinColumnsTanstack: ColumnDef<VereinWithId>[] = [
  {
    id: "dataStatus",
    header: "",
    enableSorting: false,
    size: 32,

    cell: ({ row }) => {
      const status = row.original.dataStatus;

      if (!status || status.status === "OK") {
        return null;
      }

      const isError = status.status === "ERROR";

      const messages = Object.values(status.fields ?? {})
        .filter((field) => field.status === status.status)
        .map((field) => field.message);

      if (messages.length === 0) {
        return null;
      }

      return (
        <Tooltip
          title={
            <Box>
              {messages.map((message, index) => (
                <div key={index}>{message}</div>
              ))}
            </Box>
          }
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
            sx={{
              width: 10,
              height: 10,
              borderRadius: "50%",
              backgroundColor: isError ? "error.main" : "#febf02",
              mx: "auto",
            }}
          />
        </Tooltip>
      );
    },

    meta: {
      align: "center",
    },
  },

  {
    accessorKey: "abk",
    header: "Abk.",
    enableSorting: true,
  },

  {
    accessorKey: "name",
    header: "Verein",
    enableSorting: true,
  },

  {
    accessorKey: "mitgliederCount",
    header: "Mitgl.",
    enableSorting: true,

    cell: ({ getValue }) => {
      const value = getValue<number>();
      return value && value > 0 ? value : "";
    },

    meta: {
      align: "center",
    },
  },

  {
    accessorKey: "strasse",
    header: "Straße",
    enableSorting: true,
  },

  {
    accessorKey: "plz",
    header: "PLZ",
    enableSorting: true,
  },

  {
    accessorKey: "ort",
    header: "Ort",
    enableSorting: true,
  },

  {
    accessorKey: "countryCode",
    header: "Land",

    cell: ({ getValue }) => {
      const code = getValue<string>();

      return COUNTRIES.find((c) => c.code === code)?.label ?? code;
    },
  },
];
