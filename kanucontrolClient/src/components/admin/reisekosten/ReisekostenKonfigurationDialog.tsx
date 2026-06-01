import { useEffect, useState } from "react";

import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  Stack,
  TextField,
} from "@mui/material";

import InputAdornment from "@mui/material/InputAdornment";

import { DatePicker } from "@mui/x-date-pickers";

import dayjs, { Dayjs } from "dayjs";

import {
  getAktuelleReisekostenKonfiguration,
  type ReisekostenKonfigurationSaveRequest,
} from "@/api/services/reisekostenKonfigurationApi";

interface Props {
  open: boolean;

  onClose: () => void;

  onSave: (dto: ReisekostenKonfigurationSaveRequest) => void;

  loading?: boolean;
}

export default function ReisekostenKonfigurationDialog({
  open,
  onClose,
  onSave,
  loading = false,
}: Props) {
  const [pkwSatz, setPkwSatz] = useState("");

  const [mitfahrerSatz, setMitfahrerSatz] = useState("");

  const [anhaengerSatz, setAnhaengerSatz] = useState("");

  const [gueltigVon, setGueltigVon] = useState<Dayjs | null>(dayjs());

  useEffect(() => {
    if (!open) {
      return;
    }

    getAktuelleReisekostenKonfiguration().then((cfg) => {
      setPkwSatz(cfg.pkwSatz.toString());
      setMitfahrerSatz(cfg.mitfahrerSatz.toString());
      setAnhaengerSatz(cfg.anhaengerSatz.toString());
    });
  }, [open]);

  const handleSave = () => {
    if (!gueltigVon) {
      return;
    }

    onSave({
      pkwSatz: Number(pkwSatz),
      mitfahrerSatz: Number(mitfahrerSatz),
      anhaengerSatz: Number(anhaengerSatz),
      gueltigVon: gueltigVon.format("YYYY-MM-DD"),
    });
  };

  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="sm">
      <DialogTitle>Neue Reisekosten-Konfiguration</DialogTitle>

      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          <DatePicker label="Gültig ab" value={gueltigVon} onChange={setGueltigVon} />

          <TextField
            label="PKW-Satz"
            value={pkwSatz}
            onChange={(e) => setPkwSatz(e.target.value)}
            slotProps={{
              input: {
                endAdornment: <InputAdornment position="end">€</InputAdornment>,
              },
            }}
          />

          <TextField
            label="Mitfahrer-Satz"
            value={mitfahrerSatz}
            onChange={(e) => setMitfahrerSatz(e.target.value)}
            slotProps={{
              input: {
                endAdornment: <InputAdornment position="end">€</InputAdornment>,
              },
            }}
          />

          <TextField
            label="Anhänger-Satz"
            value={anhaengerSatz}
            onChange={(e) => setAnhaengerSatz(e.target.value)}
            slotProps={{
              input: {
                endAdornment: <InputAdornment position="end">€</InputAdornment>,
              },
            }}
          />
        </Stack>
      </DialogContent>

      <DialogActions>
        <Button onClick={onClose} disabled={loading}>
          Abbrechen
        </Button>

        <Button variant="contained" onClick={handleSave} disabled={loading}>
          Speichern
        </Button>
      </DialogActions>
    </Dialog>
  );
}
