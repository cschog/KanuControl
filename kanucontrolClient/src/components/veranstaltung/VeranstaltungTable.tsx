import { Box, Typography } from "@mui/material";

import { GenericTableTanstack } from "@/components/common/GenericTableTanstack";

import { VeranstaltungList } from "@/api/types/veranstaltung/VeranstaltungList";

import { veranstaltungColumnsTanstack } from "./veranstaltungColumnsTanstack";

interface Props {
  data: VeranstaltungList[];

  selectedId: number | null;

  onSelect: (row: VeranstaltungList | null) => void;

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

export function VeranstaltungTable({
  data,
  selectedId,
  onSelect,
  sorting,
  onSortingChange,
}: Props) {
  return (
    <GenericTableTanstack
      data={data}
      columns={veranstaltungColumnsTanstack}
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
            <Typography fontWeight={600}>{row.name}</Typography>

            {row.aktiv && (
              <Box
                sx={{
                  px: 1,
                  py: 0.2,
                  borderRadius: 1,
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

          <Typography variant="body2" color="text.secondary">
            {row.typ}
            {" • "}
            {row.beginnDatum}
          </Typography>

          <Typography variant="body2">
            {row.vereinAbk}

            {(row.leiterName || row.leiterVorname) && (
              <Typography component="span" variant="body2" color="text.secondary">
                {" • Leitung: "}
                {row.leiterName}
                {row.leiterVorname ? `, ${row.leiterVorname}` : ""}
              </Typography>
            )}
          </Typography>
        </Box>
      )}
    />
  );
}
