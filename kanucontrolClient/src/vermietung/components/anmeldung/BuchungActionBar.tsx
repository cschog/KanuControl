// src/vermietung/components/anmeldung/BuchungActionBar.tsx

import React from "react";
import { Box, Button } from "@mui/material";

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
                onClick={onCancelBooking}
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
          onClick={onDelete}
          disabled={disableDelete}
        >
          Löschen
        </Button>
      </Box>
    </Box>
  );
};
