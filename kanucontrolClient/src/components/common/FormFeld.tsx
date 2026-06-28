// components/common/FormFeld.tsx

import React from "react";
import { TextField, TextFieldProps } from "@mui/material";

type FormValue = string | number | boolean | null | undefined;

interface FormFeldProps
  extends Omit<
    TextFieldProps,
    "value" | "onChange"
  > {
  value: FormValue;

  onChange?: (value: string) => void;

  maxLength?: number;
}

const FormFeld: React.FC<FormFeldProps> = ({
  value,
  onChange,
  maxLength,
  ...props
}) => {
  const displayValue =
    typeof value === "boolean"
      ? value
        ? "Ja"
        : "Nein"
      : value ?? "";

  return (
    <TextField
      {...props}
      fullWidth={props.fullWidth ?? true}
      size={props.size ?? "small"}
      value={displayValue}
      inputProps={{
        maxLength,
        ...props.inputProps,
      }}
      onChange={
        onChange
          ? (e) => onChange(e.target.value)
          : undefined
      }
    />
  );
};

export default FormFeld;

export type { FormFeldProps };