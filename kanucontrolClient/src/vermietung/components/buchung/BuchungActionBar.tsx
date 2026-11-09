// src/vermietung/components/anmeldung/BuchungActionBar.tsx

import {
  Alert,
  Box,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogContentText,
  DialogActions,
} from "@mui/material";
import StatusMailVorschauDialog from "./StatusMailVorschauDialog";
import ReservierungsbestaetigungDialog from "./ReservierungsbestaetigungDialog";
import { useRef, useState } from "react";

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
  buchungId?: number;
  direktbuchung?: boolean;
  reservierungMailGesendetAm?: string | null;
  reservierungMailFehler?: string | null;
  onRefresh?: () => Promise<void>;
  status: Buchungsstatus;
  editMode: boolean;
  notifyByEmail?: boolean;
  abschlussErforderlich?: boolean;

  onEdit: () => void;
  onCancelEdit: () => void;
  onReopen: (sendEmail: boolean, fingerprint?: string) => Promise<void>;
  onSave: () => void;

  onReserve: (sendEmail: boolean, fingerprint?: string) => Promise<void> | void;
  onConfirm: (sendEmail: boolean, fingerprint?: string) => Promise<void> | void;
  onReject: (sendEmail: boolean, fingerprint?: string) => Promise<void> | void;
  onCancelBooking: (sendEmail: boolean, fingerprint?: string) => Promise<void> | void;
  onComplete: (sendEmail: boolean, fingerprint?: string) => Promise<void> | void;

  onDelete: () => void;

  disableDelete: boolean;
}

