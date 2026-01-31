// components/common/FormFeldDate.tsx
import { TextField } from "@mui/material";
import { normalizeGermanDate } from "@/utils/dateUtils";

interface FormFeldDateProps {
  label: string;
  value: string;
  onChange: (value: string) => void;
  disabled?: boolean;
}

export function FormFeldDate({ label, value, onChange, disabled = false }: FormFeldDateProps) {
  const safeValue = value ?? "";

  const handleBlur = () => {
    if (disabled) return;

    const normalized = normalizeGermanDate(safeValue);
    if (normalized !== null) {
      onChange(normalized);
    }
  };

  return (
    <TextField
      fullWidth
      size="small"
      label={label}
      value={safeValue}
      disabled={disabled}
      onChange={(e) => onChange(e.target.value)}
      onBlur={handleBlur}
      placeholder="TT.MM.JJJJ"
    />
  );
}
