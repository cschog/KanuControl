// src/components/common/SearchField.tsx

import { TextField, InputAdornment, IconButton } from "@mui/material";
import SearchIcon from "@mui/icons-material/Search";
import ClearIcon from "@mui/icons-material/Clear";

interface SearchFieldProps {
  value: string;

  onChange: (value: string) => void;

  label?: string;

  placeholder?: string;

  autoFocus?: boolean;

  fullWidth?: boolean;
}

const SearchField = ({
  value,
  onChange,
  label = "Suche",
  placeholder,
  autoFocus = false,
  fullWidth = true,
}: SearchFieldProps) => {
  return (
    <TextField
      size="small"
      label={label}
      placeholder={placeholder}
      value={value}
      autoFocus={autoFocus}
      fullWidth={fullWidth}
      onChange={(e) => onChange(e.target.value)}
      slotProps={{
        input: {
          startAdornment: (
            <InputAdornment position="start">
              <SearchIcon fontSize="small" />
            </InputAdornment>
          ),

          endAdornment:
            value.length > 0 ? (
              <InputAdornment position="end">
                <IconButton
                  size="small"
                  onClick={() => onChange("")}
                >
                  <ClearIcon fontSize="small" />
                </IconButton>
              </InputAdornment>
            ) : undefined,
        },
      }}
    />
  );
};

export default SearchField;