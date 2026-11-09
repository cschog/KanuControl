import { useRef, useState } from "react";
import { Alert, Button, Dialog, DialogActions, DialogContent, DialogTitle, Stack, TextField, Typography } from "@mui/material";
import { getApiErrorMessage } from "@/kjfp/api/utils/apiError";

export default function BuchungsnummerAendern({ nummer, onSave }: { nummer: string; onSave: (nummer: string) => Promise<void> }) {
  const [open, setOpen] = useState(false);
  const [value, setValue] = useState(nummer);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const busyRef = useRef(false);
  const match = /^([1-9][0-9]{3})-([0-9]{1,10})$/.exec(value.trim());
  const laufend = match ? Number(match[2]) : 0;
  const valid = !!match && laufend > 0 && laufend < 2147483647;
  const neueNummer = value.trim();
  async function save() {
    if (busyRef.current || !valid || neueNummer === nummer) return;
    busyRef.current = true; setBusy(true); setError(null);
    try { await onSave(neueNummer); setOpen(false); }
    catch (err: unknown) { setError(getApiErrorMessage(err, "Die Buchungsnummer konnte nicht geändert werden.")); }
    finally { busyRef.current = false; setBusy(false); }
  }
  return <>
    <Button variant="outlined" onClick={() => { setValue(nummer); setError(null); setOpen(true); }}>Buchungsnummer ändern</Button>
    <Dialog open={open} fullWidth maxWidth="sm" onClose={() => { if (!busy) setOpen(false); }} aria-labelledby="buchungsnummer-title">
      <DialogTitle id="buchungsnummer-title">Buchungsnummer ändern</DialogTitle>
      <DialogContent><Stack spacing={2} sx={{ pt: 1 }}>
        <Typography>Aktuelle Nummer: {nummer}</Typography>
        <TextField autoFocus label="Neue Buchungsnummer" value={value} disabled={busy}
          onChange={e => { setValue(e.target.value); setError(null); }} error={!valid}
          helperText="Format: Jahr-laufende Nummer. Führende Nullen bleiben erhalten: 2026-001 und 2026-01 sind unterschiedliche Nummern. Die genaue Nummer muss im Mandanten eindeutig sein."
          slotProps={{ htmlInput: { maxLength: 15 } }} />
        <Typography variant="body2">Bereits ausgestellte PDFs und versendete E-Mails behalten ihre ursprüngliche Nummer. Durch diese Änderung wird keine E-Mail versendet.</Typography>
        {error && <Alert severity="error">{error}</Alert>}
      </Stack></DialogContent>
      <DialogActions>
        <Button disabled={busy} onClick={() => setOpen(false)}>Abbrechen</Button>
        <Button variant="contained" disabled={busy || !valid || neueNummer === nummer} onClick={() => void save()}>{busy ? "Wird gespeichert …" : "Buchungsnummer speichern"}</Button>
      </DialogActions>
    </Dialog>
  </>;
}
