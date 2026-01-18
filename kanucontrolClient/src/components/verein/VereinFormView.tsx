import React, { useState, useCallback } from "react";
import {
  Box,
  Typography,
  Button,
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Snackbar,
  Alert,
} from "@mui/material";
import { FormFeld } from "@/components/common/FormFeld";
import { Verein } from "@/api/types/Verein";

interface VereinFormViewProps {
  onNeuerVerein: () => void;
  btnNeuerVerein: boolean;
  onÄndernVerein: () => void;
  btnÄndernVerein: boolean;
  onDeleteVerein: () => void;
  btnLöschenVerein: boolean;
  onStartMenue: () => void;
  selectedVerein: Verein | null;
}

export const VereinFormView: React.FC<VereinFormViewProps> = ({
  onNeuerVerein,
  btnNeuerVerein,
  onÄndernVerein,
  btnÄndernVerein,
  onDeleteVerein,
  btnLöschenVerein,
  onStartMenue,
  selectedVerein,
}) => {
  const [confirmOpen, setConfirmOpen] = useState(false);
  const [snackbar, setSnackbar] = useState<string | null>(null);

  const handleDeleteConfirm = useCallback(() => {
    if (!selectedVerein) return;

    onDeleteVerein();
    setConfirmOpen(false);
    setSnackbar(`${selectedVerein.name} wurde gelöscht`);
  }, [selectedVerein, onDeleteVerein]);

  return (
    <>
      {/* Container */}
      <Box maxWidth="lg" mx="auto">
        <Typography variant="h5" align="center" gutterBottom>
          Vereinsdetails
        </Typography>

        {selectedVerein ? (
          <Box
            sx={{
              display: "grid",
              gridTemplateColumns: {
                xs: "1fr",
                sm: "1fr 1fr",
                lg: "1fr 1fr 1fr",
              },
              gap: 2,
            }}
          >
            <FormFeld label="Abkürzung" value={selectedVerein.abk} disabled />
            <FormFeld label="Verein" value={selectedVerein.name} disabled />
            <FormFeld label="Straße" value={selectedVerein.strasse} disabled />

            <FormFeld label="PLZ" value={selectedVerein.plz} disabled />
            <FormFeld label="Ort" value={selectedVerein.ort} disabled />
            <FormFeld label="Telefon" value={selectedVerein.telefon} disabled />

            <FormFeld label="Bank" value={selectedVerein.bankName} disabled />
            <Box sx={{ gridColumn: { xs: "1", lg: "1 / -1" } }}>
              <FormFeld label="IBAN" value={selectedVerein.iban} disabled />
            </Box>
          </Box>
        ) : (
          <Typography color="text.secondary" align="center" sx={{ mt: 2 }}>
            Bitte wählen Sie einen Verein aus der Tabelle aus.
          </Typography>
        )}

        {/* Action Buttons */}
        <Box mt={4} display="flex" gap={2} flexWrap="wrap">
          <Button
            variant="contained"
            onClick={onNeuerVerein}
            disabled={btnNeuerVerein}
          >
            Neuer Verein
          </Button>

          <Button
            variant="outlined"
            onClick={onÄndernVerein}
            disabled={btnÄndernVerein}
          >
            Bearbeiten
          </Button>

          <Button
            variant="outlined"
            color="error"
            onClick={() => setConfirmOpen(true)}
            disabled={btnLöschenVerein}
          >
            Löschen
          </Button>

          <Box flexGrow={1} />

          <Button variant="text" onClick={onStartMenue}>
            Zurück
          </Button>
        </Box>
      </Box>

      {/* Delete Confirmation Dialog */}
      <Dialog open={confirmOpen} onClose={() => setConfirmOpen(false)}>
        <DialogTitle>Verein löschen?</DialogTitle>
        <DialogContent>
          {selectedVerein &&
            `Soll der Verein "${selectedVerein.name}" wirklich gelöscht werden?`}
        </DialogContent>
        <DialogActions>
          <Button onClick={() => setConfirmOpen(false)}>Abbrechen</Button>
          <Button color="error" onClick={handleDeleteConfirm}>
            Löschen
          </Button>
        </DialogActions>
      </Dialog>

      {/* Snackbar */}
      <Snackbar
        open={!!snackbar}
        autoHideDuration={4000}
        onClose={() => setSnackbar(null)}
      >
        <Alert severity="info" onClose={() => setSnackbar(null)}>
          {snackbar}
        </Alert>
      </Snackbar>
    </>
  );
};