// components/common/TextFormFeld.tsx
import { TextField } from "@mui/material";

interface TextFormFeldProps {
  label: string;
  value?: string; // ✅ WICHTIG
  onChange?: (value: string) => void;
  disabled?: boolean;
}

export function TextFormFeld({ label, value, onChange, disabled = false }: TextFormFeldProps) {
  return (
    <TextField
      fullWidth
      size="small"
      label={label}
      value={value ?? ""} // ✅ NIE undefined ins TextField
      disabled={disabled}
      onChange={(e) => onChange?.(e.target.value)}
    />
  );
}
