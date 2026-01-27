import { TextField, MenuItem } from "@mui/material";
import { COUNTRIES } from "@/api/enums/CountryCode";
import type { CountryCode } from "@/api/enums/CountryCode";

interface CountrySelectFeldProps {
  label: string;
  value?: CountryCode;
  onChange: (value: CountryCode | undefined) => void;
}

export function CountrySelectFeld({ label, value, onChange }: CountrySelectFeldProps) {
  return (
    <TextField
      select
      fullWidth
      size="small"
      label={label}
      value={value ?? ""} // UI darf ""
      onChange={(e) =>
        onChange(e.target.value === "" ? undefined : (e.target.value as CountryCode))
      }
    >
      <MenuItem value="">– bitte wählen –</MenuItem>

      {COUNTRIES.map((c) => (
        <MenuItem key={c.code} value={c.code}>
          {c.label}
        </MenuItem>
      ))}
    </TextField>
  );
}