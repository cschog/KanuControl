import { useEffect, useRef, useState } from "react";
import { Alert, Box, Button, Checkbox, Dialog, DialogActions, DialogContent, DialogTitle, FormControlLabel, MenuItem, Stack, TextField, Typography } from "@mui/material";
import apiClient from "@/core/api/client/apiClient";
import { getApiErrorMessage } from "@/kjfp/api/utils/apiError";
import type { Mietobjekt } from "@/vermietung/types/Mietobjekt";

type Aktion = "NEU" | "ERSETZEN" | "UEBERSPRINGEN";
interface Plan { ohneLayoutFreigabe?: string[]; briefpapier: boolean; zuordnungen: { quelle: string; ziel: number | null; ueberspringen: boolean }[]; auswahl: { quelle: string; vorgang: string; aktion: Aktion }[] }
interface Manifest { quelle: { mandantBezeichnung: string }; briefpapier: { bild: string | null } | null; mietobjekte: { schluessel: string; bezeichnung: string }[]; vorlagen: { mietobjekt: string; vorgang: string; datei: string }[] }
interface Ansicht { id: string; ablauf: string; manifest: Manifest; plan: Plan | null; vergleich: { schluessel: string; ziel: number; vorgang: string; aktion: Aktion; vorher: unknown; nachher: unknown }[]; grussVorher: string | null; grussNachher: string | null; bildWirdEntfernt: boolean; neu: number; ersetzt: number; uebersprungen: number; objekteUebersprungen: number }
interface Probe { schluessel: string; szenario: string; pdf: string; betreff: string; text: string }
interface Vorschau { status: { schluessel: string; erfolgreich: boolean; ursache: string | null; ausdruecklichFreigegeben: boolean }[]; vorlagen: Probe[]; briefpapier: string | null; hinweis: string }
const vorgangNamen: Record<string, string> = {
  RESERVIERUNG: "Reservierung", BESTAETIGUNG: "Bestätigung", STORNIERUNG: "Stornierung",
  ABLEHNUNG: "Ablehnung", ABRECHENBAR: "Vorbereitung der Abrechnung", ABGERECHNET: "Schlussabrechnung",
};
function vorschauName(schluessel: string) {
  const vorgang = schluessel.split("/").at(-1) ?? schluessel;
  return vorgangNamen[vorgang] ?? schluessel;
}
const base = "/vermietung/vorlagen/pakete";
function Pdf({ value }: { value: string }) {
  const [url, setUrl] = useState("");
  useEffect(() => { const u = URL.createObjectURL(new Blob([Uint8Array.from(atob(value), c => c.charCodeAt(0))], { type: "application/pdf" })); setUrl(u); return () => URL.revokeObjectURL(u); }, [value]);
  return <Box><Button component="a" href={url} target="_blank" rel="noopener noreferrer">PDF öffnen und prüfen</Button><Box component="iframe" key={url} title="Importvorschau" src={url} sx={{ width: "100%", height: 550, border: 0 }} /></Box>;
}
export default function VorlagenPakete({ objekte, ungespeichert, briefpapierVorhanden, onImported }: { objekte: Mietobjekt[]; ungespeichert: boolean; briefpapierVorhanden?: boolean; onImported: () => void }) {
  const [open, setOpen] = useState(false), [busy, setBusy] = useState(false), [error, setError] = useState(""), [success, setSuccess] = useState("");
  const lock = useRef(false);
  const [draft, setDraft] = useState<Ansicht | null>(null), [plan, setPlan] = useState<Plan | null>(null), [saved, setSaved] = useState(false);
  const [preview, setPreview] = useState<Vorschau | null>(null), [selected, setSelected] = useState("0"), [reviewed, setReviewed] = useState<string[]>([]), [paperReviewed, setPaperReviewed] = useState(false);
  async function run(fn: () => Promise<void>) { if (lock.current) return; lock.current = true; setBusy(true); setError(""); try { await fn(); } catch (e) { setError(getApiErrorMessage(e)); } finally { lock.current = false; setBusy(false); } }
  function change(p: Plan) { setPlan(p); setSaved(false); setPreview(null); setReviewed([]); setPaperReviewed(false); }
  async function upload(file: File) {
    if (file.size > 25_000_000) throw new Error("ZIP darf höchstens 25 MB groß sein.");
    const body = new FormData(); body.append("datei", file);
    const { data } = await apiClient.post<Ansicht>(`${base}/importe`, body); setDraft(data);
    setPlan({ briefpapier: false, zuordnungen: [], auswahl: data.manifest.vorlagen.map(v => ({ quelle: v.mietobjekt, vorgang: v.vorgang, aktion: "UEBERSPRINGEN" })) });
    setSaved(false); setPreview(null); setReviewed([]); setPaperReviewed(false);
  }
  const current = preview?.vorlagen[Number(selected)];
  const allReviewed = !!preview && preview.status.every(s => s.erfolgreich || s.ausdruecklichFreigegeben) && preview.vorlagen.every((_, i) => reviewed.includes(String(i))) && (!preview.briefpapier || paperReviewed);
  return <Stack spacing={2}>
    {success && <Alert severity="success">{success}</Alert>}
    {ungespeichert && <Alert severity="warning">Der ZIP-Export enthält nur den gespeicherten Stand. Offene Editoränderungen werden nicht exportiert. Vor einem Import bitte speichern oder verwerfen.</Alert>}
    <Stack direction={{ xs: "column", sm: "row" }} spacing={1}>
      <Button variant="outlined" disabled={busy} onClick={() => void run(async () => {
        const { data } = await apiClient.get<Blob>(`${base}/export`, { responseType: "blob" }); const url = URL.createObjectURL(data); const a = document.createElement("a"); a.href = url; a.download = `KanuControl-Vorlagen-${new Date().toISOString().slice(0, 10)}.zip`; a.click(); setTimeout(() => URL.revokeObjectURL(url), 1000);
      })}>Alle Vorlagen als ZIP exportieren</Button>
      <Button variant="outlined" disabled={busy || ungespeichert} onClick={() => { setOpen(true); setSuccess(""); setError(""); }}>Vorlagen aus ZIP importieren</Button>
    </Stack>
    {!open && error && <Alert severity="error">{error}</Alert>}
    <Dialog open={open} fullWidth maxWidth="lg" slotProps={{ paper: { sx: { maxHeight: "calc(100dvh - 32px)", m: 2 } } }} onClose={() => { if (!busy && !draft) setOpen(false); }}>
      <DialogTitle>Vorlagenpaket prüfen und importieren</DialogTitle>
      <DialogContent sx={{ minHeight: 0, overflowY: "auto" }}><Stack spacing={2} component="fieldset" disabled={busy} sx={{ border: 0, m: 0, p: 0, minWidth: 0, flexShrink: 0, "& > *": { flexShrink: 0 } }}>
        <Alert severity="info">Upload und Vorschau ändern keine aktiven Vorlagen und versenden keine E-Mail. Der Entwurf gilt 30 Minuten. Es werden keine Mietobjekte angelegt und keine nicht enthaltenen Vorlagen gelöscht.</Alert>
        {!draft && <Button component="label">ZIP-Datei auswählen<input hidden type="file" accept=".zip,application/zip" onChange={e => { const f = e.target.files?.[0]; if (f) void run(() => upload(f)); e.target.value = ""; }} /></Button>}
        {draft && plan && <>
          <Typography>Quelle: {draft.manifest.quelle.mandantBezeichnung}. Gültig bis {new Date(draft.ablauf).toLocaleString("de-DE")}. {draft.manifest.vorlagen.length} gespeicherte Vorlagen.</Typography>
          {!draft.manifest.briefpapier && <Alert severity={briefpapierVorhanden === false ? "warning" : "info"}>
            Dieses ZIP enthält nur Vorlagentexte, kein zusätzliches Briefpapier. {briefpapierVorhanden === true
              ? "Das bereits gespeicherte Briefpapier mit Grußformel und gegebenenfalls Unterschrift bleibt erhalten und wird für die Vorschau verwendet. Es muss nicht erneut hochgeladen werden."
              : briefpapierVorhanden === false
                ? "Im aktuellen Mandanten ist noch kein Briefpapier gespeichert. Für die PDF-Vorschau bitte zuerst Briefpapier und Abschluss in der Vorlagenverwaltung einrichten."
                : "Bereits gespeichertes Briefpapier, Grußformel und Unterschrift bleiben erhalten und werden für die Vorschau verwendet."}
          </Alert>}
          {draft.manifest.briefpapier && <><FormControlLabel label="Gemeinsames Briefpapier, Grußformel und Bild als Einheit ersetzen" control={<Checkbox checked={plan.briefpapier} onChange={e => change({ ...plan, briefpapier: e.target.checked })} />} /><Alert severity="warning">Eine Übernahme des Briefpapiers wirkt auf ALLE Mietobjekte des Zielmandanten, auch ohne importierte Vorlage. {draft.manifest.briefpapier.bild === null ? "Das Paket enthält ausdrücklich kein Bild. Bei Übernahme wird ein bisheriges Abschlussbild entfernt." : "Das Abschlussbild aus dem Paket ersetzt das bisherige Bild."}</Alert></>}
          {draft.manifest.mietobjekte.map(o => {
            const mapping = plan.zuordnungen.find(m => m.quelle === o.schluessel);
            return <Stack key={o.schluessel} spacing={1} sx={{ border: 1, borderColor: "divider", p: 2, minWidth: 0 }}>
              <Typography>{o.bezeichnung} ({o.schluessel})</Typography>
              <TextField select fullWidth label="Zielobjekt ausdrücklich wählen" value={mapping ? mapping.ueberspringen ? "skip" : String(mapping.ziel) : ""} onChange={e => {
                const skip = e.target.value === "skip";
                change({ ...plan, zuordnungen: [...plan.zuordnungen.filter(m => m.quelle !== o.schluessel), { quelle: o.schluessel, ziel: skip ? null : Number(e.target.value), ueberspringen: skip }], auswahl: plan.auswahl.map(a => a.quelle === o.schluessel ? { ...a, aktion: "UEBERSPRINGEN" } : a) });
              }}><MenuItem value="skip">Objekt bewusst überspringen</MenuItem>{objekte.map(t => <MenuItem key={t.id} value={String(t.id)}>{t.bezeichnung} (Ziel-ID {t.id})</MenuItem>)}</TextField>
              {plan.auswahl.filter(a => a.quelle === o.schluessel).map(a => <TextField key={a.vorgang} select fullWidth label={vorgangNamen[a.vorgang] ?? a.vorgang} disabled={!mapping || mapping.ueberspringen} value={a.aktion} onChange={e => change({ ...plan, auswahl: plan.auswahl.map(x => x === a ? { ...x, aktion: e.target.value as Aktion } : x) })}><MenuItem value="UEBERSPRINGEN">Vorlage überspringen / Ziel beibehalten</MenuItem><MenuItem value="NEU">Neu anlegen (Ziel darf noch nicht existieren)</MenuItem><MenuItem value="ERSETZEN">Vorhandene Vorlage ausdrücklich ersetzen</MenuItem></TextField>)}
            </Stack>;
          })}

          {saved && <>
            <Alert severity="warning">Auswahl: {draft.neu} neu, {draft.ersetzt} ersetzt, {draft.uebersprungen} Vorlagen und {draft.objekteUebersprungen} Objekte übersprungen. {plan.briefpapier ? "Gemeinsamer Abschluss wird mandantenweit ersetzt." : "Gemeinsamer Abschluss bleibt erhalten."} {draft.bildWirdEntfernt ? "Das bisherige Bild wird entfernt." : ""}</Alert>
            {plan.briefpapier && <Box><Typography>Grußformel vorher / nachher</Typography><Box component="pre" sx={{ whiteSpace: "pre-wrap", overflowWrap: "anywhere" }}>{draft.grussVorher ?? "(kein Briefpapier)"}{"\n→\n"}{draft.grussNachher}</Box></Box>}
            {draft.vergleich.filter(c => c.aktion !== "UEBERSPRINGEN").map(c => <Box component="details" key={c.schluessel}><Box component="summary">{c.schluessel}: {c.aktion} → Ziel {c.ziel}</Box><Typography>Vorher</Typography><Box component="pre" sx={{ whiteSpace: "pre-wrap", overflowWrap: "anywhere" }}>{JSON.stringify(c.vorher, null, 2)}</Box><Typography>Nachher</Typography><Box component="pre" sx={{ whiteSpace: "pre-wrap", overflowWrap: "anywhere" }}>{JSON.stringify(c.nachher, null, 2)}</Box></Box>)}

          </>}
          {preview && <>
            <Alert severity="info">{preview.hinweis} Alle {preview.vorlagen.length} Vorschauen sind erstellt. Über die Auswahl oder „Zurück“ und „Weiter“ zwischen den Dokumenten wechseln. Erneutes Erstellen setzt die Prüfbestätigungen zurück.</Alert>
            {preview.status.filter(s => !s.erfolgreich).map(s => <Alert severity="warning" key={s.schluessel}>{s.schluessel}: Keine erfolgreiche visuelle Prüfung: {s.ursache}
              <FormControlLabel label="Fehlende Prüfbarkeit geklärt: diese Vorlage ausdrücklich ohne erfolgreiche Layoutprüfung verwalten. Vor Versand ist eine erfolgreiche Buchungsvorschau erforderlich." control={<Checkbox checked={plan.ohneLayoutFreigabe?.includes(s.schluessel) ?? false} onChange={e => { setPlan({ ...plan, ohneLayoutFreigabe: e.target.checked ? [...(plan.ohneLayoutFreigabe ?? []), s.schluessel] : (plan.ohneLayoutFreigabe ?? []).filter(k => k !== s.schluessel) }); setSaved(false); }} />} />
            </Alert>)}
            {preview.briefpapier && <><Pdf value={preview.briefpapier} /><FormControlLabel label="Gemeinsames Briefpapier und Abschluss geprüft" control={<Checkbox checked={paperReviewed} onChange={e => setPaperReviewed(e.target.checked)} />} /></>}
            {preview.vorlagen.length > 0 && <TextField select label={`Vorlagenprüfung (${reviewed.length}/${preview.vorlagen.length})`} value={selected} onChange={e => setSelected(e.target.value)}>{preview.vorlagen.map((p, i) => <MenuItem key={i} value={String(i)}>{reviewed.includes(String(i)) ? "✓ " : ""}{vorschauName(p.schluessel)} · {p.schluessel}: {p.szenario}</MenuItem>)}</TextField>}
            {current && <><Typography variant="h6">{vorschauName(current.schluessel)}</Typography><Typography>Betreff: {current.betreff}</Typography><Typography sx={{ whiteSpace: "pre-wrap" }}>{current.text}</Typography><Pdf key={selected} value={current.pdf} /><FormControlLabel label="Diese PDF- und E-Mail-Vorschau geprüft" control={<Checkbox checked={reviewed.includes(selected)} onChange={e => setReviewed(r => e.target.checked ? [...r, selected] : r.filter(i => i !== selected))} />} /></>}
          </>}
        </>}
      </Stack></DialogContent>
      <DialogActions sx={{ flexDirection: "column", alignItems: "stretch", gap: 1, px: 3, pb: 2, borderTop: 1, borderColor: "divider", flexShrink: 0 }}>
        {error && <Alert severity="error">{error}</Alert>}
        {draft && <Typography variant="body2" role="status">{!saved
          ? "Nächster Schritt: Auswahl prüfen. Noch nicht vorhandene Vorlagen mit „Neu anlegen“ importieren."
          : !preview ? "Nächster Schritt: PDF-Vorschauen erstellen."
            : !allReviewed ? `Bitte die PDF- und E-Mail-Vorschauen prüfen und bestätigen (${reviewed.length}/${preview.vorlagen.length}).`
              : "Alle Prüfungen abgeschlossen. Die ausgewählte Konfiguration kann jetzt übernommen werden."}</Typography>}
        {current && preview && <Stack direction="row" useFlexGap flexWrap="wrap" alignItems="center" gap={1}>
          <Button disabled={busy || Number(selected) <= 0} onClick={() => setSelected(String(Number(selected) - 1))}>Vorherige Vorschau</Button>
          <Typography variant="body2" sx={{ flex: 1 }}>Vorschau {Number(selected) + 1} von {preview.vorlagen.length}: {vorschauName(current.schluessel)} · {current.szenario}</Typography>
          <Button disabled={busy || Number(selected) >= preview.vorlagen.length - 1} onClick={() => setSelected(String(Number(selected) + 1))}>Nächste Vorschau</Button>
          <Button disabled={busy} variant="outlined" onClick={() => {
            setReviewed(r => r.includes(selected) ? r : [...r, selected]);
            if (Number(selected) < preview.vorlagen.length - 1) setSelected(String(Number(selected) + 1));
          }}>{Number(selected) < preview.vorlagen.length - 1 ? "Geprüft bestätigen und weiter" : "Letzte Vorschau als geprüft bestätigen"}</Button>
        </Stack>}
        <Stack direction="row" useFlexGap flexWrap="wrap" justifyContent="flex-end" gap={1}>
        {draft && plan && <Button disabled={busy || plan.zuordnungen.length !== draft.manifest.mietobjekte.length} onClick={() => void run(async () => { const { data } = await apiClient.put<Ansicht>(`${base}/importe/${draft.id}/plan`, plan); setDraft(data); setSaved(true); setPreview(null); setReviewed([]); setPaperReviewed(false); })}>1. Auswahl prüfen</Button>}
        {draft && saved && plan && <Button disabled={busy || (draft.neu + draft.ersetzt === 0 && !plan.briefpapier)} onClick={() => void run(async () => { setPreview(null); setReviewed([]); setPaperReviewed(false); const { data } = await apiClient.post<Vorschau>(`${base}/importe/${draft.id}/vorschau`); setPreview(data); setSelected("0"); })}>{preview ? "PDF-Vorschauen neu erstellen (Prüfung zurücksetzen)" : "2. PDF-Vorschauen erstellen"}</Button>}
        <Button disabled={busy} onClick={() => void run(async () => { if (draft) await apiClient.delete(`${base}/importe/${draft.id}`); setDraft(null); setPlan(null); setPreview(null); setOpen(false); })}>Abbrechen und Entwurf verwerfen</Button>
        <Button variant="contained" disabled={busy || !saved || !allReviewed || !draft} onClick={() => void run(async () => {
          if (!draft) return;
          await apiClient.post(`${base}/importe/${draft.id}/uebernehmen`);
          setSuccess(`Auswahl übernommen: ${draft.neu} neu, ${draft.ersetzt} ersetzt, ${draft.uebersprungen} Vorlagen übersprungen. Vor dem Versand ist eine aktuelle Buchungsvorschau erforderlich.`);
          setDraft(null); setPlan(null); setPreview(null); setOpen(false); onImported();
        })}>3. Geprüfte Auswahl übernehmen</Button>
        </Stack>
      </DialogActions>
    </Dialog>
  </Stack>;
}
