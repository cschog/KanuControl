// src/components/common/EmptyState.tsx

import { Box, Stack, Typography } from "@mui/material";

interface EmptyStateProps {
  title?: string;
  description?: string;
  icon?: React.ReactNode;
  minHeight?: number | string;
  action?: React.ReactNode;
}

const EmptyState = ({
  title = "Keine Daten vorhanden",
  description,
  icon,
  minHeight = 140,
  action,
}: EmptyStateProps) => {
  return (
    <Box
      sx={{
        minHeight,
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        p: 4,
      }}
    >
      <Stack
        spacing={2}
        alignItems="center"
        sx={{
          textAlign: "center",
          color: "text.secondary",
          maxWidth: 500,
        }}
      >
        {icon}

        <Typography variant="h6" fontWeight={600} color="error.main">
          {title}
        </Typography>

        {description && <Typography variant="body2">{description}</Typography>}

        {action}
      </Stack>
    </Box>
  );
};

export default EmptyState;