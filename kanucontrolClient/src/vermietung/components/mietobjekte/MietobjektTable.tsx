import { Box, Typography } from "@mui/material";

import { GenericTableTanstack } from "@/core/components/table/GenericTableTanstack";
import type { Mietobjekt } from "@/vermietung/types/Mietobjekt";

import { mietobjektColumnsTanstack } from "./mietobjektColumnsTanstack";
import { radius } from "@/core/theme/ui";

interface Props {
  data: Mietobjekt[];

  selectedId: number | null;

  onSelect: (row: Mietobjekt | null) => void;

  sorting: {
    id: string;
    desc: boolean;
  }[];

  onSortingChange: (
    sorting: {
      id: string;
      desc: boolean;
    }[],
  ) => void;
}

export function MietobjektTable({ data, selectedId, onSelect, sorting, onSortingChange }: Props) {
  return (
    <GenericTableTanstack
      data={data}
      columns={mietobjektColumnsTanstack}
      selectedRowId={selectedId}
      onSelectRow={(row) => onSelect(row ?? null)}
      sorting={sorting}
      onSortingChange={onSortingChange}
      mobileRenderRow={(row) => (
        <Box>
          <Box
            sx={{
              display: "flex",
              alignItems: "center",
              gap: 1,
            }}
          >
            <Typography fontWeight={600}>{row.bezeichnung}</Typography>

            {row.aktiv && (
              <Box
                sx={{
                  px: 1,
                  py: 0.2,
                  borderRadius: radius.dialog,
                  bgcolor: "success.main",
                  color: "white",
                  fontSize: "0.7rem",
                  fontWeight: 600,
                }}
              >
                AKTIV
              </Box>
            )}
          </Box>

          {(row.plz || row.ort) && (
            <Typography variant="body2" color="text.secondary">
              {row.plz} {row.ort}
            </Typography>
          )}

          <Typography variant="body2">{row.mietbar ? "Mietbar" : "Nicht mietbar"}</Typography>
        </Box>
      )}
    />
  );
}
