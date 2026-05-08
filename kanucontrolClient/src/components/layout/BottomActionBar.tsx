import { Stack, Button, Paper } from "@mui/material";

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
    <Paper
      elevation={3}
      sx={{
        position: "sticky",
        bottom: 0,
        zIndex: 100,
        mt: 4,
        px: 2,
        py: 1.5,
        borderTop: 1,
        borderColor: "divider",
        bgcolor: "background.paper",
      }}
    >
      <Stack direction="row" justifyContent="space-between" alignItems="center">
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
    </Paper>
  );
}
