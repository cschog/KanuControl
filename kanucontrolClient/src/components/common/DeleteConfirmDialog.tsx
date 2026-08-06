import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  DialogActions,
  Button,
} from "@mui/material";
import { ReactNode } from "react";

interface Props {
  open: boolean;
  title?: string;
  message?: string;
  onClose: () => void;
  onConfirm: () => void;
  children?: ReactNode;
}

const DeleteConfirmDialog = ({
  open,
  title = "Eintrag löschen",
  message = "Soll dieser Eintrag wirklich gelöscht werden?",
  onClose,
  onConfirm,
  children,
}: Props) => {
  return (
    <Dialog open={open} onClose={onClose} maxWidth="xs" fullWidth>
      <DialogTitle>{title}</DialogTitle>

      <DialogContent>{children ?? <DialogContentText>{message}</DialogContentText>}</DialogContent>

      <DialogActions>
        <Button onClick={onClose}>Abbrechen</Button>

        <Button color="error" variant="contained" onClick={onConfirm}>
          Löschen
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default DeleteConfirmDialog;
