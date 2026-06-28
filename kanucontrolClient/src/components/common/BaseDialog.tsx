// src/components/common/BaseDialog.tsx

import { ReactNode } from "react";

import {
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  useMediaQuery,
} from "@mui/material";

import { useTheme } from "@mui/material/styles";

interface BaseDialogProps {
  open: boolean;

  title: ReactNode;

  children: ReactNode;

  actions?: ReactNode;

  onClose: () => void;

  loading?: boolean;

  maxWidth?: "xs" | "sm" | "md" | "lg" | "xl";
}

const BaseDialog = ({
  open,
  title,
  children,
  actions,
  onClose,
  loading = false,
  maxWidth = "sm",
}: BaseDialogProps) => {
  const theme = useTheme();

  const fullScreen = useMediaQuery(
    theme.breakpoints.down("sm"),
  );

  return (
    <Dialog
      open={open}
      fullScreen={fullScreen}
      fullWidth
      maxWidth={maxWidth}
      disableEscapeKeyDown={loading}
      onClose={(_, reason) => {
        if (
          loading &&
          (reason === "backdropClick" ||
            reason === "escapeKeyDown")
        ) {
          return;
        }

        onClose();
      }}
    >
      <DialogTitle
        sx={{
          fontSize: {
            xs: "1.2rem",
            md: "1.4rem",
          },

          fontWeight: 700,

          pb: 1,
        }}
      >
        {title}
      </DialogTitle>

      <DialogContent
        sx={{
          pt: 2,

          px: {
            xs: 2,
            md: 3,
          },

          "& .MuiTextField-root": {
            mt: 1,

            "& .MuiInputBase-input": {
              fontSize: {
                xs: "1rem",
                md: "1rem",
              },
            },

            "& .MuiInputLabel-root": {
              fontSize: {
                xs: "1rem",
                md: "1rem",
              },
            },
          },
        }}
      >
        {children}
      </DialogContent>

      {actions && (
        <DialogActions
          sx={{
            px: {
              xs: 2,
              md: 3,
            },

            pb: {
              xs: 2,
              md: 2,
            },

            gap: 1,

            flexDirection: {
              xs: "column-reverse",
              sm: "row",
            },

            "& .MuiButton-root": {
              minWidth: {
                xs: "100%",
                sm: 120,
              },

              fontSize: "0.95rem",

              fontWeight: 600,
            },
          }}
        >
          {actions}
        </DialogActions>
      )}
    </Dialog>
  );
};

export default BaseDialog;