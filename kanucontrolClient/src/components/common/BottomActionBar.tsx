import { Stack, Button } from "@mui/material";

export interface BottomAction {
  label: string;
  onClick: () => void;
  disabled?: boolean;
  variant?: "text" | "outlined" | "contained";
  color?: "primary" | "secondary" | "error";
}

interface BottomActionBarProps {
  left?: BottomAction[];
  right?: BottomAction[];
}

export function BottomActionBar({ left = [], right = [] }: BottomActionBarProps) {
  return (
    <Stack direction="row" justifyContent="space-between" alignItems="center" sx={{ mt: 3 }}>
      <Stack direction="row" spacing={1}>
        {left.map((a, i) => (
          <Button
            key={i}
            variant={a.variant ?? "outlined"}
            color={a.color ?? "primary"}
            onClick={a.onClick}
            disabled={a.disabled}
          >
            {a.label}
          </Button>
        ))}
      </Stack>

      <Stack direction="row" spacing={1}>
        {right.map((a, i) => (
          <Button
            key={i}
            variant={a.variant ?? "contained"}
            color={a.color ?? "primary"}
            onClick={a.onClick}
            disabled={a.disabled}
          >
            {a.label}
          </Button>
        ))}
      </Stack>
    </Stack>
  );
}
