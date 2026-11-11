import VorlagenPakete from "./VorlagenPakete";
import { useEffect, useRef, useState } from "react";
import { Alert, Box, Button, Checkbox, Chip, FormControlLabel, MenuItem, Paper, Stack, TextField, Typography } from "@mui/material";
import apiClient from "@/core/api/client/apiClient";
import { getMietobjekte } from "@/vermietung/api/mietobjektApi";
import type { Mietobjekt } from "@/vermietung/types/Mietobjekt";
import { getApiErrorMessage } from "@/kjfp/api/utils/apiError";

interface Variante { bedingung: string; text: string }
interface Block { titel: string; text: string; bedingung: string; varianten: Variante[] }
interface Vorlage { version: number | null; betreff: string; mailtext: string; anrede: string; bloecke: Block[] }
interface Briefpapier { version: number | null; gruss: string; pdfVorhanden: boolean; bildVorhanden: boolean }
interface Katalog { felder: string[]; bedingungen: string[]; vorgaenge: string[]; pflichtfelder: string[] }
const base = "/vermietung/vorlagen";
const namen: Record<string, string> = { STORNO_ERSTATTUNG_POSITIV: "Storno: Erstattung offen", STORNO_EINBEHALT_POSITIV: "Storno: Einbehalt", STORNO_OHNE_ZAHLUNG: "Storno: kein offener Erstattungsbetrag", RESTZAHLUNG_POSITIV: "Restzahlung offen", ERSTATTUNG_POSITIV: "Erstattung offen", AUSGEGLICHEN: "Ausgeglichen", ZAHLUNG_ANZAHLUNG: "Zahlungsplan: Anzahlung", ZAHLUNG_BEI_ABRECHNUNG: "Zahlungsplan: bei Abrechnung", ZAHLUNG_GESAMTMIETE_VORAUS: "Zahlungsplan: Gesamtmiete im Voraus", RESERVIERUNG: "Reservierung", BESTAETIGUNG: "Bestätigung", STORNIERUNG: "Stornierung", ABLEHNUNG: "Ablehnung", ABRECHENBAR: "Zur Abrechnung vorgesehen", ABGERECHNET: "Abrechnung abgeschlossen", IMMER: "Immer", ANZAHLUNG_POSITIV: "Anzahlung größer als 0", KAUTION_POSITIV: "Kaution größer als 0", RESTMIETE_POSITIV: "Restmiete größer als 0", OG_GEBUCHT: "OG mitgebucht", OG_NICHT_GEBUCHT: "OG nicht mitgebucht", STORNO_UNTER_56_TAGEN: "Mietbeginn in weniger als 8 Wochen", STORNO_AB_56_TAGEN: "Mietbeginn in mindestens 8 Wochen" };
const abrechnungVorlage = (): Vorlage => ({ version: null, anrede: "Hallo {{vorname}} {{nachname}},", betreff: "{{buchungsnummer}} Abrechnung", mailtext: "Die Abrechnung findet ihr im Anhang.", bloecke: [
  { titel: "Einleitung", text: "Für euren Aufenthalt vom {{anreise}} bis {{abreise}} in {{mietobjekt}} berechnen wir folgende Kosten:", bedingung: "IMMER", varianten: [] },
  { titel: "Restzahlung", text: "Bitte überweist die Restzahlung von {{restzahlung}} € bis zum {{zahlungsfrist}} auf das Vereinskonto: {{bank_name}}, IBAN {{bank_iban}}, BIC {{bank_bic}}.", bedingung: "RESTZAHLUNG_POSITIV", varianten: [] },
  { titel: "Erstattung", text: "Die Rückerstattung von {{erstattung}} € überweisen wir in den nächsten Tagen auf folgendes Konto:\nBank: {{mieter_bank_name}}\nIBAN: {{mieter_bank_iban}}\nBIC: {{mieter_bank_bic}}\nKontoinhaber: {{mieter_kontoinhaber}}", bedingung: "ERSTATTUNG_POSITIV", varianten: [] },
  { titel: "Ausgeglichen", text: "Die Abrechnung ist ausgeglichen.", bedingung: "AUSGEGLICHEN", varianten: [] },
] });
const szenarien: Record<string, string[]> = { RESERVIERUNG: ["ANZAHLUNG", "GESAMTMIETE_VORAUS", "BEI_ABRECHNUNG"], ABGERECHNET: ["RESTZAHLUNG", "ERSTATTUNG", "AUSGEGLICHEN"], STORNIERUNG: ["STORNO_FRUEH", "STORNO_KURZ", "STORNO_OHNE_ZAHLUNG"] };
const szenarioNamen: Record<string,string> = { ANZAHLUNG: "Anzahlung", GESAMTMIETE_VORAUS: "Gesamtmiete im Voraus", BEI_ABRECHNUNG: "Zahlung bei Abrechnung", RESTZAHLUNG: "Restzahlung", ERSTATTUNG: "Erstattung", AUSGEGLICHEN: "Ausgeglichen", STORNO_FRUEH: "Storno ab 56 Tagen: volle Erstattung", STORNO_KURZ: "Storno unter 56 Tagen: keine Erstattung", STORNO_OHNE_ZAHLUNG: "Storno ohne Zahlungen", STANDARD: "Standard" };
const neuerBlock = (): Block => ({ titel: "Neuer Abschnitt", text: "", bedingung: "IMMER", varianten: [] });
const neueVorlage = (): Vorlage => ({ version: null, anrede: "Hallo {{vorname}},", betreff: "{{buchungsnummer}} Reservierungsbestätigung", mailtext: "Hallo {{vorname}},\n\ndie Reservierungsbestätigung findet ihr im Anhang.\n\nMit freundlichen Grüßen", bloecke: [
  { titel: "Reservierung", text: "Wir freuen uns auf euren Aufenthalt vom {{anreise}} bis {{abreise}} in {{mietobjekt}}.", bedingung: "IMMER", varianten: [] },
  { titel: "Mietpreis", text: "Die Miete beträgt {{mietbetrag}} €.", bedingung: "IMMER", varianten: [ { bedingung: "OG_GEBUCHT", text: "Falls das OG nicht benutzt wird, reduziert sich die Miete." }, { bedingung: "OG_NICHT_GEBUCHT", text: "Falls zusätzlich das OG benutzt wird, erhöht sich die Miete." } ] },
  { titel: "Zahlung", text: "", bedingung: "IMMER", varianten: [
    { bedingung: "ANZAHLUNG_POSITIV", text: "Bitte überweist die Anzahlung von {{anzahlung}} €." },
    { bedingung: "RESTMIETE_POSITIV", text: "Die Restmiete von {{restmiete}} € ist bis zum {{zahlungsfrist}} zu überweisen." },
    { bedingung: "ZAHLUNG_GESAMTMIETE_VORAUS", text: "Bitte überweist die Gesamtmiete von {{mietbetrag}} € im Voraus bis zum {{zahlungsfrist}}." },
    { bedingung: "ZAHLUNG_BEI_ABRECHNUNG", text: "Die Miete wird zusammen mit Verbrauch und Zusatzleistungen nach dem Aufenthalt abgerechnet." },
    { bedingung: "KAUTION_POSITIV", text: "Bitte überweist außerdem die Kaution von {{kaution}} € bis zum {{zahlungsfrist}}." },
  ] },
  { titel: "Stornierung", text: "", bedingung: "IMMER", varianten: [ { bedingung: "STORNO_UNTER_56_TAGEN", text: "Bei Stornierung weniger als 56 Tage vor Mietbeginn werden keine geleisteten Zahlungen erstattet." }, { bedingung: "STORNO_AB_56_TAGEN", text: "Bei Stornierung ab einschließlich 56 Tagen vor Mietbeginn werden alle geleisteten Zahlungen zurückerstattet. Innerhalb von 56 Tagen erfolgt keine Erstattung." } ] },
  { titel: "Nutzungshinweise", text: "Bitte beachtet unsere Hausregeln: {{hausregeln_url}}", bedingung: "IMMER", varianten: [] },
] });
function VorlagenFehler({ text }: { text: string }) {
  const ref = useRef<HTMLDivElement>(null);
  useEffect(() => { ref.current?.scrollIntoView({ block: "center" }); ref.current?.focus(); }, [text]);
  return <Alert severity="error" tabIndex={-1} ref={ref}>{text}</Alert>;
}
// Leere, neu angelegte Zusatzzeilen enthalten noch keinen bedingten Text.
const vorbereiten = (v: Vorlage): Vorlage => ({ ...v, bloecke: v.bloecke.map(b => ({ ...b, varianten: (b.varianten ?? []).filter(x => x.text.trim() !== "") })) });
function PdfVorschau({ pdf }: { pdf: string }) {
  const [url, setUrl] = useState("");
  useEffect(() => { const u = URL.createObjectURL(new Blob([Uint8Array.from(atob(pdf), c => c.charCodeAt(0))], { type: "application/pdf" })); setUrl(u); return () => URL.revokeObjectURL(u); }, [pdf]);
  return <Box><Button component="a" href={url} target="_blank" rel="noopener noreferrer">PDF in neuem Fenster prüfen</Button><Box component="iframe" title="Vorlagenvorschau – alle Seiten" src={url} sx={{ width: "100%", height: 650, border: 1, borderColor: "divider" }} /></Box>;
}
export default function VorlagenVerwaltung() {
  const [importRevision, setImportRevision] = useState(0);
  const [katalog, setKatalog] = useState<Katalog | null>(null);
  const [objekte, setObjekte] = useState<Mietobjekt[]>([]);
  const [objekt, setObjekt] = useState("");
  const [vorgang, setVorgang] = useState("RESERVIERUNG");
  const [papier, setPapier] = useState<Briefpapier | null>(null);
  const [vorlage, setVorlage] = useState<Vorlage | null>(null);
  const [pdfFile, setPdfFile] = useState<File | null>(null);
  const [bildFile, setBildFile] = useState<File | null>(null);
  const [bildEntfernen, setBildEntfernen] = useState(false);
  const [busy, setBusy] = useState(false);
  const [dirty, setDirty] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState("");
  const [buchung, setBuchung] = useState("");
  const [preview, setPreview] = useState<{ pdf: string; betreff: string; text: string; hinweis: string } | null>(null);
  const [szenario, setSzenario] = useState("ANZAHLUNG");
  const szenarioAuswahl = szenarien[vorgang] ?? ["STANDARD"];
  const aktuellesSzenario = szenarioAuswahl.includes(szenario) ? szenario : szenarioAuswahl[0];
  const [beispielOg, setBeispielOg] = useState(false);
  const [beispielKurzfristig, setBeispielKurzfristig] = useState(false);
  const [papierProbe, setPapierProbe] = useState<{ pdf: string; fingerprint: string } | null>(null);
  const gespeicherteVorlage = useRef<Vorlage | null>(null);
  const gespeichertesPapier = useRef<Briefpapier | null>(null);
  const papierGeaendert = !!papier && (!!pdfFile || !!bildFile || (bildEntfernen && papier.bildVorhanden)
    || papier.gruss !== gespeichertesPapier.current?.gruss);
  useEffect(() => {
    let cancelled = false;
    Promise.all([apiClient.get<Katalog>(`${base}/katalog`), apiClient.get<Briefpapier>(`${base}/briefpapier`), getMietobjekte()]).then(([k, p, o]) => { if (!cancelled) { setKatalog(k.data); gespeichertesPapier.current = p.data; setPapier(p.data); setObjekte(o); } }).catch((e: unknown) => { if (!cancelled) setError(getApiErrorMessage(e)); });
    return () => { cancelled = true; };
  }, [importRevision]);
  useEffect(() => {
    setVorlage(null); setPreview(null);
    if (!objekt) return;
    let cancelled = false;
    apiClient.get<Vorlage | null>(`${base}/${objekt}/${vorgang}`).then(({ data }) => { if (!cancelled) { gespeicherteVorlage.current = data; setVorlage(data ?? (vorgang === "RESERVIERUNG" ? neueVorlage() : vorgang === "ABGERECHNET" ? abrechnungVorlage() : { version: null, anrede: "Hallo {{vorname}},", betreff: "", mailtext: "", bloecke: [neuerBlock()] })); setDirty(false); } }).catch((e: unknown) => { if (!cancelled) setError(getApiErrorMessage(e)); });
    return () => { cancelled = true; };
  }, [objekt, vorgang, importRevision]);
  useEffect(() => {
    const warn = (event: BeforeUnloadEvent) => { event.preventDefault(); };
    if (dirty || papierGeaendert) window.addEventListener("beforeunload", warn);
    return () => window.removeEventListener("beforeunload", warn);
  }, [dirty, papierGeaendert]);
  async function speichernPapier() {
    const form = papierForm(); form.append("pruefung", papierProbe?.fingerprint ?? "");
    const { data } = await apiClient.put<Briefpapier>(`${base}/briefpapier`, form);
    gespeichertesPapier.current = data; setPapier(data); setPdfFile(null); setBildFile(null); setBildEntfernen(false); setPapierProbe(null); setPreview(null);
  }
  function update(value: Vorlage) { setVorlage(value); setDirty(JSON.stringify(value) !== JSON.stringify(gespeicherteVorlage.current)); setPreview(null); setSuccess(""); }
  function block(index: number, patch: Partial<Block>) { if (vorlage) update({ ...vorlage, bloecke: vorlage.bloecke.map((b, i) => i === index ? { ...b, ...patch } : b) }); }
  async function run(action: () => Promise<void>) { if (busy) return; setBusy(true); setError(null); setSuccess(""); try { await action(); } catch (e: unknown) { setError(getApiErrorMessage(e)); } finally { setBusy(false); } }
  function papierForm() { const form = new FormData(); if (papier?.version != null) form.append("version", String(papier.version)); form.append("gruss", papier?.gruss ?? ""); form.append("bildEntfernen", String(bildEntfernen)); if (pdfFile) form.append("pdf", pdfFile); if (bildFile) form.append("bild", bildFile); return form; }
  const condition = (value: string, onChange: (v: string) => void) => <TextField select label="Bedingung" value={value} onChange={e => onChange(e.target.value)}>{katalog?.bedingungen.map(b => <MenuItem key={b} value={b}>{namen[b] ?? b}</MenuItem>)}</TextField>;
  return <Stack spacing={3} sx={{ minWidth: 0 }}>
    <Typography variant="h6">E-Mail- und PDF-Vorlagen</Typography>
    {error && <VorlagenFehler text={error} />}{success && <Alert severity="success">{success}</Alert>}
    <Alert severity="info">Reservierungsschreiben bleiben einseitig; Entschädigungslisten können auf Folgeseiten erscheinen. Schlussabrechnungen verwenden die Objektvorlage ABGERECHNET und können mehrseitig sein. Versand und Testumleitung der Reservierungsbestätigung bleiben unverändert.</Alert>
    <VorlagenPakete briefpapierVorhanden={papier?.pdfVorhanden} objekte={objekte} ungespeichert={dirty || papierGeaendert} onImported={() => { setPreview(null); setPapierProbe(null); setImportRevision(x => x + 1); }} />
    {papier && <Paper variant="outlined" sx={{ p: 2 }}><Stack spacing={2} component="fieldset" disabled={busy} sx={{ border: 0, p: 0, m: 0 }}>
      <Typography variant="h6">Briefpapier und Abschluss des Mandanten</Typography>
      <Typography variant="body2">Einseitiges A4-PDF, höchstens 5 MB. Erforderliche Textfelder: {katalog?.pflichtfelder.join(", ")}. Das Feld textbereich markiert die Fläche für alle Blöcke und den Abschluss. Eine fest gedruckte Grußformel muss dort entfernt sein.</Typography>
      <Typography variant="body2">{papier.pdfVorhanden ? "Ein Briefpapier ist gespeichert." : "Noch kein Briefpapier gespeichert."} {papier.bildVorhanden ? "Ein Unterschriftsbild ist gespeichert." : "Kein Unterschriftsbild gespeichert."}</Typography>
      <Button component="label" variant="outlined">PDF-Briefpapier auswählen<input hidden type="file" accept="application/pdf" onChange={e => { setPdfFile(e.target.files?.[0] ?? null); setPapierProbe(null); }} /></Button>{pdfFile?.name}
      <TextField label="Gemeinsame Grußformel und Name" multiline minRows={3} value={papier.gruss} onChange={e => { setPapier({ ...papier, gruss: e.target.value }); setPapierProbe(null); }} />
      <Button component="label" variant="outlined">Optionales Unterschriftsbild auswählen<input hidden type="file" accept="image/png,image/jpeg" onChange={e => { setBildFile(e.target.files?.[0] ?? null); setPapierProbe(null); }} /></Button>{bildFile?.name}
      <FormControlLabel label="Gespeichertes Bild entfernen" control={<Checkbox checked={bildEntfernen} onChange={e => { setBildEntfernen(e.target.checked); setPapierProbe(null); }} />} />
      <Button onClick={() => void run(async () => { const { data } = await apiClient.post<{ pdf: string; fingerprint: string }>(`${base}/briefpapier/vorschau`, papierForm()); setPapierProbe(data); })}>Nur Briefpapier und Abschluss prüfen</Button>
      {papierProbe && <PdfVorschau pdf={papierProbe.pdf} />}
      <Button variant="contained" disabled={busy || !papierProbe || !papierGeaendert} onClick={() => void run(async () => { await speichernPapier(); setSuccess("Briefpapier und Abschluss gespeichert."); })}>Geprüftes Briefpapier speichern</Button>
    </Stack></Paper>}
    {papierGeaendert && <Alert severity="warning">Briefpapier, Unterschriftsbild oder Grußformel sind noch nicht gespeichert. Bitte die Vorschau prüfen und anschließend speichern.</Alert>}
    <Stack direction={{ xs: "column", sm: "row" }} spacing={2}>
      <TextField select fullWidth label="Mietobjekt" value={objekt} disabled={busy || dirty} onChange={e => setObjekt(e.target.value)}>{objekte.map(o => <MenuItem key={o.id} value={String(o.id)}>{o.bezeichnung}</MenuItem>)}</TextField>
      <TextField select fullWidth label="Dokumentvorgang" value={vorgang} disabled={busy || dirty} onChange={e => setVorgang(e.target.value)}>{katalog?.vorgaenge.map(v => <MenuItem key={v} value={v}>{namen[v] ?? v}</MenuItem>)}</TextField>
    </Stack>
    {dirty && <Button onClick={() => { setDirty(false); setVorlage(null); void run(async () => { const { data } = await apiClient.get<Vorlage | null>(`${base}/${objekt}/${vorgang}`); gespeicherteVorlage.current = data; setVorlage(data ?? (vorgang === "RESERVIERUNG" ? neueVorlage() : vorgang === "ABGERECHNET" ? abrechnungVorlage() : { version: null, anrede: "Hallo {{vorname}},", betreff: "", mailtext: "", bloecke: [neuerBlock()] })); }); }}>Änderungen verwerfen</Button>}
    {vorgang === "ABGERECHNET" && <Alert severity="info">Die Abrechnungstabelle wird automatisch nach dem ersten sichtbaren Textblock eingefügt. Weitere Blöcke folgen danach. Zahlungstexte mit „Restzahlung offen“, „Erstattung offen“ oder „Ausgeglichen“ bedingen.</Alert>}
    {katalog && <Box><Typography variant="subtitle2">Erlaubte Platzhalter (anklicken zum Kopieren)</Typography><Stack direction="row" useFlexGap flexWrap="wrap" gap={1}>{katalog.felder.map(f => <Chip key={f} label={`{{${f}}}`} onClick={() => { void navigator.clipboard.writeText(`{{${f}}}`).catch(() => setError("Bitte den angezeigten Platzhalter manuell kopieren.")); }} />)}</Stack></Box>}
    {vorlage && <Stack spacing={2} component="fieldset" disabled={busy} sx={{ border: 0, p: 0, m: 0, minWidth: 0 }}>
      <Paper variant="outlined" sx={{ p: 2 }}><Stack spacing={2}>
        <Typography variant="h6">Vorschau der konfigurierten PDF-Vorlage</Typography>
        <Typography variant="body2">Zeigt die aktuellen Textblöcke mit dem ausgewählten oder gespeicherten Briefpapier und Abschluss. Auch ungespeicherte Änderungen werden berücksichtigt. Eine Buchung ist dafür nicht erforderlich.</Typography>
        <TextField select label="Fachliches Beispielszenario" value={aktuellesSzenario} onChange={e => { setSzenario(e.target.value); setPreview(null); }}>{szenarioAuswahl.map(v => <MenuItem key={v} value={v}>{szenarioNamen[v]}</MenuItem>)}</TextField>
        <FormControlLabel label="Beispiel: OG mitgebucht" control={<Checkbox checked={beispielOg} onChange={e => { setBeispielOg(e.target.checked); setPreview(null); }} />} />
        <FormControlLabel label="Beispiel: Mietbeginn in weniger als 8 Wochen" control={<Checkbox checked={beispielKurzfristig} onChange={e => { setBeispielKurzfristig(e.target.checked); setPreview(null); }} />} />
        <Button variant="outlined" onClick={() => void run(async () => {
          setPreview(null);
          const form = papierForm();
          form.append("vorlage", new Blob([JSON.stringify(vorbereiten(vorlage))], { type: "application/json" }));
          form.append("szenario", aktuellesSzenario); form.append("ogGebucht", String(beispielOg)); form.append("kurzfristig", String(beispielKurzfristig));
          const { data } = await apiClient.post<{ pdf: string; betreff: string; text: string }>(`${base}/${objekt}/${vorgang}/beispiel-szenario`, form);
          if (papierGeaendert) {
            const { data: probe } = await apiClient.post<{ pdf: string; fingerprint: string }>(`${base}/briefpapier/vorschau`, papierForm());
            setPapierProbe(probe);
          }
          setPreview({ ...data, hinweis: "Vollständige Vorlage mit dem ausgewählten fachlichen Beispielszenario. Es wird nichts gespeichert oder versendet." });
        })}>PDF-Vorlage mit Beispieldaten anzeigen</Button>
        {!papier?.pdfVorhanden && <Alert severity="info">{pdfFile ? "Für die Vorschau wird das ausgewählte PDF verwendet. Das Briefpapier ist noch nicht gespeichert." : "Bitte oben ein PDF-Briefpapier auswählen. Für die Vorschau muss es noch nicht gespeichert werden."}</Alert>}
        {preview && <><Alert severity="info">{preview.hinweis}</Alert><Typography variant="subtitle2">E-Mail-Betreff</Typography><Typography>{preview.betreff}</Typography><Typography variant="subtitle2">E-Mail-Begleittext</Typography><Typography sx={{ whiteSpace: "pre-wrap" }}>{preview.text}</Typography><PdfVorschau pdf={preview.pdf} /></>}
      </Stack></Paper>
      <TextField label="Anrede im PDF" helperText="Zum Beispiel: Hallo {{vorname}} {{nachname}}," value={vorlage.anrede} onChange={e => update({ ...vorlage, anrede: e.target.value })} />
      <TextField label="E-Mail-Betreff" value={vorlage.betreff} onChange={e => update({ ...vorlage, betreff: e.target.value })} />
      <TextField label="E-Mail-Begleittext" multiline minRows={4} value={vorlage.mailtext} onChange={e => update({ ...vorlage, mailtext: e.target.value })} />
      {vorlage.bloecke.map((b, i) => <Paper key={i} variant="outlined" sx={{ p: 2 }}><Stack spacing={2}>
        <TextField label={`Block ${i + 1}: Titel`} value={b.titel} onChange={e => block(i, { titel: e.target.value })} />
        {condition(b.bedingung, v => block(i, { bedingung: v }))}
        <TextField label="Text" multiline minRows={3} value={b.text} onChange={e => block(i, { text: e.target.value })} />
        {(b.varianten ?? []).map((v, n) => <Stack key={n} spacing={1}>{condition(v.bedingung, value => block(i, { varianten: b.varianten.map((x, k) => k === n ? { ...x, bedingung: value } : x) }))}<TextField label="Bedingter Zusatztext" helperText="Leere Zusatztexte werden beim Speichern und in der Vorschau weggelassen." multiline value={v.text} onChange={e => block(i, { varianten: b.varianten.map((x, k) => k === n ? { ...x, text: e.target.value } : x) })} /><Button onClick={() => block(i, { varianten: b.varianten.filter((_, k) => k !== n) })}>Variante entfernen</Button></Stack>)}
        <Button disabled={(b.varianten?.length ?? 0) >= 8 || (b.varianten ?? []).some(v => v.text.trim() === "")} onClick={() => block(i, { varianten: [...(b.varianten ?? []), { bedingung: "IMMER", text: "" }] })}>Bedingten Text ergänzen</Button>
        <Stack direction="row" useFlexGap flexWrap="wrap"><Button disabled={i === 0} onClick={() => { const rows = [...vorlage.bloecke]; [rows[i - 1], rows[i]] = [rows[i], rows[i - 1]]; update({ ...vorlage, bloecke: rows }); }}>Nach oben</Button><Button disabled={i === vorlage.bloecke.length - 1} onClick={() => { const rows = [...vorlage.bloecke]; [rows[i], rows[i + 1]] = [rows[i + 1], rows[i]]; update({ ...vorlage, bloecke: rows }); }}>Nach unten</Button><Button disabled={vorlage.bloecke.length <= 1} onClick={() => update({ ...vorlage, bloecke: vorlage.bloecke.filter((_, n) => n !== i) })}>Block entfernen</Button></Stack>
      </Stack></Paper>)}
      <Button disabled={vorlage.bloecke.length >= 5} onClick={() => update({ ...vorlage, bloecke: [...vorlage.bloecke, neuerBlock()] })}>Textblock hinzufügen (höchstens fünf)</Button>
      {(vorgang === "RESERVIERUNG" || vorgang === "ABGERECHNET") && <><TextField label="Buchungs-ID für Entwurfsvorschau" type="number" value={buchung} onChange={e => { setBuchung(e.target.value); setPreview(null); }} /><Button disabled={!/^[1-9]\d*$/.test(buchung)} onClick={() => void run(async () => { const { data } = await apiClient.post<{ pdf: string; betreff: string; text: string }>(`${base}/${objekt}/${vorgang}/vorschau`, { buchungId: Number(buchung), vorlage: vorbereiten(vorlage) }); setPreview({ ...data, hinweis: "Entwurf mit den Daten der ausgewählten Buchung. Es wird nichts gespeichert oder versendet." }); })}>Ungespeicherten Entwurf prüfen</Button></>}
      <Button variant="contained" disabled={busy || (!dirty && vorlage.version != null && !papierGeaendert) || (papierGeaendert && !papierProbe)} onClick={() => void run(async () => { if (papierGeaendert) await speichernPapier(); const { data } = await apiClient.put<Vorlage>(`${base}/${objekt}/${vorgang}`, vorbereiten(vorlage)); gespeicherteVorlage.current = data; setVorlage(data); setDirty(false); setSuccess("Vorlage und Briefpapier gespeichert. Vor dem Versand ist eine aktuelle Buchungsvorschau erforderlich."); })}>Vorlage und Briefpapier speichern</Button>
    </Stack>}
  </Stack>;
}
