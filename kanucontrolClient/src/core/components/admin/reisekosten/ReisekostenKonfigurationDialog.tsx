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
} from "@/kjfp/api/services/reisekostenKonfigurationApi";

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
     <Dialog
       open={open}
       onClose={onClose}
       fullWidth
       maxWidth="sm"
       scroll="paper"
       sx={{
         "& .MuiDialog-paper": {
           m: { xs: 1, sm: 3 },
           width: { xs: "calc(100% - 16px)", sm: "100%" },
           maxHeight: {
             xs: "calc(100% - 16px)",
             sm: "calc(100% - 64px)",
           },
         },
       }}
     >
       <DialogTitle>Neue Reisekosten-Konfiguration</DialogTitle>

       <DialogContent dividers>
         <Stack spacing={2} sx={{ mt: 1 }}>
           <DatePicker
             label="Gültig ab"
             value={gueltigVon}
             onChange={setGueltigVon}
             slotProps={{
               textField: {
                 fullWidth: true,
               },
             }}
           />

           <TextField
             fullWidth
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
             fullWidth
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
             fullWidth
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

       <DialogActions
         sx={{
           px: 2,
           py: 1.5,
           flexShrink: 0,
           gap: 1,
         }}
       >
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
