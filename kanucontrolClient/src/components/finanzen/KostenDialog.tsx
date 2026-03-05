import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  TextField,
  MenuItem,
  Stack,
} from "@mui/material";
import { useState } from "react";

interface Props {
  open: boolean;
  onClose: () => void;
}

const personen = [
  { id: 1, kuerzel: "MS" },
  { id: 2, kuerzel: "AB" },
];

const kategorien = ["UNTERKUNFT", "VERPFLEGUNG", "MATERIAL", "TEILNEHMERBEITRAG", "KJFP_ZUSCHUSS"];

const KostenDialog = ({ open, onClose }: Props) => {
  const [datum, setDatum] = useState("");
  const [person, setPerson] = useState("");
  const [kategorie, setKategorie] = useState("");
  const [betrag, setBetrag] = useState("");
  const [kommentar, setKommentar] = useState("");

  const handleSave = () => {
    const data = {
      datum,
      person,
      kategorie,
      betrag,
      kommentar,
    };

    console.log("Neue Buchung", data);

    onClose();
  };

  return (
    <Dialog open={open} onClose={onClose} maxWidth="sm" fullWidth>
      <DialogTitle>Neue Buchung</DialogTitle>

      <DialogContent>
        <Stack spacing={2} mt={1}>
          <TextField
            label="Datum"
            type="date"
            InputLabelProps={{ shrink: true }}
            value={datum}
            onChange={(e) => setDatum(e.target.value)}
          />

          <TextField
            label="Person"
            select
            value={person}
            onChange={(e) => setPerson(e.target.value)}
          >
            <MenuItem value="">-</MenuItem>

            {personen.map((p) => (
              <MenuItem key={p.id} value={p.id}>
                {p.kuerzel}
              </MenuItem>
            ))}
          </TextField>

          <TextField
            label="Kategorie"
            select
            value={kategorie}
            onChange={(e) => setKategorie(e.target.value)}
          >
            {kategorien.map((k) => (
              <MenuItem key={k} value={k}>
                {k}
              </MenuItem>
            ))}
          </TextField>

          <TextField
            label="Betrag"
            type="number"
            value={betrag}
            onChange={(e) => setBetrag(e.target.value)}
          />

          <TextField
            label="Kommentar"
            value={kommentar}
            onChange={(e) => setKommentar(e.target.value)}
          />
        </Stack>
      </DialogContent>

      <DialogActions>
        <Button onClick={onClose}>Abbrechen</Button>

        <Button variant="contained" onClick={handleSave}>
          Speichern
        </Button>
      </DialogActions>
    </Dialog>
  );
};

export default KostenDialog;
