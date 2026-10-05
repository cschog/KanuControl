import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Typography,
} from "@mui/material";

interface Props {
  open: boolean;

  name?: string;

  onClose: () => void;

  onConfirm: () => void;
}

export default function TeilnehmerEntfernenDialog({ open, name, onClose, onConfirm }: Props) {
  return (
    <Dialog open={open} onClose={onClose}>
      <DialogTitle>Teilnehmer entfernen</DialogTitle>

      <DialogContent>
        <Typography>
          Möchten Sie <strong>{name}</strong> wirklich aus diesem Konto entfernen?
        </Typography>
      </DialogContent>

      <DialogActions>
        <Button onClick={onClose}>Abbrechen</Button>

        <Button color="error" variant="contained" onClick={onConfirm}>
          Entfernen
        </Button>
      </DialogActions>
    </Dialog>
  );
}
