// components/common/FormFeld.tsx
import { TextField } from "@mui/material";

type FormValue = string | number | boolean | null | undefined;

interface FormFeldProps {
  label: string;
  value: FormValue;
  disabled?: boolean;
  onChange?: (value: string) => void;
}

export const FormFeld: React.FC<FormFeldProps> = ({
  label,
  value,
  disabled = false,
  onChange,
}) => {
  const displayValue =
    typeof value === "boolean"
      ? value ? "Ja" : "Nein"
      : value ?? "";

  return (
    <TextField
      fullWidth
      size="small"
      label={label}
      value={displayValue}
      disabled={disabled}
      onChange={
        onChange
          ? (e) => onChange(e.target.value)
          : undefined
      }
    />
  );
};