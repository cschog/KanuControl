import { useEffect, useState } from "react";
import { useNavigate, useSearchParams } from "react-router-dom";
import { Alert, Box, Button, Chip, CircularProgress, Dialog, DialogActions, DialogContent, DialogTitle, LinearProgress, MenuItem, Paper, Stack, Table, TableBody, TableCell, TableContainer, TableHead, TableRow, TextField, Typography } from "@mui/material";
import { getFinanzDashboard, saveFinanzDashboardGrenze } from "@/vermietung/api/finanzDashboardApi";
import type { FinanzDashboard } from "@/vermietung/types/FinanzDashboard";
import { getApiErrorMessage } from "@/kjfp/api/utils/apiError";

const euro = (n: number) => new Intl.NumberFormat("de-DE", { style: "currency", currency: "EUR" }).format(n);
const monatsname = (m: number) => new Intl.DateTimeFormat("de-DE", { month: "short" }).format(new Date(2026, m - 1, 1));
const quelle = (s: string) => s === "AIRBNB" ? "Airbnb" : "Direkt";
function Karte({ label, value, detail }: { label: string; value: string; detail: string }) {
  return <Paper variant="outlined" sx={{ p: 2, minWidth: 0 }}><Typography color="text.secondary">{label}</Typography><Typography variant="h5" sx={{ my: 1, fontVariantNumeric: "tabular-nums" }}>{value}</Typography><Typography variant="body2" color="text.secondary">{detail}</Typography></Paper>;
}
function Jahresverlauf({ data }: { data: FinanzDashboard }) {
  const current = Number(data.stand.slice(0, 4));
  const count = data.jahr < current ? 12 : data.jahr === current ? Number(data.stand.slice(5, 7)) : 0;
  const values = data.monate.flatMap(m => [m.kumuliert, m.vorjahrKumuliert]);
  const low = Math.min(0, ...values); const high = Math.max(data.umsatzgrenze, data.prognose, ...values, 1) * 1.12;
  const x = (i: number) => 80 + i * 55; const y = (n: number) => 230 - (n - low) / (high - low) * 200;
  return <Box><svg viewBox="0 0 720 270" role="img" aria-label={`Kumulierter Umsatz nach Mietbeginn ${data.jahr}, Vorjahr und Umsatzgrenze`} style={{ width: "100%", maxHeight: 320 }}>
    <line x1="80" x2="685" y1={y(0)} y2={y(0)} stroke="currentColor" opacity="0.3" />
    <line x1="80" x2="685" y1={y(data.umsatzgrenze)} y2={y(data.umsatzgrenze)} stroke="#b26a00" strokeDasharray="5 4" />
    <text x="6" y={y(data.umsatzgrenze) - 6} fontSize="12" fill="currentColor">{euro(data.umsatzgrenze)}</text>
    <polyline points={data.monate.map((m, i) => `${x(i)},${y(m.vorjahrKumuliert)}`).join(" ")} fill="none" stroke="#64748b" strokeWidth="2" />
    {count > 0 && <polyline points={data.monate.slice(0, count).map((m, i) => `${x(i)},${y(m.kumuliert)}`).join(" ")} fill="none" stroke="#1976d2" strokeWidth="3" />}
    {count === 1 && <circle cx={x(0)} cy={y(data.monate[0].kumuliert)} r="4" fill="#1976d2" />}
    {data.trendprognose !== null && count > 0 && count < 12 && <line x1={x(count - 1)} y1={y(data.umsatz)} x2={x(11)} y2={y(data.trendprognose)} stroke="#1976d2" strokeWidth="2" strokeDasharray="4 4" />}
    {data.monate.map((m, i) => <text key={m.monat} x={x(i)} y="254" textAnchor="middle" fontSize="12" fill="currentColor">{monatsname(m.monat)}</text>)}
  </svg><Stack direction="row" spacing={2} useFlexGap flexWrap="wrap"><Typography variant="body2" sx={{ color: "#1976d2" }}>● {data.jahr} / gestrichelt: linearer Trend</Typography><Typography variant="body2" sx={{ color: "#64748b" }}>● {data.jahr - 1}</Typography><Typography variant="body2" sx={{ color: "#b26a00" }}>┄ Planungsgrenze</Typography></Stack></Box>;
}
export default function FinanzDashboardPage() {
  const [params, setParams] = useSearchParams(); const navigate = useNavigate();
  const currentYear = Number(new Intl.DateTimeFormat("en", { timeZone: "Europe/Berlin", year: "numeric" }).format(new Date()));
  const requested = Number(params.get("jahr") ?? currentYear);
  const jahr = Number.isInteger(requested) && requested >= 2000 && requested <= 2100 ? requested : currentYear;
  const [data, setData] = useState<FinanzDashboard | null>(null); const [error, setError] = useState<string | null>(null);
  const [reload, setReload] = useState(0); const [edit, setEdit] = useState(false); const [grenze, setGrenze] = useState("");
  const [saving, setSaving] = useState(false); const [saveError, setSaveError] = useState<string | null>(null);
  useEffect(() => {
    const controller = new AbortController(); setData(null); setError(null);
    getFinanzDashboard(jahr, controller.signal).then(d => { if (!controller.signal.aborted) setData(d); })
      .catch(e => { if (!controller.signal.aborted) setError(getApiErrorMessage(e)); });
    return () => controller.abort();
  }, [jahr, reload]);
  const save = async () => {
    setSaving(true); setSaveError(null);
    try { setData(await saveFinanzDashboardGrenze(jahr, Number(grenze.replace(",", ".")))); setEdit(false); }
    catch (e) { setSaveError(getApiErrorMessage(e)); } finally { setSaving(false); }
  };
  const parsed = Number(grenze.replace(",", "."));
  const validLimit = /^\d{1,10}([.,]\d{1,2})?$/.test(grenze) && parsed > 0;
  const usage = data ? data.umsatz / data.umsatzgrenze * 100 : 0;
  const over = !!data && data.umsatz > data.umsatzgrenze;
  return <Box sx={{ p: { xs: 2, md: 3 } }}>
    <Stack direction={{ xs: "column", sm: "row" }} spacing={2} justifyContent="space-between" sx={{ mb: 2 }}>
      <Box><Typography variant="h4">Finanz-Dashboard</Typography><Typography color="text.secondary">Alle Vermietungen in KC · Jahresumsatz nach Mietbeginn und Ausblick</Typography></Box>
      <Stack direction="row" spacing={1} alignItems="center"><TextField select label="Jahr" size="small" value={jahr} disabled={saving} onChange={e => setParams({ jahr: e.target.value })} sx={{ minWidth: 100 }} SelectProps={{ MenuProps: { PaperProps: { sx: { maxHeight: 300 } } } }}>{Array.from({ length: 101 }, (_, i) => 2100 - i).map(y => <MenuItem key={y} value={y}>{y}</MenuItem>)}</TextField><Button variant="outlined" onClick={() => navigate("/vermietung/abrechnung")}>Abrechnung</Button></Stack>
    </Stack>
    {error && <Alert severity="error" action={<Button onClick={() => setReload(n => n + 1)}>Erneut laden</Button>}>{error}</Alert>}
    {!data && !error && <Box sx={{ py: 5 }}><CircularProgress aria-label="Finanz-Dashboard wird geladen" /></Box>}
    {data && <>
      <Paper variant="outlined" sx={{ p: 2, mb: 2 }}>
        <Stack direction={{ xs: "column", sm: "row" }} spacing={1} justifyContent="space-between" alignItems={{ sm: "center" }}>
          <Typography variant="h6">{euro(data.umsatz)} von {euro(data.umsatzgrenze)}</Typography>
          <Chip color={over ? "error" : usage >= 80 ? "warning" : "success"} label={over ? "Grenze überschritten" : usage >= 100 ? "Grenze erreicht" : `${usage.toLocaleString("de-DE", { minimumFractionDigits: 1, maximumFractionDigits: 1 })} % ausgeschöpft`} />
        </Stack>
        <LinearProgress aria-label="Ausgeschöpfte Umsatzgrenze" variant="determinate" value={Math.min(100, Math.max(0, usage))} color={over ? "error" : usage >= 80 ? "warning" : "primary"} sx={{ height: 12, borderRadius: 1, my: 2 }} />
        <Stack direction={{ xs: "column", sm: "row" }} justifyContent="space-between" spacing={1}><Typography>{data.verbleibend >= 0 ? `Verfügbarer Spielraum: ${euro(data.verbleibend)}` : `Über der Grenze: ${euro(-data.verbleibend)}`}</Typography><Button size="small" onClick={() => { setGrenze(String(data.umsatzgrenze)); setSaveError(null); setEdit(true); }}>Grenze für {jahr} ändern</Button></Stack>
      </Paper>
      {data.prognose > data.umsatzgrenze && jahr >= currentYear && <Alert severity="warning" sx={{ mb: 2 }}>Die Jahresprognose liegt mit {euro(data.prognose)} um {euro(data.prognose - data.umsatzgrenze)} über deiner Planungsgrenze.</Alert>}
      <Box sx={{ display: "grid", gridTemplateColumns: { xs: "1fr", sm: "1fr 1fr", lg: "repeat(4, 1fr)" }, gap: 2, mb: 2 }}>
        <Karte label="Erfasster Umsatz" value={euro(data.umsatz)} detail={`Nach Mietbeginn ${jahr} · Endbeträge abgeschlossener Buchungen und erfasste Zahlungen bis ${new Intl.DateTimeFormat("de-DE").format(new Date(data.stand + "T12:00:00"))}`} />
        <Karte label="Offene Buchungsbeträge" value={euro(data.geplantOffen)} detail="Bekannte Restbeträge · Anreise im gewählten Jahr" />
        <Karte label="Jahresprognose" value={euro(data.prognose)} detail={jahr < currentYear ? "Abgeschlossenes Jahr: erfasster Umsatz" : "Höherer Wert aus Buchungsplanung und linearem Trend"} />
        <Karte label={`Vorjahr ${jahr - 1}`} value={euro(data.vorjahresumsatz)} detail="Umsatz nach Mietbeginn im Vorjahr" />
      </Box>
      <Paper variant="outlined" sx={{ p: 2, mb: 2 }}><Typography variant="h6">Jahresverlauf · kumuliert nach Mietbeginn</Typography><Jahresverlauf data={data} /></Paper>
      <Paper variant="outlined" sx={{ p: 2, mb: 2 }}><Typography variant="h6" gutterBottom>So entsteht der Ausblick</Typography>
        <Typography>Buchungsplanung: {euro(data.buchungsprognose)}{jahr < currentYear ? " (keine Hochrechnung für abgeschlossene Jahre)" : " = Umsatz + offene Beträge nicht abgeschlossener Buchungen"}.</Typography>
        <Typography>Linearer Trend: {data.trendprognose === null ? "nur im laufenden Jahr verfügbar" : `${euro(data.trendprognose)} · bisheriger Umsatz / vergangene Kalendertage × Tage im Jahr`}.</Typography>
        <Typography variant="body2" color="text.secondary" sx={{ mt: 1 }}>Abgeschlossene Buchungen zählen mit Endmiete und gespeicherten Verbrauchskosten; bei noch nicht abgeschlossenen Buchungen zählen erfasste Zahlungseingänge abzüglich Rückzahlungen. Alles wird dem Monat und Jahr des Mietbeginns zugeordnet. Offene Beträge abgeschlossener Buchungen bleiben in der Übersicht sichtbar, werden aber nicht zusätzlich zur Umsatzprognose addiert. Künftige Zahlungstermine und weitere Buchungen sind unbekannt; die lineare Hochrechnung berücksichtigt keine Saisonalität. Noch nicht abgerechnete Verbrauchskosten fehlen in den Restbeträgen.</Typography>
        {data.unbefristeteBuchungen > 0 && <Alert severity="info" sx={{ mt: 1 }}>{data.unbefristeteBuchungen} unbefristete Buchung(en) sind nur mit ihren tatsächlichen Zahlungen enthalten; zukünftige Einnahmen werden dafür nicht geschätzt.</Alert>}
      </Paper>
      <Paper variant="outlined" sx={{ p: 2, mb: 2 }}><Typography variant="h6" gutterBottom>Einnahmen nach Buchungsquelle</Typography><Stack direction="row" spacing={2} useFlexGap flexWrap="wrap">{data.quellen.length ? data.quellen.map(q => <Chip key={q.quelle} label={`${quelle(q.quelle)}: ${euro(q.umsatz)}`} />) : <Typography color="text.secondary">Noch keine Einnahmen erfasst.</Typography>}</Stack></Paper>
      <Paper variant="outlined" sx={{ mb: 2 }}><Typography variant="h6" sx={{ p: 2 }}>Monatsübersicht · Zuordnung nach Mietbeginn</Typography><TableContainer><Table size="small" aria-label="Monatliche Vermietungsumsätze nach Mietbeginn"><TableHead><TableRow><TableCell>Monat des Mietbeginns</TableCell><TableCell align="right">Umsatz</TableCell><TableCell align="right">Kumuliert</TableCell><TableCell align="right">Vorjahr kumuliert</TableCell></TableRow></TableHead><TableBody>{data.monate.map(m => <TableRow key={m.monat}><TableCell>{monatsname(m.monat)}</TableCell><TableCell align="right">{euro(m.umsatz)}</TableCell><TableCell align="right">{euro(m.kumuliert)}</TableCell><TableCell align="right">{euro(m.vorjahrKumuliert)}</TableCell></TableRow>)}</TableBody></Table></TableContainer></Paper>
      <Paper variant="outlined" sx={{ mb: 2 }}><Typography variant="h6" sx={{ p: 2 }}>Buchungen · Einnahmen und bekannte Restbeträge</Typography><TableContainer><Table size="small" aria-label="Finanzübersicht je Buchung"><TableHead><TableRow><TableCell>Buchung</TableCell><TableCell>Mieter / Objekt</TableCell><TableCell>Quelle</TableCell><TableCell align="right">Umsatz {jahr}</TableCell><TableCell align="right">Offen geplant</TableCell></TableRow></TableHead><TableBody>{data.buchungen.map(b => <TableRow key={b.id}><TableCell>{["STORNIERT", "ABGELEHNT"].includes(b.status) ? <>{b.nummer ?? `#${b.id}`}<Typography variant="caption" display="block">{b.status === "STORNIERT" ? "Storniert" : "Abgelehnt"}</Typography></> : <Button onClick={() => navigate(`/vermietung/abrechnung?buchungId=${b.id}`, { state: { returnTo: `/vermietung/finanz-dashboard?jahr=${jahr}` } })}>{b.nummer ?? `#${b.id}`}</Button>}</TableCell><TableCell>{b.mieter}<Typography variant="body2" color="text.secondary">{b.mietobjekt}</Typography></TableCell><TableCell>{quelle(b.quelle)}</TableCell><TableCell align="right">{euro(b.umsatz)}</TableCell><TableCell align="right">{euro(b.geplantOffen)}</TableCell></TableRow>)}{!data.buchungen.length && <TableRow><TableCell colSpan={5}>Keine Einnahmen oder offenen Buchungsbeträge für dieses Jahr erfasst.</TableCell></TableRow>}</TableBody></Table></TableContainer></Paper>
      <Alert severity="info">Abgeschlossene Buchungen zählen mit ihrer Endmiete und den gespeicherten Verbrauchskosten. Bei noch nicht abgeschlossenen Buchungen zählen in KC erfasste Zahlungen: Anzahlungen, Miete und Verbrauchskosten abzüglich Rückzahlungen. Alle Beträge werden dem Monat und Jahr des Mietbeginns zugeordnet, unabhängig vom Zahlungsdatum. Reine Kautionen zählen nicht; in Verbrauchskosten umgewandelte Kaution zählt einmal. Zahlungen stornierter Buchungen bleiben bis zur erfassten Rückzahlung enthalten. Die einstellbare Planungsgrenze startet mit 25.000 €; sie ist keine automatische steuerliche Beurteilung. <a href="https://www.gesetze-im-internet.de/ustg_1980/__19.html" target="_blank" rel="noreferrer">§ 19 UStG</a> unterscheidet derzeit 25.000 € Vorjahresumsatz und 100.000 € im laufenden Jahr. Weitere Umsätze außerhalb von KC sind nicht enthalten.</Alert>
    </>}
    <Dialog open={edit} onClose={() => { if (!saving) setEdit(false); }} fullWidth maxWidth="xs"><DialogTitle>Planungsgrenze {jahr}</DialogTitle><DialogContent><Typography variant="body2" sx={{ mb: 2 }}>Diese Grenze gilt für das gewählte Kalenderjahr und alle Vermietungen dieses Vereins.</Typography><TextField autoFocus fullWidth label="Umsatzgrenze in Euro" value={grenze} onChange={e => setGrenze(e.target.value)} disabled={saving} error={!validLimit} helperText="Positiver Betrag, höchstens zwei Nachkommastellen" inputProps={{ inputMode: "decimal" }} />{saveError && <Alert severity="error" sx={{ mt: 1 }}>{saveError}</Alert>}</DialogContent><DialogActions><Button disabled={saving} onClick={() => setEdit(false)}>Abbrechen</Button><Button variant="contained" disabled={saving || !validLimit} onClick={save}>{saving ? "Speichert …" : "Speichern"}</Button></DialogActions></Dialog>
  </Box>;
}
