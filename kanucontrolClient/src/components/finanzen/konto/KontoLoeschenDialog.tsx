import {
  Alert,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Typography,
} from "@mui/material";

interface Props {
  open: boolean;

  kuerzel?: string;

  error: string | null;

  onClose: () => void;

  onConfirm: () => void;
}

export default function KontoLoeschenDialog({ open, kuerzel, error, onClose, onConfirm }: Props) {
  return (
    <Dialog open={open} onClose={onClose}>
      <DialogTitle>Konto löschen</DialogTitle>

      <DialogContent>
        {error ? (
          <Alert severity="error">{error}</Alert>
        ) : (
          <Typography>
            Möchten Sie das Konto <strong>{kuerzel}</strong> wirklich löschen?
          </Typography>
        )}
      </DialogContent>

      <DialogActions>
        <Button onClick={onClose}>Abbrechen</Button>

        {!error && (
          <Button color="error" variant="contained" onClick={onConfirm}>
            Löschen
          </Button>
        )}
      </DialogActions>
    </Dialog>
  );
}
