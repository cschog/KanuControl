import Verein from "@/kjfp/types/verein/VereinFormModel";
import { Box, Tooltip, Typography } from "@mui/material";
import { GenericTableTanstack } from "@/core/components/table/GenericTableTanstack";

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
        <Box>
          <Typography fontWeight={600}>
            {row.dataStatus?.status === "ERROR" && (
              <Tooltip title="Fehlende Pflichtangaben">
                <Box
                  component="span"
                  sx={{
                    display: "inline-block",
                    width: 9,
                    height: 9,
                    borderRadius: "50%",
                    backgroundColor: "error.main",
                    mr: 1,
                  }}
                />
              </Tooltip>
            )}

            {row.dataStatus?.status === "WARNING" && (
              <Tooltip title="Empfohlene Angaben fehlen">
                <Box
                  component="span"
                  sx={{
                    display: "inline-block",
                    width: 9,
                    height: 9,
                    borderRadius: "50%",
                    backgroundColor: "#febf02",
                    mr: 1,
                  }}
                />
              </Tooltip>
            )}

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
      )}
    />
  );
};
