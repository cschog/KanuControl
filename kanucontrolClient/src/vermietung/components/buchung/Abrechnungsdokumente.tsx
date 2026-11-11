import { useEffect, useState } from "react";
import { Alert, Button, Stack, Typography } from "@mui/material";
import apiClient from "@/core/api/client/apiClient";
import { getApiErrorMessage } from "@/kjfp/api/utils/apiError";
interface Dokument {id:number;revision:number;erstellt:string;korrekturgrund:string;}
export default function Abrechnungsdokumente({id}:{id:number;ausstellbar:boolean}) {
 const [docs,setDocs]=useState<Dokument[]>([]),[error,setError]=useState("");const url=`/vermietung/buchungen/${id}/abrechnungen`;
 useEffect(()=>{let active=true;apiClient.get<Dokument[]>(url).then(r=>{if(active)setDocs(r.data);}).catch(e=>{if(active)setError(getApiErrorMessage(e));});return()=>{active=false;};},[url]);
 async function download(d:Dokument){try{const r=await apiClient.get(`${url}/${d.id}/pdf`,{responseType:"blob"});const href=URL.createObjectURL(r.data);const a=document.createElement("a");a.href=href;a.download=`Abrechnung-${id}-${d.revision}.pdf`;a.click();setTimeout(()=>URL.revokeObjectURL(href),1000);}catch(e){setError(getApiErrorMessage(e));}}
 return <Stack spacing={1} sx={{mt:3}}><Typography variant="h6">Abrechnungsdokumente</Typography><Typography variant="body2">E-Mail und PDF lassen sich über „Vorschau“ bereits vor dem Abschluss prüfen und exportieren/importieren. Neue Rechnungen entstehen über „Abschluss und Nachricht prüfen“. Für Korrekturen die Buchung wieder öffnen, Daten prüfen und mit Begründung erneut abschließen. Bestehende PDFs bleiben unverändert.</Typography>{error&&<Alert severity="error">{error}</Alert>}{docs.map(d=><Stack key={d.id} direction={{xs:"column",sm:"row"}} spacing={1}><Typography>Revision {d.revision} · {new Date(d.erstellt).toLocaleString("de-DE")}{d.korrekturgrund?` · ${d.korrekturgrund}`:""}</Typography><Button onClick={()=>void download(d)}>PDF herunterladen</Button></Stack>)}</Stack>;
}
