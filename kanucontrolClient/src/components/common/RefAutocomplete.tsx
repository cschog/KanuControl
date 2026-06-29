// src/components/common/RefAutocomplete.tsx

import { Autocomplete, TextField } from "@mui/material";

export interface BaseRef {
  id: number;
  bezeichnung: string;
}

interface RefAutocompleteProps<T extends BaseRef> {
  options: T[];

  value?: T | null;

  onChange: (value: T | null) => void;

  label: string;

  disabled?: boolean;

  required?: boolean;

  helperText?: string;

  autoFocus?: boolean;

  loading?: boolean;
}

export function RefAutocomplete<T extends BaseRef>({
  options,
  value,
  onChange,
  label,
  disabled = false,
  required = false,
  helperText,
  autoFocus = false,
  loading = false,
}: RefAutocompleteProps<T>) {
  return (
    <Autocomplete<T>
      options={options}
      value={value ?? null}
      loading={loading}
      disabled={disabled}
      fullWidth
      isOptionEqualToValue={(a, b) => a.id === b.id}
      getOptionLabel={(option) => option.bezeichnung}
      onChange={(_, value) => onChange(value)}
      renderInput={(params) => (
        <TextField
          {...params}
          label={label}
          size="small"
          required={required}
          helperText={helperText}
          autoFocus={autoFocus}
        />
      )}
    />
  );
}