import { useEffect, useState } from "react";
import { Alert, Button, Dialog, DialogActions, DialogContent, DialogTitle, Stack, Typography } from "@mui/material";
import apiClient from "@/core/api/client/apiClient";
import { getApiErrorMessage } from "@/kjfp/api/utils/apiError";
interface Eintrag { id: string; erstelltAm: string; ergebnis: string; smtpAm: string | null; vorgang: string }
interface Nachricht { betreff?: string; mailtext?: string; routing?: { empfaenger: string; cc: string | null; betreff: string }; dateiname?: string; externAm?: string | null; }
export default function VorlagenVersandarchiv({ id, refresh }: { id: number; refresh: boolean }) {
  const [nachricht, setNachricht] = useState<Nachricht | null>(null);
  const [rows, setRows] = useState<Eintrag[]>([]);
  const [error, setError] = useState<string | null>(null);
  const [reload, setReload] = useState(0);
  useEffect(() => { let cancelled = false; setRows([]); setError(null); apiClient.get<Eintrag[]>(`/vermietung/vorlagen/archiv/${id}`).then(r => { if (!cancelled) setRows(r.data); }).catch((e: unknown) => { if (!cancelled) setError(getApiErrorMessage(e)); }); return () => { cancelled = true; }; }, [id, refresh, reload]);
  async function anzeigen(row: Eintrag) {
    try { setNachricht((await apiClient.get<Nachricht>(`/vermietung/vorlagen/archiv/nachricht/${row.id}`)).data); }
    catch (e: unknown) { setError(getApiErrorMessage(e)); }
  }
  async function download(row: Eintrag) {
    try {
      const { data } = await apiClient.get<Blob>(`/vermietung/vorlagen/archiv/datei/${row.id}`, { responseType: "blob" });
      const url = URL.createObjectURL(data); const a = document.createElement("a"); a.href = url; a.download = `Versand-${id}-${row.id}.pdf`; a.click(); setTimeout(() => URL.revokeObjectURL(url), 1000);
    } catch (e: unknown) { setError(getApiErrorMessage(e)); }
  }
  return <Stack spacing={1}><Typography variant="h6">Versandarchiv</Typography>{error && <Alert severity="error" action={<Button color="inherit" onClick={() => setReload(value => value + 1)}>Erneut laden</Button>}>{error}</Alert>}{rows.length === 0 && !error && <Typography variant="body2">Noch kein archivierter Versandversuch.</Typography>}{rows.map(row => <Stack key={row.id} direction={{ xs: "column", sm: "row" }} spacing={1}><Typography>{new Date(row.erstelltAm).toLocaleString("de-DE")} · {row.ergebnis === "SMTP_BESTAETIGT" ? "An SMTP übergeben" : row.ergebnis === "VORBEREITET" ? "Vorbereitet; Versand nicht bestätigt" : "Fehlgeschlagen oder unklar"}</Typography><Button onClick={() => void anzeigen(row)}>Archivierte Nachricht ansehen</Button><Button onClick={() => void download(row)}>Archiviertes PDF herunterladen</Button></Stack>)}<Dialog open={!!nachricht} onClose={() => setNachricht(null)} fullWidth maxWidth="md"><DialogTitle>{nachricht?.routing?.betreff ?? nachricht?.betreff ?? "Archivierte Nachricht"}</DialogTitle><DialogContent><Stack spacing={2}>{nachricht?.mailtext ? <><Typography>An: {nachricht.routing?.empfaenger}{nachricht.routing?.cc ? ` · Kopie: ${nachricht.routing.cc}` : ""}</Typography><Typography sx={{ whiteSpace: "pre-wrap" }}>{nachricht.mailtext}</Typography><Typography>{nachricht.dateiname} · {nachricht.externAm ? "Extern bearbeiteter Anhang" : "Automatisch erzeugter Anhang"}</Typography></> : <Alert severity="info">Für diesen älteren Archivversuch wurde noch kein vollständiger Mailtext gespeichert. Der archivierte PDF-Anhang bleibt verfügbar.</Alert>}</Stack></DialogContent><DialogActions><Button onClick={() => setNachricht(null)}>Schließen</Button></DialogActions></Dialog></Stack>;
}
