import { useEffect, useMemo, useState } from "react";
import { Accordion, AccordionDetails, AccordionSummary, Alert, Box, Button, Chip, Dialog, DialogActions, DialogContent, DialogTitle, Stack, TextField, Typography } from "@mui/material";
import AddIcon from "@mui/icons-material/Add";
import ArrowBackIcon from "@mui/icons-material/ArrowBack";
import ExpandMoreIcon from "@mui/icons-material/ExpandMore";
import { useLocation, useNavigate, useSearchParams } from "react-router-dom";
import { getBuchungFinanzen, erfasseFinanzbewegung } from "@/vermietung/api/finanzenApi";
import type { BuchungFinanzen, Finanzbewegung, Finanzbewegungsart, Finanzkategorie } from "@/vermietung/types/Finanzen";
import { ErrorDialog } from "@/core/components/common/ErrorDialog";
import { getApiErrorMessage } from "@/kjfp/api/utils/apiError";

const kategorien: { key: Finanzkategorie; label: string }[] = [
  { key: "ANZAHLUNG", label: "Anzahlung" }, { key: "KAUTION", label: "Kaution" },
  { key: "MIETE", label: "Miete" }, { key: "VERBRAUCHSKOSTEN", label: "Verbrauchskosten" },
];
const betragLabel = (v: number) => new Intl.NumberFormat("de-DE", { style: "currency", currency: "EUR" }).format(v || 0);
const datumLabel = (v?: string | null) => v ? new Intl.DateTimeFormat("de-DE").format(new Date(v)) : "–";
const offenNachBestaetigung = (status: string) => !["ANFRAGE", "ABGELEHNT", "STORNIERT"].includes(status);
const bewegungsLabel = (b: Finanzbewegung) => ({ EINGANG: "Eingang", RUECKZAHLUNG: "Rückzahlung", VERRECHNUNG: "Verrechnung" }[b.art]);

