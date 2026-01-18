// components/common/FormGrid.tsx
import { Box } from "@mui/material";
import { ReactNode } from "react";

export function FormGrid({ children }: { children: ReactNode }) {
  return (
    <Box
      display="grid"
      gridTemplateColumns={{
        xs: "1fr",
        sm: "1fr 1fr",
        md: "1fr 1fr 1fr",
      }}
      gap={2}
    >
      {children}
    </Box>
  );
}