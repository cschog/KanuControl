// components/common/FormFeld.tsx
import React from "react";
import { TextField } from "@mui/material";

type FormValue = string | number | boolean | null | undefined;

interface FormFeldProps {
  label: string;
  value: FormValue;
  disabled?: boolean;
  onChange?: (value: string) => void;
  type?: React.InputHTMLAttributes<HTMLInputElement>["type"];
  helperText?: string;
}

export const FormFeld: React.FC<FormFeldProps> = ({
  label,
  value,
  disabled = false,
  onChange,
  type = "text",
  helperText,
}) => {
  const displayValue = typeof value === "boolean" ? (value ? "Ja" : "Nein") : value ?? "";

  return (
    <TextField
      fullWidth
      size="small"
      label={label}
      value={displayValue}
      disabled={disabled}
      type={type}
      helperText={helperText}
      onChange={onChange ? (e) => onChange(e.target.value) : undefined}
    />
  );
};
