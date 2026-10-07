// src/vermietung/components/mietobjekte/mietbereiche/mietbereichEigenschaft/MietbereichEigenschaftTable.tsx
import { Box, Typography } from "@mui/material";

import { GenericTableTanstack } from "@/core/components/table/GenericTableTanstack";

import type { MietbereichEigenschaft } from "@/vermietung/types/MietbereichEigenschaft";

import { mietbereichEigenschaftColumnsTanstack } from "./mietbereichEigenschaftColumnsTanstack";

interface Props {
  data: MietbereichEigenschaft[];

  selectedId: number | null;

  onSelect: (row: MietbereichEigenschaft | null) => void;

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

export function MietbereichEigenschaftTable({
  data,
  selectedId,
  onSelect,
  sorting,
  onSortingChange,
}: Props) {
  return (
    <GenericTableTanstack
      data={data}
      columns={mietbereichEigenschaftColumnsTanstack}
      selectedRowId={selectedId}
      onSelectRow={(row) => onSelect(row ?? null)}
      sorting={sorting}
      onSortingChange={onSortingChange}
      mobileRenderRow={(row) => (
        <Box>
          <Typography fontWeight={600}>{row.bezeichnung}</Typography>

          <Typography variant="body2" color="text.secondary">
            {formatWert(row)}
          </Typography>
        </Box>
      )}
    />
  );
}

function formatWert(eigenschaft: MietbereichEigenschaft): string {
  switch (eigenschaft.typ) {
    case "FLAECHE":
      return `${eigenschaft.wert} m²`;

    case "HOEHE":
    case "LAENGE":
      return `${Number(eigenschaft.wert).toLocaleString("de-DE")} m`;

    case "BOOLEAN":
      return eigenschaft.wert === "true" ? "Ja" : "Nein";

    default:
      return eigenschaft.wert;
  }
}
