import React from "react";
import { Dialog, DialogTitle, DialogContent, DialogActions, Button } from "@mui/material";

interface ConfirmDeleteDialogProps {
  open: boolean;
  title?: string;
  description: string;
  onClose: () => void;
  onConfirm: () => void;
}

export const ConfirmDeleteDialog: React.FC<ConfirmDeleteDialogProps> = ({
  open,
  title = "Löschen bestätigen",
  description,
  onClose,
  onConfirm,
}) => {
  return (
    <Dialog open={open} onClose={onClose}>
      <DialogTitle>{title}</DialogTitle>

      <DialogContent>{description}</DialogContent>

      <DialogActions>
        <Button onClick={onClose}>Abbrechen</Button>
        <Button color="error" onClick={onConfirm}>
          Löschen
        </Button>
      </DialogActions>
    </Dialog>
  );
};
