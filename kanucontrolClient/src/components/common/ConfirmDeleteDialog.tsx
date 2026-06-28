// src/components/common/ConfirmDeleteDialog.tsx

import { Button, Typography } from "@mui/material";
import { ReactNode } from "react";

import BaseDialog from "@/components/common/BaseDialog";

interface ConfirmDeleteDialogProps {
  open: boolean;

  title?: ReactNode;

  description?: ReactNode;

  onClose: () => void;

  onConfirm: () => void;

  loading?: boolean;

  confirmLabel?: string;
}

const ConfirmDeleteDialog = ({
  open,
  title = "Löschen bestätigen",
  description,
  onClose,
  onConfirm,
  loading = false,
  confirmLabel = "Löschen",
}: ConfirmDeleteDialogProps) => {
  return (
    <BaseDialog
      open={open}
      title={title}
      onClose={onClose}
      loading={loading}
      maxWidth="xs"
      actions={
        <>
          <Button onClick={onClose} disabled={loading}>
            Abbrechen
          </Button>

          <Button
            color="error"
            variant="contained"
            onClick={onConfirm}
            disabled={loading}
          >
            {confirmLabel}
          </Button>
        </>
      }
    >
      {description && <Typography>{description}</Typography>}
    </BaseDialog>
  );
};

export default ConfirmDeleteDialog;