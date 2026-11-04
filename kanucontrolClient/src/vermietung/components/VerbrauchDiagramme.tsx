import { Box, Stack, Typography } from "@mui/material";
import type { VerbrauchZaehler } from "@/vermietung/types/VerbrauchDashboard";
export const zahl = (n: number | null | undefined, digits = 3) => n == null ? "–" : n.toLocaleString("de-DE", { maximumFractionDigits: digits });
export const datum = (s: string) => new Intl.DateTimeFormat("de-DE", { dateStyle: "short", timeStyle: "short" }).format(new Date(s));
export const monat = (m: number) => new Intl.DateTimeFormat("de-DE", { month: "short" }).format(new Date(2026, m - 1, 1));
const blue = "#1976d2", orange = "#b26a00", dark = "#475569";

export function MonatsDiagramm({ meter }: { meter: VerbrauchZaehler }) {
  const max = Math.max(1, ...meter.monate.map(m => m.verbrauch)) * 1.1;
  const x = (i: number) => 70 + i * 52; const y = (v: number) => 240 - v / max * 195;
  const points = meter.monate.map((m, i) => m.belegungsquote == null ? null : `${x(i) + 16},${240 - m.belegungsquote / 100 * 195}`).filter(Boolean).join(" ");
  return <Box><Box sx={{ overflowX: "auto" }}><svg role="img" aria-label={`Monatsverbrauch ${meter.bezeichnung} mit und ohne Buchung und Buchungsanteil`} viewBox="0 0 735 290" style={{ width: "100%", minWidth: 620, maxHeight: 350 }}>
    <defs><pattern id="verbrauch-schaetzung" width="6" height="6" patternUnits="userSpaceOnUse"><path d="M0,6 L6,0" stroke="white" strokeWidth="1.5" opacity="0.7" /></pattern></defs>
    {[0, 0.5, 1].map(t => <g key={t}><line x1="60" x2="680" y1={y(t * max)} y2={y(t * max)} stroke="currentColor" opacity="0.15" /><text x="55" y={y(t * max) + 4} textAnchor="end" fontSize="11" fill="currentColor">{zahl(t * max, 1)}</text><text x="690" y={y(t * max) + 4} fontSize="11" fill="currentColor">{zahl(t * 100, 0)} %</text></g>)}
    <text x="60" y="20" fontSize="12" fill="currentColor">Verbrauch ({meter.einheit})</text><text x="680" y="20" textAnchor="end" fontSize="12" fill="currentColor">Zeit mit Buchung</text>
    {meter.monate.map((m, i) => <g key={m.monat}><title>{monat(m.monat)}: {m.abgedeckteStunden ? `${zahl(m.verbrauch)} ${meter.einheit}` : "keine Messdaten"}; Buchungsanteil {zahl(m.belegungsquote, 1)} %; {m.geschaetzt ? "zeitanteilig geschätzt" : "gemessen"}</title>
      {m.abgedeckteStunden > 0 ? <><rect x={x(i)} y={y(m.verbrauch)} width="32" height={(m.ohneBuchung / max) * 195} fill={orange} /><rect x={x(i)} y={y(m.mitBuchung)} width="32" height={(m.mitBuchung / max) * 195} fill={blue} />{m.geschaetzt && <rect x={x(i)} y={y(m.verbrauch)} width="32" height={(m.verbrauch / max) * 195} fill="url(#verbrauch-schaetzung)" />}</> : <rect x={x(i)} y="235" width="32" height="5" fill="#cbd5e1" />}
      <text x={x(i) + 16} y="265" textAnchor="middle" fontSize="12" fill="currentColor">{monat(m.monat)}</text></g>)}
    <polyline points={points} fill="none" stroke={dark} strokeWidth="2" strokeDasharray="5 3" />
    {meter.monate.map((m, i) => m.belegungsquote == null ? null : <circle key={m.monat} cx={x(i) + 16} cy={240 - m.belegungsquote / 100 * 195} r="3" fill={dark} />)}
  </svg></Box><Stack direction="row" spacing={2} useFlexGap flexWrap="wrap"><Typography variant="body2" sx={{ color: blue }}>■ mit Buchung</Typography><Typography variant="body2" sx={{ color: orange }}>■ ohne Buchung</Typography><Typography variant="body2">┄ Zeitanteil mit Buchung (rechte Achse)</Typography><Typography variant="body2" color="text.secondary">Schraffiert: zeitanteilig geschätzt · Grau: keine Messdaten</Typography></Stack></Box>;
}

