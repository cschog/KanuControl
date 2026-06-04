// src/components/finanzen/reisekosten/ReisekostenTable.tsx

import { useMemo } from "react";
import { Box, Button, IconButton, Stack, Typography } from "@mui/material";
import { ConfirmDeleteDialog } from "@/components/common/ConfirmDeleteDialog";
import AddIcon from "@mui/icons-material/Add";
import EditIcon from "@mui/icons-material/Edit";
import { ColumnDef } from "@tanstack/react-table";
import { GenericTableTanstack } from "@/components/common/GenericTableTanstack";
import Money from "@/components/common/Money";
import { useNavigate } from "react-router-dom";
import { useReisekostenabrechnungen } from "@/hooks/reisekosten/useReisekostenabrechnungen";
import { ReisekostenabrechnungListResponse } from "@/api/types/Reisekostenabrechnung";
import DeleteIcon from "@mui/icons-material/Delete";
import { deleteReisekostenabrechnung } from "@/api/services/reisekostenApi";
import { useQueryClient } from "@tanstack/react-query";


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

  const [deleteId, setDeleteId] = useState<number | null>(null);
  const queryClient = useQueryClient();

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
        size: 120,
        enableSorting: false,

        cell: ({ row }) => (
          <Stack direction="row" spacing={1}>
            <IconButton
              size="small"
              onClick={() =>
                navigate(`/veranstaltungen/${veranstaltungId}/reisekosten/${row.original.id}`)
              }
            >
              <EditIcon fontSize="small" />
            </IconButton>

            <IconButton size="small" color="error" onClick={() => setDeleteId(row.original.id)}>
              <DeleteIcon fontSize="small" />
            </IconButton>
          </Stack>
        ),
      },
    ],
    [navigate, veranstaltungId],
  );

  return (
    <Box>
      <ReisekostenCreateDialog
        open={dialogOpen}
        veranstaltungId={veranstaltungId}
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
          <Box
            sx={{
              display: "flex",
              justifyContent: "space-between",
              alignItems: "center",
              width: "100%",
            }}
          >
            <Box sx={{ minWidth: 0 }}>
              <Typography variant="body2" fontWeight={600} noWrap>
                {row.fahrerName}
              </Typography>

              <Typography variant="body2" color="text.secondary">
                {row.gesamtKilometer} km · <Money value={row.gesamtBetrag} />
              </Typography>
            </Box>

            <Stack direction="row" spacing={0.5}>
              <IconButton
                size="small"
                onClick={() =>
                  navigate(`/veranstaltungen/${veranstaltungId}/reisekosten/${row.id}`)
                }
              >
                <EditIcon fontSize="small" />
              </IconButton>

              <IconButton size="small" color="error" onClick={() => setDeleteId(row.id)}>
                <DeleteIcon fontSize="small" />
              </IconButton>
            </Stack>
          </Box>
        )}
      />
      <ConfirmDeleteDialog
        open={deleteId !== null}
        onClose={() => setDeleteId(null)}
        onConfirm={async () => {
          if (deleteId == null) {
            return;
          }

          await deleteReisekostenabrechnung(deleteId);

          await queryClient.invalidateQueries({
            queryKey: ["reisekosten", veranstaltungId],
          });

          setDeleteId(null);
        }}
        description="Soll die Reisekostenabrechnung wirklich gelöscht werden?"
      />
    </Box>
  );
};

export default ReisekostenTable;