function paidFor(row: BuchungFinanzen, category: Finanzkategorie) {
  return row.bewegungen.filter((b) => b.kategorie === category).reduce((total, b) => {
    if (b.art === "EINGANG") return total + b.betrag;
    if (b.art === "RUECKZAHLUNG") return total - b.betrag;
    return total + (category === "KAUTION" ? -b.betrag : b.betrag);
  }, 0);
}
function mietbewegungen(row: BuchungFinanzen) {
  return row.bewegungen.filter((b) => b.kategorie === "ANZAHLUNG" || b.kategorie === "MIETE");
}
function mietRueckzahlung(row: BuchungFinanzen) {
  return mietbewegungen(row)
    .filter((b) => b.art === "RUECKZAHLUNG")
    .reduce((sum, b) => sum + b.betrag, 0);
}
function mietUeberzahlungVorRueckzahlung(row: BuchungFinanzen) {
  const eingegangen = mietbewegungen(row)
    .filter((b) => b.art === "EINGANG")
    .reduce((sum, b) => sum + b.betrag, 0);
  const mietSollGesamt = soll(row, "ANZAHLUNG") + soll(row, "MIETE");
  return Math.max(0, eingegangen - mietSollGesamt);
}
function kautionAusMietRueckzahlung(row: BuchungFinanzen) {
  return Math.max(0, mietRueckzahlung(row) - mietUeberzahlungVorRueckzahlung(row));
}
function rentPaid(row: BuchungFinanzen) {
  const eingegangen = mietbewegungen(row)
    .filter((b) => b.art === "EINGANG")
    .reduce((sum, b) => sum + b.betrag, 0);
  return Math.max(0, eingegangen - Math.min(mietRueckzahlung(row), mietUeberzahlungVorRueckzahlung(row)));
}
function kautionEingegangen(row: BuchungFinanzen) {
  return row.bewegungen
    .filter((b) => b.kategorie === "KAUTION" && b.art === "EINGANG")
    .reduce((sum, b) => sum + b.betrag, 0);
}
function kautionVerrechnet(row: BuchungFinanzen) {
  return row.bewegungen
    .filter((b) => b.kategorie === "KAUTION" && b.art === "VERRECHNUNG")
    .reduce((sum, b) => sum + b.betrag, 0);
}
function kautionZurueckzuzahlen(row: BuchungFinanzen) {
  const rueckgezahlt = row.bewegungen
    .filter((b) => b.kategorie === "KAUTION" && b.art === "RUECKZAHLUNG")
    .reduce((sum, b) => sum + b.betrag, 0);
  return Math.max(0, kautionEingegangen(row) - kautionVerrechnet(row) - rueckgezahlt - kautionAusMietRueckzahlung(row));
}
function advanceSoll(row: BuchungFinanzen) {
  return soll(row, "ANZAHLUNG");
}
function soll(row: BuchungFinanzen, category: Finanzkategorie) {
  if (!offenNachBestaetigung(row.buchungsstatus)) return 0;
  if (row.buchungsquelle === "AIRBNB" && (category === "KAUTION" || category === "ANZAHLUNG")) return 0;
  if (category === "ANZAHLUNG") return Math.min(row.anzahlungSoll, row.mieteSoll);
  if (category === "MIETE") return Math.max(0, row.mieteSoll - Math.min(row.anzahlungSoll, row.mieteSoll));
  return ({ KAUTION: row.kautionSoll, VERBRAUCHSKOSTEN: row.nebenkostenSoll })[category] || 0;
}
function erfasst(row: BuchungFinanzen, category: Finanzkategorie) {
  if (category === "ANZAHLUNG") return Math.min(advanceSoll(row), rentPaid(row));
  if (category === "MIETE") return Math.max(0, rentPaid(row) - advanceSoll(row));
  if (category === "KAUTION") return kautionEingegangen(row);
  return paidFor(row, category);
}
function offeneSumme(row: BuchungFinanzen, category: Finanzkategorie) {
  if (category === "ANZAHLUNG") return Math.max(0, soll(row, category) - rentPaid(row));
  if (category === "MIETE") return Math.max(0, soll(row, category) - Math.max(0, rentPaid(row) - advanceSoll(row)));
  const credited = category === "KAUTION"
    ? kautionEingegangen(row)
    : paidFor(row, category);
  return Math.max(0, soll(row, category) - credited);
}
function statusText(row: BuchungFinanzen) {
  if (row.buchungsstatus === "STORNIERT") return "Storniert";
  if (row.buchungsstatus === "ABGESCHLOSSEN") return "Abgerechnet";
  const targets = kategorien.map((k) => soll(row, k.key)).filter((x) => x > 0);
  if (targets.length === 0) return row.buchungsstatus === "ANFRAGE" ? "Anfrage" : "Offen";
  const totalDue = targets.reduce((a, b) => a + b, 0);
  const totalPaid = kategorien.reduce((a, k) => a + Math.min(soll(row, k.key), Math.max(0, erfasst(row, k.key))), 0);
  if (totalPaid <= 0) return "Offen";
  if (totalPaid + 0.005 < totalDue) return "Angezahlt";
  return "Bezahlt";
}
const AbrechnungPage = () => {
  const [rows, setRows] = useState<BuchungFinanzen[]>([]);
  const location = useLocation();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const markierteBuchungId = Number(searchParams.get("buchungId"));
  const navigationState = location.state as { returnTo?: string; returnToState?: unknown } | null;
  const [dialog, setDialog] = useState<{ row: BuchungFinanzen; category: Finanzkategorie } | null>(null);
  const [art, setArt] = useState<Finanzbewegungsart>("EINGANG");
  const [betrag, setBetrag] = useState("");
  const [datum, setDatum] = useState(new Date().toLocaleDateString("sv-SE"));
  const [bemerkung, setBemerkung] = useState("");
  const [error, setError] = useState<string | null>(null);
  const [saveError, setSaveError] = useState<string | null>(null);
  const [saving, setSaving] = useState(false);
  const laden = async () => setRows(await getBuchungFinanzen());
  useEffect(() => { void laden().catch((e: unknown) => setError(getApiErrorMessage(e))); }, []);
  const sortiert = useMemo(() => [...rows].sort((a,b) => (b.anreise ?? "").localeCompare(a.anreise ?? "")), [rows]);
  useEffect(() => {
    if (!Number.isFinite(markierteBuchungId) || markierteBuchungId <= 0 || rows.length === 0) return;
    document.getElementById(`finanz-buchung-${markierteBuchungId}`)?.scrollIntoView({ behavior: "smooth", block: "center" });
  }, [markierteBuchungId, rows.length]);
  const openDialog = (row: BuchungFinanzen, category: Finanzkategorie, type: Finanzbewegungsart) => {
    const vorschlag = type === "VERRECHNUNG" && row.verbrauchVorschauVollstaendig
      ? Math.min(Math.max(0, paidFor(row, "KAUTION")), Math.max(0, offeneSumme(row, "VERBRAUCHSKOSTEN")))
      : type === "RUECKZAHLUNG" && category === "KAUTION"
        ? kautionZurueckzuzahlen(row)
        : 0;
    setDialog({row,category}); setArt(type); setBetrag(vorschlag > 0 ? vorschlag.toFixed(2) : ""); setBemerkung(""); setDatum(new Date().toLocaleDateString("sv-SE")); setSaveError(null);
  };
  const save = async () => {
    if (!dialog) return;
    setSaving(true); setSaveError(null);
    try {
      await erfasseFinanzbewegung(dialog.row.buchungId, { kategorie: dialog.category, art, betrag: Number(betrag), datum, bemerkung });
      await laden(); setDialog(null);
    } catch (e: unknown) { setSaveError(getApiErrorMessage(e)); }
    finally { setSaving(false); }
  };
  return <Box sx={{ p: 3 }}>
    <Stack direction={{ xs: "column", sm: "row" }} justifyContent="space-between" alignItems={{ xs: "stretch", sm: "center" }} spacing={1} sx={{ mb: 1 }}>
      <Typography variant="h4">Abrechnung Vermietung</Typography>
      {navigationState?.returnTo && (
        <Button
          variant="outlined"
          startIcon={<ArrowBackIcon />}
          onClick={() => navigate(navigationState.returnTo!, { state: navigationState.returnToState ?? null })}
        >
          {navigationState.returnTo?.includes("finanz-dashboard") ? "Zurück zum Finanz-Dashboard" : "Zurück zur Buchung"}
        </Button>
      )}
    </Stack>
    <Typography color="text.secondary" sx={{ mb: 2 }}>Zahlungseingänge, Rückzahlungen und offene Beträge je Buchung. Anzahlung und Kaution werden bei bestätigten Direktbuchungen als angefragt ausgewiesen.</Typography>
    <Stack spacing={2}>
      {sortiert.map((row) => <Accordion id={`finanz-buchung-${row.buchungId}`} key={row.buchungId} defaultExpanded={row.buchungId === markierteBuchungId} sx={{border: 1, borderColor: row.buchungId === markierteBuchungId ? "primary.main" : "divider", borderWidth: row.buchungId === markierteBuchungId ? 2 : 1, borderRadius: "4px !important", "&:before": {display: "none"}}}>
        <AccordionSummary expandIcon={<ExpandMoreIcon />}>
          <Stack direction={{xs:"column",md:"row"}} justifyContent="space-between" alignItems={{xs:"flex-start",md:"center"}} spacing={1} sx={{width:"100%",pr:1}}>
            <Box>
              <Typography variant="h6">{row.buchungsnummer} · {datumLabel(row.anreise)} bis {row.unbefristet ? "unbegrenzt" : datumLabel(row.abreise)}</Typography>
              <Typography color="text.secondary">{row.mieter || "ohne Mieter"} · {row.mietobjekt} · {row.buchungsquelle === "AIRBNB" ? "Airbnb" : "Direkt"}</Typography>
            </Box>
            <Stack direction="row" alignItems="center" spacing={2}>
              <Box sx={{textAlign:{xs:"left",md:"right"}}}>
                <Typography variant="caption" color="text.secondary">{row.buchungsstatus === "STORNIERT" ? "Einbehalt" : "Umsatz"}</Typography>
                <Typography fontWeight={700}>{betragLabel(row.buchungsstatus === "STORNIERT" ? row.stornoEinbehalt ?? 0 : row.abrechnungGesamt)}</Typography>
              </Box>
              <Chip label={statusText(row)} color={statusText(row) === "Abgerechnet" ? "success" : statusText(row) === "Angezahlt" ? "info" : "default"} />
            </Stack>
          </Stack>
        </AccordionSummary>
        <AccordionDetails>
          <Stack direction="row" justifyContent="flex-end" sx={{mb:1}}>
            <Button size="small" onClick={() => {
              const params = new URLSearchParams(location.search);
              params.set("buchungId", String(row.buchungId));
              navigate(`/vermietung/buchung?buchungId=${row.buchungId}`, { state: { returnTo: `${location.pathname}?${params.toString()}` } });
            }}>Buchung öffnen</Button>
          </Stack>
        {(() => {
          const saldo = row.saldo;
          const abgerechnet = row.buchungsstatus === "ABGESCHLOSSEN";
          const saldoText = saldo > 0
            ? `Der Mieter muss noch ${betragLabel(saldo)} nachzahlen.`
            : saldo < 0
              ? `An den Mieter sind ${betragLabel(Math.abs(saldo))} zurückzuzahlen.`
              : "Ausgeglichen.";
          return (
            <Alert severity={saldo > 0 ? "warning" : "success"} sx={{mb:2}}>
              <Typography fontWeight={700}>{row.buchungsstatus === "STORNIERT" ? "Stornosaldo" : abgerechnet ? "Endsaldo" : "Vorläufiger Saldo"}</Typography>
              <Typography>{saldoText}</Typography>
              {!abgerechnet && row.buchungsstatus !== "STORNIERT" && <Typography variant="body2" color="text.secondary">Der Betrag kann sich bis zum Abschluss der Buchung noch ändern.</Typography>}
            </Alert>
          );
        })()}
        {row.buchungsstatus === "STORNIERT" ? <Stack spacing={1}>
          <Alert severity="info">Verbindlicher Einbehalt: {betragLabel(row.stornoEinbehalt ?? 0)}. Die Stornierungsnachricht bucht keine Überweisung. Tatsächlich ausgeführte Erstattungen hier erfassen.</Alert>
          {kategorien.map(category => { const verfuegbar = paidFor(row, category.key); return verfuegbar > 0 ? <Stack key={category.key} direction="row" spacing={1} alignItems="center"><Typography>{category.label}: {betragLabel(verfuegbar)} verbleibend</Typography>{!(row.stornoEinbehalt ?? 0) && <Button onClick={() => openDialog(row, category.key, "RUECKZAHLUNG")}>Ausgeführte Rückzahlung erfassen</Button>}</Stack> : null; })}
        </Stack> : <Stack spacing={1}>
          {kategorien.map((category) => {
            const due=soll(row,category.key);
            const paid=category.key === "KAUTION" ? kautionEingegangen(row) : erfasst(row,category.key);
            const balance=category.key === "KAUTION" ? kautionZurueckzuzahlen(row) : offeneSumme(row,category.key);
            if (due===0 && paid===0 && !(category.key === "MIETE" && row.mietkosten.length > 0)) return null;
            return <Box key={category.key} sx={{display:"grid",gridTemplateColumns:{xs:"minmax(0,1fr) minmax(0,1fr)",md:"minmax(0,1.2fr) minmax(0,1fr) minmax(0,1fr) minmax(0,1fr) 280px"},alignItems:"center",gap:1,borderTop:1,borderColor:"divider",pt:1}}>
              <Typography>{category.key === "MIETE" && advanceSoll(row) > 0 ? "Restmiete" : category.label}{(category.key==="ANZAHLUNG" || category.key==="KAUTION") && due>0 ? " · angefragt" : ""}</Typography>
              <Box sx={{display:"flex",justifyContent:"flex-start",alignItems:"baseline",flexWrap:"wrap",columnGap:1,rowGap:0,minWidth:0}}>
                <Typography variant="body2">Soll:</Typography>
                <Typography variant="body2" sx={{textAlign:"right",whiteSpace:"nowrap",fontVariantNumeric:"tabular-nums"}}>{betragLabel(due)}</Typography>
              </Box>
              <Box sx={{display:"flex",justifyContent:"flex-start",alignItems:"baseline",flexWrap:"wrap",columnGap:1,rowGap:0,minWidth:0}}>
                <Typography variant="body2">Eingang:</Typography>
                <Typography variant="body2" sx={{textAlign:"right",whiteSpace:"nowrap",fontVariantNumeric:"tabular-nums"}}>{betragLabel(paid)}</Typography>
              </Box>
              <Box sx={{display:"flex",justifyContent:"flex-start",alignItems:"baseline",flexWrap:"wrap",columnGap:1,rowGap:0,minWidth:0}}>
                <Typography variant="body2" fontWeight={600}>{category.key === "KAUTION" ? "Zurückzuzahlen:" : "Offen:"}</Typography>
                <Typography variant="body2" fontWeight={600} sx={{textAlign:"right",whiteSpace:"nowrap",fontVariantNumeric:"tabular-nums"}}>{betragLabel(balance)}</Typography>
              </Box>
              <Stack direction="row" spacing={.5} sx={{gridColumn:{xs:"1 / -1",md:"auto"}}}>
                <Button size="small" startIcon={<AddIcon/>} onClick={()=>openDialog(row,category.key,"EINGANG")}>Eingang</Button>
                {category.key === "KAUTION" && <>
                  {kautionZurueckzuzahlen(row) > 0 && <Button size="small" onClick={()=>openDialog(row,category.key,"RUECKZAHLUNG")}>Rückzahlen</Button>}
                  {paidFor(row, "KAUTION") > 0 && offeneSumme(row, "VERBRAUCHSKOSTEN") > 0 && <Button size="small" onClick={()=>openDialog(row,category.key,"VERRECHNUNG")}>Verrechnen</Button>}
                </>}
                {category.key !== "KAUTION" && paid > 0 && <Button size="small" onClick={()=>openDialog(row,category.key,"RUECKZAHLUNG")}>Rückzahlen</Button>}
              </Stack>
              {category.key === "KAUTION" && <Typography variant="body2" color="text.secondary" sx={{gridColumn:"1 / -1"}}>
                Noch vom Mieter offen: {betragLabel(offeneSumme(row, "KAUTION"))} · Mit Verbrauchskosten verrechnet: {betragLabel(kautionVerrechnet(row))}{kautionAusMietRueckzahlung(row) > 0 ? ` · Durch Mietrückzahlung erledigt: ${betragLabel(kautionAusMietRueckzahlung(row))}` : ""}
              </Typography>}
              {category.key === "MIETE" && row.mietkosten.length > 0 && <Box sx={{gridColumn:"1 / -1",pl:1,borderLeft:2,borderColor:"divider"}}>
                {row.mietkosten.map((item,index)=><Typography key={index} variant="body2" color="text.secondary">
                  {item.bezeichnung}: {item.anzahl} × {item.berechnungsart === "TAG" ? `${item.abrechnungseinheiten ?? 1} Tage × ` : ""}{betragLabel(item.einzelpreis)} = {betragLabel(item.gesamtbetrag)}
                </Typography>)}
                {row.mietrabatt > 0 && <Typography variant="body2" color="text.secondary">Buchungsrabatt: −{betragLabel(row.mietrabatt)}</Typography>}
                <Typography variant="body2" color="text.secondary">Mietkosten gesamt: {betragLabel(row.mieteSoll)} · Anzahlung {betragLabel(advanceSoll(row))} wird angerechnet.</Typography>
              </Box>}
            </Box>;
          })}
        </Stack>}
        {row.buchungsstatus !== "STORNIERT" && row.entschaedigungen.length > 0 && <Box sx={{mt:2}}>
          <Typography variant="h6">Entschädigung</Typography>
          {row.entschaedigungen.map((p,i)=><Typography key={p.id??i} sx={{overflowWrap:'anywhere'}}>{p.anzahl} × {p.bezeichnung} · {p.anlass==='VERLUST'?'Verlust':'Beschädigung'} · {betragLabel(p.einzelpreis)} = {betragLabel(p.gesamtbetrag??0)}</Typography>)}
          <Typography fontWeight={600}>Entschädigung gesamt: {betragLabel(row.entschaedigungSoll)}</Typography>
          <Typography variant="body2">Für Zahlung und Kautionsverrechnung im Soll der Verbrauchskosten enthalten.</Typography>
        </Box>}
        {row.bewegungen.length > 0 && <Box sx={{mt:2}}><Typography variant="subtitle2">Bewegungen</Typography>{row.bewegungen.map((m)=><Typography key={m.id} variant="body2" color="text.secondary">{datumLabel(m.datum)} · {kategorien.find(k=>k.key===m.kategorie)?.label} · {bewegungsLabel(m)} {betragLabel(m.betrag)}{m.bemerkung ? ` · ${m.bemerkung}` : ""}</Typography>)}</Box>}
        </AccordionDetails></Accordion>)}
      {sortiert.length===0 && <Alert severity="info">Es gibt noch keine Buchungen.</Alert>}
    </Stack>
    <Dialog open={!!dialog} onClose={()=>!saving&&setDialog(null)} fullWidth maxWidth="sm">
      <DialogTitle>{art==="EINGANG"?"Zahlungseingang erfassen":art==="RUECKZAHLUNG"?"Rückzahlung erfassen":"Kaution mit Verbrauchskosten verrechnen"}</DialogTitle>
      <DialogContent><Stack spacing={2} sx={{pt:1}}>
        <Typography>{dialog?.row.buchungsnummer} · {dialog?.row.mieter} · {kategorien.find(k=>k.key===dialog?.category)?.label}</Typography>
        <TextField
          label="Betrag (€)"
          type="number"
          value={betrag}
          onChange={(e)=>setBetrag(e.target.value)}
          inputProps={{min:0.01,step:"0.01"}}
          helperText={art === "VERRECHNUNG" && dialog?.row.verbrauchVorschauVollstaendig
            ? `Vorschlag: ${betragLabel(Math.min(Math.max(0, paidFor(dialog.row, "KAUTION")), Math.max(0, offeneSumme(dialog.row, "VERBRAUCHSKOSTEN"))))} (offene Verbrauchskosten, höchstens Kautionssaldo)`
            : undefined}
          required
        />
        {art === "VERRECHNUNG" && dialog && !dialog.row.verbrauchVorschauVollstaendig && (
          <Alert severity="warning">Die Verbrauchskosten-Vorschau ist noch unvollständig. Bitte prüfe die Verbrauchskosten, bevor du einen Verrechnungsbetrag einträgst.</Alert>
        )}
        <TextField label="Datum" type="date" value={datum} onChange={(e)=>setDatum(e.target.value)} InputLabelProps={{shrink:true}} required />
        <TextField label="Bemerkung" value={bemerkung} onChange={(e)=>setBemerkung(e.target.value)} multiline minRows={2} />
        {saveError && <Alert severity="error">{saveError}</Alert>}
      </Stack></DialogContent>
      <DialogActions><Button onClick={()=>setDialog(null)} disabled={saving}>Abbrechen</Button><Button onClick={()=>void save()} disabled={saving || Number(betrag)<=0 || !datum} variant="contained">Speichern</Button></DialogActions>
    </Dialog>
    <ErrorDialog open={!!error} message={error ?? ""} onClose={()=>setError(null)} />
  </Box>;
};
export default AbrechnungPage;
