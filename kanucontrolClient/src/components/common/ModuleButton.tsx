// src/components/common/ModuleButton.tsx

import { Button } from "@mui/material";
import { moduleColors, moduleHover, ModuleType } from "@/theme/moduleColors";
import { radius } from "@/theme/ui";

interface Props {
  label: string;
  moduleType: ModuleType;
  onClick?: () => void;
  disabled?: boolean;
}

export const ModuleButton: React.FC<Props> = ({ label, moduleType, onClick, disabled = false }) => {
  return (
    <Button
      fullWidth
      variant="contained"
      disabled={disabled}
      onClick={onClick}
      sx={{
        backgroundColor: moduleColors[moduleType],
        color: "#fff",
        fontSize: "1.1rem",
        py: 2,
        borderRadius: radius.dialog,
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
