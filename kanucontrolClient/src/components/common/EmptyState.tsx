// src/components/common/EmptyState.tsx

import { Box, Stack, Typography } from "@mui/material";
import InboxOutlinedIcon from "@mui/icons-material/InboxOutlined";

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
  icon = <InboxOutlinedIcon sx={{ fontSize: 64 }} color="disabled" />,
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

        <Typography
          variant="h6"
          fontWeight={600}
        >
          {title}
        </Typography>

        {description && (
          <Typography variant="body2">
            {description}
          </Typography>
        )}

        {action}
      </Stack>
    </Box>
  );
};

export default EmptyState;