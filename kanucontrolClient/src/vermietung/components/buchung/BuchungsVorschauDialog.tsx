import { useEffect, useRef, useState } from "react";
import { Alert, Box, Button, Dialog, DialogActions, DialogContent, DialogTitle, MenuItem, Stack, TextField, Typography } from "@mui/material";
import apiClient from "@/core/api/client/apiClient";
import { getApiErrorMessage } from "@/kjfp/api/utils/apiError";
import type { Buchung } from "@/vermietung/types/Buchung";
import type { Buchungsstatus } from "@/vermietung/enums/Buchungsstatus";

interface Fassung {
  id: string; art: Buchungsstatus; gespeichert: boolean; herkunft: string; zeitpunkt: string;
  benutzer: string; dateiname: string; pruefsumme: string; simulation: boolean; datenGeaendert: boolean;
  betreff: string; mailtext: string; pdf: string; hinweise: string[]; letzterVersand: string | null; versandstatus: string | null;
}
interface Mail {
  dokumentId: string; pruefsumme: string; routingHash: string; mieter: string;
  routing: { absender: string; empfaenger: string; cc: string | null; replyTo: string | null; testmodus: boolean };
  betreff: string; mailtext: string; migrationsBetreff: string; migrationsText: string; fassung: Fassung; buchungsstatus: Buchungsstatus;
}
interface Versand { id: string; ergebnis: string; fehler: string | null }
const namen: Partial<Record<Buchungsstatus, string>> = {
  RESERVIERT: "Reservierungsbestätigung", BESTAETIGT: "Buchungsbestätigung", ABGELEHNT: "Ablehnung",
  STORNIERT: "Stornierung", ABGESCHLOSSEN: "Abrechnung / Rechnung als Simulation",
};
function blob(f: Fassung) { return new Blob([Uint8Array.from(atob(f.pdf), c => c.charCodeAt(0))], { type: "application/pdf" }); }
function exportieren(f: Fassung) {
  const url = URL.createObjectURL(blob(f)), a = document.createElement("a");
  a.href = url; a.download = f.dateiname; a.click(); setTimeout(() => URL.revokeObjectURL(url), 1000);
}
export default function BuchungsVorschauDialog({ buchung, ziel, ungespeichert = false, onClose, onVersandt }: {
  buchung: Buchung; ziel: Buchungsstatus; ungespeichert?: boolean; onClose: () => void; onVersandt?: () => void;
}) {
  const [art, setArt] = useState(ziel), [saved, setSaved] = useState<Fassung | null>(null);
  const [selected, setSelected] = useState<Fassung | null>(null), [preview, setPreview] = useState<Fassung | null>(null);
  const [limit, setLimit] = useState(10_000_000), [busy, setBusy] = useState(false), [loading, setLoading] = useState(true);
  const [error, setError] = useState(""), [pdfUrl, setPdfUrl] = useState("");
  const [replace, setReplace] = useState<"save" | File | null>(null);
  const [mail, setMail] = useState<Mail | null>(null), [betreff, setBetreff] = useState(""), [text, setText] = useState("");
  const [versand, setVersand] = useState<Versand | null>(null), [attempt, setAttempt] = useState("");
  const lock = useRef(false);
  const base = `/vermietung/buchungen/${buchung.id}/dokumente`;
  useEffect(() => {
    let active = true;
    setLoading(true); setSelected(null); setSaved(null); setPreview(null); setMail(null); setError("");
    apiClient.get<{ dokumente: Fassung[]; uploadLimit: number }>(base).then(({ data }) => {
      if (!active) return;
      const f = data.dokumente.find(d => d.art === art) ?? null;
      setSaved(f); setSelected(f); setLimit(data.uploadLimit);
    }).catch(e => { if (active) setError(getApiErrorMessage(e)); }).finally(() => { if (active) setLoading(false); });
    return () => { active = false; };
  }, [base, art]);
  useEffect(() => {
    if (!selected) { setPdfUrl(""); return; }
    const url = URL.createObjectURL(blob(selected)); setPdfUrl(url); return () => URL.revokeObjectURL(url);
  }, [selected]);
  async function run(action: () => Promise<void>) {
    if (lock.current) return;
    lock.current = true; setBusy(true); setError("");
    try { await action(); } catch (e) { setError(getApiErrorMessage(e)); }
    finally { lock.current = false; setBusy(false); }
  }
  async function generate() {
    await run(async () => {
      const { data } = await apiClient.post<Fassung>(`${base}/vorschau`, { art });
      setPreview(data); setSelected(data); setMail(null);
    });
  }
  async function persist(file?: File) {
    await run(async () => {
      let f: Fassung;
      if (file) {
        if (file.size > limit) throw new Error(`PDF darf höchstens ${limit / 1_000_000} MB groß sein.`);
        const form = new FormData(); form.append("datei", file); form.append("art", art);
        if (saved) form.append("ersetztId", saved.id);
        f = (await apiClient.post<Fassung>(`${base}/import`, form)).data;
      } else {
        if (!selected) return;
        f = (await apiClient.post<Fassung>(`${base}/${selected.id}/speichern`, { ersetztId: saved?.id ?? null })).data;
      }
      setSaved(f); setSelected(f); setPreview(null); setReplace(null); setMail(null);
    });
  }
  async function prepare() {
    if (!selected) return;
    await run(async () => {
      const { data } = await apiClient.post<Mail>(`${base}/${selected.id}/mail`);
      setSelected(data.fassung); setMail(data); setBetreff(data.betreff); setText(data.mailtext); setVersand(null); setAttempt(crypto.randomUUID());
    });
  }
  async function send() {
    if (!selected || !mail || versand) return;
    await run(async () => {
      const { data } = await apiClient.post<Versand>(`${base}/${selected.id}/senden`, {
        versuch: attempt, pruefsumme: mail.pruefsumme, routingHash: mail.routingHash, betreff, mailtext: text,
      });
      setVersand(data); onVersandt?.();
      const versandstand = (f: Fassung | null) => f?.id === selected.id
        ? { ...f, letzterVersand: new Date().toISOString(), versandstatus: data.ergebnis } : f;
      setSelected(versandstand); setSaved(versandstand); setPreview(versandstand);
    });
  }
  const disabled = busy || loading;
  const warnings = selected && <>
    {selected.simulation && <Alert severity="warning">ENTWURF / SIMULATION – keine endgültige Rechnung. Ein Import macht daraus keine geprüfte Rechnung.</Alert>}
    {selected.datenGeaendert && <Alert severity="warning">Buchungsdaten wurden seit Erstellung/Import geändert. Die ausgewählte PDF wird unverändert verwendet.</Alert>}
    {selected.art !== (mail?.buchungsstatus ?? buchung.status) && <Alert severity="warning">Die Dokumentart weicht vom aktuellen Buchungsstatus {mail?.buchungsstatus ?? buchung.status} ab. Der Status bleibt unverändert.</Alert>}
    {selected.herkunft === "Manuell hochgeladen" && <Alert severity="info">Manuell bearbeitete PDF. Spätere Buchungsänderungen werden nicht automatisch übernommen.</Alert>}
  </>;
  return <Dialog open fullWidth maxWidth="lg" onClose={() => { if (!busy) onClose(); }} slotProps={{ paper: { sx: { maxHeight: "calc(100dvh - 32px)", m: 2 } } }}>
    <DialogTitle>Buchungsdokumente · {buchung.buchungsnummer} · Status {buchung.status}</DialogTitle>
    <DialogContent dividers><Stack spacing={2}>
      <Alert severity="info">Vorschau, Speichern, Import und manueller Versand ändern keinen Buchungsstatus. Verwendet werden die aktuellen gespeicherten Buchungsdaten.</Alert>
      {ungespeichert && <Alert severity="warning">Das Buchungsformular ist zur Bearbeitung geöffnet. Ungespeicherte Änderungen werden nicht verwendet. Für deren Übernahme diesen Dialog schließen und die Buchung zuerst speichern.</Alert>}
      {error && <Alert severity="error">{error}</Alert>}
      <TextField select label="Dokumentart" value={art} disabled={disabled} onChange={e => setArt(e.target.value as Buchungsstatus)}>
        {Object.entries(namen).map(([key, value]) => <MenuItem key={key} value={key}>{value}</MenuItem>)}
      </TextField>
      {loading && <Typography role="status">Gespeicherte Dokumente werden geladen …</Typography>}
      {!loading && <>
        <Typography>{saved ? `Gespeichert: ${saved.herkunft} · ${new Date(saved.zeitpunkt).toLocaleString("de-DE")} · ${saved.benutzer}` : "Für diese Dokumentart ist noch keine aktuelle PDF gespeichert."}</Typography>
        <Stack direction="row" useFlexGap flexWrap="wrap" gap={1}>
          <Button disabled={disabled} onClick={() => void generate()}>Vorschau mit aktuellen Daten</Button>
          <Button disabled={disabled || !saved} onClick={() => { setSelected(saved); setMail(null); }}>Gespeicherte Fassung anzeigen</Button>
          <Button disabled={disabled || !preview} onClick={() => { setSelected(preview); setMail(null); }}>Neue Vorschau anzeigen</Button>
          <Button component="label" disabled={disabled}>Bearbeitete PDF hochladen<input hidden type="file" accept="application/pdf,.pdf" onChange={e => {
            const file = e.target.files?.[0]; e.target.value = ""; if (file) { if (saved) setReplace(file); else void persist(file); }
          }} /></Button>
        </Stack>
        <Typography variant="body2">Uploadlimit: {limit / 1_000_000} MB. PDFs werden serverseitig geprüft; es werden keine Buchungsdaten ausgelesen.</Typography>
      </>}
      {selected && <>
        <Typography variant="h6">{selected.gespeichert ? "Gespeicherte Fassung" : "Neue, noch ungespeicherte Vorschau"} · {selected.dateiname}</Typography>
        <Typography variant="body2">{selected.herkunft} · {new Date(selected.zeitpunkt).toLocaleString("de-DE")}</Typography>
        {warnings}
        {selected.hinweise.map(h => <Typography key={h} variant="body2">{h}</Typography>)}
        {selected.letzterVersand && <Typography>Letzter Versand: {new Date(selected.letzterVersand).toLocaleString("de-DE")} · {selected.versandstatus}</Typography>}
        <Stack direction="row" useFlexGap flexWrap="wrap" gap={1}>
          <Button disabled={disabled || selected.gespeichert} onClick={() => { if (saved) setReplace("save"); else void persist(); }}>PDF speichern</Button>
          <Button disabled={disabled} onClick={() => exportieren(selected)}>Herunterladen</Button>
          <Button disabled={disabled} onClick={() => void prepare()}>E-Mail vorbereiten</Button>
        </Stack>
        {pdfUrl && <Box component="iframe" title="Ausgewählte PDF-Fassung" src={pdfUrl} sx={{ width: "100%", height: { xs: 420, md: 600 }, border: 1, borderColor: "divider" }} />}
      </>}
    </Stack></DialogContent>
    <DialogActions><Button disabled={busy} onClick={onClose}>Schließen</Button></DialogActions>
    <Dialog open={!!replace} onClose={() => { if (!busy) setReplace(null); }}>
      <DialogTitle>Gespeicherte PDF ersetzen?</DialogTitle><DialogContent>Die aktuelle Fassung wird ersetzt. Bereits versendete Anhänge bleiben im Versandprotokoll erhalten.</DialogContent>
      <DialogActions><Button disabled={busy} onClick={() => setReplace(null)}>Abbrechen</Button><Button disabled={busy} onClick={() => void persist(replace instanceof File ? replace : undefined)}>Fassung ausdrücklich ersetzen</Button></DialogActions>
    </Dialog>
    <Dialog open={!!mail} fullWidth maxWidth="lg" onClose={() => { if (!busy) setMail(null); }}>
      <DialogTitle>E-Mail vorbereiten · {buchung.buchungsnummer}</DialogTitle>
      <DialogContent dividers><Stack spacing={2}>
        {error && <Alert severity="error">{error}</Alert>}{warnings}
        {mail && <>
          <Typography>Mieter: {mail.mieter}</Typography>
          {mail.routing.testmodus && <Alert severity="info">SMTP-Testmodus aktiv. Tatsächliches Ziel: {mail.routing.empfaenger}. Kopien werden gemäß Testmodus behandelt.</Alert>}
          <Typography>Von: {mail.routing.absender} · An: {mail.routing.empfaenger}</Typography>
          {mail.routing.cc && <Typography>CC: {mail.routing.cc}</Typography>}
          {mail.routing.replyTo && <Typography>Antwort an: {mail.routing.replyTo}</Typography>}
          <Button disabled={busy || !!versand} onClick={() => { setBetreff(mail.migrationsBetreff); setText(mail.migrationsText); }}>Mailvorschlag „Umstieg auf KanuControl“ übernehmen</Button>
          <TextField label="Betreff" value={betreff} disabled={busy || !!versand} onChange={e => setBetreff(e.target.value)} slotProps={{ htmlInput: { maxLength: 240 } }} />
          <TextField label="Mailtext" multiline minRows={6} value={text} disabled={busy || !!versand} onChange={e => setText(e.target.value)} slotProps={{ htmlInput: { maxLength: 30000 } }} />
          <Typography>Anhang: {selected?.dateiname} · {selected?.herkunft} · SHA-256: {mail.pruefsumme}</Typography>
          {pdfUrl && <Box component="iframe" title="Exakter E-Mail-Anhang" src={pdfUrl} sx={{ width: "100%", height: 400, border: 1, borderColor: "divider" }} />}
        </>}
        {versand && <Alert severity={versand.ergebnis === "SMTP_BESTAETIGT" ? "success" : "warning"}>{versand.ergebnis === "SMTP_BESTAETIGT" ? "Nachricht an SMTP übergeben." : versand.fehler || "Versandergebnis unklar. Postausgang prüfen; keine automatische Wiederholung."}</Alert>}
      </Stack></DialogContent>
      <DialogActions>
        <Button disabled={busy} onClick={() => setMail(null)}>Schließen</Button>
        {versand && <Button disabled={busy} onClick={() => { setVersand(null); setAttempt(crypto.randomUUID()); }}>Bewusst einen neuen Versand vorbereiten</Button>}
        <Button variant="contained" disabled={busy || !!versand || !betreff.trim() || !text.trim()} onClick={() => void send()}>{busy ? "Wird verarbeitet …" : "Jetzt senden"}</Button>
      </DialogActions>
    </Dialog>
  </Dialog>;
}
