// src/components/verwaltung/foerdersatz/FoerdersatzDialog.tsx

import { useEffect, useState } from "react";

import {
  Button,
  Dialog,
  DialogActions,
  DialogContent,
  DialogTitle,
  MenuItem,
  Stack,
  TextField,
} from "@mui/material";

import { DatePicker } from "@mui/x-date-pickers";

import dayjs, { Dayjs } from "dayjs";

import {
  FoerdersatzCreateUpdateDTO,
  FoerdersatzDTO,
  VeranstaltungTyp,
} from "@/api/types/Foerdersatz";

interface Props {
  open: boolean;

  initialData?: FoerdersatzDTO | null;

  onClose: () => void;

  onSave: (dto: FoerdersatzCreateUpdateDTO) => void;

  loading?: boolean;
}

const veranstaltungTypen: VeranstaltungTyp[] = ["FM", "JEM", "BM", "GV"];

const FoerdersatzDialog = ({ open, initialData, onClose, onSave, loading = false }: Props) => {
  /* =========================================================
     STATE
     ========================================================= */

  const [typ, setTyp] = useState<VeranstaltungTyp>("FM");

  const [gueltigVon, setGueltigVon] = useState<Dayjs | null>(dayjs());

  const [gueltigBis, setGueltigBis] = useState<Dayjs | null>(null);

  const [foerdersatz, setFoerdersatz] = useState("");

  const [foerderdeckel, setFoerderdeckel] = useState("");

  const [beschluss, setBeschluss] = useState("");

  /* =========================================================
     EDIT MODE
     ========================================================= */

  useEffect(() => {
    if (!initialData) {
      setTyp("FM");

      setGueltigVon(dayjs());

      setGueltigBis(null);

      setFoerdersatz("");

      setFoerderdeckel("");

      setBeschluss("");

      return;
    }

    setTyp(initialData.typ);

    setGueltigVon(dayjs(initialData.gueltigVon));

    setGueltigBis(initialData.gueltigBis ? dayjs(initialData.gueltigBis) : null);

    setFoerdersatz(initialData.foerdersatz.toString());

    setFoerderdeckel(initialData.foerderdeckel?.toString() ?? "");

    setBeschluss(initialData.beschluss ?? "");
  }, [initialData, open]);

  /* =========================================================
     SAVE
     ========================================================= */

  const handleSave = () => {
    if (!gueltigVon) return;

    const dto: FoerdersatzCreateUpdateDTO = {
      typ,

      gueltigVon: gueltigVon.format("YYYY-MM-DD"),

      gueltigBis: gueltigBis ? gueltigBis.format("YYYY-MM-DD") : null,

      foerdersatz: Number(foerdersatz),

      foerderdeckel: foerderdeckel ? Number(foerderdeckel) : null,

      beschluss: beschluss || null,
    };

    onSave(dto);
  };

  /* ========================================================= */

  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="sm">
      <DialogTitle>{initialData ? "Fördersatz bearbeiten" : "Neuer Fördersatz"}</DialogTitle>

      <DialogContent>
        <Stack spacing={2} sx={{ mt: 1 }}>
          <TextField
            select
            label="Typ"
            value={typ}
            onChange={(e) => setTyp(e.target.value as VeranstaltungTyp)}
          >
            {veranstaltungTypen.map((t) => (
              <MenuItem key={t} value={t}>
                {t}
              </MenuItem>
            ))}
          </TextField>

          <DatePicker label="Gültig von" value={gueltigVon} onChange={setGueltigVon} />

          <DatePicker label="Gültig bis" value={gueltigBis} onChange={setGueltigBis} />

          <TextField
            label="Fördersatz"
            type="number"
            value={foerdersatz}
            onChange={(e) => setFoerdersatz(e.target.value)}
          />

          <TextField
            label="Förderdeckel"
            type="number"
            value={foerderdeckel}
            onChange={(e) => setFoerderdeckel(e.target.value)}
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

export default FoerdersatzDialog;
