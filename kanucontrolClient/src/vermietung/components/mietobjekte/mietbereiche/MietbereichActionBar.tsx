import React from "react";
import { Box, Button } from "@mui/material";

interface Props {
  editMode: boolean;

  onEdit: () => void;
  onCancelEdit: () => void;
  onSave: () => void;

  onDelete: () => void;
  onBack: () => void;

  disableDelete: boolean;
}

export const MietbereichActionBar: React.FC<Props> = ({
  editMode,
  onEdit,
  onCancelEdit,
  onSave,
  onDelete,
  onBack,
  disableDelete,
}) => {
  return (
    <Box
      sx={{
        display: "flex",
        justifyContent: "flex-start",
        gap: 1,
        mt: 3,
      }}
    >
      {editMode ? (
        <>
          <Button variant="contained" onClick={onSave}>
            Speichern
          </Button>

          <Button variant="outlined" onClick={onCancelEdit}>
            Abbrechen
          </Button>
        </>
      ) : (
        <>
          <Button variant="contained" onClick={onEdit}>
            Ändern
          </Button>

          <Button variant="outlined" color="error" onClick={onDelete} disabled={disableDelete}>
            Löschen
          </Button>

          <Button variant="outlined" onClick={onBack}>
            Zurück
          </Button>
        </>
      )}
    </Box>
  );
};
