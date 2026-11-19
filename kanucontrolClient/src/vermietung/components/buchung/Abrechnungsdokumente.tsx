import { useEffect, useState } from "react";
import { Alert, Button, Dialog, DialogActions, DialogContent, DialogTitle, Stack, Typography } from "@mui/material";
import apiClient from "@/core/api/client/apiClient";
import { getApiErrorMessage } from "@/kjfp/api/utils/apiError";
interface Dokument {id:number;revision:number;erstellt:string;korrekturgrund:string;}
export default function Abrechnungsdokumente({id,ausstellbar}:{id:number;ausstellbar:boolean}) {
 const [busy,setBusy]=useState(false),[confirm,setConfirm]=useState(false);
 const [docs,setDocs]=useState<Dokument[]>([]),[error,setError]=useState("");const url=`/vermietung/buchungen/${id}/abrechnungen`;
 useEffect(()=>{let active=true;apiClient.get<Dokument[]>(url).then(r=>{if(active)setDocs(r.data);}).catch(e=>{if(active)setError(getApiErrorMessage(e));});return()=>{active=false;};},[url]);
 async function erstellen(){if(busy)return;setBusy(true);setError("");try{const r=await apiClient.post<Dokument>(url,{ersetztDokumentId:docs[0]?.id??null,korrekturgrund:""});setDocs([r.data]);setConfirm(false);}catch(e){setError(getApiErrorMessage(e));}finally{setBusy(false);}}
 async function download(d:Dokument){try{const r=await apiClient.get(`${url}/${d.id}/pdf`,{responseType:"blob"});const href=URL.createObjectURL(r.data);const a=document.createElement("a");a.href=href;a.download=`Abrechnung-${id}.pdf`;a.click();setTimeout(()=>URL.revokeObjectURL(href),1000);}catch(e){setError(getApiErrorMessage(e));}}
 return <Stack spacing={1} sx={{mt:3}}><Typography variant="h6">Abrechnungsdokumente</Typography><Typography variant="body2">E-Mail und PDF lassen sich über „Vorschau“ bereits vor dem Abschluss prüfen und exportieren/importieren. Neue Rechnungen entstehen über „Abschluss und Nachricht prüfen“. Nach einer Zahlungskorrektur kann die Abrechnung ohne Wiederöffnen der Buchung neu erstellt werden. Es gilt immer die letzte PDF-Fassung. Frühere PDFs werden ersetzt.</Typography>{error&&<Alert severity="error">{error}</Alert>}{ausstellbar&&<Button disabled={busy} onClick={()=>setConfirm(true)}>Aktuelle Abrechnung neu erstellen</Button>}
 <Dialog open={confirm} onClose={()=>{if(!busy)setConfirm(false);}}><DialogTitle>Aktuelle Abrechnung neu erstellen?</DialogTitle><DialogContent>Die Abrechnung wird mit den aktuellen Zahlungen erstellt und ersetzt das bisherige Dokument. Der Buchungsstatus bleibt abgeschlossen.</DialogContent><DialogActions><Button disabled={busy} onClick={()=>setConfirm(false)}>Abbrechen</Button><Button disabled={busy} onClick={()=>void erstellen()}>Neu erstellen</Button></DialogActions></Dialog>
 {docs.slice(0,1).map(d=><Stack key={d.id} direction={{xs:"column",sm:"row"}} spacing={1}><Typography>Aktuelles Dokument · {new Date(d.erstellt).toLocaleString("de-DE")}</Typography><Button onClick={()=>void download(d)}>PDF herunterladen</Button></Stack>)}</Stack>;
}