export function KorrelationsDiagramm({ meter }: { meter: VerbrauchZaehler }) {
  const points = meter.korrelation.punkte; const max = Math.max(1, ...points.map(p => p.pro24h)) * 1.1;
  return <Box sx={{ overflowX: "auto" }}><svg role="img" aria-label={`Zusammenhang von Buchungsanteil und Verbrauch pro 24 Stunden für ${meter.bezeichnung}`} viewBox="0 0 620 310" style={{ width: "100%", minWidth: 360, maxHeight: 310 }}>
    {[0, 0.5, 1].map(t => <g key={t}><line x1="65" x2="580" y1={250 - t * 200} y2={250 - t * 200} stroke="currentColor" opacity="0.15" /><text x="58" y={254 - t * 200} textAnchor="end" fontSize="12" fill="currentColor">{zahl(t * max, 2)}</text><text x={65 + t * 515} y="274" textAnchor="middle" fontSize="12" fill="currentColor">{t * 100} %</text></g>)}
    <text x="65" y="25" fontSize="12" fill="currentColor">{meter.einheit} pro 24 Messstunden</text><text x="315" y="300" fontSize="12" textAnchor="middle" fill="currentColor">Buchungsanteil im vollständigen Messintervall</text>
    {points.map((p, i) => <circle key={i} cx={65 + p.belegungsquote / 100 * 515} cy={250 - p.pro24h / max * 200} r="5" fill={blue} opacity="0.7"><title>{datum(p.von)} – {datum(p.bis)}: {zahl(p.belegungsquote, 1)} % mit Buchung; {zahl(p.pro24h)} {meter.einheit}/24 h</title></circle>)}
    {!points.length && <text x="320" y="150" textAnchor="middle" fill="currentColor" fontSize="14">Noch keine vollständigen Messintervalle</text>}
  </svg></Box>;
}

export function BuchungsZeitstrahl({ meter, year, stand }: { meter: VerbrauchZaehler; year: number; stand: string }) {
  const from = new Date(`${year}-01-01T00:00:00`).getTime();
  const to = Math.min(new Date(`${year + 1}-01-01T00:00:00`).getTime(), new Date(stand).getTime());
  const duration = Math.max(1, to - from); const height = Math.max(65, meter.buchungen.length * 26 + 42);
  return <Box sx={{ overflow: "auto", maxHeight: 280 }}><svg role="img" aria-label="Buchungszeiträume für die zugeordneten Mietbereiche" viewBox={`0 0 730 ${height}`} style={{ width: "100%", minWidth: 620 }}>
    <text x="130" y="16" fontSize="12" fill="currentColor">1. Januar {year}</text><text x="705" y="16" textAnchor="end" fontSize="12" fill="currentColor">{to > from ? datum(new Date(to).toISOString()) : "Zeitraum liegt in der Zukunft"}</text>
    {meter.buchungen.map((b, i) => { const start = Math.max(from, new Date(b.von).getTime()), end = Math.min(to, new Date(b.bis).getTime()); return <g key={b.id}><title>{b.nummer}: {datum(b.von)} – {b.bis.startsWith("3000") ? "unbefristet" : datum(b.bis)} ({b.status})</title><text x="5" y={40 + i * 26} fontSize="12" fill="currentColor">{b.nummer ?? `#${b.id}`}</text><rect x="130" y={27 + i * 26} width="575" height="18" fill="#e2e8f0" /><rect x={130 + (start - from) / duration * 575} y={27 + i * 26} width={Math.max(1, (end - start) / duration * 575)} height="18" fill={b.status === "RESERVIERT" ? "#b26a00" : "#1976d2"} /></g>; })}
    {!meter.buchungen.length && <text x="130" y="45" fontSize="13" fill="currentColor">Keine zugeordneten Buchungen in diesem Zeitraum</text>}
  </svg></Box>;
}
