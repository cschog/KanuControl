// src/components/verwaltung/kik/KikDialog.tsx

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

import { DatePicker } from "@mui/x-date-pickers";

import dayjs, { Dayjs } from "dayjs";

import {
  KikCreateUpdateDTO,
  KikDTO,
} from "@/api/types/Kik";

interface Props {
  open: boolean;

  initialData?: KikDTO | null;

  onClose: () => void;

  onSave: (dto: KikCreateUpdateDTO) => void;

  loading?: boolean;
}

const KikDialog = ({ open, initialData, onClose, onSave, loading = false }: Props) => {
  /* =========================================================
     STATE
     ========================================================= */

  const [gueltigVon, setGueltigVon] = useState<Dayjs | null>(dayjs());

  const [gueltigBis, setGueltigBis] = useState<Dayjs | null>(null);

  const [beschluss, setBeschluss] = useState("");

  const [kikZuschlag, setKikZuschlag] = useState("");

  /* =========================================================
     EDIT MODE
     ========================================================= */

  useEffect(() => {
    if (!initialData) {

      setGueltigVon(dayjs());

      setGueltigBis(null);

      setKikZuschlag("");

      setBeschluss("");

      return;
    }

    setGueltigVon(dayjs(initialData.gueltigVon));

    setGueltigBis(initialData.gueltigBis ? dayjs(initialData.gueltigBis) : null);

    setKikZuschlag(initialData.kikZuschlag.toString());

    setBeschluss(initialData.beschluss ?? "");
  }, [initialData, open]);

  /* =========================================================
     SAVE
     ========================================================= */

  const handleSave = () => {
    console.log("SAVE CLICK");

      if (!gueltigVon) {
        console.log("NO DATE");

        return;
      }

    const dto: KikCreateUpdateDTO = {

      gueltigVon: gueltigVon.format("YYYY-MM-DD"),

      gueltigBis: gueltigBis ? gueltigBis.format("YYYY-MM-DD") : null,

      kikZuschlag: Number(kikZuschlag),

      beschluss: beschluss || null,
    };

    console.log(dto);
    onSave(dto);
  };

  /* ========================================================= */

  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="sm">
      <DialogTitle>{initialData ? "KiK Zuschlag bearbeiten" : "Neuer Kik Zuschlag"}</DialogTitle>

      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>

          <DatePicker label="Gültig von" value={gueltigVon} onChange={setGueltigVon} />

          <DatePicker label="Gültig bis" value={gueltigBis} onChange={setGueltigBis} />

          <TextField
            label="KiK Zuschlag"
            type="number"
            value={kikZuschlag}
            onChange={(e) => setKikZuschlag(e.target.value)}
          />

          <TextField
            label="Beschluss"
            value={beschluss}
            onChange={(e) => setBeschluss(e.target.value)}
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
};

export default KikDialog;
