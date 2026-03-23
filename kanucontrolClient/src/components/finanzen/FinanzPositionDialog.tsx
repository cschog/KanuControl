import {
  Dialog,
  DialogTitle,
  DialogContent,
  DialogActions,
  Button,
  TextField,
  MenuItem,
} from "@mui/material";
import { useEffect, useState } from "react";

import { FinanzKategorie } from "@/api/types/finanz";
import type { PlanungPosition, PlanungPositionCreate } from "@/api/types/planung";

interface Props {
  open: boolean;
  typ: "KOSTEN" | "EINNAHME";
  initialData?: PlanungPosition;
  onClose: () => void;
  onSave: (data: PlanungPositionCreate) => void;
}

const kategorien: Record<"KOSTEN" | "EINNAHME", FinanzKategorie[]> = {
  KOSTEN: [
    "UNTERKUNFT",
    "VERPFLEGUNG",
    "HONORARE",
    "FAHRTKOSTEN",
    "VERBRAUCHSMATERIAL",
    "KULTUR",
    "MIETE",
    "SONSTIGE_KOSTEN",
  ],
  EINNAHME: ["TEILNEHMERBEITRAG", "PFAND", "KJFP_ZUSCHUSS", "SONSTIGE_EINNAHMEN"],
};

export default function FinanzPositionDialog({ open, typ, initialData, onClose, onSave }: Props) {
  const [kategorie, setKategorie] = useState<FinanzKategorie | "">("");

  const [betrag, setBetrag] = useState<number>(0);
  const [kommentar, setKommentar] = useState<string>("");

  useEffect(() => {
    if (initialData) {
      setKategorie(initialData.kategorie);
      setBetrag(initialData.betrag);
      setKommentar(initialData.kommentar ?? "");
    }
  }, [initialData]);

  const handleSave = () => {
    if (!kategorie || betrag <= 0) return;

    onSave({
      kategorie,
      betrag,
      kommentar,
    });
  };

  return (
    <Dialog open={open} onClose={onClose} fullWidth>
      <DialogTitle>{typ === "KOSTEN" ? "Ausgabe erfassen" : "Einnahme erfassen"}</DialogTitle>

      <DialogContent>
        <TextField
          select
          label="Kategorie"
          fullWidth
          margin="normal"
          value={kategorie}
          onChange={(e) => setKategorie(e.target.value as FinanzKategorie)}
        >
          {kategorien[typ].map((k) => (
            <MenuItem key={k} value={k}>
              {k.replaceAll("_", " ")}
            </MenuItem>
          ))}
        </TextField>

        <TextField
          label="Betrag"
          type="number"
          fullWidth
          margin="normal"
          value={betrag}
          onChange={(e) => setBetrag(Number(e.target.value))}
        />

        <TextField
          label="Kommentar"
          fullWidth
          margin="normal"
          value={kommentar}
          onChange={(e) => setKommentar(e.target.value)}
        />
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
