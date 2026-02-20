// src/components/common/ModuleButton.tsx

import { Button } from "@mui/material";
import { moduleColors, moduleHover, ModuleType } from "@/theme/moduleColors";

interface Props {
  label: string;
  moduleType: ModuleType;
  onClick?: () => void;
}

export const ModuleButton: React.FC<Props> = ({ label, moduleType, onClick }) => {
  return (
    <Button
      fullWidth
      variant="contained"
      onClick={onClick}
      sx={{
        backgroundColor: moduleColors[moduleType],
        color: "#fff",
        fontSize: "1.1rem",
        py: 2,
        borderRadius: 2,
        boxShadow: 2,
        "&:hover": {
          backgroundColor: moduleHover[moduleType],
        },
      }}
    >
      {label}
    </Button>
  );
};
