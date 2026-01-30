import React, { useState } from "react";
import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  TextField,
  Button,
  MenuItem,
} from "@mui/material";
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

  const handleAdd = async () => {
    await apiClient.post("/mitglied", {
      personId,
      vereinId: selectedVereinId,
    });

    setSelectedVereinId("");
    onClose();
    await onAdded();
  };

  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="sm">
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
        <Button onClick={onClose}>Abbrechen</Button>
        <Button disabled={!selectedVereinId} onClick={handleAdd}>
          Zuordnen
        </Button>
      </DialogActions>
    </Dialog>
  );
};
