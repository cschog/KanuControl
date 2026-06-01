import { useMemo, useState } from "react";

import { Box, Button } from "@mui/material";

import AddIcon from "@mui/icons-material/Add";

import { ColumnDef } from "@tanstack/react-table";

import Money from "@/components/common/Money";
import { GenericTableTanstack } from "@/components/common/GenericTableTanstack";

import ReisekostenKonfigurationDialog from "@/components/admin/reisekosten/ReisekostenKonfigurationDialog";

import {
  createReisekostenKonfiguration,
  type ReisekostenKonfiguration,
} from "@/api/services/reisekostenKonfigurationApi";

import { useReisekostenKonfigurationen } from "@/hooks/reisekosten/useReisekostenKonfigurationen";

export default function ReisekostenKonfigurationTable() {
  const { data = [], isLoading, refetch } = useReisekostenKonfigurationen();

  const [dialogOpen, setDialogOpen] = useState(false);

  const columns = useMemo<ColumnDef<ReisekostenKonfiguration>[]>(
    () => [
      {
        accessorKey: "gueltigVon",
        header: "Gültig von",
      },
      {
        accessorKey: "gueltigBis",
        header: "Gültig bis",
        cell: ({ row }) => row.original.gueltigBis ?? "offen",
      },
      {
        accessorKey: "pkwSatz",
        header: "PKW",
        cell: ({ row }) => <Money value={row.original.pkwSatz} />,
      },
      {
        accessorKey: "mitfahrerSatz",
        header: "Mitfahrer",
        cell: ({ row }) => <Money value={row.original.mitfahrerSatz} />,
      },
      {
        accessorKey: "anhaengerSatz",
        header: "Anhänger",
        cell: ({ row }) => <Money value={row.original.anhaengerSatz} />,
      },
    ],
    [],
  );

  return (
    <Box>
      <Box
        sx={{
          display: "flex",
          justifyContent: "flex-end",
          mb: 2,
        }}
      >
        <Button variant="contained" startIcon={<AddIcon />} onClick={() => setDialogOpen(true)}>
          Neue Version
        </Button>
      </Box>

      <GenericTableTanstack data={data} columns={columns} loading={isLoading} height={600} />

      <ReisekostenKonfigurationDialog
        open={dialogOpen}
        onClose={() => setDialogOpen(false)}
        onSave={async (dto) => {
          await createReisekostenKonfiguration(dto);

          await refetch();

          setDialogOpen(false);
        }}
      />
    </Box>
  );
}
