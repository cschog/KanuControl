import { DatePicker } from "@mui/x-date-pickers";
import dayjs, { Dayjs } from "dayjs";

interface Props {
  label: string;
  value?: string;
  disabled?: boolean;
  minDate?: string;
  referenceDate?: string;
  onChange: (value: string | undefined) => void;
}

export function FormFeldDatePicker({ label, value, disabled, minDate, referenceDate, onChange }: Props) {
  const parsed = value ? dayjs(value) : null;

  return (
    <DatePicker
      label={label}
      value={parsed}
      disabled={disabled}
      minDate={minDate ? dayjs(minDate) : undefined}
      referenceDate={referenceDate ? dayjs(referenceDate) : undefined}
      format="DD.MM.YYYY"
      onChange={(d: Dayjs | null) => onChange(d?.isValid() ? d.format("YYYY-MM-DD") : undefined)}
      slotProps={{
        textField: {
          fullWidth: true,
          size: "small",
        },
      }}
    />
  );
}
