import { useEffect, useRef, useState } from "react";
import { Alert, Box, Button, Checkbox, Dialog, DialogActions, DialogContent, DialogTitle, FormControlLabel, Stack, TextField, Typography } from "@mui/material";
import apiClient from "@/core/api/client/apiClient";
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
function VorschauPdf({pdf}:{pdf:string}){
 const [url,setUrl]=useState("");
 useEffect(()=>{const u=URL.createObjectURL(new Blob([Uint8Array.from(atob(pdf),c=>c.charCodeAt(0))],{type:"application/pdf"}));setUrl(u);return()=>URL.revokeObjectURL(u);},[pdf]);
 return <Box><Button component="a" href={url} target="_blank" rel="noopener noreferrer">PDF mit allen Seiten öffnen</Button><Box component="iframe" key={url} title="Finaler PDF-Anhang" src={url} sx={{width:"100%",height:550,border:1,borderColor:"divider"}}/></Box>;
}
export default function BuchungsnachrichtDialog({buchung,ziel,entwurfId,onClose,onApplied}:{buchung:Buchung;ziel?:Buchungsstatus;entwurfId?:string;onClose:()=>void;onApplied:()=>Promise<void>}){
 const [draft,setDraft]=useState<Entwurf|null>(null),[betreff,setBetreff]=useState(""),[text,setText]=useState("");
 const [busy,setBusy]=useState(false),[error,setError]=useState(""),[reviewed,setReviewed]=useState(false),[retry,setRetry]=useState(false),[confirmReadings,setConfirmReadings]=useState(false),[grund,setGrund]=useState("");
 const lock=useRef(false);const base=`/vermietung/buchungen/${buchung.id}/nachrichten`;
 const zielstatus=draft?.ziel??ziel;const completion=zielstatus==="ABGESCHLOSSEN";
 const [pruefbuchung,setPruefbuchung]=useState<Buchung|null>(null),[pruefungRevision,setPruefungRevision]=useState(0),[readingsLoading,setReadingsLoading]=useState(false);
 const readings=(pruefbuchung?.verbrauchskostenVorschau??[]).filter(v=>v.bestaetigungErforderlich);
 const incompleteReadings=(pruefbuchung?.verbrauchskostenVorschau??[]).filter(v=>!v.erfolgreich&&!v.bestaetigungErforderlich);
 const dirty=!!draft&&(betreff!==draft.betreff||text!==draft.mailtext);
 const applied=useRef(onApplied);applied.current=onApplied;
 function accept(d:Entwurf){setDraft(d);setBetreff(d.betreff);setText(d.mailtext);setReviewed(false);setRetry(false);}
 async function run(fn:()=>Promise<void>){if(lock.current)return;lock.current=true;setBusy(true);setError("");try{await fn();}catch(e){setError(getApiErrorMessage(e));}finally{lock.current=false;setBusy(false);}}
 useEffect(()=>{if(!entwurfId)return;let active=true;apiClient.get<Entwurf>(`${base}/${entwurfId}`).then(r=>{if(active)accept(r.data);}).catch(e=>{if(active)setError(getApiErrorMessage(e));});return()=>{active=false;};},[base,entwurfId]);
 useEffect(()=>{
  if(!completion||entwurfId)return;
  let active=true;setPruefbuchung(null);setConfirmReadings(false);setReadingsLoading(true);setError("");
  getBuchung(buchung.id).then(b=>{if(active)setPruefbuchung(b);}).catch(e=>{if(active)setError(getApiErrorMessage(e));}).finally(()=>{if(active)setReadingsLoading(false);});
  return()=>{active=false;};
 },[buchung.id,completion,entwurfId,pruefungRevision]);
 async function erstellen(){
  if(completion&&(!pruefbuchung||incompleteReadings.length>0||(readings.length>0&&!confirmReadings)))return;
  await run(async()=>{
  const ablesungen=confirmReadings?readings.map(v=>({zaehlerId:v.zaehlerId,anreise:pruefbuchung!.anreise,abreise:pruefbuchung!.abreise,anfangsablesung:v.anfangsablesung,endablesung:v.endablesung,anfangsstand:v.anfangsstand,endstand:v.endstand})):[];
  accept((await apiClient.post<Entwurf>(base,{ziel:zielstatus,ablesungen,korrekturgrund:grund})).data);
 });}
 function download(){if(!draft?.pdf)return;const u=URL.createObjectURL(new Blob([Uint8Array.from(atob(draft.pdf),c=>c.charCodeAt(0))],{type:"application/pdf"}));const a=document.createElement("a");a.href=u;a.download=draft.dateiname;a.click();setTimeout(()=>URL.revokeObjectURL(u),1000);}
 const succeeded=!!draft?.ausgefuehrt&&(draft.ohneMail||draft.ergebnis==="SMTP_BESTAETIGT");
 const action=draft?.ohneMail?"Bestätigen ohne Mail":zielstatus==="RESERVIERT"?"Reservieren und senden":zielstatus==="BESTAETIGT"?"Bestätigen und senden":zielstatus==="ABGELEHNT"?"Ablehnen und senden":zielstatus==="STORNIERT"?"Stornieren und senden":"Abschließen und senden";
 return <Dialog open fullWidth maxWidth="lg" slotProps={{paper:{sx:{maxHeight:"calc(100dvh - 32px)",m:2}}}} onClose={()=>{if(!busy)onClose();}}>
  <DialogTitle>{nachrichtNamen[zielstatus??""]??"Buchungsnachricht"} vor Ausführung prüfen</DialogTitle>
  <DialogContent dividers sx={{minHeight:0,overflowY:"auto"}}><Stack spacing={2}>
   <Typography>Buchung {buchung.buchungsnummer} · {statusNamen[draft?.quelle??buchung.status]} → {statusNamen[zielstatus??""]}. Vorschau und Bearbeitung ändern keine Buchungs- oder Finanzdaten.</Typography>
   {!draft&&<>
    {completion&&<><Alert severity="info">Vor dem Abschluss werden Verbrauch und Kautionsabwicklung geprüft. Eine vorhandene Rechnung wird nur als neue Korrekturrevision ergänzt.</Alert><TextField label="Korrekturgrund (bei bereits ausgestellter Rechnung erforderlich)" value={grund} onChange={e=>setGrund(e.target.value)} slotProps={{htmlInput:{maxLength:1000}}}/>
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
     <Button disabled={busy||readingsLoading} onClick={()=>{setPruefbuchung(null);setConfirmReadings(false);setPruefungRevision(v=>v+1);}}>Verbrauchswerte erneut laden</Button>
    </>}
    <Button variant="outlined" disabled={busy||(completion&&(!pruefbuchung||readingsLoading||incompleteReadings.length>0||(readings.length>0&&!confirmReadings)))} onClick={()=>void erstellen()}>Nachrichtenvorschau erstellen</Button>
   </>}
   {draft&&<>
    {draft.ohneMail?<Alert severity="info">Statusänderung ohne Mailversand. Die geforderte Anzahlung beziehungsweise Vorauszahlung wird geprüft; die spätere Restmiete löst keine zusätzliche Nachricht aus.</Alert>:<>
     {draft.routing?.testmodus&&<Alert severity="warning">Testumleitung aktiv. Angezeigt werden die tatsächlichen Versandempfänger.</Alert>}
     <Typography>Von: {draft.routing?.absender} · An: {draft.routing?.empfaenger}{draft.routing?.cc?` · Kopie: ${draft.routing.cc}`:""}{draft.routing?.replyTo?` · Antwort an: ${draft.routing.replyTo}`:""}</Typography>
     {draft.routing?.testmodus && <Typography>Versandbetreff: {draft.routing.betreff}</Typography>}
     <TextField label="Betreff" value={betreff} disabled={busy||draft.ausgefuehrt} onChange={e=>{setBetreff(e.target.value);setReviewed(false);}} slotProps={{htmlInput:{maxLength:240}}}/>
     <TextField label="Mailtext für diesen Versand" value={text} multiline minRows={5} disabled={busy||draft.ausgefuehrt} onChange={e=>{setText(e.target.value);setReviewed(false);}}/>
     {!draft.ausgefuehrt&&<Button disabled={busy||!dirty} onClick={()=>void run(async()=>accept((await apiClient.put<Entwurf>(`${base}/${draft.id}`,{version:draft.version,betreff,mailtext:text,originalWiederherstellen:false})).data))}>Individuelle Texte übernehmen und neu prüfen</Button>}
     {draft.storno&&<Alert severity="info">Stornodatum {draft.storno.datum}: {draft.storno.volleErstattung?"mindestens 56 Tage – vollständige Erstattung":"weniger als 56 Tage – keine Erstattung"}. Noch zu erstatten: {draft.storno.erstattung.toLocaleString("de-DE",{style:"currency",currency:"EUR"})}. Die Ankündigung bucht keine Überweisung.</Alert>}
     <Typography variant="subtitle1">{draft.externAm?"Extern bearbeiteter PDF-Anhang":"Automatisch erzeugter PDF-Anhang"} · {draft.dateiname}</Typography>
     <Stack direction="row" useFlexGap flexWrap="wrap" gap={1}>
      <Button disabled={busy||!draft.pdf} onClick={download}>PDF zur Bearbeitung herunterladen</Button>
      {!draft.ausgefuehrt&&<><Button component="label" disabled={busy||dirty}>Bearbeitetes PDF übernehmen<input hidden type="file" accept="application/pdf,.pdf" onChange={e=>{const file=e.target.files?.[0];e.target.value="";if(!file)return;void run(async()=>{if(file.size>10000000)throw new Error("PDF darf höchstens 10 MB groß sein.");const body=new FormData();body.append("version",String(draft.version));body.append("datei",file);accept((await apiClient.post<Entwurf>(`${base}/${draft.id}/pdf`,body)).data);});}}/></Button>
      <Button disabled={busy||dirty||!draft.externAm} onClick={()=>void run(async()=>accept((await apiClient.put<Entwurf>(`${base}/${draft.id}`,{version:draft.version,betreff,mailtext:text,originalWiederherstellen:true})).data))}>Automatisch erzeugtes PDF wiederherstellen</Button></>}
     </Stack>
     <Typography variant="body2">In PDF Expert oder Acrobat bearbeiten, speichern und hier wieder hochladen. Änderungen am PDF ändern keine Finanzdaten.</Typography>
     {draft.pdf&&<VorschauPdf key={draft.pdfHash} pdf={draft.pdf}/>}
    </>}
    {draft.ausgefuehrt&&<Alert severity={succeeded?"success":"warning"}>{succeeded?draft.ohneMail?"Status ohne Mailversand geändert.":"Status gespeichert; SMTP hat den Versand bestätigt.":draft.fehler??"Status gespeichert. Der Versand ist noch nicht bestätigt. Vor einer Wiederholung Postausgang prüfen."}</Alert>}
   </>}
  </Stack></DialogContent>
  <DialogActions sx={{flexDirection:"column",alignItems:"stretch",gap:1,px:3,pb:2,flexShrink:0}}>
   {error&&<Alert severity="error">{error}</Alert>}
   {draft&&!succeeded&&<FormControlLabel label={draft.ohneMail?"Statusänderung ohne Versand geprüft":"Empfänger, Betreff, Mailtext und vollständigen PDF-Anhang geprüft"} control={<Checkbox checked={reviewed} disabled={busy||dirty} onChange={e=>setReviewed(e.target.checked)}/>}/>}
   {draft?.ausgefuehrt&&!succeeded&&!draft.ohneMail&&<FormControlLabel label="Postausgang geprüft. Ich möchte diesen gespeicherten Versand ausdrücklich wiederholen." control={<Checkbox checked={retry} onChange={e=>setRetry(e.target.checked)}/>}/>}
   {dirty&&<Typography color="warning.main">Textänderungen zuerst übernehmen; danach die aktuelle Nachricht erneut prüfen.</Typography>}
   <Stack direction="row" useFlexGap flexWrap="wrap" justifyContent="flex-end" gap={1}>
    <Button disabled={busy} onClick={onClose}>{draft?.ausgefuehrt?"Schließen":"Abbrechen"}</Button>
    {draft&&!succeeded&&<Button variant="contained" disabled={busy||dirty||!reviewed||(draft.ausgefuehrt&&!retry)} onClick={()=>void run(async()=>{const next=(await apiClient.post<Entwurf>(`${base}/${draft.id}/ausfuehren`,{fingerprint:draft.fingerprint,wiederholung:retry})).data;accept(next);await applied.current();})}>{draft.ausgefuehrt?"Gespeicherte Nachricht erneut senden":action}</Button>}
   </Stack>
  </DialogActions>
 </Dialog>;
}
