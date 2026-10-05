// components/common/EnumSelectFeld.tsx
import {
    FormControl,
    InputLabel,
    Select,
    MenuItem,
  } from "@mui/material";
  
  interface EnumSelectFeldProps<T extends string> {
    label: string;
    value: T;
    options: readonly T[];
    disabled?: boolean;
    onChange: (value: T) => void;
  }
  
  export function EnumSelectFeld<T extends string>({
    label,
    value,
    options,
    disabled = false,
    onChange,
  }: EnumSelectFeldProps<T>) {
    return (
      <FormControl fullWidth size="small">
        <InputLabel>{label}</InputLabel>
        <Select
          label={label}
          value={value}
          disabled={disabled}
          onChange={(e) => onChange(e.target.value as T)}
        >
          {options.map((opt) => (
            <MenuItem key={opt} value={opt}>
              {opt}
            </MenuItem>
          ))}
        </Select>
      </FormControl>
    );
  }