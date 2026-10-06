import { Box, Typography } from "@mui/material";

import { GenericTableTanstack } from "@/core/components/table/GenericTableTanstack";
import type { Mietbereich } from "@/vermietung/types/Mietbereich";

import { mietbereichColumnsTanstack } from "./mietbereichColumnsTanstack";
import { radius } from "@/core/theme/ui";

interface Props {
  data: Mietbereich[];

  selectedId: number | null;

  onSelect: (row: Mietbereich | null) => void;

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

export function MietbereichTable({ data, selectedId, onSelect, sorting, onSortingChange }: Props) {
  return (
    <GenericTableTanstack
      data={data}
      columns={mietbereichColumnsTanstack}
      selectedRowId={selectedId}
      onSelectRow={(row) => onSelect(row ?? null)}
      sorting={sorting}
      onSortingChange={onSortingChange}
      mobileRenderRow={(row) => (
        <Box>
          <Typography fontWeight={600}>{row.bezeichnung}</Typography>

          {row.beschreibung && (
            <Typography variant="body2" color="text.secondary">
              {row.beschreibung}
            </Typography>
          )}

          <Typography variant="body2">{row.mietbar ? "Mietbar" : "Nicht mietbar"}</Typography>

          {row.mietbar && (
            <Box
              sx={{
                display: "inline-block",
                mt: 0.5,
                px: 1,
                py: 0.2,
                borderRadius: radius.dialog,
                bgcolor: "success.main",
                color: "white",
                fontSize: "0.7rem",
                fontWeight: 600,
              }}
            >
              MIETBAR
            </Box>
          )}
        </Box>
      )}
    />
  );
}
