import { TextField, Tooltip, Box } from "@mui/material";
import { normalizeGermanDate } from "@/utils/dateUtils";
import type { FieldStatus } from "./FormFeld";

interface FormFeldDateProps {
  label: string;
  value: string;
  onChange: (value: string) => void;
  disabled?: boolean;

  dataStatus?: FieldStatus;
  dataStatusMessage?: string;
}

export function FormFeldDate({
  label,
  value,
  onChange,
  disabled = false,
  dataStatus,
  dataStatusMessage,
}: FormFeldDateProps) {
  const safeValue = value ?? "";

  const invalid = safeValue.trim() !== "" && normalizeGermanDate(safeValue) === null;

  const handleBlur = () => {
    if (disabled) return;

    const normalized = normalizeGermanDate(safeValue);

    if (normalized !== null) {
      onChange(normalized);
    }
  };

  const field = (
    <TextField
      fullWidth
      size="small"
      label={label}
      value={safeValue}
      disabled={disabled}
      onChange={(e) => onChange(e.target.value)}
      onBlur={handleBlur}
      placeholder="TT.MM.JJJJ"
      error={invalid}
      helperText={invalid ? "Ungültiges Datum" : ""}
      sx={{
        ...(dataStatus === "ERROR" &&
          !invalid && {
            "& .MuiOutlinedInput-root": {
              backgroundColor: "rgba(209, 3, 3, 0.18)",
            },
            "& .MuiInputLabel-root": {
              color: "error.main",
            },
          }),

        ...(dataStatus === "WARNING" &&
          !invalid && {
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

  if (!dataStatus || !dataStatusMessage) {
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
}
