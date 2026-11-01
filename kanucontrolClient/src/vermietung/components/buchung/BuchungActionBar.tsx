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
import EventAvailableIcon from "@mui/icons-material/EventAvailable";
import BlockIcon from "@mui/icons-material/Block";
import TaskAltIcon from "@mui/icons-material/TaskAlt";

import type { Buchungsstatus } from "@/vermietung/enums/Buchungsstatus";

interface Props {
  status: Buchungsstatus;
  editMode: boolean;

  onEdit: () => void;
  onCancelEdit: () => void;
  onReopen: () => Promise<void>;
  onSave: () => void;

  onReserve: () => void;
  onConfirm: () => void;
  onReject: () => void;
  onCancelBooking: () => void;
  onComplete: () => void;

  onDelete: () => void;

  disableDelete: boolean;
}

export const BuchungActionBar: React.FC<Props> = ({
  status,
  editMode,
  onEdit,
  onCancelEdit,
  onSave,
  onReserve,
  onConfirm,
  onReject,
  onReopen,
  onCancelBooking,
  onComplete,
  onDelete,
  disableDelete,
}) => {
  const [reserveDialogOpen, setReserveDialogOpen] = useState(false);
  const [confirmDialogOpen, setConfirmDialogOpen] = useState(false);
  const [reopenDialogOpen, setReopenDialogOpen] = useState(false);
  const [cancelDialogOpen, setCancelDialogOpen] = useState(false);
  const [rejectDialogOpen, setRejectDialogOpen] = useState(false);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [completeDialogOpen, setCompleteDialogOpen] = useState(false);

  const handleConfirmCancel = () => {
    setCancelDialogOpen(false);
    onCancelBooking();
  };

  const handleConfirmReject = () => {
    setRejectDialogOpen(false);
    onReject();
  };

  const handleConfirmComplete = () => {
    setCompleteDialogOpen(false);
    onComplete();
  };

  const handleConfirmDelete = () => {
    setDeleteDialogOpen(false);
    onDelete();
  };

  const handleConfirmReserve = () => {
    setReserveDialogOpen(false);
    onReserve();
  };

  const handleConfirmBooking = () => {
    setConfirmDialogOpen(false);
    onConfirm();
  };

  const handleConfirmReopen = async () => {
    setReopenDialogOpen(false);
    await onReopen();
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
              <>
                <Button
                  variant="contained"
                  color="info"
                  startIcon={<EventAvailableIcon />}
                  onClick={() => setReserveDialogOpen(true)}
                >
                  Reservieren
                </Button>

                <Button
                  variant="contained"
                  color="success"
                  startIcon={<CheckCircleIcon />}
                  onClick={() => setConfirmDialogOpen(true)}
                >
                  Bestätigen
                </Button>

                <Button
                  variant="outlined"
                  color="error"
                  startIcon={<BlockIcon />}
                  onClick={() => setRejectDialogOpen(true)}
                >
                  Ablehnen
                </Button>
              </>
            )}

            {status === "RESERVIERT" && (
              <>
                <Button
                  variant="contained"
                  color="success"
                  startIcon={<CheckCircleIcon />}
                  onClick={() => setConfirmDialogOpen(true)}
                >
                  Bestätigen
                </Button>

                <Button
                  variant="outlined"
                  color="error"
                  startIcon={<CancelIcon />}
                  onClick={() => setCancelDialogOpen(true)}
                >
                  Stornieren
                </Button>
              </>
            )}

            {status === "BESTAETIGT" && (
              <Button
                variant="outlined"
                color="error"
                startIcon={<CancelIcon />}
                onClick={() => setCancelDialogOpen(true)}
              >
                Stornieren
              </Button>
            )}

            {status === "ABRECHENBAR" && (
              <>
                <Button
                  variant="contained"
                  color="success"
                  startIcon={<TaskAltIcon />}
                  onClick={() => setCompleteDialogOpen(true)}
                >
                  Abschließen
                </Button>

                <Button
                  variant="outlined"
                  color="error"
                  startIcon={<CancelIcon />}
                  onClick={() => setCancelDialogOpen(true)}
                >
                  Stornieren
                </Button>
              </>
            )}

            <Button
              variant="contained"
              startIcon={<EditIcon />}
              onClick={() => {
                if (status === "ABGESCHLOSSEN") {
                  setReopenDialogOpen(true);
                } else {
                  onEdit();
                }
              }}
            >
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

      {/* Abgeschlossene Buchung wieder öffnen */}
      <Dialog open={reopenDialogOpen} onClose={() => setReopenDialogOpen(false)}>
        <DialogTitle>Abgeschlossene Buchung wieder öffnen?</DialogTitle>

        <DialogContent>
          <DialogContentText>
            Möchtest du diese abgeschlossene Buchung wirklich wieder öffnen? Der Status wird auf
            „Abrechenbar“ zurückgesetzt. Anschließend kannst du die Buchungsdaten korrigieren. Beim
            erneuten Abschließen werden die Verbrauchskosten erneut geprüft und gespeichert.
          </DialogContentText>
        </DialogContent>

        <DialogActions>
          <Button onClick={() => setReopenDialogOpen(false)}>Abbrechen</Button>

          <Button
            onClick={handleConfirmReopen}
            color="warning"
            variant="contained"
            startIcon={<EditIcon />}
          >
            Buchung wieder öffnen
          </Button>
        </DialogActions>
      </Dialog>

      {/* Reservieren */}
      <Dialog open={reserveDialogOpen} onClose={() => setReserveDialogOpen(false)}>
        <DialogTitle>Buchung reservieren?</DialogTitle>

        <DialogContent>
          <DialogContentText>
            Möchtest du diese Buchung wirklich reservieren? Die Buchung erhält anschließend den
            Status „Reserviert“.
          </DialogContentText>
        </DialogContent>

        <DialogActions>
          <Button onClick={() => setReserveDialogOpen(false)}>Abbrechen</Button>

          <Button
            onClick={handleConfirmReserve}
            color="info"
            variant="contained"
            startIcon={<EventAvailableIcon />}
          >
            Jetzt reservieren
          </Button>
        </DialogActions>
      </Dialog>

      {/* Bestätigen */}
      <Dialog open={confirmDialogOpen} onClose={() => setConfirmDialogOpen(false)}>
        <DialogTitle>Buchung bestätigen?</DialogTitle>

        <DialogContent>
          <DialogContentText>
            Möchtest du diese Buchung wirklich bestätigen? Bitte prüfe vorher den Buchungszeitraum
            und die ausgewählten Mietbereiche.
          </DialogContentText>
        </DialogContent>

        <DialogActions>
          <Button onClick={() => setConfirmDialogOpen(false)}>Abbrechen</Button>

          <Button
            onClick={handleConfirmBooking}
            color="success"
            variant="contained"
            startIcon={<CheckCircleIcon />}
          >
            Jetzt bestätigen
          </Button>
        </DialogActions>
      </Dialog>

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

      <Dialog open={rejectDialogOpen} onClose={() => setRejectDialogOpen(false)}>
        <DialogTitle>Buchungsanfrage ablehnen?</DialogTitle>

        <DialogContent>
          <DialogContentText>
            Möchtest du diese Buchungsanfrage wirklich ablehnen? Die Buchung erhält den Status
            „Abgelehnt“. Dieser Vorgang kann nicht rückgängig gemacht werden.
          </DialogContentText>
        </DialogContent>

        <DialogActions>
          <Button onClick={() => setRejectDialogOpen(false)}>Abbrechen</Button>

          <Button
            onClick={handleConfirmReject}
            color="error"
            variant="contained"
            startIcon={<BlockIcon />}
          >
            Jetzt ablehnen
          </Button>
        </DialogActions>
      </Dialog>

      <Dialog open={completeDialogOpen} onClose={() => setCompleteDialogOpen(false)}>
        <DialogTitle>Buchung abschließen?</DialogTitle>

        <DialogContent>
          <DialogContentText>
            Möchtest du diese Buchung wirklich abschließen? Dabei werden die Verbrauchskosten und
            die erbrachten Leistungen abschließend geprüft. Die berechneten Verbrauchskosten werden
            dauerhaft gespeichert.
          </DialogContentText>
        </DialogContent>

        <DialogActions>
          <Button onClick={() => setCompleteDialogOpen(false)}>Abbrechen</Button>

          <Button
            onClick={handleConfirmComplete}
            color="success"
            variant="contained"
            startIcon={<TaskAltIcon />}
          >
            Jetzt abschließen
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
