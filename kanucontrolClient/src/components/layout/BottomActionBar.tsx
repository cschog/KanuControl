import { Stack, Button, Paper, useMediaQuery, useTheme } from "@mui/material";

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
  
    const theme = useTheme();

    const isMobile = useMediaQuery(theme.breakpoints.down("sm"));
  
  return (
    <Paper
      elevation={3}
      sx={{
        position: "sticky",
        bottom: 0,
        zIndex: 100,
        mt: isMobile ? 1 : 4,
        px: isMobile ? 1 : 2,
        py: isMobile ? 0.75 : 1.5,
        borderTop: 1,
        borderColor: "divider",
        bgcolor: "background.paper",
      }}
    >
      <Stack
        direction={isMobile ? "column" : "row"}
        justifyContent="space-between"
        alignItems={isMobile ? "stretch" : "center"}
        spacing={isMobile ? 1 : 0}
      >
        <Stack
          direction={isMobile ? "column" : "row"}
          spacing={1}
          width={isMobile ? "100%" : "auto"}
        >
          {left.map((a, i) => (
            <Button
              fullWidth={isMobile}
              key={i}
              size={isMobile ? "small" : "medium"}
              sx={{
                minWidth: isMobile ? 0 : undefined,
              }}
              variant={a.variant ?? "outlined"}
              color={a.color ?? "primary"}
              onClick={a.onClick}
              disabled={a.disabled}
            >
              {a.label}
            </Button>
          ))}
        </Stack>

        <Stack
          direction={isMobile ? "column" : "row"}
          spacing={1}
          width={isMobile ? "100%" : "auto"}
        >
          {right.map((a, i) => (
            <Button
              key={i}
              fullWidth={isMobile}
              size={isMobile ? "small" : "medium"}
              sx={{
                minWidth: isMobile ? 0 : undefined,
              }}
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
