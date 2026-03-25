import { Box, Button, TextField, MenuItem } from "@mui/material";
import { PersonFilter } from "@/api/types/PersonFilter";

interface Props {
  filters: PersonFilter;
  onChange: (filters: PersonFilter) => void;
  onReset?: () => void;
}

export function PersonFilterBar({ filters, onChange, onReset }: Props) {
  return (
    <Box display="flex" gap={2} flexWrap="wrap" mb={2}>
      <TextField
        label="Name / Vorname"
        size="small"
        value={filters.search ?? ""}
        onChange={(e) => onChange({ ...filters, search: e.target.value })}
      />

      <TextField
        label="Verein"
        size="small"
        value={filters.verein ?? ""}
        onChange={(e) => onChange({ ...filters, verein: e.target.value })}
      />

      <TextField
        select
        label="Aktiv"
        size="small"
        value={filters.aktiv === undefined ? "" : filters.aktiv ? "true" : "false"}
        onChange={(e) =>
          onChange({
            ...filters,
            aktiv: e.target.value === "" ? undefined : e.target.value === "true",
          })
        }
      >
        <MenuItem value="">Alle</MenuItem>
        <MenuItem value="true">Aktiv</MenuItem>
        <MenuItem value="false">Inaktiv</MenuItem>
      </TextField>

      <Button size="small" onClick={onReset}>
        Reset
      </Button>
    </Box>
  );
}
