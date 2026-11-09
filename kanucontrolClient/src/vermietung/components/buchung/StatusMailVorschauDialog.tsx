import { useEffect, useRef, useState } from "react";
import { Alert, Box, Button, CircularProgress, Dialog, DialogActions, DialogContent, DialogTitle, Stack, Typography, useMediaQuery, useTheme } from "@mui/material";
import apiClient from "@/core/api/client/apiClient";
import { getApiErrorMessage } from "@/kjfp/api/utils/apiError";
import type { Buchungsstatus } from "@/vermietung/enums/Buchungsstatus";

interface Vorschau {
  absender: string; empfaenger: string; cc: string | null; replyTo: string | null;
  betreff: string; text: string; testmodus: boolean; fingerprint: string;
}
const statusNamen: Partial<Record<Buchungsstatus, string>> = {
  RESERVIERT: "Reserviert", BESTAETIGT: "Bestätigt", ABGELEHNT: "Abgelehnt", STORNIERT: "Storniert",
  ABRECHENBAR: "Abrechenbar", ABGESCHLOSSEN: "Abgeschlossen",
};
export default function StatusMailVorschauDialog({ id, status, onClose, onDecision }: {
  id: number; status: Buchungsstatus; onClose: () => void;
  onDecision: (sendEmail: boolean, fingerprint?: string) => Promise<void>;
}) {
  const fullScreen = useMediaQuery(useTheme().breakpoints.down("sm"));
  const [vorschau, setVorschau] = useState<Vorschau | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);
  const [versuch, setVersuch] = useState(0);
  const busyRef = useRef(false);
  useEffect(() => {
    const controller = new AbortController();
    setVorschau(null); setError(null);
    apiClient.get<Vorschau>(`/vermietung/buchungen/${id}/status/vorschau`, { params: { status }, signal: controller.signal })
      .then(({ data }) => { if (!controller.signal.aborted) setVorschau(data); })
      .catch((err: unknown) => { if (!controller.signal.aborted) setError(getApiErrorMessage(err, "Die E-Mail-Vorschau konnte nicht geladen werden.")); });
    return () => controller.abort();
  }, [id, status, versuch]);
  async function entscheiden(sendEmail: boolean) {
    if (busyRef.current || (sendEmail && !vorschau)) return;
    busyRef.current = true; setBusy(true); setError(null);
    try { await onDecision(sendEmail, sendEmail ? vorschau?.fingerprint : undefined); }
    catch (err: unknown) { setError(getApiErrorMessage(err)); setVorschau(null); }
    finally { busyRef.current = false; setBusy(false); }
  }
  return <Dialog open fullScreen={fullScreen} fullWidth maxWidth="md" onClose={() => { if (!busy) onClose(); }} aria-labelledby="status-mail-vorschau-title">
    <DialogTitle id="status-mail-vorschau-title">E-Mail vor dem Statuswechsel prüfen</DialogTitle>
    <DialogContent dividers><Stack spacing={2}>
      <Typography>Geplanter Status: {statusNamen[status] ?? status}. Die Buchung bleibt unverändert, bis du dich unten entscheidest.</Typography>
      {error && <Alert severity="error" action={<Button color="inherit" disabled={busy} onClick={() => setVersuch(v => v + 1)}>Vorschau neu laden</Button>}>{error}</Alert>}
      {!vorschau && !error && <Box role="status" sx={{ display: "flex", gap: 2, alignItems: "center" }}><CircularProgress size={24} />E-Mail-Vorschau wird geladen …</Box>}
      {vorschau && <>
        {vorschau.testmodus && <Alert severity="info">Testversand aktiv. Angezeigt werden die tatsächlich verwendeten Empfänger.</Alert>}
        <Box sx={{ overflowWrap: "anywhere" }}>
          <Typography><strong>Von:</strong> {vorschau.absender || "Noch nicht konfiguriert"}</Typography>
          <Typography><strong>An:</strong> {vorschau.empfaenger}</Typography>
          {vorschau.cc && <Typography><strong>Kopie:</strong> {vorschau.cc}</Typography>}
          {vorschau.replyTo && <Typography><strong>Antwort an:</strong> {vorschau.replyTo}</Typography>}
          <Typography><strong>Betreff:</strong> {vorschau.betreff}</Typography>
        </Box>
        <Typography sx={{ whiteSpace: "pre-wrap", overflowWrap: "anywhere" }}>{vorschau.text}</Typography>
        <Typography variant="body2" color="text.secondary">Diese E-Mail enthält keine Anhänge.</Typography>
      </>}
    </Stack></DialogContent>
    <DialogActions sx={{ flexWrap: "wrap", gap: 1 }}>
      <Button autoFocus disabled={busy} onClick={onClose}>Abbrechen</Button>
      <Button disabled={busy || (!vorschau && !error)} onClick={() => void entscheiden(false)}>Status ändern ohne E-Mail</Button>
      <Button variant="contained" disabled={busy || !vorschau} onClick={() => void entscheiden(true)}>{busy ? "Wird verarbeitet …" : "Status ändern und diese E-Mail senden"}</Button>
    </DialogActions>
  </Dialog>;
}