export const BuchungActionBar: React.FC<Props> = ({
  buchungId, direktbuchung = false, reservierungMailGesendetAm, reservierungMailFehler, onRefresh,
  status,
  editMode,
  notifyByEmail = false,
  abschlussErforderlich = false,
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
  const [vorschauOpen, setVorschauOpen] = useState(false);
  const [emailDecisionStatus, setEmailDecisionStatus] = useState<Buchungsstatus | null>(null);
  const [busy, setBusy] = useState(false);
  const busyRef = useRef(false);
  const [reserveDialogOpen, setReserveDialogOpen] = useState(false);
  const [confirmDialogOpen, setConfirmDialogOpen] = useState(false);
  const [reopenDialogOpen, setReopenDialogOpen] = useState(false);
  const [cancelDialogOpen, setCancelDialogOpen] = useState(false);
  const [rejectDialogOpen, setRejectDialogOpen] = useState(false);
  const [deleteDialogOpen, setDeleteDialogOpen] = useState(false);
  const [completeDialogOpen, setCompleteDialogOpen] = useState(false);

  const handleConfirmCancel = async () => {
    setCancelDialogOpen(false);
    await perform(() => onCancelBooking(false));
  };

  const handleConfirmReject = async () => {
    setRejectDialogOpen(false);
    await perform(() => onReject(false));
  };

  const handleConfirmComplete = async () => {
    setCompleteDialogOpen(false);
    await perform(() => onComplete(false));
  };

  const handleConfirmDelete = () => {
    setDeleteDialogOpen(false);
    onDelete();
  };

  const handleConfirmReserve = () => {
    setReserveDialogOpen(false);
    void perform(() => onReserve(false));
  };

  const handleConfirmBooking = () => {
    setConfirmDialogOpen(false);
    void perform(() => onConfirm(false));
  };

  const handleConfirmReopen = async () => {
    setReopenDialogOpen(false);
    await perform(() => onReopen(false));
  };

  const perform = async (action: () => Promise<void> | void) => {
    if (busyRef.current) return;
    busyRef.current = true;
    setBusy(true);
    try { await action(); } finally { setBusy(false); busyRef.current = false; }
  };

  const requestStatus = (target: Buchungsstatus, openExisting: () => void) => {
    if (target === "RESERVIERT" && direktbuchung && buchungId) setVorschauOpen(true);
    else if (notifyByEmail) setEmailDecisionStatus(target);
    else openExisting();
  };

  const confirmEmailDecision = async (sendEmail: boolean, fingerprint?: string) => {
    if (!emailDecisionStatus || busy) return;
    const target = emailDecisionStatus;
    const action = target === "RESERVIERT" ? onReserve
      : target === "BESTAETIGT" ? onConfirm
      : target === "ABGELEHNT" ? onReject
      : target === "STORNIERT" ? onCancelBooking
      : target === "ABGESCHLOSSEN" ? onComplete
      : target === "ABRECHENBAR" ? onReopen
      : null;
    if (action) await perform(() => action(sendEmail, fingerprint));
    setEmailDecisionStatus(null);
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
                  disabled={busy}
                  onClick={() => requestStatus("RESERVIERT", () => setReserveDialogOpen(true))}
                >
                  Reservieren
                </Button>

                <Button
                  variant="contained"
                  color="success"
                  startIcon={<CheckCircleIcon />}
                  disabled={busy}
                  onClick={() => requestStatus("BESTAETIGT", () => setConfirmDialogOpen(true))}
                >
                  Bestätigen
                </Button>

                <Button
                  variant="outlined"
                  color="error"
                  startIcon={<BlockIcon />}
                  disabled={busy}
                  onClick={() => requestStatus("ABGELEHNT", () => setRejectDialogOpen(true))}
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
                  disabled={busy}
                  onClick={() => requestStatus("BESTAETIGT", () => setConfirmDialogOpen(true))}
                >
                  Bestätigen
                </Button>

                <Button
                  variant="outlined"
                  color="error"
                  startIcon={<CancelIcon />}
                  disabled={busy}
                  onClick={() => requestStatus("STORNIERT", () => setCancelDialogOpen(true))}
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
                disabled={busy}
                onClick={() => requestStatus("STORNIERT", () => setCancelDialogOpen(true))}
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
                  disabled={busy}
                  onClick={() => requestStatus("ABGESCHLOSSEN", () => setCompleteDialogOpen(true))}
                >
                  Abschließen
                </Button>

                <Button
                  variant="outlined"
                  color="error"
                  startIcon={<CancelIcon />}
                  disabled={busy}
                  onClick={() => requestStatus("STORNIERT", () => setCancelDialogOpen(true))}
                >
                  Stornieren
                </Button>
              </>
            )}

            {status === "ABGESCHLOSSEN" && <>
              {abschlussErforderlich && <Button disabled={busy} color="success" variant="contained" onClick={()=>requestStatus("ABGESCHLOSSEN",()=>setCompleteDialogOpen(true))}>Erneut abschließen</Button>}
              <Button disabled={busy} onClick={()=>requestStatus("ABRECHENBAR",()=>setReopenDialogOpen(true))}>Wieder öffnen</Button>
            </>}
            <Button
              variant="contained"
              startIcon={<EditIcon />}
              disabled={busy}
              onClick={onEdit}
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

      {!editMode && status === "RESERVIERT" && direktbuchung && buchungId && <Box sx={{ width: "100%" }}>
        {reservierungMailFehler && <Alert severity="error" sx={{ mb: 1 }}>{reservierungMailFehler}</Alert>}
        {reservierungMailGesendetAm && <Alert severity="success" sx={{ mb: 1 }}>Reservierungsbestätigung am {new Date(reservierungMailGesendetAm).toLocaleString("de-DE")} an SMTP übergeben.</Alert>}
        <Button disabled={busy} onClick={() => setVorschauOpen(true)}>
          {reservierungMailGesendetAm ? "Aktuelle Bestätigung ansehen" : reservierungMailFehler ? "Bestätigung prüfen und Versand wiederholen" : "Bestätigung prüfen und senden"}
        </Button>
      </Box>}
      {vorschauOpen && buchungId && <ReservierungsbestaetigungDialog
        key={buchungId} id={buchungId} reserviert={status === "RESERVIERT"} gesendetAm={reservierungMailGesendetAm}
        onClose={() => setVorschauOpen(false)} onReserve={sendEmail => perform(() => onReserve(sendEmail))} onRefresh={onRefresh}
      />}

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
          <Button disabled={busy} onClick={() => setReopenDialogOpen(false)}>Abbrechen</Button>

          <Button
            disabled={busy}
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
          <Button disabled={busy} onClick={() => setReserveDialogOpen(false)}>Abbrechen</Button>

          <Button
            disabled={busy}
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
          <Button disabled={busy} onClick={() => setConfirmDialogOpen(false)}>Abbrechen</Button>

          <Button
            disabled={busy}
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
          <Button disabled={busy} onClick={() => setCancelDialogOpen(false)}>Abbrechen</Button>

          <Button
            disabled={busy}
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
          <Button disabled={busy} onClick={() => setRejectDialogOpen(false)}>Abbrechen</Button>

          <Button
            disabled={busy}
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
          <Button disabled={busy} onClick={() => setCompleteDialogOpen(false)}>Abbrechen</Button>

          <Button
            disabled={busy}
            onClick={handleConfirmComplete}
            color="success"
            variant="contained"
            startIcon={<TaskAltIcon />}
          >
            Jetzt abschließen
          </Button>
        </DialogActions>
      </Dialog>

      {emailDecisionStatus && buchungId && <StatusMailVorschauDialog
        key={`${buchungId}-${emailDecisionStatus}`} id={buchungId} status={emailDecisionStatus}
        onClose={() => { if (!busy) setEmailDecisionStatus(null); }} onDecision={confirmEmailDecision}
      />}

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
