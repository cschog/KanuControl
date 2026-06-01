// src/components/finanzen/reisekosten/ReisekostenTable.tsx

import { useMemo } from "react";
import { Box, Button, IconButton, Stack } from "@mui/material";
import AddIcon from "@mui/icons-material/Add";
import EditIcon from "@mui/icons-material/Edit";
import { ColumnDef } from "@tanstack/react-table";
import { GenericTableTanstack } from "@/components/common/GenericTableTanstack";
import Money from "@/components/common/Money";
import { useNavigate } from "react-router-dom";
import { useReisekostenabrechnungen } from "@/hooks/reisekosten/useReisekostenabrechnungen";
import { ReisekostenabrechnungListResponse } from "@/api/types/Reisekostenabrechnung";

import { useState } from "react";

import ReisekostenCreateDialog from "./ReisekostenCreateDialog";

import { createReisekostenabrechnung } from "@/api/services/reisekostenApi";

interface Props {
  veranstaltungId: number;
}

const ReisekostenTable = ({ veranstaltungId }: Props) => {
  const navigate = useNavigate();

  const { data = [], isLoading } = useReisekostenabrechnungen(veranstaltungId);

  const [dialogOpen, setDialogOpen] = useState(false);

  const columns = useMemo<ColumnDef<ReisekostenabrechnungListResponse>[]>(
    () => [
      {
        accessorKey: "fahrerName",
        header: "Fahrer",
        size: 250,
      },

      {
        accessorKey: "gesamtKilometer",
        header: "Kilometer",
        size: 120,
        meta: {
          align: "right",
        },
      },

      {
        accessorKey: "gesamtBetrag",
        header: "Betrag",
        size: 150,
        meta: {
          align: "right",
        },

        cell: ({ row }) => <Money value={row.original.gesamtBetrag} />,
      },

      {
        id: "actions",
        header: "",
        size: 80,
        enableSorting: false,

        cell: ({ row }) => (
          <IconButton
            size="small"
            onClick={() =>
              navigate(`/veranstaltungen/${veranstaltungId}/reisekosten/${row.original.id}`)
            }
          >
            <EditIcon fontSize="small" />
          </IconButton>
        ),
      },
    ],
    [navigate, veranstaltungId],
  );

  return (
    <Box>
      <ReisekostenCreateDialog
        open={dialogOpen}
        onClose={() => setDialogOpen(false)}
        onSave={async (fahrerId, abrechnungsdatum, bemerkung) => {
          const id = await createReisekostenabrechnung({
            veranstaltungId,
            fahrerId,
            abrechnungsdatum,
            bemerkung,
          });

          setDialogOpen(false);

          navigate(`/veranstaltungen/${veranstaltungId}/reisekosten/${id}`);
        }}
      />
      <Stack direction="row" justifyContent="flex-end" sx={{ mb: 2 }}>
        <Button variant="contained" startIcon={<AddIcon />} onClick={() => setDialogOpen(true)}>
          Neue Reisekostenabrechnung
        </Button>
      </Stack>

      <GenericTableTanstack<ReisekostenabrechnungListResponse>
        data={data}
        columns={columns}
        loading={isLoading}
        height={600}
        mobileRenderRow={(row) => (
          <Box>
            <Box
              sx={{
                fontWeight: 600,
              }}
            >
              {row.fahrerName}
            </Box>

            <Box
              sx={{
                mt: 0.5,
              }}
            >
              {row.gesamtKilometer} km
            </Box>

            <Box
              sx={{
                mt: 0.5,
                color: "primary.main",
                fontWeight: 700,
              }}
            >
              <Money value={row.gesamtBetrag} />
            </Box>

            <Button
              size="small"
              variant="outlined"
              sx={{ mt: 1 }}
              onClick={() => navigate(`/veranstaltungen/${veranstaltungId}/reisekosten/${row.id}`)}
            >
              Bearbeiten
            </Button>
          </Box>
        )}
      />
    </Box>
  );
};

export default ReisekostenTable;
