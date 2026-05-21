import { ColumnDef } from "@tanstack/react-table";

import { Button, Chip, Stack, Typography } from "@mui/material";

import { FinanzGruppe } from "@/api/services/finanzgruppenApi";

interface Props {
  onAddTeilnehmer: (gruppeId: number) => void;

  onDelete: (gruppe: FinanzGruppe) => void;

  onRemoveTeilnehmer: (gruppeId: number, personId: number, name: string) => void;
}

export const kuerzelColumns = ({
  onAddTeilnehmer,
  onDelete,
  onRemoveTeilnehmer,
}: Props): ColumnDef<FinanzGruppe>[] => [
  {
    accessorKey: "kuerzel",

    header: "Kürzel",

    size: 120,

    cell: ({ row }) => <Typography fontWeight={600}>{row.original.kuerzel}</Typography>,
  },

  {
    id: "teilnehmer",

    header: "Teilnehmer",

    size: 500,

    cell: ({ row }) => (
      <Stack direction="row" spacing={1} useFlexGap flexWrap="wrap">
        {row.original.teilnehmer.map((t) => (
          <Chip
            key={t.id}
            size="small"
            label={`${t.vorname} ${t.nachname}`}
            onDelete={() =>
              onRemoveTeilnehmer(row.original.id, t.personId, `${t.vorname} ${t.nachname}`)
            }
          />
        ))}
      </Stack>
    ),
  },

  {
    accessorKey: "belegCount",

    header: "Belege",

    size: 90,
  },

  {
    id: "actions",

    header: "",

    size: 180,

    enableSorting: false,

    cell: ({ row }) => (
      <Stack direction="row" spacing={1} justifyContent="flex-end">
        <Button size="small" onClick={() => onAddTeilnehmer(row.original.id)}>
          + Teilnehmer
        </Button>

        <Button size="small" color="error" onClick={() => onDelete(row.original)}>
          Löschen
        </Button>
      </Stack>
    ),
  },
];
