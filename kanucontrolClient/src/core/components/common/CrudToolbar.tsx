// src/components/common/CrudToolbar.tsx

import { Box, Button, Stack, Typography } from "@mui/material";
import AddIcon from "@mui/icons-material/Add";

interface CrudToolbarProps {
  title: string;

  addLabel?: string;

  onAdd?: () => void;

  addDisabled?: boolean;

  children?: React.ReactNode;
}

const CrudToolbar = ({
  title,
  addLabel = "Neu",
  onAdd,
  addDisabled = false,
  children,
}: CrudToolbarProps) => {
  return (
    <Stack
      direction={{
        xs: "column",
        sm: "row",
      }}
      justifyContent="space-between"
      alignItems={{
        xs: "stretch",
        sm: "center",
      }}
      spacing={2}
      sx={{
        mb: 2,
      }}
    >
      <Typography
        variant="h5"
        sx={{
          fontWeight: 700,

          fontSize: {
            xs: "1.3rem",
            md: "1.6rem",
          },
        }}
      >
        {title}
      </Typography>

      <Box
        sx={{
          display: "flex",
          gap: 1,
          flexWrap: "wrap",
        }}
      >
        {children}

        {onAdd && (
          <Button
            variant="contained"
            startIcon={<AddIcon />}
            onClick={onAdd}
            disabled={addDisabled}
          >
            {addLabel}
          </Button>
        )}
      </Box>
    </Stack>
  );
};

export default CrudToolbar;