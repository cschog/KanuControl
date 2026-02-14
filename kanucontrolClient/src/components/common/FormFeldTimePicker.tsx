import { TimePicker } from "@mui/x-date-pickers";
import dayjs, { Dayjs } from "dayjs";

interface Props {
  label: string;
  value?: string;
  disabled?: boolean;
  onChange: (value: string | undefined) => void;
}

export function FormFeldTimePicker({ label, value, disabled, onChange }: Props) {
  const parsed = value ? dayjs(`2000-01-01T${value}`) : null;

  return (
    <TimePicker
      label={label}
      value={parsed}
      disabled={disabled}
      ampm={false}
      onChange={(t: Dayjs | null) => onChange(t ? t.format("HH:mm") : undefined)}
      slotProps={{
        textField: {
          fullWidth: true,
          size: "small",
        },
      }}
    />
  );
}
