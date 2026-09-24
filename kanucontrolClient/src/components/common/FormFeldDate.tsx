import { TextField, Tooltip, Box } from "@mui/material";
import { normalizeGermanDate } from "@/utils/dateUtils";
import { DataFieldStatus } from "./FormFeld";

interface FormFeldDateProps {
  label: string;
  value: string;
  onChange: (value: string) => void;
  disabled?: boolean;

  dataStatus?: DataFieldStatus;
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
            "& .MuiOutlinedInput-notchedOutline": {
              borderColor: "error.main",
            },
          }),

        ...(dataStatus === "WARNING" &&
          !invalid && {
            "& .MuiOutlinedInput-notchedOutline": {
              borderColor: "warning.main",
            },
          }),
      }}
    />
  );

  if (!dataStatus || !dataStatusMessage) {
    return field;
  }

  return (
    <Tooltip title={dataStatusMessage} arrow placement="top">
      <Box sx={{ width: "100%" }}>{field}</Box>
    </Tooltip>
  );
}
