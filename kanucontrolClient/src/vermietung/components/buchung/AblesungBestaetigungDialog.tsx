import { Alert, Box, Button, Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle, Typography } from "@mui/material";
import type { Buchung } from "@/vermietung/types/Buchung";

const zeit = (value: string | null) => value ? new Date(value).toLocaleString("de-DE") : "–";
const zahl = (value: number | null) => value?.toLocaleString("de-DE", { maximumFractionDigits: 3 }) ?? "–";
function abstand(ablesung: string | null, termin: string) {
  if (!ablesung) return "";
  const stunden = (Date.parse(ablesung) - Date.parse(termin)) / 3_600_000;
  return `${Math.abs(stunden).toLocaleString("de-DE", { maximumFractionDigits: 1 })} Stunden ${stunden < 0 ? "vor" : "nach"} dem Termin`;
}

export default function AblesungBestaetigungDialog({ buchung, busy, onCancel, onConfirm }: {
  buchung: Buchung | null; busy: boolean; onCancel: () => void; onConfirm: () => void;
}) {
  return <Dialog open={!!buchung} onClose={() => { if (!busy) onCancel(); }} maxWidth="sm" fullWidth>
    <DialogTitle>Entfernte Zählerstände verwenden?</DialogTitle>
    <DialogContent>
      <DialogContentText>
        Mindestens eine Ablesung liegt mehr als 24 Stunden vom Buchungstermin entfernt.
        Können die folgenden nächstliegenden Zählerstände für diese Abrechnung verwendet werden?
      </DialogContentText>
      <Alert severity="warning" sx={{ my: 2 }}>Der gemessene Verbrauch kann auch Zeiten außerhalb der Buchung enthalten.</Alert>
      {buchung?.verbrauchskostenVorschau.filter(k => k.bestaetigungErforderlich).map((k, i) => <Box key={`${k.zaehlerId}-${i}`} sx={{ my: 2 }}>
        <Typography fontWeight={600}>{k.bezeichnung} – {k.zaehlerBezeichnung}</Typography>
        <Typography variant="body2">Anreise: {zeit(buchung.anreise)}</Typography>
        <Typography variant="body2">Anfang: {zahl(k.anfangsstand)} {k.mengeneinheit} am {zeit(k.anfangsablesung)}</Typography>
        <Typography variant="body2" color="text.secondary">{abstand(k.anfangsablesung, buchung.anreise)}</Typography>
        <Typography variant="body2" sx={{ mt: 1 }}>Abreise: {zeit(buchung.abreise)}</Typography>
        <Typography variant="body2">Ende: {zahl(k.endstand)} {k.mengeneinheit} am {zeit(k.endablesung)}</Typography>
        <Typography variant="body2" color="text.secondary">{abstand(k.endablesung, buchung.abreise)}</Typography>
        <Typography variant="body2" sx={{ mt: 1 }}>Verbrauch: {zahl(k.verbrauch)} {k.mengeneinheit} · Kosten: {k.kosten?.toLocaleString("de-DE", { style: "currency", currency: "EUR" })}</Typography>
      </Box>)}
    </DialogContent>
    <DialogActions>
      <Button disabled={busy} onClick={onCancel}>Abbrechen</Button>
      <Button disabled={busy} variant="contained" onClick={onConfirm}>Werte verwenden und abschließen</Button>
    </DialogActions>
  </Dialog>;
}
