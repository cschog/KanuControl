import React, { useState } from "react";

import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  MenuItem,
  TextField,
} from "@mui/material";

import apiClient from "@/api/client/apiClient";
import { VereinRef } from "@/api/types/verein/VereinRef";
import { getApiErrorMessage } from "@/api/utils/apiError";
import { ErrorDialog } from "@/components/common/ErrorDialog";

interface AddMembershipDialogProps {
  open: boolean;

  onClose: () => void;

  personId: number;

  availableVereine: VereinRef[];

  onAdded: () => Promise<void>;
}

export const AddMembershipDialog: React.FC<AddMembershipDialogProps> = ({
  open,
  onClose,
  personId,
  availableVereine,
  onAdded,
}) => {
  const [selectedVereinId, setSelectedVereinId] = useState<number | "">("");

  const [error, setError] = useState<string | null>(null);

  /* ========================================================= */

  const handleAdd = async () => {
    try {
      setError(null);

      await apiClient.post("/mitglied", {
        personId,
        vereinId: selectedVereinId,
      });

      setSelectedVereinId("");

      onClose();

      await onAdded();
    } catch (err: unknown) {
      setError(getApiErrorMessage(err, "Mitgliedschaft konnte nicht angelegt werden."));
    }
  };

  /* ========================================================= */

  const handleClose = () => {
    setError(null);
    setSelectedVereinId("");

    onClose();
  };

  /* ========================================================= */

  return (
    <>
      <Dialog open={open} onClose={handleClose} fullWidth maxWidth="sm">
        <DialogTitle>Verein zuordnen</DialogTitle>

        <DialogContent>
          <TextField
            select
            fullWidth
            label="Verein"
            value={selectedVereinId}
            onChange={(e) => setSelectedVereinId(Number(e.target.value))}
            sx={{ mt: 1 }}
          >
            {availableVereine.map((v) => (
              <MenuItem key={v.id} value={v.id}>
                {v.name}
              </MenuItem>
            ))}
          </TextField>
        </DialogContent>

        <DialogActions>
          <Button onClick={handleClose}>Abbrechen</Button>

          <Button variant="contained" disabled={!selectedVereinId} onClick={handleAdd}>
            Zuordnen
          </Button>
        </DialogActions>
      </Dialog>

      <ErrorDialog open={!!error} message={error ?? ""} onClose={() => setError(null)} />
    </>
  );
};
