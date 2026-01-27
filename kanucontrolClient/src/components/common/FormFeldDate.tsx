import { TextField } from "@mui/material";
import { normalizeGermanDate } from "@/utils/dateUtils";

interface FormFeldDateProps {
  label: string;
  value: string;
  onChange: (value: string) => void;
}

export function FormFeldDate({ label, value, onChange }: FormFeldDateProps) {
  const safeValue = value ?? ""; // 🔑 HIER

  const handleBlur = () => {
    const normalized = normalizeGermanDate(safeValue);
    if (normalized !== null) {
      onChange(normalized);
    }
  };

  return (
    <TextField
      fullWidth
      label={label}
      value={safeValue} // 🔑 NIE null
      onChange={(e) => onChange(e.target.value)}
      onBlur={handleBlur}
      placeholder="TT.MM.JJJJ"
      helperText="z.B. 28.08.1955 oder 28.8.55"
      size="small"
    />
  );
}