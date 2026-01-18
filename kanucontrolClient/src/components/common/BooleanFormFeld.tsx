// components/common/BooleanFormFeld.tsx
import { Switch, FormControlLabel } from "@mui/material";

interface BooleanFormFeldProps {
  label: string;
  value: boolean;
  disabled?: boolean;
  onChange: (value: boolean) => void;
}

export function BooleanFormFeld({
  label,
  value,
  disabled = false,
  onChange,
}: BooleanFormFeldProps) {
  return (
    <FormControlLabel
      label={label}
      control={
        <Switch
          checked={value}
          disabled={disabled}
          onChange={(e) => onChange(e.target.checked)}
        />
      }
    />
  );
}