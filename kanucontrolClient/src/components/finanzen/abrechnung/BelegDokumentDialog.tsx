import { Dialog, DialogTitle, DialogContent, DialogActions, Button } from "@mui/material";

import BelegDokumentPanel from "./BelegDokumentPanel";

interface Props {
  open: boolean;
  belegId: number;
  onClose: () => void;
}

export default function BelegDokumentDialog({ open, belegId, onClose }: Props) {
  return (
    <Dialog open={open} onClose={onClose} maxWidth="md" fullWidth>
      <DialogTitle>Belegdokumente</DialogTitle>

      <DialogContent dividers>
        <BelegDokumentPanel belegId={belegId} readOnly />
      </DialogContent>

      <DialogActions>
        <Button onClick={onClose}>Schließen</Button>
      </DialogActions>
    </Dialog>
  );
}
