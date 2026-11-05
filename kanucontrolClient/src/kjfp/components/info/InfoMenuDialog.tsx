import {
  Dialog,
  DialogTitle,
  DialogContent,
  List,
  ListItemButton,
  ListItemText,
} from "@mui/material";

import type { InfoPage } from "@/kjfp/api/enums/InfoPage";

type Props = {
  open: boolean;
  onClose: () => void;
  onSelect: (page: InfoPage) => void;
};

export function InfoMenuDialog({ open, onClose, onSelect }: Props) {
  const handleClick = (page: InfoPage) => {
    onSelect(page);
    onClose();
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>❓ Hilfe & Info</DialogTitle>

      <DialogContent>
        <List>
          <ListItemButton onClick={() => handleClick("news")}>
            <ListItemText primary="📢 Was ist neu?" />
          </ListItemButton>

          <ListItemButton onClick={() => handleClick("help")}>
            <ListItemText primary="Hilfeübersicht" />
          </ListItemButton>

          <ListItemButton onClick={() => handleClick("help-kjfp")}>
            <ListItemText primary="Bedienungsanleitung KJFP" />
          </ListItemButton>

          <ListItemButton onClick={() => handleClick("help-vermietung")}>
            <ListItemText primary="Bedienungsanleitung Vermietung" />
          </ListItemButton>

          <ListItemButton onClick={() => handleClick("help-fahrten")}>
            <ListItemText primary="Bedienungsanleitung Fahrten" />
          </ListItemButton>

          <ListItemButton onClick={() => handleClick("homepage")}>
            <ListItemText primary="Homepage-Anbindung" />
          </ListItemButton>

          <ListItemButton onClick={() => handleClick("privacy")}>
            <ListItemText primary="🔒 Datenschutz" />
          </ListItemButton>

          <ListItemButton onClick={() => handleClick("imprint")}>
            <ListItemText primary="⚖️ Impressum" />
          </ListItemButton>

          <ListItemButton onClick={() => handleClick("about")}>
            <ListItemText primary="ℹ️ Über KanuControl" />
          </ListItemButton>
        </List>
      </DialogContent>
    </Dialog>
  );
}
