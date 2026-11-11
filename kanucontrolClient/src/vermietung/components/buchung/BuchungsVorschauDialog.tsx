import { useEffect, useState } from "react";
import { Alert, Box, Button, Dialog, DialogActions, DialogContent, DialogTitle, Stack, TextField, Typography } from "@mui/material";
import apiClient from "@/core/api/client/apiClient";
import { getApiErrorMessage } from "@/kjfp/api/utils/apiError";
import type { Buchung } from "@/vermietung/types/Buchung";
import type { Buchungsstatus } from "@/vermietung/enums/Buchungsstatus";

interface Vorschau { ziel: Buchungsstatus; empfaenger: string | null; betreff: string; mailtext: string; pdf: string; dateiname: string; hinweise: string[] }
const namen: Partial<Record<Buchungsstatus,string>> = { RESERVIERT: "Reservierung", BESTAETIGT: "Bestätigung", ABGELEHNT: "Ablehnung", STORNIERT: "Stornierung", ABGESCHLOSSEN: "Schlussabrechnung" };
function exportieren(blob: Blob, name: string) {
  const url = URL.createObjectURL(blob), a = document.createElement("a");
  a.href = url; a.download = name; a.click(); setTimeout(() => URL.revokeObjectURL(url), 1000);
}
export default function BuchungsVorschauDialog({ buchung, ziel, onClose }: { buchung: Buchung; ziel: Buchungsstatus; onClose: () => void }) {
  const [vorschau, setVorschau] = useState<Vorschau | null>(null);
  const [betreff, setBetreff] = useState(""), [mailtext, setMailtext] = useState("");
  const [pdf, setPdf] = useState<Blob | null>(null), [pdfUrl, setPdfUrl] = useState("");
  const [importiert, setImportiert] = useState(false), [revision, setRevision] = useState(0);
  const [busy, setBusy] = useState(true), [error, setError] = useState("");
  useEffect(() => {
    let active = true; setBusy(true); setError(""); setVorschau(null); setPdf(null); setImportiert(false);
    apiClient.post<Vorschau>(`/vermietung/buchungen/${buchung.id}/nachrichten/vorschau`, { ziel }).then(({data}) => {
      if (!active) return;
      setVorschau(data); setBetreff(data.betreff); setMailtext(data.mailtext);
      setPdf(new Blob([Uint8Array.from(atob(data.pdf), c => c.charCodeAt(0))], { type: "application/pdf" }));
    }).catch(e => { if (active) setError(getApiErrorMessage(e)); }).finally(() => { if (active) setBusy(false); });
    return () => { active = false; };
  }, [buchung.id, ziel, revision]);
  useEffect(() => {
    if (!pdf) { setPdfUrl(""); return; }
    const url = URL.createObjectURL(pdf); setPdfUrl(url); return () => URL.revokeObjectURL(url);
  }, [pdf]);
  async function importPdf(file: File) {
    try {
      setError("");
      if (file.size > 10_000_000) throw new Error("PDF darf höchstens 10 MB groß sein.");
      if (!(await file.slice(0, 5).text()).startsWith("%PDF-")) throw new Error("Bitte eine gültige PDF-Datei auswählen.");
      setPdf(new Blob([await file.arrayBuffer()], { type: "application/pdf" })); setImportiert(true);
    } catch (e) { setError(getApiErrorMessage(e)); }
  }
  return <Dialog open fullWidth maxWidth="lg" onClose={onClose} slotProps={{paper:{sx:{maxHeight:"calc(100dvh - 32px)",m:2}}}}>
    <DialogTitle>{namen[ziel]} – Vorschau · {buchung.buchungsnummer}</DialogTitle>
    <DialogContent dividers sx={{minHeight:0,overflowY:"auto"}}><Stack spacing={2}>
      <Alert severity="info">Diese Vorschau des gespeicherten Buchungsstands ist unabhängig vom Statuswechsel und benötigt keine E-Mail-Adresse des Mieters. Buchungsstatus, Finanzdaten und Versand bleiben unverändert.</Alert>
      {busy && <Typography role="status">E-Mail und PDF werden erstellt …</Typography>}
      {vorschau && <>
        {vorschau.hinweise.map(h => <Typography key={h} variant="body2" color="text.secondary">{h}</Typography>)}
        <Typography>Empfänger: {vorschau.empfaenger?.trim() || "Noch keine E-Mail-Adresse beim Mieter hinterlegt"}</Typography>
        <TextField label="E-Mail-Betreff" value={betreff} onChange={e => setBetreff(e.target.value)} slotProps={{htmlInput:{maxLength:240}}}/>
        <TextField label="E-Mail-Text" multiline minRows={5} value={mailtext} onChange={e => setMailtext(e.target.value)} slotProps={{htmlInput:{maxLength:20000}}}/>
        <Typography variant="body2" color="text.secondary">Betreff und E-Mail-Text direkt oben bearbeiten. In dieser unabhängigen Vorschau werden Änderungen nicht gespeichert und beim Schließen oder erneuten Laden verworfen. Für den Versand die Texte im Statusdialog übernehmen und dort speichern.</Typography>
        <Stack direction="row" useFlexGap flexWrap="wrap" gap={1}>
          <Button disabled={!pdf} onClick={() => { if(pdf)exportieren(pdf,vorschau.dateiname); }}>PDF exportieren</Button>
          <Button component="label">PDF importieren<input hidden type="file" accept="application/pdf,.pdf" onChange={e => { const f=e.target.files?.[0];e.target.value="";if(f)void importPdf(f); }}/></Button>
        </Stack>
        <Typography variant="body2">PDF extern bearbeiten und wieder importieren, um die geänderte Fassung hier zu prüfen. Zum späteren Versand das bearbeitete PDF im Statusdialog importieren. Das importierte PDF bleibt bis zum Schließen in dieser Vorschau; bei Bedarf vorher als PDF exportieren.</Typography>
        <Typography variant="subtitle1">{importiert ? "Importierte PDF-Fassung" : "Automatisch erzeugte PDF-Vorschau"}</Typography>
        {pdfUrl && <><Button component="a" href={pdfUrl} target="_blank" rel="noopener noreferrer">PDF in neuem Fenster öffnen</Button><Box component="iframe" title="Unabhängige PDF-Vorschau" src={pdfUrl} sx={{width:"100%",height:{xs:420,md:600},border:1,borderColor:"divider"}}/></>}
      </>}
    </Stack></DialogContent>
    <DialogActions sx={{flexWrap:"wrap",px:3,pb:2,flexShrink:0}}>
      {error && <Alert severity="error" sx={{width:"100%"}}>{error}</Alert>}
      <Button disabled={busy} onClick={() => setRevision(v => v+1)}>Vorschau neu laden (Änderungen verwerfen)</Button>
      <Button onClick={onClose}>Schließen</Button>
    </DialogActions>
  </Dialog>;
}
