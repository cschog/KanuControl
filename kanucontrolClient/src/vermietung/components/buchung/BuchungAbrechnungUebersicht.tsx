import { useEffect, useState } from "react";
import { Alert, Box, Button, Typography } from "@mui/material";
import { getBuchungFinanzenById } from "@/vermietung/api/finanzenApi";
import type { BuchungFinanzen } from "@/vermietung/types/Finanzen";
import { getApiErrorMessage } from "@/kjfp/api/utils/apiError";
const euro = (n: number) => n.toLocaleString('de-DE', { style: 'currency', currency: 'EUR' });
/** Zahlungs- und Kautionssaldo ausschließlich aus der vorhandenen Serverabrechnung. */
export function BuchungAbrechnungUebersicht({ id, revision, ungespeichert, onFinanzen }: {
  id: number; revision: unknown; ungespeichert: boolean; onFinanzen: () => void;
}) {
  const [daten, setDaten] = useState<BuchungFinanzen | null>(null);
  const [fehler, setFehler] = useState('');
  const [versuch, setVersuch] = useState(0);
  useEffect(() => {
    let aktiv = true;
    setDaten(null); setFehler('');
    getBuchungFinanzenById(id).then(d => { if (aktiv) setDaten(d); })
      .catch(e => { if (aktiv) setFehler(getApiErrorMessage(e)); });
    return () => { aktiv = false; };
  }, [id, revision, versuch]);
  const vollstaendig = daten?.verbrauchVorschauVollstaendig && ['ABRECHENBAR', 'ABGESCHLOSSEN'].includes(daten.buchungsstatus);
  const zeile = (label: string, value: string) => <Box key={label} sx={{display:'contents'}}><Typography component="dt">{label}</Typography><Typography component="dd">{value}</Typography></Box>;
  return <Box sx={{ p: 2, border: 1, borderColor: 'divider', borderRadius: 1, display: 'grid', gap: 1 }}>
    <Typography variant="h6">Abrechnung vorbereiten · gespeicherter Stand</Typography>
    {ungespeichert && <Alert severity="info">Ungespeicherte Änderungen sind in dieser Abrechnungsübersicht noch nicht enthalten. Bitte vor dem Abschluss speichern.</Alert>}
    {fehler ? <Alert severity="error">{fehler}<Button onClick={() => setVersuch(v => v + 1)}>Erneut laden</Button></Alert>
      : !daten ? <Typography role="status">Abrechnung wird geladen …</Typography> : <>
        {!vollstaendig && <Alert severity="warning">Die Abrechnung ist noch nicht vollständig vorbereitet. Bitte Zeitraum und Verbrauchswerte prüfen. Gesamtkosten und verbleibender Zahlbetrag sind noch nicht vollständig ermittelt.</Alert>}
        <Box component="dl" sx={{ m: 0, display: 'grid', gridTemplateColumns: 'minmax(0, 1fr) minmax(0, auto)', gap: 1, '& dt, & dd': { m: 0, overflowWrap: 'anywhere' } }}>
          {zeile('Miete nach Rabatt',euro(daten.mieteSoll))}
          {zeile('Verbrauchskosten',vollstaendig?euro(daten.verbrauchSoll):'Noch nicht vollständig')}
          {zeile('Schäden und Fehlbestände',euro(daten.entschaedigungSoll))}
          {zeile('Gesamtkosten',vollstaendig?euro(daten.abrechnungGesamt):'Noch nicht vollständig')}
          {zeile('Zahlungseingänge ohne Kaution',euro(daten.bewegungen.filter(b=>b.kategorie!=='KAUTION'&&b.art==='EINGANG').reduce((s,b)=>s+b.betrag,0)))}
          {zeile('Rückzahlungen ohne Kaution',euro(daten.bewegungen.filter(b=>b.kategorie!=='KAUTION'&&b.art==='RUECKZAHLUNG').reduce((s,b)=>s+b.betrag,0)))}
          {zeile(`${daten.saldo<0?'Verbleibendes Guthaben':'Verbleibender Zahlbetrag'} (inkl. Kautionsabwicklung)`,vollstaendig?euro(Math.abs(daten.saldo)):'Noch nicht vollständig')}
          {zeile('Kaution vereinbart (separat)',euro(daten.kautionSoll))}
          {(['EINGANG','VERRECHNUNG','RUECKZAHLUNG'] as const).map(art=>zeile(`Kaution · ${{EINGANG:'eingegangen',VERRECHNUNG:'ausdrücklich verrechnet',RUECKZAHLUNG:'zurückgezahlt'}[art]}`,euro(daten.bewegungen.filter(b=>b.kategorie==='KAUTION'&&b.art===art).reduce((s,b)=>s+b.betrag,0))))}
        </Box>
        <Typography variant="body2" color="text.secondary">Der Gesamtsaldo enthält gemäß bestehender Zahlungslogik auch noch fällige oder zurückzuzahlende Kaution. Eine Kautionsverrechnung mit Verbrauchskosten wird weiterhin ausdrücklich in der Kautionsabwicklung erfasst.</Typography>
      </>}
    <Button sx={{ justifySelf: 'start' }} onClick={onFinanzen}>Zahlungen und Kautionsabwicklung öffnen</Button>
  </Box>;
}
