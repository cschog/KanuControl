import Verein from "@/api/types/verein/VereinFormModel";
import { Box, Typography } from "@mui/material";
import { GenericTableTanstack } from "@/components/common/GenericTableTanstack";

import { vereinColumnsTanstack, VereinWithId } from "./vereinColumnsTanstack";

interface VereinTableProps {
  data: Verein[];

  selectedVerein: Verein | null;

  onSelectVerein: (verein: Verein | null) => void;
}

export const VereinTable: React.FC<VereinTableProps> = ({
  data,
  selectedVerein,
  onSelectVerein,
}) => {
  const rows: VereinWithId[] = data.filter((v): v is VereinWithId => typeof v.id === "number");

  return (
    <GenericTableTanstack<VereinWithId>
      data={rows}
      columns={vereinColumnsTanstack}
      selectedRowId={selectedVerein?.id ?? null}
      onSelectRow={(row) => onSelectVerein(row)}
      mobileRenderRow={(row) => (
        <>
          <Box>
            <Typography fontWeight={600}>
              {row.abk}

              <Typography component="span" variant="body2" color="text.secondary" sx={{ ml: 0.5 }}>
                ({row.mitgliederCount})
              </Typography>
            </Typography>

            <Typography variant="body2">{row.name}</Typography>

            <Typography variant="caption" color="text.secondary">
              {row.ort}
            </Typography>
          </Box>
        </>
      )}
    />
  );
};
