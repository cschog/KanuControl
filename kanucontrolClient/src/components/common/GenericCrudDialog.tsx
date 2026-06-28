// src/components/common/GenericCrudDialog.tsx

import { ReactNode } from "react";
import { Button } from "@mui/material";

import BaseDialog from "@/components/common/BaseDialog";

interface GenericCrudDialogProps {
  open: boolean;

  title: ReactNode;

  children: ReactNode;

  onClose: () => void;

  onSave: () => void;

  loading?: boolean;

  saveLabel?: string;

  cancelLabel?: string;

  maxWidth?: "xs" | "sm" | "md" | "lg" | "xl";

  disableSave?: boolean;
}

const GenericCrudDialog = ({
  open,
  title,
  children,
  onClose,
  onSave,
  loading = false,
  saveLabel = "Speichern",
  cancelLabel = "Abbrechen",
  maxWidth = "sm",
  disableSave = false,
}: GenericCrudDialogProps) => {
  return (
    <BaseDialog
      open={open}
      title={title}
      onClose={onClose}
      loading={loading}
      maxWidth={maxWidth}
      actions={
        <>
          <Button onClick={onClose} disabled={loading}>
            {cancelLabel}
          </Button>

          <Button
            variant="contained"
            onClick={onSave}
            disabled={loading || disableSave}
          >
            {saveLabel}
          </Button>
        </>
      }
    >
      {children}
    </BaseDialog>
  );
};

export default GenericCrudDialog;