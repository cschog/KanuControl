import { useEffect, useState } from "react";
import { Alert, Button, Checkbox, Dialog, DialogActions, DialogContent, DialogTitle, FormControlLabel, Stack, Typography } from "@mui/material";
import apiClient from "@/core/api/client/apiClient";
import { getApiErrorMessage } from "@/kjfp/api/utils/apiError";
import type { BuchungFinanzen } from "@/vermietung/types/Finanzen";
const geld=(v:number)=>v.toLocaleString("de-DE",{style:"currency",currency:"EUR"});
const kategorie:Record<string,string>={ANZAHLUNG:"Anzahlung",MIETE:"Miete",KAUTION:"Kaution",VERBRAUCHSKOSTEN:"Verbrauchskosten",ENTSCHAEDIGUNG:"Entschädigung"};
const art:Record<string,string>={EINGANG:"Eingang",RUECKZAHLUNG:"Rückzahlung",VERRECHNUNG:"Verrechnung"};
export default function ZahlungenAuswahl({row,onChanged}:{row:BuchungFinanzen;onChanged:()=>Promise<void>}){
 const [ids,setIds]=useState<number[]>([]),[confirm,setConfirm]=useState(false),[busy,setBusy]=useState(false),[error,setError]=useState("");
 useEffect(()=>{setIds([]);},[row]);
 const selected=row.bewegungen.filter(m=>ids.includes(m.id));
 async function remove(){if(busy)return;setBusy(true);setError("");try{await apiClient.post(`/vermietung/finanzen/${row.buchungId}/bewegungen/entfernen`,{ids});await onChanged();setIds([]);setConfirm(false);}catch(e){setError(getApiErrorMessage(e));}finally{setBusy(false);}}
 const label=(m:BuchungFinanzen["bewegungen"][number])=>`${new Date(m.datum).toLocaleDateString("de-DE")} · ${kategorie[m.kategorie]??m.kategorie} · ${art[m.art]} ${geld(m.betrag)}${m.bemerkung?` · ${m.bemerkung}`:""}`;
 return <Stack spacing={1} sx={{mt:2}}><Typography variant="subtitle2">Erfolgte Zahlungen</Typography>{error&&<Alert severity="error">{error}</Alert>}
 <FormControlLabel control={<Checkbox disabled={busy} checked={ids.length===row.bewegungen.length&&ids.length>0} indeterminate={ids.length>0&&ids.length<row.bewegungen.length} onChange={e=>setIds(e.target.checked?row.bewegungen.map(m=>m.id):[])}/>} label="Alle auswählen"/>
 {row.bewegungen.map(m=><FormControlLabel key={m.id} control={<Checkbox disabled={busy} checked={ids.includes(m.id)} onChange={e=>setIds(old=>e.target.checked?[...old,m.id]:old.filter(id=>id!==m.id))}/>} label={label(m)}/>)}
 <Button color="error" disabled={busy||ids.length===0} onClick={()=>setConfirm(true)}>Ausgewählte Zahlungen löschen ({ids.length})</Button>
 <Dialog open={confirm} onClose={()=>{if(!busy)setConfirm(false);}} fullWidth maxWidth="sm"><DialogTitle>Ausgewählte Zahlungen löschen?</DialogTitle><DialogContent><Stack spacing={1}>{selected.map(m=><Typography key={m.id}>{label(m)}</Typography>)}<Typography>Summe der ausgewählten Beträge: {geld(selected.reduce((s,m)=>s+m.betrag,0))}</Typography><Typography>Die Zahlungen werden aus der Aufstellung entfernt und die Beträge neu berechnet. Der Buchungsstatus bleibt erhalten.</Typography></Stack></DialogContent><DialogActions><Button disabled={busy} onClick={()=>setConfirm(false)}>Abbrechen</Button><Button color="error" disabled={busy} onClick={()=>void remove()}>{busy?"Wird gelöscht …":"Zahlungen löschen"}</Button></DialogActions></Dialog>
 </Stack>;
}
