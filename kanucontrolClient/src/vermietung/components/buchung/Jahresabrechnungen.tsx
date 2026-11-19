import { useEffect, useState } from "react";
import { Alert, Button, Dialog, DialogActions, DialogContent, DialogTitle, Stack, Typography } from "@mui/material";
import apiClient from "@/core/api/client/apiClient";
import { getApiErrorMessage } from "@/kjfp/api/utils/apiError";
interface Dokument { id: number; jahr: number; erstellt: string; pdfVorhanden: boolean; }
interface Uebersicht { aktuellesJahr: number; dokumente: Dokument[]; }
interface Vorschau { jahr: number; fingerprint: string; pdfBase64: string | null; miete: number; saldo: number; }
const geld = (n: number) => Number(n).toLocaleString("de-DE", { style: "currency", currency: "EUR" });
export default function Jahresabrechnungen({ id, status, unbefristet }: { id: number; status: string; unbefristet: boolean }) {
 const url = `/vermietung/buchungen/${id}/jahresabrechnungen`;
 const [daten, setDaten] = useState<Uebersicht | null>(null), [vorschau, setVorschau] = useState<Vorschau | null>(null);
 const [busy, setBusy] = useState(false), [error, setError] = useState(""), [pdfUrl, setPdfUrl] = useState("");
 useEffect(() => { let active = true; setDaten(null); setVorschau(null); setError(""); apiClient.get<Uebersicht>(url).then(r => { if (active) setDaten(r.data); }).catch(e => { if (active) setError(getApiErrorMessage(e)); }); return () => { active = false; }; }, [url]);
 useEffect(() => {
  if (!vorschau?.pdfBase64) { setPdfUrl(""); return; }
  const bytes = Uint8Array.from(atob(vorschau.pdfBase64), c => c.charCodeAt(0));
  const href = URL.createObjectURL(new Blob([bytes], { type: "application/pdf" })); setPdfUrl(href);
  return () => URL.revokeObjectURL(href);
 }, [vorschau]);
 async function pruefen() { if (!daten || busy) return; setBusy(true); setError(""); try { const r = await apiClient.get<Vorschau>(`${url}/${daten.aktuellesJahr}/vorschau`); setVorschau(r.data); } catch (e) { setError(getApiErrorMessage(e)); } finally { setBusy(false); } }
 async function abschliessen() { if (!vorschau || busy) return; setBusy(true); setError(""); try { await apiClient.post(`${url}/${vorschau.jahr}`, { fingerprint: vorschau.fingerprint }); const r = await apiClient.get<Uebersicht>(url); setDaten(r.data); setVorschau(null); } catch (e) { setVorschau(null); setError(getApiErrorMessage(e)); } finally { setBusy(false); } }
 async function download(d: Dokument) { try { const r = await apiClient.get(`${url}/${d.jahr}/pdf`, { responseType: "blob" }); const href = URL.createObjectURL(r.data); const a = document.createElement("a"); a.href = href; a.download = `Jahresabrechnung-${id}-${d.jahr}.pdf`; a.click(); setTimeout(() => URL.revokeObjectURL(href), 1000); } catch (e) { setError(getApiErrorMessage(e)); } }
 const vorhanden = daten?.dokumente.some(d => d.jahr === daten.aktuellesJahr);
 if (!unbefristet && !daten?.dokumente.length && !error) return null;
 return <Stack spacing={1} sx={{ mt: 3 }}>
  <Typography variant="h6">Jahresabrechnungen</Typography>
  {unbefristet && <Typography variant="body2">Das laufende Kalenderjahr kann jederzeit abgerechnet werden. Die Buchung bleibt ohne Enddatum und mit unverändertem Status bestehen. Es wird keine E-Mail versendet.</Typography>}
  <Alert severity="info">Abgerechnet wird die vereinbarte Miete ohne zeitanteilige Kürzung. Berücksichtigt werden Mietzahlungen und Anzahlungen mit Zahlungsdatum im Abrechnungsjahr. Kaution und Verbrauchskosten bleiben außerhalb dieser Jahresabrechnung.</Alert>
  {error && <Alert severity="error">{error}</Alert>}
  {unbefristet && daten && ["RESERVIERT", "BESTAETIGT", "ABRECHENBAR", "ABGESCHLOSSEN"].includes(status) && <Button variant="contained" disabled={busy} onClick={() => void pruefen()}>Jahresabrechnung {daten.aktuellesJahr} {vorhanden ? "neu erstellen" : "prüfen und abschließen"}</Button>}
  {daten?.dokumente.map(d => <Stack key={d.id} direction={{ xs: "column", sm: "row" }} spacing={1}><Typography>Jahr {d.jahr} abgeschlossen · {new Date(d.erstellt).toLocaleString("de-DE")}</Typography>{d.pdfVorhanden && <Button onClick={() => void download(d)}>PDF herunterladen</Button>}</Stack>)}
  <Dialog open={!!vorschau} fullWidth maxWidth="lg" onClose={() => { if (!busy) setVorschau(null); }}><DialogTitle>Jahresabrechnung {vorschau?.jahr} prüfen</DialogTitle><DialogContent><Stack spacing={2}>
   <Typography>Miete: {geld(vorschau?.miete ?? 0)} · Saldo: {geld(vorschau?.saldo ?? 0)}</Typography>
   <Typography>Der aktuelle Jahresabschluss wird gespeichert. Eine bereits vorhandene Abrechnung dieses Jahres wird ersetzt. Die unbefristete Buchung und die Kalenderbelegung bleiben bestehen.</Typography>
   {pdfUrl ? <><Button href={pdfUrl} target="_blank" rel="noopener">PDF-Vorschau öffnen</Button><iframe title="Jahresabrechnung Vorschau" src={pdfUrl} style={{ width: "100%", height: "60vh", border: 0 }} /></> : <Alert severity="info">Für Airbnb und „Ohne Abrechnung“ wird nur der Jahresabschluss gespeichert, kein PDF erstellt.</Alert>}
  </Stack></DialogContent><DialogActions><Button disabled={busy} onClick={() => setVorschau(null)}>Abbrechen</Button><Button variant="contained" disabled={busy} onClick={() => void abschliessen()}>Jahr abschließen – ohne E-Mail</Button></DialogActions></Dialog>
 </Stack>;
}
