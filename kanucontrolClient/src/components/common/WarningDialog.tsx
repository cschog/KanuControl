import React from "react";
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  Typography,
} from "@mui/material";

interface WarningDialogProps {
  open: boolean;
  title?: string;
  warnings: string[];
  onClose: () => void;
}

export const WarningDialog: React.FC<WarningDialogProps> = ({
  open,
  title = "Hinweis",
  warnings,
  onClose,
}) => {
  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>{title}</DialogTitle>

      <DialogContent>
        {warnings.map((warning, index) => (
          <Typography key={index} sx={{ mb: 1 }}>
            • {warning}
          </Typography>
        ))}
      </DialogContent>

      <DialogActions>
        <Button onClick={onClose} autoFocus>
          OK
        </Button>
      </DialogActions>
    </Dialog>
  );
};
