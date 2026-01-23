// components/common/TextFormFeld.tsx
import { TextField } from "@mui/material";

interface TextFormFeldProps {
  label: string;
  value: string;
  disabled?: boolean;
  onChange: (value: string) => void;
}

export function TextFormFeld({
  label,
  value,
  disabled = false,
  onChange,
}: TextFormFeldProps) {
  return (
    <TextField
      fullWidth
      size="small"
      label={label}
      value={value ?? ""}
      disabled={disabled}
      onChange={(e) => onChange(e.target.value)}
    />
  );
}