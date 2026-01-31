import { PersonRef } from "@/api/types/PersonRef";
import { Dialog, DialogTitle, DialogContent, Button } from "@mui/material";

interface Props {
  open: boolean;
  persons: PersonRef[];
  onSelect: (p: PersonRef) => void;
  onClose: () => void;
}

export function KontoinhaberDialog({ open, persons, onSelect, onClose }: Props) {
  return (
    <Dialog open={open} onClose={onClose}>
      <DialogTitle>Kontoinhaber auswählen</DialogTitle>
      <DialogContent>
        {persons.map((p) => (
          <Button
            key={p.id}
            fullWidth
            onClick={() => {
              onSelect(p);
              onClose();
            }}
          >
            {p.name}, {p.vorname}
          </Button>
        ))}
      </DialogContent>
    </Dialog>
  );
}
