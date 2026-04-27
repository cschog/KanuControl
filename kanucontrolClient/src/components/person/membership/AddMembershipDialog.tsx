import React, { useState } from "react";

import {
  Alert,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  MenuItem,
  TextField,
} from "@mui/material";

import axios from "axios";

import apiClient from "@/api/client/apiClient";

import { VereinRef } from "@/api/types/VereinRef";

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
      if (axios.isAxiosError(err)) {
        setError(err.response?.data?.message ?? "Mitgliedschaft konnte nicht angelegt werden.");
      } else {
        setError("Unbekannter Fehler.");
      }
    }
  };

  /* ========================================================= */

  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="sm">
      <DialogTitle>Verein zuordnen</DialogTitle>

      <DialogContent>
        {error && (
          <Alert severity="error" sx={{ mb: 2 }}>
            {error}
          </Alert>
        )}

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
        <Button onClick={onClose}>Abbrechen</Button>

        <Button variant="contained" disabled={!selectedVereinId} onClick={handleAdd}>
          Zuordnen
        </Button>
      </DialogActions>
    </Dialog>
  );
};
