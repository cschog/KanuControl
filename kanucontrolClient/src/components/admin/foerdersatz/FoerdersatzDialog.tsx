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

import InputAdornment from "@mui/material/InputAdornment";

import { DatePicker } from "@mui/x-date-pickers";

import { getFoerderdeckel } from "@/api/services/configApi";

import dayjs, { Dayjs } from "dayjs";

import {
  FoerdersatzCreateUpdateDTO,
  FoerdersatzDTO,
  VeranstaltungTyp,
} from "@/api/types/Foerdersatz";
import PriceField from "@/components/common/MoneyField";

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

  const [beschluss, setBeschluss] = useState("");

  const [foerderdeckel, setFoerderdeckel] = useState<number | null>(null);

  /* =========================================================
     EDIT MODE
     ========================================================= */

  useEffect(() => {
    if (!initialData) {
      setTyp("FM");

      setGueltigVon(dayjs());

      setGueltigBis(null);

      setFoerdersatz("");

      setBeschluss("");

      return;
    }

    setTyp(initialData.typ);

    setGueltigVon(dayjs(initialData.gueltigVon));

    setGueltigBis(initialData.gueltigBis ? dayjs(initialData.gueltigBis) : null);

    setFoerdersatz(initialData.foerdersatz.toString());

    setBeschluss(initialData.beschluss ?? "");
  }, [initialData, open]);

  useEffect(() => {
    getFoerderdeckel().then(setFoerderdeckel);
  }, []);

  /* =========================================================
     SAVE
     ========================================================= */

  const handleSave = () => {

    if (!gueltigVon) {

      return;
    }

    const dto: FoerdersatzCreateUpdateDTO = {
      typ,

      gueltigVon: gueltigVon.format("YYYY-MM-DD"),

      gueltigBis: gueltigBis ? gueltigBis.format("YYYY-MM-DD") : null,

      foerdersatz: Number(foerdersatz),

      beschluss: beschluss || null,
    };

    onSave(dto);
  };

  /* ========================================================= */

  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="sm">
      <DialogTitle
        sx={{
          fontSize: {
            xs: "1.2rem",
            md: "1.4rem",
          },
          fontWeight: 700,
          pb: 1,
        }}
      >
        {initialData ? "Fördersatz bearbeiten" : "Neuer Fördersatz"}
      </DialogTitle>

      <DialogContent>
        <Stack
          spacing={2}
          sx={{
            mt: 1,

            "& .MuiTextField-root": {
              "& .MuiInputBase-input": {
                fontSize: {
                  xs: "1rem",
                  md: "1rem",
                },
              },

              "& .MuiInputLabel-root": {
                fontSize: {
                  xs: "1rem",
                  md: "1rem",
                },
              },
            },
          }}
        >
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

          <PriceField
            label="Fördersatz"
            value={foerdersatz}
            onChange={setFoerdersatz}
          />

          <TextField
            label="Förderdeckel"
            value={foerderdeckel != null ? foerderdeckel.toFixed(2).replace(".", ",") : ""}
            slotProps={{
              input: {
                readOnly: true,
                endAdornment: <InputAdornment position="end">€</InputAdornment>,
              },
            }}
          />

          <TextField
            label="Beschluss"
            value={beschluss}
            onChange={(e) => setBeschluss(e.target.value)}
          />
        </Stack>
      </DialogContent>

      <DialogActions
        sx={{
          px: 3,
          pb: 2,

          "& .MuiButton-root": {
            minWidth: 120,
            fontSize: "0.95rem",
            fontWeight: 600,
          },
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
};

export default FoerdersatzDialog;
