import { TextField } from "@mui/material";
import { normalizeGermanDate } from "@/utils/dateUtils";

interface FormFeldDateProps {
  label: string;
  value: string;
  onChange: (value: string) => void;
}

export function FormFeldDate({ label, value, onChange }: FormFeldDateProps) {
  const handleBlur = () => {
    const normalized = normalizeGermanDate(value);
    if (normalized) {
      onChange(normalized);
    }
  };

  return (
    <TextField
      fullWidth
      label={label}
      value={value}
      onChange={(e) => onChange(e.target.value)}
      onBlur={handleBlur}
      placeholder="TT.MM.JJJJ"
      helperText="z.B. 28.08.1955 oder 28.8.55"
      size="small"
    />
  );
}
