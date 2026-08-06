import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  MenuItem,
  TextField,
  Stack,
  Typography,
} from "@mui/material";

import Accordion from "@mui/material/Accordion";
import AccordionSummary from "@mui/material/AccordionSummary";
import AccordionDetails from "@mui/material/AccordionDetails";
import ExpandMoreIcon from "@mui/icons-material/ExpandMore";

import { useEffect, useState } from "react";

import { BelegCreate, AbrechnungBeleg } from "@/api/types/abrechnung";

import BelegDokumentPanel from "@/components/finanzen/abrechnung/BelegDokumentPanel";

interface Props {
  open: boolean;
  kuerzelListe: string[];
  onClose: () => void;
  onSave: (data: BelegCreate) => void | Promise<void>;
  initialData?: AbrechnungBeleg;
}

export default function BelegDialog({ open, kuerzelListe, onClose, onSave, initialData }: Props) {
  const [datum, setDatum] = useState("");
  const [beschreibung, setBeschreibung] = useState("");
  const [aussteller, setAussteller] = useState("");
  const [externeBelegnummer, setExterneBelegnummer] = useState("");
  const [kuerzel, setKuerzel] = useState("");

  useEffect(() => {
    if (initialData) {
      setKuerzel(initialData.kuerzel);
      setDatum(initialData.datum);
      setAussteller(initialData.aussteller ?? "");
      setExterneBelegnummer(initialData.externeBelegnummer ?? "");
      setBeschreibung(initialData.beschreibung ?? "");
    } else {
      setKuerzel("");
      setDatum("");
      setAussteller("");
      setExterneBelegnummer("");
      setBeschreibung("");
    }
  }, [initialData, open]);

  const resetForm = () => {
    setDatum("");
    setAussteller("");
    setExterneBelegnummer("");
    setBeschreibung("");
    setKuerzel("");
  };

  const handleSave = async () => {
    if (!datum || !kuerzel) return;

   await onSave({
     kuerzel,
     datum,
     aussteller,
     externeBelegnummer,
     beschreibung,
   });

    resetForm();
  };

  return (
    <Dialog open={open} onClose={onClose} fullWidth maxWidth="md">
      <DialogTitle>
        <Stack direction="row" justifyContent="space-between" alignItems="center">
          <Typography variant="h5" fontWeight={700}>
            {initialData ? "Beleg bearbeiten" : "Neuer Beleg"}
          </Typography>

          {initialData && (
            <Typography variant="h6" fontWeight={700} color="primary">
              {initialData.belegnummer}
            </Typography>
          )}
        </Stack>
      </DialogTitle>

      <DialogContent>
        <Stack spacing={2} mt={1}>
          <TextField
            select
            label="Finanzgruppe"
            value={kuerzel}
            onChange={(e) => setKuerzel(e.target.value)}
            required
            fullWidth
          >
            {kuerzelListe.map((k) => (
              <MenuItem key={k} value={k}>
                {k}
              </MenuItem>
            ))}
          </TextField>

          <TextField
            label="Datum"
            type="date"
            value={datum}
            onChange={(e) => setDatum(e.target.value)}
            InputLabelProps={{ shrink: true }}
            required
            fullWidth
          />

          <TextField
            label="Aussteller"
            value={aussteller}
            onChange={(e) => setAussteller(e.target.value)}
            fullWidth
          />

          <TextField
            label="Externe Belegnummer"
            value={externeBelegnummer}
            onChange={(e) => setExterneBelegnummer(e.target.value)}
            fullWidth
          />

          <TextField
            label="Beschreibung"
            value={beschreibung}
            onChange={(e) => setBeschreibung(e.target.value)}
            fullWidth
          />
        </Stack>
        {initialData?.id && (
          <Accordion sx={{ mt: 3 }}>
            <AccordionSummary expandIcon={<ExpandMoreIcon />}>
              <Typography variant="h6">Belegdokumente</Typography>
            </AccordionSummary>

            <AccordionDetails>
              <BelegDokumentPanel belegId={initialData.id} />
            </AccordionDetails>
          </Accordion>
        )}

        {!initialData && (
          <Typography sx={{ mt: 3 }} variant="body2" color="text.secondary">
            Dokumente können nach dem ersten Speichern des Belegs hochgeladen werden.
          </Typography>
        )}
      </DialogContent>

      <DialogActions>
        <Button onClick={onClose}>Abbrechen</Button>
        <Button variant="contained" onClick={handleSave}>
          Speichern
        </Button>
      </DialogActions>
    </Dialog>
  );
}
