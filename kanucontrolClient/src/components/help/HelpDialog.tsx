// src/components/help/HelpDialog.tsx

import { useEffect, useState } from "react";

import { Dialog, DialogTitle, DialogContent, IconButton } from "@mui/material";

import CloseIcon from "@mui/icons-material/Close";

import ReactMarkdown from "react-markdown";

type Props = {
  open: boolean;
  onClose: () => void;
};

export const HelpDialog = ({ open, onClose }: Props) => {
  const [markdown, setMarkdown] = useState("");

  useEffect(() => {
    if (!open) return;

    fetch("/help/bedienungsanleitung.md")
      .then((r) => r.text())
      .then(setMarkdown)
      .catch(() => setMarkdown("# Fehler\n\nDie Bedienungsanleitung konnte nicht geladen werden."));
  }, [open]);

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>
        Bedienungsanleitung
        <IconButton
          onClick={onClose}
          sx={{
            position: "absolute",
            right: 8,
            top: 8,
          }}
        >
          <CloseIcon />
        </IconButton>
      </DialogTitle>

      <DialogContent dividers>
        <ReactMarkdown>{markdown}</ReactMarkdown>
      </DialogContent>
    </Dialog>
  );
};
