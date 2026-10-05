import { DatePicker } from "@mui/x-date-pickers";
import dayjs, { Dayjs } from "dayjs";

interface Props {
  label: string;
  value?: string;
  disabled?: boolean;
  onChange: (value: string | undefined) => void;
}

export function FormFeldDatePicker({ label, value, disabled, onChange }: Props) {
  const parsed = value ? dayjs(value) : null;

  return (
    <DatePicker
      label={label}
      value={parsed}
      disabled={disabled}
      format="DD.MM.YYYY"
      onChange={(d: Dayjs | null) => onChange(d ? d.format("YYYY-MM-DD") : undefined)}
      slotProps={{
        textField: {
          fullWidth: true,
          size: "small",
        },
      }}
    />
  );
}
