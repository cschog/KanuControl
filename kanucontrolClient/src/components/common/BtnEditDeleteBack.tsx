import React from "react";
import { Button, Stack } from "@mui/material";

interface BtnEditDeleteBackProps {
  onÄndern: () => void;
  btnÄndern: boolean;
  setVisible: React.Dispatch<React.SetStateAction<boolean>>;
  btnLöschen: boolean;
  onStartMenue: () => void;
}

export function BtnEditDeleteBack({
  onÄndern,
  btnÄndern,
  setVisible,
  btnLöschen,
  onStartMenue,
}: Readonly<BtnEditDeleteBackProps>) {
  return (
    <Stack direction="row" spacing={2}>
      <Button variant="outlined" color="warning" onClick={onÄndern} disabled={btnÄndern}>
        Ändern
      </Button>

      <Button
        variant="outlined"
        color="error"
        onClick={() => setVisible(true)}
        disabled={btnLöschen}
      >
        Löschen
      </Button>

      <Button variant="outlined" onClick={onStartMenue}>
        Zurück
      </Button>
    </Stack>
  );
}
