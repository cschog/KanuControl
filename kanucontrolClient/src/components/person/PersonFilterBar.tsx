import { Box, Button, TextField, MenuItem, Select, FormControl, InputLabel } from "@mui/material";
import { VereinRef } from "@/api/types/VereinRef";
import { PersonFilterState } from "@/components/person/Personen";


interface Props {
  filters: PersonFilterState;
  vereine: VereinRef[];
  onChange: (filters: PersonFilterState) => void;
  onReset?: () => void;
}

type AktivSelectValue = "" | "true" | "false";

export function PersonFilterBar({ filters, vereine, onChange, onReset }: Props) {
  return (
    <Box display="flex" gap={2} flexWrap="wrap" mb={2}>
      <TextField
        label="Name"
        size="small"
        value={filters.name ?? ""}
        onChange={(e) => onChange({ ...filters, name: e.target.value })}
      />

      <TextField
        label="Vorname"
        size="small"
        value={filters.vorname ?? ""}
        onChange={(e) => onChange({ ...filters, vorname: e.target.value })}
      />

      <FormControl size="small" sx={{ minWidth: 160 }}>
        <InputLabel>Verein</InputLabel>
        <Select
          label="Verein"
          value={filters.vereinId ?? ""}
          onChange={(e) =>
            onChange({
              ...filters,
              vereinId: e.target.value || undefined,
            })
          }
        >
          <MenuItem value="">Alle</MenuItem>
          {vereine.map((v) => (
            <MenuItem key={v.id} value={v.id}>
              {v.name}
            </MenuItem>
          ))}
        </Select>
      </FormControl>

      <FormControl size="small" sx={{ minWidth: 140 }}>
        <InputLabel>Aktiv</InputLabel>
        <Select<AktivSelectValue>
          label="Aktiv"
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
        </Select>
      </FormControl>
      <Button size="small" onClick={onReset}>
        Reset
      </Button>
    </Box>
  );
}
