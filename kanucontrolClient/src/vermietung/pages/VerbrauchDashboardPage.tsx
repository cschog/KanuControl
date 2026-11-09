import { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { Accordion, AccordionDetails, AccordionSummary, Alert, Box, Button, Chip, CircularProgress, MenuItem, Paper, Stack, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, TextField, Typography } from "@mui/material";
import ExpandMoreIcon from "@mui/icons-material/ExpandMore";
import { getMietobjekte } from "@/vermietung/api/mietobjektApi";
import { getVerbrauchDashboard } from "@/vermietung/api/verbrauchDashboardApi";
import type { VerbrauchDashboard } from "@/vermietung/types/VerbrauchDashboard";
import type { Mietobjekt } from "@/vermietung/types/Mietobjekt";
import { getApiErrorMessage } from "@/kjfp/api/utils/apiError";
import { MonatsDiagramm, KorrelationsDiagramm, BuchungsZeitstrahl } from "@/vermietung/components/VerbrauchDiagramme";
import { zahl, datum, monat } from "@/vermietung/utils/verbrauchFormat";
const art = (s: string) => ({ GAS: "Gas", WASSER: "Wasser", STROM: "Strom" })[s] ?? s;
function Wert({ label, value, detail }: { label: string; value: string; detail: string }) { return <Paper variant="outlined" sx={{ p: 2 }}><Typography color="text.secondary">{label}</Typography><Typography variant="h5" sx={{ my: 1 }}>{value}</Typography><Typography variant="body2" color="text.secondary">{detail}</Typography></Paper>; }
export default function VerbrauchDashboardPage() {
  const navigate = useNavigate(); const [params, setParams] = useSearchParams();
  const current = Number(new Intl.DateTimeFormat("en", { year: "numeric", timeZone: "Europe/Berlin" }).format(new Date()));
  const requestedYear = Number(params.get("jahr") ?? current); const year = requestedYear >= 2000 && requestedYear <= 2100 && Number.isInteger(requestedYear) ? requestedYear : current;
  const objectId = Number(params.get("objekt") ?? 0);
  const [objects, setObjects] = useState<Mietobjekt[]>([]); const [objectLoading, setObjectLoading] = useState(true); const [objectError, setObjectError] = useState<string | null>(null);
  const [data, setData] = useState<VerbrauchDashboard | null>(null); const [error, setError] = useState<string | null>(null); const [reload, setReload] = useState(0); const [meterId, setMeterId] = useState<number | null>(null);
  useEffect(() => { let live = true; setObjectLoading(true); setObjectError(null);
    getMietobjekte().then(items => { if (live) setObjects(items); }).catch(e => { if (live) setObjectError(getApiErrorMessage(e)); }).finally(() => { if (live) setObjectLoading(false); });
    return () => { live = false; };
  }, [reload]);
  useEffect(() => { if (objects.length && !objects.some(o => o.id === objectId)) {
    const first = objects.find(o => o.aktiv) ?? objects[0]; setParams(p => { const next = new URLSearchParams(p); next.set("objekt", String(first.id)); return next; }, { replace: true });
  } }, [objects, objectId, setParams]);
  useEffect(() => { const controller = new AbortController(); setData(null); setError(null);
    if (objectId > 0 && objects.some(o => o.id === objectId)) getVerbrauchDashboard(objectId, year, controller.signal)
      .then(d => { if (!controller.signal.aborted) setData(d); }).catch(e => { if (!controller.signal.aborted) setError(getApiErrorMessage(e)); });
    return () => controller.abort();
  }, [objectId, year, objects, reload]);
  const meter = data?.zaehler.find(m => m.id === meterId) ?? data?.zaehler[0]; const k = meter?.kennzahlen;
  const unit = meter?.einheit ?? ""; const hasData = !!k && k.abgedeckteStunden > 0;
  const select = (key: string, value: string) => setParams(p => { const next = new URLSearchParams(p); next.set(key, value); return next; });
  return <Box sx={{ p: { xs: 2, md: 3 } }}>
    <Stack direction={{ xs: "column", md: "row" }} spacing={2} justifyContent="space-between" sx={{ mb: 2 }}><Box><Typography variant="h4">Verbrauch & Belegung</Typography><Typography color="text.secondary">Gas, Wasser und Strom · Buchungszeiten und Zeiten ohne Buchung</Typography></Box><Stack direction="row" spacing={1} useFlexGap flexWrap="wrap" alignItems="center"><TextField size="small" select label="Mietobjekt" value={objects.some(o => o.id === objectId) ? objectId : ""} onChange={e => select("objekt", e.target.value)} sx={{ minWidth: 180 }}>{objects.map(o => <MenuItem key={o.id} value={o.id}>{o.bezeichnung}</MenuItem>)}</TextField><TextField size="small" select label="Jahr" value={year} onChange={e => select("jahr", e.target.value)} SelectProps={{ MenuProps: { PaperProps: { sx: { maxHeight: 300 } } } }}>{Array.from({ length: 101 }, (_, i) => 2100 - i).map(y => <MenuItem key={y} value={y}>{y}</MenuItem>)}</TextField><Button variant="outlined" onClick={() => navigate("/vermietung/verwaltung?bereich=zaehler")}>Zählerstände</Button><Button onClick={() => setReload(n => n + 1)}>Aktualisieren</Button></Stack></Stack>
    <Alert severity="info" sx={{ mb: 2 }}>Gemessen ist jeweils der Verbrauch zwischen zwei Ablesungen. Überspannt ein Intervall Buchungen und Leerzeiten oder Monatsgrenzen, wird zeitanteilig geschätzt. „Ohne Buchung“ bedeutet nicht zwingend ungenutzt. Gas, Wasser und Strom werden in den jeweiligen Zählereinheiten angezeigt.</Alert>
    {(objectError || error) && <Alert severity="error" action={<Button onClick={() => setReload(n => n + 1)}>Erneut laden</Button>}>{objectError ?? error}</Alert>}
    {objectLoading && <CircularProgress aria-label="Mietobjekte werden geladen" />}
    {!objectLoading && !objectError && objects.length === 0 && <Alert severity="info">Lege zuerst ein Mietobjekt und seine Zähler an.</Alert>}
    {!objectLoading && objectId > 0 && !data && !error && !objectError && <CircularProgress aria-label="Verbrauch wird ausgewertet" />}
    {data && data.zaehler.length === 0 && <Alert severity="info">Diesem Mietobjekt sind über seine Mietbereiche noch keine Zähler zugeordnet. Die Zuordnung kannst du in der Zählerverwaltung pflegen.</Alert>}
    {data && <Box sx={{ display: "grid", gridTemplateColumns: { xs: "1fr", sm: "repeat(2, 1fr)", md: "repeat(3, 1fr)" }, gap: 2, my: 2 }}>{data.zaehler.map(m => <Paper key={m.id} variant="outlined" sx={{ p: 2, borderColor: meter?.id === m.id ? "primary.main" : undefined, borderWidth: meter?.id === m.id ? 2 : 1 }}><Button onClick={() => setMeterId(m.id)} sx={{ px: 0, textAlign: "left" }}>{art(m.verbrauchsart)} · {m.bezeichnung}</Button><Typography variant="h5">{m.kennzahlen.abgedeckteStunden > 0 ? `${zahl(m.kennzahlen.gesamt)} ${m.einheit}` : "Keine Messdaten"}</Typography><Typography variant="body2" color="text.secondary">Abdeckung: {zahl(m.kennzahlen.abdeckungProzent, 1)} % · {m.aktiv ? "aktiv" : "inaktiv"}</Typography>{m.gemeinsam && <Chip size="small" label="Gemeinsamer Zähler" sx={{ mt: 1 }} />}</Paper>)}</Box>}
    {data && meter && k && <>
      <Typography variant="h5" sx={{ mt: 3, mb: 1 }}>{art(meter.verbrauchsart)} · {meter.bezeichnung} ({unit})</Typography>
      <Typography variant="body2" color="text.secondary" sx={{ mb: 2 }}>Letzte vorhandene Ablesung: {meter.letzteAblesung ? datum(meter.letzteAblesung) : "–"} · Datenstand: {datum(data.stand)}</Typography>
      {meter.hinweise.map((h, i) => <Alert key={i} severity="info" sx={{ mb: 1 }}>{h}</Alert>)}
      <Box sx={{ display: "grid", gridTemplateColumns: { xs: "1fr", sm: "1fr 1fr", md: "repeat(4, 1fr)" }, gap: 2, my: 2 }}>
        <Wert label="Verbrauch in Buchungszeiten" value={hasData ? `${zahl(k.mitBuchung)} ${unit}` : "–"} detail="Bei gemischten Messintervallen zeitanteilige Schätzung" />
        <Wert label="Verbrauch ohne Buchung" value={hasData ? `${zahl(k.ohneBuchung)} ${unit}` : "–"} detail="Leerzeit-Verbrauch einschließlich möglicher anderer Nutzung" />
        <Wert label="Mit Buchung · pro 24 h" value={k.pro24hMit == null ? "–" : `${zahl(k.pro24hMit)} ${unit}`} detail="Normiert auf abgedeckte Stunden mit Buchung" />
        <Wert label="Ohne Buchung · pro 24 h" value={k.pro24hOhne == null ? "–" : `${zahl(k.pro24hOhne)} ${unit}`} detail="Normiert auf abgedeckte Stunden ohne Buchung" />
      </Box>
      <Typography variant="body2" sx={{ mb: 2 }}>{zahl(k.abgedeckteStunden, 1)} von {zahl(k.zeitraumStunden, 1)} Stunden im ausgewählten Jahr bis zum Datenstand sind durch gültige Messintervalle abgedeckt. Fehlende Zeiten werden nicht als Nullverbrauch gewertet.</Typography>
      <Paper variant="outlined" sx={{ p: 2, mb: 2 }}><Typography variant="h6">Monatsverbrauch und Buchungsanteil</Typography><MonatsDiagramm meter={meter} /></Paper>
      <Paper variant="outlined" sx={{ p: 2, mb: 2 }}><Stack direction={{ xs: "column", sm: "row" }} justifyContent="space-between" spacing={1}><Typography variant="h6">Zusammenhang mit der Belegung</Typography><Chip label={meter.korrelation.pearson == null ? "Korrelation nicht bestimmbar" : `Pearson r = ${zahl(meter.korrelation.pearson, 2)}`} /></Stack><KorrelationsDiagramm meter={meter} /><Typography variant="body2">{meter.korrelation.intervalle} vollständige Messintervalle im gewählten Zeitraum. Jeder Punkt zeigt einen tatsächlich gemessenen Intervallverbrauch pro 24 Stunden gegenüber dem Buchungsanteil desselben Intervalls.</Typography><Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>r nahe +1: höherer Verbrauch bei höherem Buchungsanteil; nahe 0: kein linearer Zusammenhang; nahe −1: gegenläufig. Erforderlich sind mindestens drei vollständige Intervalle und Unterschiede in beiden Größen. Wenige oder lange Intervalle erlauben nur eine grobe Einschätzung. Das belegt keine Ursache: Wetter, Heizung und sonstige Nutzung können den Verbrauch ebenfalls beeinflussen. Geschätzte Monatsstücke werden nicht als zusätzliche Datenpunkte verwendet.</Typography></Paper>
      <Paper variant="outlined" sx={{ p: 2, mb: 2 }}><Typography variant="h6">Zugeordnete Buchungszeiträume</Typography><BuchungsZeitstrahl meter={meter} year={year} stand={data.stand} /><Typography variant="body2" color="text.secondary">Blau: bestätigt / abrechenbar / abgeschlossen · Orange: reserviert. Überlappende Buchungen zählen zeitlich nur einmal. Grundlage ist die aktuelle Zuordnung der Zähler zu Mietbereichen; Sperrzeiten und nicht erfasste Vereinsnutzung sind keine Buchung.</Typography></Paper>
      <Paper variant="outlined" sx={{ mb: 2 }}><Typography variant="h6" sx={{ p: 2 }}>Monatsübersicht</Typography><TableContainer><Table size="small" aria-label="Monatlicher Verbrauch mit und ohne Buchung"><TableHead><TableRow><TableCell>Monat</TableCell><TableCell align="right">Verbrauch ({unit})</TableCell><TableCell align="right">Mit Buchung</TableCell><TableCell align="right">Ohne Buchung</TableCell><TableCell align="right">Buchungsanteil</TableCell><TableCell align="right">Messstunden</TableCell><TableCell>Zuordnung</TableCell></TableRow></TableHead><TableBody>{meter.monate.map(m => <TableRow key={m.monat}><TableCell>{monat(m.monat)}</TableCell><TableCell align="right">{m.abgedeckteStunden ? zahl(m.verbrauch) : "–"}</TableCell><TableCell align="right">{m.abgedeckteStunden ? zahl(m.mitBuchung) : "–"}</TableCell><TableCell align="right">{m.abgedeckteStunden ? zahl(m.ohneBuchung) : "–"}</TableCell><TableCell align="right">{m.belegungsquote == null ? "–" : `${zahl(m.belegungsquote, 1)} %`}</TableCell><TableCell align="right">{zahl(m.abgedeckteStunden, 1)}</TableCell><TableCell>{m.abgedeckteStunden ? m.geschaetzt ? "Zeitanteilig geschätzt" : "Gemessen" : "Keine Messdaten"}</TableCell></TableRow>)}</TableBody></Table></TableContainer></Paper>
      <Accordion><AccordionSummary expandIcon={<ExpandMoreIcon />}><Typography>Messintervalle und Buchungsnummern ({meter.intervalle.length})</Typography></AccordionSummary><AccordionDetails><TableContainer><Table size="small" aria-label="Ableseintervalle und zugehörige Buchungen"><TableHead><TableRow><TableCell>Zeitraum in Auswertung</TableCell><TableCell>Ablesungen</TableCell><TableCell align="right">Verbrauch ({unit})</TableCell><TableCell>Buchungen</TableCell><TableCell>Zuordnung</TableCell></TableRow></TableHead><TableBody>{meter.intervalle.map((v, i) => <TableRow key={i}><TableCell>{datum(v.von)}<br />bis {datum(v.bis)}</TableCell><TableCell>{datum(v.ablesungVon)}<br />bis {datum(v.ablesungBis)}</TableCell><TableCell align="right">{zahl(v.verbrauch)}</TableCell><TableCell>{v.buchungen.join(", ") || "Ohne Buchung"}</TableCell><TableCell>{v.geschaetzt ? "Zeitanteilig geschätzt" : "Gemessen"}<Typography variant="caption" display="block">Buchungsanteil {zahl(v.belegungsquote, 1)} %</Typography></TableCell></TableRow>)}</TableBody></Table></TableContainer></AccordionDetails></Accordion>
    </>}
  </Box>;
}
