import VorlagenVersandarchiv from "./VorlagenVersandarchiv";
import { useEffect, useRef, useState } from "react";
import {
  Alert, Box, Button, CircularProgress, Dialog, DialogActions, DialogContent,
  DialogTitle, Stack, Typography, useMediaQuery, useTheme,
} from "@mui/material";
import apiClient from "@/core/api/client/apiClient";
import { getApiErrorMessage } from "@/kjfp/api/utils/apiError";

interface Vorschau {
  pdf: string;
  bild: string;
  dateiname: string;
  betreff: string;
  text: string;
  empfaenger: string;
  cc: string | null;
  testmodus: boolean;
  fingerprint: string;
}
interface Props {
  id: number;
  reserviert: boolean;
  gesendetAm?: string | null;
  onClose: () => void;
  onReserve: (sendEmail: boolean) => Promise<void> | void;
  onRefresh?: () => Promise<void>;
}

export default function ReservierungsbestaetigungDialog({ id, reserviert, gesendetAm, onClose, onReserve, onRefresh }: Props) {
  const fullScreen = useMediaQuery(useTheme().breakpoints.down("sm"));
  const [vorschau, setVorschau] = useState<Vorschau | null>(null);
  const [pdfUrl, setPdfUrl] = useState<string | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);
  const [versendet, setVersendet] = useState(!!gesendetAm);
  const [versuch, setVersuch] = useState(0);
  const busyRef = useRef(false);
  const url = `/vermietung/buchungen/${id}/reservierungsbestaetigung`;

  useEffect(() => {
    const controller = new AbortController();
    let objectUrl: string | null = null;
    setVorschau(null); setPdfUrl(null); setError(null);
    apiClient.get<Vorschau>(url, { signal: controller.signal }).then(({ data }) => {
      if (controller.signal.aborted) return;
      const bytes = Uint8Array.from(atob(data.pdf), c => c.charCodeAt(0));
      objectUrl = URL.createObjectURL(new Blob([bytes], { type: "application/pdf" }));
      setPdfUrl(objectUrl); setVorschau(data);
    }).catch((err: unknown) => {
      if (!controller.signal.aborted) setError(getApiErrorMessage(err, "Die Vorschau konnte nicht geladen werden."));
    });
    return () => { controller.abort(); if (objectUrl) URL.revokeObjectURL(objectUrl); };
  }, [url, versuch]);

  async function bestaetigen(sendEmail: boolean) {
    if (busyRef.current || (sendEmail && !vorschau)) return;
    busyRef.current = true; setBusy(true); setError(null);
    try {
      if (reserviert) {
        await apiClient.post(`${url}/senden`, { fingerprint: vorschau?.fingerprint });
        setVersendet(true);
        await onRefresh?.();
      } else {
        // Parent handles errors and refreshes the actual status, including committed reservations after SMTP failures.
        if (sendEmail) {
          await apiClient.post(`${url}/reservieren`, { fingerprint: vorschau?.fingerprint });
          await onRefresh?.();
          onClose();
        } else { onClose(); await onReserve(false); }
      }
    } catch (err: unknown) {
      setError(getApiErrorMessage(err));
      setVorschau(null);
      await onRefresh?.();
    } finally { busyRef.current = false; setBusy(false); }
  }

  return <Dialog open fullScreen={fullScreen} fullWidth maxWidth="md" onClose={() => { if (!busy) onClose(); }} aria-labelledby="reservierung-vorschau-title">
    <DialogTitle id="reservierung-vorschau-title">Vorschau der Reservierungsbestätigung</DialogTitle>
    <DialogContent dividers sx={{ p: { xs: 1.5, sm: 3 }, overflowWrap: "anywhere" }}>
      <Stack spacing={2}>
        <Typography variant="body2">{reserviert
          ? "Die Vorschau zeigt die aktuell gespeicherten Buchungsdaten."
          : "Bitte prüfe die Bestätigung. Die Anfrage bleibt unverändert, bis du unten reservierst."}</Typography>
        {error && <Alert severity="error" action={!vorschau ? <Button color="inherit" onClick={() => setVersuch(v => v + 1)}>Neu laden</Button> : undefined}>{error}</Alert>}
        {!vorschau && !error && <Box role="status" sx={{ display: "flex", gap: 2, alignItems: "center" }}><CircularProgress size={24} />Vorschau wird erstellt …</Box>}
        <VorlagenVersandarchiv id={id} refresh={versendet} />
        {vorschau && <>
          {vorschau.testmodus && <Alert severity="info">Testversand aktiv. Empfänger und Kopie stehen unten.</Alert>}
          <Box>
            <Typography variant="body2"><strong>An:</strong> {vorschau.empfaenger}</Typography>
            {vorschau.cc && <Typography variant="body2"><strong>Kopie:</strong> {vorschau.cc}</Typography>}
            <Typography variant="body2"><strong>Betreff:</strong> {vorschau.betreff}</Typography>
          </Box>
          <Typography variant="body2" sx={{ whiteSpace: "pre-wrap" }}>{vorschau.text}</Typography>
          {pdfUrl && <Button component="a" href={pdfUrl} target="_blank" rel="noopener noreferrer">PDF öffnen</Button>}
          <Box component="img" src={`data:image/png;base64,${vorschau.bild}`} alt="Ausgefüllte Reservierungsbestätigung mit Mietbetrag, Zahlungsplan und Bankverbindung" sx={{ display: "block", width: "100%", height: "auto", border: "1px solid", borderColor: "divider" }} />
          {pdfUrl && <Box component="iframe" title="Vollständiges PDF einschließlich Entschädigungsliste" src={pdfUrl} sx={{ width: "100%", height: 650, border: 0 }} />}
          {versendet && <Alert severity="success">Die Reservierungsbestätigung wurde an den SMTP-Server übergeben.</Alert>}
        </>}
      </Stack>
    </DialogContent>
    <DialogActions sx={{ p: 2, gap: 1, flexWrap: "wrap", "& > :not(style) ~ :not(style)": { ml: 0 } }}>
      <Button disabled={busy} onClick={onClose}>{reserviert ? "Schließen" : "Abbrechen"}</Button>
      {!reserviert && <Button disabled={busy || (!vorschau && !error)} onClick={() => void bestaetigen(false)}>Reservieren ohne E-Mail</Button>}
      {!versendet && <Button variant="contained" disabled={busy || !vorschau} onClick={() => void bestaetigen(true)}>
        {busy ? "Wird verarbeitet …" : reserviert ? "Bestätigung senden" : "Reservieren und E-Mail senden"}
      </Button>}
    </DialogActions>
  </Dialog>;
}
