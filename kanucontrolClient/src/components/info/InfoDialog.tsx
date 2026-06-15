// src/components/info/InfoDialog.tsx

import { useEffect, useState } from "react";
import { Dialog, DialogTitle, DialogContent, IconButton } from "@mui/material";
import CloseIcon from "@mui/icons-material/Close";
import ReactMarkdown from "react-markdown";

const PAGE_CONFIG = {
  help: {
    title: "Bedienungsanleitung",

    file: "/info/bedienungsanleitung.md",
  },

  news: {
    title: "Was ist neu?",

    file: "/info/releases/v0.9.md",
  },

  privacy: {
    title: "Datenschutz",

    file: "/info/datenschutz.md",
  },

  imprint: {
    title: "Impressum",

    file: "/info/impressum.md",
  },

  about: {
    title: "Über KanuControl",

    file: "/info/about.md",
  },
} as const;

type Props = {
  open: boolean;

  onClose: () => void;

  page: "help" | "news" | "privacy" | "imprint" | "about";
};

export const InfoDialog = ({ open, onClose, page }: Props) => {
  const [markdown, setMarkdown] = useState("");

  const config = PAGE_CONFIG[page];

useEffect(() => {
  if (!open) return;

  fetch(config.file)
    .then((r) => r.text())
    .then(setMarkdown)
    .catch(() => setMarkdown("# Fehler\n\nDie Datei konnte nicht geladen werden."));
}, [open, config.file]);

  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>
        {config.title}

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
