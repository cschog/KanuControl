import { ColumnDef } from "@tanstack/react-table";

import { Box, Checkbox, Chip, TextField, Typography } from "@mui/material";

import Money from "@/components/common/Money";

import { TeilnehmerListDTO } from "@/api/types/beitraege";


interface Props {
  individuelleGebuehren: boolean;

  onBezahltChange: (id: number, checked: boolean) => void;

  onBeitragChange: (id: number, value?: number) => void;

  setData: React.Dispatch<React.SetStateAction<TeilnehmerListDTO[]>>;
}

export const beitraegeColumns = ({
  individuelleGebuehren,
  onBezahltChange,
  onBeitragChange,
  setData,
}: Props): ColumnDef<TeilnehmerListDTO>[] => [
  {
    id: "teilnehmer",

    header: "Teilnehmer",

    size: 240,

    cell: ({ row }) => (
      <Box>
        <Typography
          sx={{
            fontSize: {
              xs: "0.95rem",

              md: "1.2rem",
            },
            fontWeight: 600,
          }}
        >
          {row.original.person.name}
        </Typography>

        <Typography
          color="text.secondary"
          sx={{
            fontSize: {
              xs: "0.95rem",

              md: "1.1rem",
            },
          }}
        >
          {row.original.person.vorname}
        </Typography>
      </Box>
    ),
  },

  {
    accessorKey: "alterBeiBeginn",

    header: "Alter",

    size: 70,

    meta: {
      align: "right",
    },

    cell: ({ row }) => (
      <Box
        sx={{
          width: "100%",
          textAlign: "right",
        }}
      >
        {row.original.alterBeiBeginn ?? "-"}
      </Box>
    ),
  },

  {
    id: "verein",

    header: "Verein",

    size: 90,

    cell: ({ row }) => row.original.person.hauptvereinAbk ?? "-",
  },

  {
    accessorKey: "rolle",

    header: "Rolle",

    size: 120,

    cell: ({ row }) => (
      <>
        {row.original.rolle === "L" && <Chip size="small" label="Leiter" color="secondary" />}

        {row.original.rolle === "M" && <Chip size="small" label="Mitarbeiter" />}
      </>
    ),
  },

  {
    accessorKey: "betrag",

    header: "Beitrag",

    size: 140,

    meta: {
      align: "right",
    },

    cell: ({ row }) => (
      <Box
        sx={{
          width: "100%",
          display: "flex",
          justifyContent: "flex-end",
        }}
      >
        {individuelleGebuehren ? (
          <TextField
            type="number"
            size="small"
            value={row.original.individuellerBeitrag ?? row.original.effektiverBeitrag}
            onChange={(e) => {
              const value = e.target.value;

              setData((prev) =>
                prev.map((x) =>
                  x.id === row.original.id
                    ? {
                        ...x,
                        individuellerBeitrag: value === "" ? undefined : Number(value),
                      }
                    : x,
                ),
              );
            }}
            onBlur={() => onBeitragChange(row.original.id, row.original.individuellerBeitrag)}
            sx={{
              width: 90,
            }}
            slotProps={{
              htmlInput: {
                style: {
                  textAlign: "right",
                },
              },
            }}
          />
        ) : (
          <Money value={row.original.effektiverBeitrag} />
        )}
      </Box>
    ),
  },

  {
    accessorKey: "bezahlt",

    header: "Bezahlt",

    size: 90,

    meta: {
      align: "center",
    },

    cell: ({ row }) => (
      <Checkbox
        checked={row.original.bezahlt}
        onChange={(e) => onBezahltChange(row.original.id, e.target.checked)}
      />
    ),
  },

  {
    accessorKey: "bezahltAm",

    header: "Bezahlt am",

    size: 120,
  },
];
