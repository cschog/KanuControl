import { useEffect, useRef } from "react";

import {
  Box,
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  TextField,
  Typography,
} from "@mui/material";

type TeilnehmerSearch = {
  personId: number;
  vorname: string;
  name: string;
  hauptvereinAbk?: string;
};

interface Props {
  open: boolean;

  search: string;
  results: TeilnehmerSearch[];
  selectedIds: number[];

  onClose: () => void;

  onSearchChange: (value: string) => void;

  onToggle: (personId: number) => void;

  onAssign: () => void;
}

export default function TeilnehmerZuordnenDialog({
  open,
  search,
  results,
  selectedIds,
  onClose,
  onSearchChange,
  onToggle,
  onAssign,
}: Props) {
  const inputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    if (!open) {
      return;
    }

    const timeout = window.setTimeout(() => {
      inputRef.current?.focus();
    }, 50);

    return () => {
      window.clearTimeout(timeout);
    };
  }, [open]);

  return (
    <Dialog open={open} onClose={onClose} fullWidth>
      <DialogTitle>Teilnehmer hinzufügen</DialogTitle>

      <DialogContent>
        <TextField
          size="small"
          fullWidth
          inputRef={inputRef}
          placeholder="Name suchen..."
          value={search}
          onChange={(event) => onSearchChange(event.target.value)}
          sx={{ mb: 2 }}
        />

        {results.map((teilnehmer) => {
          const selected = selectedIds.includes(teilnehmer.personId);

          return (
            <Box
              key={teilnehmer.personId}
              sx={{
                cursor: "pointer",
                p: 1,

                bgcolor: selected ? "action.selected" : "transparent",
              }}
              onClick={() => onToggle(teilnehmer.personId)}
            >
              <Typography>
                {teilnehmer.name}, {teilnehmer.vorname}
              </Typography>
            </Box>
          );
        })}
      </DialogContent>

      <DialogActions>
        <Button onClick={onClose}>Abbrechen</Button>

        <Button variant="contained" onClick={onAssign} disabled={selectedIds.length === 0}>
          Zuweisen
        </Button>
      </DialogActions>
    </Dialog>
  );
}
