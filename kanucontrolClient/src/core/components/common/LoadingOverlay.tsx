// src/components/common/LoadingOverlay.tsx

import {
  Box,
  CircularProgress,
  Stack,
  Typography,
} from "@mui/material";

interface LoadingOverlayProps {
  loading: boolean;

  minHeight?: number | string;

  size?: number;

  text?: string;
}

const LoadingOverlay = ({
  loading,
  minHeight = 120,
  size = 40,
  text = "Daten werden geladen...",
}: LoadingOverlayProps) => {
  if (!loading) {
    return null;
  }

  return (
    <Box
      sx={{
        display: "flex",
        justifyContent: "center",
        alignItems: "center",
        p: 4,
        minHeight,
      }}
    >
      <Stack spacing={2} alignItems="center">
        <CircularProgress size={size} />

        <Typography color="text.secondary">
          {text}
        </Typography>
      </Stack>
    </Box>
  );
};

export default LoadingOverlay;