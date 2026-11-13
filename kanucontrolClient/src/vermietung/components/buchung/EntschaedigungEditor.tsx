import { useEffect, useState } from "react";
import { Alert, Button, MenuItem, Paper, Stack, TextField, Typography } from "@mui/material";
import { getEntschaedigungKatalog } from "../../api/entschaedigungKatalogApi";
import type { EntschaedigungKatalog } from "../../types/EntschaedigungKatalog";
import type { EntschaedigungPosition } from "../../types/EntschaedigungPosition";
import { getApiErrorMessage } from "@/kjfp/api/utils/apiError";
export default function EntschaedigungEditor({objektId,rows,editable,onChange}:{objektId:number;rows:EntschaedigungPosition[];editable:boolean;onChange:(rows:EntschaedigungPosition[])=>void}) {
 const [katalog,setKatalog]=useState<EntschaedigungKatalog[]>([]),[auswahl,setAuswahl]=useState(''),[error,setError]=useState<string|null>(null);
 useEffect(()=>{let cancelled=false;setKatalog([]);setAuswahl('');setError(null);if(objektId)getEntschaedigungKatalog(objektId).then(r=>{if(!cancelled)setKatalog(r);}).catch(e=>{if(!cancelled)setError(getApiErrorMessage(e));});return()=>{cancelled=true;};},[objektId]);
 const change=(i:number,p:Partial<EntschaedigungPosition>)=>onChange(rows.map((row,index)=>index===i?{...row,...p}:row));
 const money=(n:number)=>n.toLocaleString('de-DE',{style:'currency',currency:'EUR'});
 return <Paper elevation={0} sx={{minWidth:0}}><Stack spacing={2}>
 <Typography variant="h6">Schäden und Fehlbestände</Typography>
 {error&&<Alert severity="error">{error}</Alert>}
 {rows.map((row,i)=><Paper variant="outlined" key={row.id??`neu-${i}`} sx={{p:2}}><Stack spacing={1}>
 <TextField label="Gegenstand" disabled={!editable} required value={row.bezeichnung} inputProps={{maxLength:250}} onChange={e=>change(i,{bezeichnung:e.target.value})}/>
 <Stack spacing={1} direction="column" sx={{display:"grid",gridTemplateColumns:"1fr","@container buchungsmaske (min-width: 720px)":{gridTemplateColumns:"repeat(2,minmax(0,1fr))"}}}>
 <TextField select label="Anlass" disabled={!editable} value={row.anlass} onChange={e=>change(i,{anlass:e.target.value as EntschaedigungPosition['anlass']})}><MenuItem value="BESCHAEDIGUNG">Beschädigung</MenuItem><MenuItem value="VERLUST">Verlust</MenuItem></TextField>
 <TextField label="Anzahl" type="number" disabled={!editable} value={row.anzahl} inputProps={{min:1,max:999999,step:1}} onChange={e=>change(i,{anzahl:Number(e.target.value)})}/>
 <TextField label="Einzelpreis (€)" type="number" disabled={!editable} value={row.einzelpreis} inputProps={{min:0.01,max:99999999.99,step:0.01}} onChange={e=>change(i,{einzelpreis:Number(e.target.value)})}/>
 <TextField label="Sortierung" type="number" disabled={!editable} value={row.sortierung} inputProps={{min:0,step:1}} onChange={e=>change(i,{sortierung:Number(e.target.value)})}/>
 </Stack>
 {row.katalogpreis!=null&&<TextField label="Preisabweichungsbegründung (intern)" disabled={!editable} required={row.einzelpreis!==row.katalogpreis} value={row.preisbegruendung??''} helperText={`Übernommener Katalogpreis: ${money(row.katalogpreis)}`} inputProps={{maxLength:1000}} onChange={e=>change(i,{preisbegruendung:e.target.value})}/>}
 <TextField label="Bemerkung (intern)" multiline disabled={!editable} value={row.bemerkung??''} inputProps={{maxLength:1000}} onChange={e=>change(i,{bemerkung:e.target.value})}/>
 {!editable&&row.gesamtbetrag!=null&&<Typography>Betrag: {money(row.gesamtbetrag)}</Typography>}
 {editable&&<Button color="error" onClick={()=>onChange(rows.filter((_,index)=>index!==i))}>Position entfernen</Button>}
 </Stack></Paper>)}
 {editable&&<Stack spacing={1} direction="column">
 <TextField select label="Katalogauswahl" value={auswahl} sx={{minWidth:0,flex:1}} onChange={e=>setAuswahl(e.target.value)}><MenuItem value="">Freie Position</MenuItem>{katalog.filter(k=>k.aktiv).map(k=><MenuItem key={k.id} value={String(k.id)}>{k.bezeichnung} · {money(k.einzelpreis)}</MenuItem>)}</TextField>
 <Button disabled={rows.length>=500} onClick={()=>{const k=katalog.find(k=>String(k.id)===auswahl);onChange([...rows,{katalogId:k?.id??null,bezeichnung:k?.bezeichnung??'',anlass:'BESCHAEDIGUNG',anzahl:1,einzelpreis:k?.einzelpreis??0,katalogpreis:k?.einzelpreis??null,bemerkung:null,preisbegruendung:null,sortierung:rows.length}]);}}>Position hinzufügen</Button>
 </Stack>}
 {rows.length===0?<Typography color="text.secondary">Keine Schäden oder Fehlbestände erfasst.</Typography>:<Typography fontWeight={600}>{editable?'Entschädigung gesamt (Vorschau): ':'Entschädigung gesamt: '}{money(rows.reduce((sum,row)=>sum+(editable?Math.round(row.einzelpreis*100)*row.anzahl/100:row.gesamtbetrag??0),0))}</Typography>}
 </Stack></Paper>;
}
