import React from "react";
import { TextField, TextFieldProps, Tooltip, Box } from "@mui/material";

type FormValue = string | number | boolean | null | undefined;

export type DataFieldStatus = "ERROR" | "WARNING";

interface FormFeldProps extends Omit<TextFieldProps, "value" | "onChange"> {
  value: FormValue;
  onChange?: (value: string) => void;
  maxLength?: number;

  /**
   * Kontextbezogener Datenstatus.
   */
  dataStatus?: DataFieldStatus;

  /**
   * Erklärung zum Datenstatus.
   */
  dataStatusMessage?: string;
}

const FormFeld: React.FC<FormFeldProps> = ({
  value,
  onChange,
  maxLength,
  dataStatus,
  dataStatusMessage,
  ...props
}) => {
  const displayValue = typeof value === "boolean" ? (value ? "Ja" : "Nein") : (value ?? "");

  const hasDataStatus = !!dataStatus;

  const field = (
    <TextField
      {...props}
      fullWidth={props.fullWidth ?? true}
      size={props.size ?? "small"}
      value={displayValue}
      inputProps={{
        maxLength,
        ...props.inputProps,
      }}
      onChange={onChange ? (e) => onChange(e.target.value) : undefined}
      sx={{
        ...props.sx,

        ...(dataStatus === "ERROR" && {
          "& .MuiOutlinedInput-root": {
            backgroundColor: "rgba(209, 3, 3, 0.18)",
          },
          "& .MuiInputLabel-root": {
            color: "error.main",
          },
        }),

        ...(dataStatus === "WARNING" && {
          "& .MuiOutlinedInput-root": {
            backgroundColor: "rgba(244, 200, 4, 0.3)",
          },
          "& .MuiInputLabel-root": {
            color: "warning.main",
          },
        }),
      }}
    />
  );

  if (!hasDataStatus || !dataStatusMessage) {
    return field;
  }

  return (
    <Tooltip
      title={dataStatusMessage}
      arrow
      placement="top"
      slotProps={{
        tooltip: {
          sx: {
            fontSize: "0.95rem",
            lineHeight: 1.4,
            maxWidth: 360,
            padding: "10px 14px",
          },
        },
        arrow: {
          sx: {
            fontSize: "1rem",
          },
        },
      }}
    >
      <Box sx={{ width: "100%" }}>{field}</Box>
    </Tooltip>
  );
};

export default FormFeld;

export type { FormFeldProps };
