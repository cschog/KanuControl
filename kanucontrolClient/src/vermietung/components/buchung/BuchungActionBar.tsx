// src/vermietung/components/anmeldung/BuchungActionBar.tsx

import {
  Box,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  DialogActions,
} from "@mui/material";
import { useState } from "react";

import EditIcon from "@mui/icons-material/Edit";
import SaveIcon from "@mui/icons-material/Save";
import DeleteIcon from "@mui/icons-material/Delete";
import CancelIcon from "@mui/icons-material/Cancel";

import CheckCircleIcon from "@mui/icons-material/CheckCircle";

import type { Buchungsstatus } from "@/vermietung/enums/Buchungsstatus";

interface Props {
  status: Buchungsstatus;
  editMode: boolean;

  onEdit: () => void;
  onCancelEdit: () => void;
  onSave: () => void;
  onConfirm: () => void;
  onCancelBooking: () => void;
  onDelete: () => void;

  disableDelete: boolean;
}

export const BuchungActionBar: React.FC<Props> = ({
  status,
  editMode,
  onEdit,
  onCancelEdit,
  onSave,
  onConfirm,
  onCancelBooking,
  onDelete,
  disableDelete,
}) => {
const [cancelDialogOpen, setCancelDialogOpen] = useState(false);
const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);

const handleConfirmCancel = () => {
  setCancelDialogOpen(false);
  onCancelBooking();
};

const handleConfirmDelete = () => {
  setDeleteDialogOpen(false);
  onDelete();
};
    
    

  return (
    <Box
      sx={{
        display: "flex",
        justifyContent: "space-between",
        alignItems: "center",
        mt: 4,
        gap: 1,
        flexWrap: "wrap",
      }}
    >
      <Box
        sx={{
          display: "flex",
          gap: 1,
          flexWrap: "wrap",
          justifyContent: "flex-end",
        }}
      >
        {!editMode ? (
          <>
            {status === "ANFRAGE" && (
              <Button
                variant="contained"
                color="success"
                startIcon={<CheckCircleIcon />}
                onClick={onConfirm}
              >
                Bestätigen
              </Button>
            )}

            {(status === "ANFRAGE" || status === "BESTAETIGT") && (
              <Button
                variant="outlined"
                color="error"
                startIcon={<CancelIcon />}
                onClick={() => setCancelDialogOpen(true)}
              >
                Stornieren
              </Button>
            )}

            <Button variant="contained" startIcon={<EditIcon />} onClick={onEdit}>
              Bearbeiten
            </Button>
          </>
        ) : (
          <>
            <Button variant="outlined" startIcon={<CancelIcon />} onClick={onCancelEdit}>
              Abbrechen
            </Button>

            <Button variant="contained" startIcon={<SaveIcon />} onClick={onSave}>
              Speichern
            </Button>
          </>
        )}

        <Button
          variant="outlined"
          color="error"
          startIcon={<DeleteIcon />}
          onClick={() => setDeleteDialogOpen(true)}
          disabled={disableDelete}
        >
          Löschen
        </Button>
      </Box>
      <Dialog open={cancelDialogOpen} onClose={() => setCancelDialogOpen(false)}>
        <DialogTitle>Buchung stornieren?</DialogTitle>

        <DialogContent>
          <DialogContentText>
            Möchtest du diese Buchung wirklich stornieren? Der Status wird auf „Storniert“ gesetzt.
          </DialogContentText>
        </DialogContent>

        <DialogActions>
          <Button onClick={() => setCancelDialogOpen(false)}>Abbrechen</Button>

          <Button
            onClick={handleConfirmCancel}
            color="error"
            variant="contained"
            startIcon={<CancelIcon />}
          >
            Jetzt stornieren
          </Button>
        </DialogActions>
      </Dialog>

      <Dialog open={deleteDialogOpen} onClose={() => setDeleteDialogOpen(false)}>
        <DialogTitle>Buchung löschen?</DialogTitle>

        <DialogContent>
          <DialogContentText>
            Möchtest du diese Buchung wirklich unwiderruflich löschen? Diese Aktion kann nicht
            rückgängig gemacht werden.
          </DialogContentText>
        </DialogContent>

        <DialogActions>
          <Button onClick={() => setDeleteDialogOpen(false)}>Abbrechen</Button>

          <Button
            onClick={handleConfirmDelete}
            color="error"
            variant="contained"
            startIcon={<DeleteIcon />}
          >
            Endgültig löschen
          </Button>
        </DialogActions>
      </Dialog>
    </Box>
  );
};
