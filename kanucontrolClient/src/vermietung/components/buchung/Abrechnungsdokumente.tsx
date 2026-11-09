import { useEffect, useState } from "react";
import { Alert, Button, Stack, TextField, Typography } from "@mui/material";
import apiClient from "@/core/api/client/apiClient";
import { getApiErrorMessage } from "@/kjfp/api/utils/apiError";
interface Dokument {id:number;revision:number;erstellt:string;korrekturgrund:string;}
export default function Abrechnungsdokumente({id,ausstellbar}:{id:number;ausstellbar:boolean}) {
 const [docs,setDocs]=useState<Dokument[]>([]),[grund,setGrund]=useState(''),[error,setError]=useState<string|null>(null),[busy,setBusy]=useState(false),[loaded,setLoaded]=useState(false);
 const url=`/vermietung/buchungen/${id}/abrechnungen`;
 useEffect(()=>{let cancelled=false;setLoaded(false);setError(null);setDocs([]);apiClient.get<Dokument[]>(url).then(r=>{if(!cancelled){setDocs(r.data);setLoaded(true);}}).catch(e=>{if(!cancelled)setError(getApiErrorMessage(e));});return()=>{cancelled=true;};},[url]);
 async function create(){setBusy(true);setError(null);try{await apiClient.post(url,{ersetztDokumentId:docs[0]?.id??null,korrekturgrund:grund});setDocs((await apiClient.get<Dokument[]>(url)).data);setGrund('');}catch(e){setError(getApiErrorMessage(e));}finally{setBusy(false);}}
 async function download(d:Dokument){try{const r=await apiClient.get(`${url}/${d.id}/pdf`,{responseType:'blob'});const href=URL.createObjectURL(r.data);const a=document.createElement('a');a.href=href;a.download=`Abrechnung-${id}-${d.revision}.pdf`;a.click();setTimeout(()=>URL.revokeObjectURL(href),1000);}catch(e){setError(getApiErrorMessage(e));}}
 return <Stack spacing={1} sx={{mt:3}}>
 <Typography variant="h6">Abrechnungsdokumente</Typography>
 <Typography variant="body2">Ausgestellte PDFs bleiben erhalten. Nach Änderungen den aktuellen Buchungsstand erneut abschließen und ausdrücklich eine Korrekturabrechnung erstellen.</Typography>
 {!ausstellbar&&<Alert severity="info">Für ein neues Dokument muss der aktuelle Buchungsstand abgeschlossen sein.</Alert>}
 {error&&<Alert severity="error">{error}</Alert>}
 {docs.length>0&&<TextField label="Korrekturgrund (intern)" value={grund} inputProps={{maxLength:1000}} onChange={e=>setGrund(e.target.value)}/>}
 <Button disabled={!ausstellbar||!loaded||busy||(docs.length>0&&!grund.trim())} onClick={()=>void create()}>{docs.length?'Korrekturabrechnung erstellen':'Abrechnungs-PDF ausstellen'}</Button>
 {docs.map(d=><Stack key={d.id} spacing={1} direction={{xs:'column',sm:'row'}}><Typography>Revision {d.revision} · {new Date(d.erstellt).toLocaleString('de-DE')}{d.korrekturgrund?` · ${d.korrekturgrund}`:''}</Typography><Button onClick={()=>void download(d)}>PDF herunterladen</Button></Stack>)}
 </Stack>;
}
