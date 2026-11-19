import { useEffect, useState } from "react";
import { Alert, Button, Stack, Typography } from "@mui/material";
import apiClient from "@/core/api/client/apiClient";
import { getApiErrorMessage } from "@/kjfp/api/utils/apiError";
interface Alt {id:number;nummer:string;von:string;bis:string}
export default function Altanfragen({onChanged}:{onChanged:()=>Promise<void>}) {
 const [rows,setRows]=useState<Alt[]>([]),[error,setError]=useState(""),[busy,setBusy]=useState(false);
 const load=async()=>setRows((await apiClient.get<Alt[]>("/vermietung/altanfragen")).data);
 useEffect(()=>{void load().catch(e=>setError(getApiErrorMessage(e)));},[]);
 async function apply(id:number,ablehnen:boolean){setBusy(true);setError("");try{await apiClient.post(`/vermietung/altanfragen/${id}`,{ablehnen});await load();await onChanged();}catch(e){setError(getApiErrorMessage(e));}finally{setBusy(false);}}
 if(!rows.length&&!error)return null;
 return <Alert severity="warning" sx={{mb:2}}><Stack spacing={1}><Typography>Vorhandene Altanfragen müssen geprüft werden. Reservieren ist nur bei freier Verfügbarkeit möglich; Konflikte bleiben unverändert zur Bearbeitung stehen.</Typography>{error&&<Typography color="error">{error}</Typography>}{rows.map(row=><Stack key={row.id} direction={{xs:"column",sm:"row"}} spacing={1}><Typography>{row.nummer} · {row.von.slice(0,10)} – {row.bis.slice(0,10)}</Typography><Button disabled={busy} onClick={()=>void apply(row.id,false)}>Prüfen und reservieren</Button><Button disabled={busy} onClick={()=>void apply(row.id,true)}>Ablehnen ohne E-Mail</Button></Stack>)}</Stack></Alert>;
}
