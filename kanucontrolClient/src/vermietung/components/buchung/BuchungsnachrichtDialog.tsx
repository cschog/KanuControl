import { useEffect, useRef, useState } from "react";
import { Alert, Box, Button, Checkbox, Dialog, DialogActions, DialogContent, DialogTitle, FormControlLabel, MenuItem, Stack, TextField, Typography } from "@mui/material";
import DokumentPdfVorschau from "./DokumentPdfVorschau";
import MieterStammdatenLink from "../mieter/MieterStammdatenLink";
import apiClient from "@/core/api/client/apiClient";
import { getBuchungFinanzenById, verrechneAbschlussKaution } from "@/vermietung/api/finanzenApi";
import type { BuchungFinanzen, Finanzkategorie } from "@/vermietung/types/Finanzen";
import type { AblesungBestaetigung } from "@/vermietung/types/VerbrauchskostenVorschau";
import { getBuchung } from "@/vermietung/api/buchungApi";
import { getApiErrorMessage } from "@/kjfp/api/utils/apiError";
import type { Buchung } from "@/vermietung/types/Buchung";
import type { Buchungsstatus } from "@/vermietung/enums/Buchungsstatus";
const nachrichtNamen: Record<string,string> = { RESERVIERT: "Reservierung", BESTAETIGT: "Bestätigung", ABGELEHNT: "Ablehnung", STORNIERT: "Stornierung", ABGESCHLOSSEN: "Schlussabrechnung" };
const statusNamen: Record<string,string> = { ANFRAGE: "Anfrage", RESERVIERT: "Reserviert", BESTAETIGT: "Bestätigt", ABGELEHNT: "Abgelehnt", STORNIERT: "Storniert", ABRECHENBAR: "Abrechenbar", ABGESCHLOSSEN: "Abgeschlossen" };
interface Entwurf {
 id:string;version:number;quelle:Buchungsstatus;ziel:Buchungsstatus;vorgang:string;ohneMail:boolean;
 betreff:string;mailtext:string;routing:{absender:string;empfaenger:string;cc:string|null;replyTo:string|null;betreff:string;testmodus:boolean}|null;
 pdf:string|null;dateiname:string;externAm:string|null;pdfHash:string|null;fingerprint:string;ausgefuehrt:boolean;
 versuchId:string|null;ergebnis:string|null;fehler:string|null;
 storno:{datum:string;volleErstattung:boolean;gezahlt:number;bereitsErstattet:number;erstattung:number;einbehalt:number}|null;
}
const euro=(v:number)=>v.toLocaleString("de-DE",{style:"currency",currency:"EUR"});
const cent=(v:number)=>Math.round((v+Number.EPSILON)*100)/100;
function kautionssaldo(f:BuchungFinanzen):number {
 const zahlungen=f.bewegungen.filter(m=>m.kategorie==="KAUTION").reduce((s,m)=>s+(m.art==="EINGANG"?m.betrag:-m.betrag),0);
 const miete=f.bewegungen.filter(m=>m.kategorie==="MIETE"||m.kategorie==="ANZAHLUNG");
 const eingang=miete.filter(m=>m.art==="EINGANG").reduce((s,m)=>s+m.betrag,0);
 const rueck=miete.filter(m=>m.art==="RUECKZAHLUNG").reduce((s,m)=>s+m.betrag,0);
 return cent(Math.max(0,zahlungen-Math.max(0,rueck-Math.max(0,eingang-f.mieteSoll))));
}
function eingang(f:BuchungFinanzen,k:Finanzkategorie):number{return f.bewegungen.filter(m=>m.kategorie===k).reduce((s,m)=>s+(m.art==="RUECKZAHLUNG"?-m.betrag:m.betrag),0);}
export default function BuchungsnachrichtDialog({buchung,ziel,entwurfId,onClose,onApplied}:{buchung:Buchung;ziel?:Buchungsstatus;entwurfId?:string;onClose:()=>void;onApplied:()=>Promise<void>}){
 const [draft,setDraft]=useState<Entwurf|null>(null),[betreff,setBetreff]=useState(""),[text,setText]=useState("");
 const [busy,setBusy]=useState(false),[error,setError]=useState(""),[reviewed,setReviewed]=useState(false),[retry,setRetry]=useState(false),[confirmReadings,setConfirmReadings]=useState(false);
 const [sendEmail,setSendEmail]=useState(false);
 const lock=useRef(false);const base=`/vermietung/buchungen/${buchung.id}/nachrichten`;
 const kostenlos=buchung.zahlungsvariante==="OHNE_ABRECHNUNG";
 const dokumentlos=kostenlos||buchung.buchungsquelle==="AIRBNB";
 const zielstatus=draft?.ziel??ziel;const completion=zielstatus==="ABGESCHLOSSEN";
 const [pruefbuchung,setPruefbuchung]=useState<Buchung|null>(null),[pruefungRevision,setPruefungRevision]=useState(0),[readingsLoading,setReadingsLoading]=useState(false);
 const [finanzen,setFinanzen]=useState<BuchungFinanzen|null>(null),[verrechnungHinweis,setVerrechnungHinweis]=useState("");
 const [verrechnungsdatum,setVerrechnungsdatum]=useState(()=>new Intl.DateTimeFormat("sv-SE",{timeZone:"Europe/Berlin"}).format(new Date()));
 const kaution=finanzen?kautionssaldo(finanzen):0;
 const verbrauch=(pruefbuchung?.verbrauchskostenVorschau??[]).reduce((s,v)=>s+(v.kosten??0),0);
 const offen=cent(Math.max(0,verbrauch-(finanzen?eingang(finanzen,"VERBRAUCHSKOSTEN"):0)));
 const verrechenbar=cent(Math.min(kaution,offen));
 const kautionVerrechnet=!!finanzen?.bewegungen.some(m=>m.kategorie==="KAUTION"&&m.art==="VERRECHNUNG"&&m.betrag>0);
 const kautionAbschlussBereit=kaution<=0 || kautionVerrechnet&&offen<=0;
 const readings=(pruefbuchung?.verbrauchskostenVorschau??[]).filter(v=>v.bestaetigungErforderlich);
 const incompleteReadings=(pruefbuchung?.verbrauchskostenVorschau??[]).filter(v=>!v.erfolgreich&&!v.bestaetigungErforderlich);
 const dirty=!!draft&&(betreff!==draft.betreff||text!==draft.mailtext);
 const applied=useRef(onApplied);applied.current=onApplied;
 function accept(d:Entwurf){setDraft(d);setBetreff(d.betreff);setText(d.mailtext);setReviewed(false);setRetry(false);}
 async function run(fn:()=>Promise<void>){if(lock.current)return;lock.current=true;setBusy(true);setError("");try{await fn();}catch(e){setError(getApiErrorMessage(e));}finally{lock.current=false;setBusy(false);}}
 useEffect(()=>{if(!entwurfId)return;let active=true;apiClient.get<Entwurf>(`${base}/${entwurfId}`).then(r=>{if(active)accept(r.data);}).catch(e=>{if(active)setError(getApiErrorMessage(e));});return()=>{active=false;};},[base,entwurfId]);
 useEffect(()=>{
  if(!completion||entwurfId)return;
  let active=true;setPruefbuchung(null);setFinanzen(null);setConfirmReadings(false);setReadingsLoading(true);setError("");
  Promise.all([getBuchung(buchung.id),getBuchungFinanzenById(buchung.id)]).then(([b,f])=>{if(active){setPruefbuchung(b);setFinanzen(f);}}).catch(e=>{if(active)setError(getApiErrorMessage(e));}).finally(()=>{if(active)setReadingsLoading(false);});
  return()=>{active=false;};
 },[buchung.id,completion,entwurfId,pruefungRevision]);
 function bestaetigteAblesungen():AblesungBestaetigung[]{
  return confirmReadings?readings.map(v=>({zaehlerId:v.zaehlerId!,anreise:pruefbuchung!.anreise,abreise:pruefbuchung!.abreise,anfangsablesung:v.anfangsablesung!,endablesung:v.endablesung!,anfangsstand:v.anfangsstand!,endstand:v.endstand!})):[];
 }
 const abschlussBereit=!!pruefbuchung&&!!finanzen&&!readingsLoading&&incompleteReadings.length===0&&(readings.length===0||confirmReadings);
 async function createDraft(){accept((await apiClient.post<Entwurf>(base,{ziel:zielstatus,ablesungen:bestaetigteAblesungen(),sendEmail:completion||zielstatus==="BESTAETIGT"?sendEmail:undefined})).data);}
 async function erstellen(){
  if(completion&&!abschlussBereit)return;
  await run(createDraft);
 }
 async function verrechnen(){
  if(!abschlussBereit||verrechenbar<=0||!verrechnungsdatum)return;
  await run(async()=>{
   const f=await verrechneAbschlussKaution(buchung.id,{betrag:verrechenbar,datum:verrechnungsdatum,ablesungen:bestaetigteAblesungen()});
   setFinanzen(f);setVerrechnungHinweis(`${euro(verrechenbar)} Kaution wurden verbindlich mit Verbrauchskosten verrechnet. Diese Verrechnung bleibt auch beim Abbrechen des Dialogs gespeichert.`);
   await applied.current();
   if(kautionssaldo(f)<=0 || cent(Math.max(0,verbrauch-eingang(f,"VERBRAUCHSKOSTEN")))<=0)await createDraft();
  });
 }
 function download(){if(!draft?.pdf)return;const u=URL.createObjectURL(new Blob([Uint8Array.from(atob(draft.pdf),c=>c.charCodeAt(0))],{type:"application/pdf"}));const a=document.createElement("a");a.href=u;a.download=draft.dateiname;a.click();setTimeout(()=>URL.revokeObjectURL(u),1000);}
 const succeeded=!!draft?.ausgefuehrt&&(draft.ohneMail||draft.ergebnis==="SMTP_BESTAETIGT");
 const action=draft?.ohneMail?(completion?kostenlos?"Ohne Rechnung abschließen":"Ohne Mail abschließen":"Bestätigen ohne Mail"):zielstatus==="RESERVIERT"?"Reservieren und senden":zielstatus==="BESTAETIGT"?"Bestätigen und senden":zielstatus==="ABGELEHNT"?"Ablehnen und senden":zielstatus==="STORNIERT"?"Stornieren und senden":"Abschließen und senden";
 return <Dialog open fullWidth maxWidth="lg" slotProps={{paper:{sx:{maxHeight:"calc(100dvh - 32px)",m:2}}}} onClose={()=>{if(!busy)onClose();}}>
  <DialogTitle>{kostenlos&&completion?"Verbrauchswerte und Abschluss":nachrichtNamen[zielstatus??""]??"Buchungsnachricht"} vor Ausführung prüfen</DialogTitle>
  <DialogContent dividers sx={{minHeight:0,overflowY:"auto"}}><Stack spacing={2}>
   <Typography>Buchung {buchung.buchungsnummer} · {statusNamen[draft?.quelle??buchung.status]} → {statusNamen[zielstatus??""]}. Vorschau und Bearbeitung ändern keine Buchungs- oder Finanzdaten.</Typography>
   {buchung.buchungsquelle==="AIRBNB"&&<Alert severity="info">Für Airbnb-Buchungen werden keine PDFs erstellt und keine E-Mails versendet.</Alert>}
   {kostenlos&&pruefbuchung&&<Stack spacing={0.5}>
    <Typography variant="subtitle1">Verbrauchswerte für spätere Auswertungen</Typography>
    {(pruefbuchung.verbrauchskostenVorschau??[]).map((v,i)=><Typography key={i} variant="body2">
      {v.bezeichnung}{v.zaehlerBezeichnung?` · ${v.zaehlerBezeichnung}`:""}: {v.verbrauch!=null?`${v.verbrauch.toLocaleString("de-DE")} ${v.mengeneinheit??""}`:v.abrechnungstage!=null?`${v.abrechnungstage} Abrechnungseinheiten`:"Keine Messmenge"}
    </Typography>)}
   </Stack>}
   {!draft&&<>
    {zielstatus==="BESTAETIGT"&&!dokumentlos&&<TextField select label="E-Mail bei Bestätigung" value={sendEmail?"MIT_MAIL":"OHNE_MAIL"} disabled={busy} onChange={e=>setSendEmail(e.target.value==="MIT_MAIL")} helperText={sendEmail?"Die Bestätigung mit PDF wird nach deiner Prüfung an den Mieter gesendet. Bankdaten sind optional.":"Die Buchung wird ohne E-Mail bestätigt. Bankdaten sind dafür nicht erforderlich."}><MenuItem value="OHNE_MAIL">Ohne E-Mail bestätigen</MenuItem><MenuItem value="MIT_MAIL">Mit Bestätigungsmail</MenuItem></TextField>}
    {completion&&<>{!dokumentlos&&<TextField select label="Mailversand beim Abschluss" value={buchung.buchungsquelle!=="AIRBNB"&&sendEmail?"MIT_MAIL":"OHNE_MAIL"} disabled={busy || buchung.buchungsquelle==="AIRBNB"} onChange={e=>setSendEmail(e.target.value==="MIT_MAIL")} helperText={buchung.buchungsquelle==="AIRBNB"?"Abschluss ohne PDF und Mailversand.":"Die letzte Rechnung wird gespeichert. Ohne Mailversand ist keine E-Mail-Adresse des Mieters erforderlich."}><MenuItem value="MIT_MAIL">Mit Mailversand abschließen</MenuItem><MenuItem value="OHNE_MAIL">Ohne Mailversand abschließen</MenuItem></TextField>}<Alert severity="info">{kostenlos?"Die Verbrauchswerte werden für spätere Auswertungen gespeichert. Es entstehen keine Kosten, keine Rechnung und keine E-Mail.":"Vor dem Abschluss werden die Verbrauchswerte geprüft. Die aktuelle Abschlussrechnung ersetzt die vorherige PDF-Fassung."}</Alert>
     {readingsLoading&&<Typography role="status">Aktuelle Verbrauchswerte werden geladen …</Typography>}
     {!readingsLoading&&!pruefbuchung&&<Alert severity="warning">Die aktuellen Verbrauchswerte konnten nicht geladen werden. Bitte erneut laden, bevor die Vorschau erstellt wird.</Alert>}
     {incompleteReadings.length>0&&<Alert severity="error">Verbrauchswerte sind unvollständig: {incompleteReadings.map(v=>`${v.bezeichnung}: ${v.hinweis??"Bitte Zählerstände prüfen."}`).join("; ")}</Alert>}
     {readings.length>0&&<Alert severity="warning"><Stack spacing={1.5}>
      <Typography>Diese Ablesungen liegen mehr als 24 Stunden vom Buchungstermin entfernt. Der gemessene Verbrauch kann auch Zeiten außerhalb der Buchung enthalten.</Typography>
      {readings.map((v,i)=><Box key={`${v.zaehlerId}-${i}`}>
       <Typography fontWeight={600}>{v.bezeichnung}{v.zaehlerBezeichnung?` · ${v.zaehlerBezeichnung}`:""}</Typography>
       <Typography variant="body2">Anreise: {new Date(pruefbuchung!.anreise).toLocaleString("de-DE")} · Anfang: {v.anfangsstand} {v.mengeneinheit} am {v.anfangsablesung?new Date(v.anfangsablesung).toLocaleString("de-DE"):"–"}</Typography>
       <Typography variant="body2">Abreise: {new Date(pruefbuchung!.abreise).toLocaleString("de-DE")} · Ende: {v.endstand} {v.mengeneinheit} am {v.endablesung?new Date(v.endablesung).toLocaleString("de-DE"):"–"}</Typography>
       <Typography variant="body2">Verbrauch: {v.verbrauch} {v.mengeneinheit} · Kosten: {v.kosten?.toLocaleString("de-DE",{style:"currency",currency:"EUR"})??"–"}</Typography>
      </Box>)}
      <FormControlLabel label="Ich habe die aktuellen Verbrauchswerte geprüft und bestätige die Verwendung dieser Ablesungen." control={<Checkbox checked={confirmReadings} disabled={busy||readingsLoading} onChange={e=>setConfirmReadings(e.target.checked)}/>}/>
     </Stack></Alert>}
     {finanzen&&kaution>0&&<Box sx={{p:2,border:1,borderColor:"divider",borderRadius:2}}><Stack spacing={1.5}>
      <Typography variant="h6">Kaution mit Verbrauchskosten verrechnen</Typography>
      <Typography>Verfügbare Kaution: {euro(kaution)} · Offene Verbrauchskosten: {euro(offen)}</Typography>
      {verrechenbar>0?<>
       <Typography>Verrechnen: {euro(verrechenbar)} · Danach noch zurückzuzahlen: {euro(Math.max(0,kaution-verrechenbar))}</Typography>
       <TextField label="Verrechnungsdatum" type="date" value={verrechnungsdatum} disabled={busy} onChange={e=>setVerrechnungsdatum(e.target.value)} slotProps={{inputLabel:{shrink:true}}}/>
       <Typography variant="body2">Der folgende Button bucht die Verrechnung verbindlich. Sie bleibt beim Abbrechen gespeichert. Anschließend werden die Abschlussrechnung und bei gewähltem Mailversand die Abschlussmail zur Prüfung geöffnet. Ein verbleibender Kautionsbetrag wird als Erstattung ausgewiesen. Es wird noch keine E-Mail versandt.</Typography>
       <Button variant="contained" disabled={busy||!abschlussBereit||!verrechnungsdatum} onClick={()=>void verrechnen()}>{sendEmail?"Kaution verrechnen und Abschlussmail öffnen":"Kaution verrechnen und Abschlussrechnung öffnen"}</Button>
      </>:<Alert severity="info">Keine offenen Verbrauchskosten zum Verrechnen vorhanden.</Alert>}
      {kaution>verrechenbar&&!kautionAbschlussBereit&&<Alert severity="warning">Den verbleibenden Kautionsbetrag vor dem Abschluss tatsächlich zurückzahlen und die ausgeführte Rückzahlung unter Abrechnung erfassen.</Alert>}
     </Stack></Box>}
     <Button disabled={busy||readingsLoading} onClick={()=>{setPruefbuchung(null);setConfirmReadings(false);setPruefungRevision(v=>v+1);}}>Verbrauchswerte erneut laden</Button>
    </>}
    <Button variant="outlined" disabled={busy||(completion&&(!abschlussBereit||!kautionAbschlussBereit))} onClick={()=>void erstellen()}>{completion?dokumentlos?"Verbrauchswerte prüfen und Abschluss vorbereiten":sendEmail?"Abschlussmail und PDF öffnen":"Abschlussrechnung öffnen":zielstatus==="BESTAETIGT"&&!sendEmail?"Bestätigung ohne E-Mail prüfen":"Nachrichtenvorschau erstellen"}</Button>
   </>}
   {draft&&<>
    {draft.ohneMail?<Alert severity="info">{completion?dokumentlos?"Abschluss ohne Rechnung und Mailversand. Die Verbrauchswerte werden gespeichert.":"Abschluss ohne Mailversand: Die letzte Rechnung wird gespeichert.":"Statusänderung ohne Mailversand. Bankdaten sind für diese Bestätigung nicht erforderlich."}</Alert>:<>
     {draft.routing?.testmodus&&<Alert severity="warning">Testumleitung aktiv. Angezeigt werden die tatsächlichen Versandempfänger.</Alert>}
     <Typography>Von: {draft.routing?.absender} · An: {draft.routing?.empfaenger}{draft.routing?.cc?` · Kopie: ${draft.routing.cc}`:""}{draft.routing?.replyTo?` · Antwort an: ${draft.routing.replyTo}`:""}</Typography>
     {draft.routing?.testmodus && <Typography>Versandbetreff: {draft.routing.betreff}</Typography>}
     <TextField label="Betreff" value={betreff} disabled={busy||draft.ausgefuehrt} onChange={e=>{setBetreff(e.target.value);setReviewed(false);}} slotProps={{htmlInput:{maxLength:240}}}/>
     <TextField label="Mailtext für diesen Versand" value={text} multiline minRows={5} disabled={busy||draft.ausgefuehrt} onChange={e=>{setText(e.target.value);setReviewed(false);}}/>
     {!draft.ausgefuehrt&&<Button disabled={busy||!dirty} onClick={()=>void run(async()=>accept((await apiClient.put<Entwurf>(`${base}/${draft.id}`,{version:draft.version,betreff,mailtext:text,originalWiederherstellen:false})).data))}>Individuelle Texte übernehmen und neu prüfen</Button>}
    </>}
    {draft.pdf&&<>
     {draft.storno&&<Alert severity="info">Stornodatum {draft.storno.datum}: {draft.storno.volleErstattung?"mindestens 56 Tage – vollständige Erstattung":"weniger als 56 Tage – keine Erstattung"}. Noch zu erstatten: {draft.storno.erstattung.toLocaleString("de-DE",{style:"currency",currency:"EUR"})}. Die Ankündigung bucht keine Überweisung.</Alert>}
     <Typography variant="subtitle1">{draft.externAm?"Extern bearbeiteter PDF-Anhang":"Automatisch erzeugter PDF-Anhang"} · {draft.dateiname}</Typography>
     <Stack direction="row" useFlexGap flexWrap="wrap" gap={1}>
      <Button disabled={busy||!draft.pdf} onClick={download}>PDF zur Bearbeitung herunterladen</Button>
      {!draft.ausgefuehrt&&<><Button component="label" disabled={busy||dirty}>Bearbeitetes PDF übernehmen<input hidden type="file" accept="application/pdf,.pdf" onChange={e=>{const file=e.target.files?.[0];e.target.value="";if(!file)return;void run(async()=>{if(file.size>10000000)throw new Error("PDF darf höchstens 10 MB groß sein.");const body=new FormData();body.append("version",String(draft.version));body.append("datei",file);accept((await apiClient.post<Entwurf>(`${base}/${draft.id}/pdf`,body)).data);});}}/></Button>
      <Button disabled={busy||dirty||!draft.externAm} onClick={()=>void run(async()=>accept((await apiClient.put<Entwurf>(`${base}/${draft.id}`,{version:draft.version,betreff,mailtext:text,originalWiederherstellen:true})).data))}>Automatisch erzeugtes PDF wiederherstellen</Button></>}
     </Stack>
     <Typography variant="body2">In PDF Expert oder Acrobat bearbeiten, speichern und hier wieder hochladen. Änderungen am PDF ändern keine Finanzdaten.</Typography>
     {draft.pdf&&<DokumentPdfVorschau pdf={draft.pdf} dateiname={draft.dateiname} title="Finaler PDF-Anhang" height={550}/>}
    </>}
    {draft.ausgefuehrt&&<Alert severity={succeeded?"success":"warning"}>{succeeded?draft.ohneMail?(completion?dokumentlos?"Buchung ohne Rechnung und Mailversand abgeschlossen. Die Verbrauchswerte sind gespeichert.":"Buchung abgeschlossen und letzte Rechnung gespeichert. Die Abschlussmail kann später über „Abschlussmail senden“ vorbereitet werden.":"Status ohne Mailversand geändert."):"Status gespeichert; SMTP hat den Versand bestätigt.":draft.fehler??"Status gespeichert. Der Versand ist noch nicht bestätigt. Vor einer Wiederholung Postausgang prüfen."}</Alert>}
   </>}
  </Stack></DialogContent>
  <DialogActions sx={{flexDirection:"column",alignItems:"stretch",gap:1,px:3,pb:2,flexShrink:0}}>
   {verrechnungHinweis&&<Alert severity="success">{verrechnungHinweis}</Alert>}
   {error&&<Alert severity="error" action={<MieterStammdatenLink personId={buchung.mieterId} buchungId={buchung.id} fehler={error} />}>{error}</Alert>}
   {draft&&!succeeded&&<FormControlLabel label={draft.ohneMail?(completion?kostenlos?"Verbrauchswerte und Abschluss ohne Rechnung geprüft":"Rechnung und Abschluss ohne Mailversand geprüft":"Statusänderung ohne Versand geprüft"):"Empfänger, Betreff, Mailtext und vollständigen PDF-Anhang geprüft"} control={<Checkbox checked={reviewed} disabled={busy||dirty} onChange={e=>setReviewed(e.target.checked)}/>}/>}
   {draft?.ausgefuehrt&&!succeeded&&!draft.ohneMail&&<FormControlLabel label="Postausgang geprüft. Ich möchte diesen gespeicherten Versand ausdrücklich wiederholen." control={<Checkbox checked={retry} onChange={e=>setRetry(e.target.checked)}/>}/>}
   {dirty&&<Typography color="warning.main">Textänderungen zuerst übernehmen; danach die aktuelle Nachricht erneut prüfen.</Typography>}
   <Stack direction="row" useFlexGap flexWrap="wrap" justifyContent="flex-end" gap={1}>
    <Button disabled={busy} onClick={onClose}>{draft?.ausgefuehrt?"Schließen":"Abbrechen"}</Button>
    {draft&&!succeeded&&<Button variant="contained" disabled={busy||dirty||!reviewed||(draft.ausgefuehrt&&!retry)} onClick={()=>void run(async()=>{const next=(await apiClient.post<Entwurf>(`${base}/${draft.id}/ausfuehren`,{fingerprint:draft.fingerprint,wiederholung:retry})).data;accept(next);await applied.current();})}>{draft.ausgefuehrt?"Gespeicherte Nachricht erneut senden":action}</Button>}
   </Stack>
  </DialogActions>
 </Dialog>;
}
