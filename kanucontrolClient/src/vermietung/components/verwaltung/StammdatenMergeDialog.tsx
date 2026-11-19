import { useEffect, useState } from "react";
import { Alert, Autocomplete, Button, Checkbox, Dialog, DialogActions, DialogContent, DialogTitle, FormControlLabel, MenuItem, Stack, Table, TableBody, TableCell, TableHead, TableRow, TextField, Typography } from "@mui/material";
import apiClient from "@/core/api/client/apiClient";
import { getApiErrorMessage } from "@/kjfp/api/utils/apiError";
type Datensatz = { id: number; name: string; vorname?: string } & Record<string, unknown>;
interface Vorschau { quelle: Datensatz; ziel: Datensatz; felder: string[]; buchungen: { id:number; nummer:string; status:string }[]; zielGeschuetzt:boolean; fingerprint:string }
const labels:Record<string,string>={vorname:"Vorname",name:"Name",email:"E-Mail",telefon:"Telefon",telefonFestnetz:"Festnetz",strasse:"Straße / Hausnummer",plz:"PLZ",ort:"Ort",countryCode:"Land",organisation:"Organisation",kontoinhaber:"Kontoinhaber",kontoinhaberId:"Kontoinhaber-ID",bankName:"Bankname",iban:"IBAN",bic:"BIC",abk:"Abkürzung",airbnb:"Airbnb-Mieter"};
const name=(v:Datensatz)=>[v.vorname,v.name].filter(Boolean).join(" ")+` (#${v.id})`;
const text=(v:unknown)=>v==null||v===""?"–":typeof v==="boolean"?v?"Ja":"Nein":String(v);
export default function StammdatenMergeDialog({art,id,onClose,onDone}:{art:"mieter"|"veranstalter";id:number;onClose:()=>void;onDone:()=>void}) {
 const [rows,setRows]=useState<Datensatz[]>([]),[other,setOther]=useState<Datensatz|null>(null),[ziel,setZiel]=useState(id),[preview,setPreview]=useState<Vorschau|null>(null);
 const [choices,setChoices]=useState<Record<string,boolean>>({}),[confirmed,setConfirmed]=useState(false),[busy,setBusy]=useState(false),[error,setError]=useState("");
 useEffect(()=>{let active=true;apiClient.get<Datensatz[]>(`/vermietung/${art}`).then(r=>{if(active)setRows(r.data);}).catch(e=>{if(active)setError(getApiErrorMessage(e));});return()=>{active=false;};},[art]);
 useEffect(()=>{setPreview(null);setConfirmed(false);setChoices({});if(!other)return;let active=true;
  const quelle=ziel===id?other.id:id;
  apiClient.get<Vorschau>(`/vermietung/zusammenfuehren/${art}`,{params:{quelle,ziel}}).then(r=>{if(active)setPreview(r.data);}).catch(e=>{if(active)setError(getApiErrorMessage(e));});return()=>{active=false;};
 },[art,id,other,ziel]);
 async function apply(){if(!preview||!confirmed||busy)return;setBusy(true);setError("");try{await apiClient.post(`/vermietung/zusammenfuehren/${art}`,{quelle:preview.quelle.id,ziel:preview.ziel.id,ausQuelle:choices,fingerprint:preview.fingerprint});onDone();}catch(e){setError(getApiErrorMessage(e));setConfirmed(false);}finally{setBusy(false);}}
 return <Dialog open fullWidth maxWidth="lg" onClose={()=>{if(!busy)onClose();}}><DialogTitle>{art==="mieter"?"Mieter":"Veranstalter"} zusammenführen</DialogTitle><DialogContent><Stack spacing={2}>
 {error&&<Alert severity="error">{error}</Alert>}
 <Autocomplete disabled={busy} options={rows.filter(r=>r.id!==id)} value={other} getOptionLabel={name} isOptionEqualToValue={(a,b)=>a.id===b.id} onChange={(_,v)=>{setOther(v);setZiel(id);setError("");}} renderInput={params=><TextField {...params} label="Zweiten Datensatz suchen"/>}/>
 {other&&<TextField select disabled={busy} label="Dieser Datensatz bleibt erhalten" value={ziel} onChange={e=>setZiel(Number(e.target.value))}>{[rows.find(r=>r.id===id),other].filter((r):r is Datensatz=>!!r).map(r=><MenuItem key={r.id} value={r.id}>{name(r)}</MenuItem>)}</TextField>}
 {preview&&<>
 {preview.zielGeschuetzt&&<Alert severity="info">Der Zieldatensatz wird auch in KJFP verwendet. Seine Stammdaten bleiben erhalten. KJFP-Zuordnungen werden durch die Zusammenführung nicht geändert.</Alert>}
 <Typography>Alle {preview.buchungen.length} Buchungen des Quelldatensatzes werden dem Ziel zugeordnet. Buchungsnummern und aktuelle gespeicherte PDFs bleiben erhalten.</Typography>
 <div style={{overflowX:"auto"}}><Table size="small"><TableHead><TableRow><TableCell>Feld</TableCell><TableCell>Quelle: {name(preview.quelle)}</TableCell><TableCell>Ziel: {name(preview.ziel)}</TableCell><TableCell>Übernehmen</TableCell></TableRow></TableHead><TableBody>{preview.felder.map(field=><TableRow key={field}><TableCell>{labels[field]??field}</TableCell><TableCell sx={{overflowWrap:"anywhere"}}>{text(preview.quelle[field])}</TableCell><TableCell sx={{overflowWrap:"anywhere"}}>{text(preview.ziel[field])}</TableCell><TableCell><TextField size="small" select disabled={busy||preview.zielGeschuetzt} value={choices[field]?"QUELLE":"ZIEL"} onChange={e=>{setChoices(old=>({...old,[field]:e.target.value==="QUELLE"}));setConfirmed(false);}}><MenuItem value="ZIEL">Ziel behalten</MenuItem><MenuItem value="QUELLE">Quelle übernehmen</MenuItem></TextField></TableCell></TableRow>)}</TableBody></Table></div>
 <Typography variant="subtitle2">Betroffene Buchungen</Typography>{preview.buchungen.map(b=><Typography key={b.id} variant="body2">{b.nummer} · {b.status}</Typography>)}
 <FormControlLabel control={<Checkbox disabled={busy} checked={confirmed} onChange={e=>setConfirmed(e.target.checked)}/>} label={`Ich bestätige die Zusammenführung in ${name(preview.ziel)}. Der Quelldatensatz wird in der Vermietung deaktiviert.`}/>
 </>}
 </Stack></DialogContent><DialogActions><Button disabled={busy} onClick={onClose}>Abbrechen</Button><Button variant="contained" disabled={busy||!preview||!confirmed} onClick={()=>void apply()}>{busy?"Wird zusammengeführt …":"Zusammenführen"}</Button></DialogActions></Dialog>;
}
