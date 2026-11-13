import { useEffect, useState } from "react";
import { Alert, Box, Button, Dialog, DialogActions, DialogContent, DialogTitle, Menu, MenuItem, Stack, Typography } from "@mui/material";
import type { Buchungsstatus } from "@/vermietung/enums/Buchungsstatus";
import apiClient from "@/core/api/client/apiClient";
import { getApiErrorMessage } from "@/kjfp/api/utils/apiError";
import BuchungsnachrichtDialog from "./BuchungsnachrichtDialog";
import BuchungsVorschauDialog from "./BuchungsVorschauDialog";
import VisibilityOutlinedIcon from "@mui/icons-material/VisibilityOutlined";
interface Props {
  onPrepareAbrechnung: () => void;
  abrechnungVorbereitet: boolean;
  saving?: boolean;
  buchung?: import("@/vermietung/types/Buchung").Buchung;
  buchungId?: number;
  direktbuchung?: boolean;
  reservierungMailGesendetAm?: string | null;
  reservierungMailFehler?: string | null;
  onRefresh?: () => Promise<void>;
  onDokumentVersandt?: () => void;
  status: Buchungsstatus;
  editMode: boolean;
  notifyByEmail?: boolean;
  abschlussErforderlich?: boolean;

  onEdit: () => void;
  onCancelEdit: () => void;
  onReopen: (sendEmail: boolean, fingerprint?: string) => Promise<void>;
  onSave: () => void;

  onReserve: (sendEmail: boolean, fingerprint?: string) => Promise<void> | void;
  onConfirm: (sendEmail: boolean, fingerprint?: string) => Promise<void> | void;
  onReject: (sendEmail: boolean, fingerprint?: string) => Promise<void> | void;
  onCancelBooking: (sendEmail: boolean, fingerprint?: string) => Promise<void> | void;
  onComplete: (sendEmail: boolean, fingerprint?: string) => Promise<void> | void;

  onDelete: () => void;

  disableDelete: boolean;
}

const nachrichtNamen: Record<string,string> = { RESERVIERT: "Reservierung", BESTAETIGT: "Bestätigung", ABGELEHNT: "Ablehnung", STORNIERT: "Stornierung", ABGESCHLOSSEN: "Abrechnung / Rechnung als Simulation" };
interface Kurz {id:string;ziel:Buchungsstatus;ausgefuehrt:boolean;ergebnis:string|null;}
export const BuchungActionBar: React.FC<Props> = ({onPrepareAbrechnung,abrechnungVorbereitet,saving=false,buchung,status,editMode,abschlussErforderlich=false,onEdit,onCancelEdit,onSave,onDelete,disableDelete,onReopen,onRefresh,onDokumentVersandt}) => {
 const [weitereAnchor,setWeitereAnchor]=useState<HTMLElement|null>(null);
 const [vorschauAnchor,setVorschauAnchor]=useState<HTMLElement|null>(null),[vorschauZiel,setVorschauZiel]=useState<Buchungsstatus|null>(null);
 const kostenlos=buchung?.zahlungsvariante==="OHNE_ABRECHNUNG";
 const vorschauZiele: Buchungsstatus[] = ["RESERVIERT","BESTAETIGT","ABGELEHNT","STORNIERT","ABGESCHLOSSEN"];
 const [target,setTarget]=useState<Buchungsstatus|null>(null),[draftId,setDraftId]=useState<string|undefined>();
 const [rows,setRows]=useState<Kurz[]>([]),[revision,setRevision]=useState(0),[error,setError]=useState(""),[reopen,setReopen]=useState(false),[deleting,setDeleting]=useState(false);
 useEffect(()=>{if(!buchung)return;let active=true;apiClient.get<Kurz[]>(`/vermietung/buchungen/${buchung.id}/nachrichten`).then(r=>{if(active)setRows(r.data);}).catch(e=>{if(active)setError(getApiErrorMessage(e));});return()=>{active=false;};},[buchung,revision]);
 async function refresh(){setRevision(v=>v+1);await onRefresh?.();}
 function open(ziel:Buchungsstatus){setDraftId(undefined);setTarget(ziel);}
 return <Box sx={{mt:3,display:"contents"}}>
  {error&&<Alert severity="error">{error}</Alert>}
  <Menu anchorEl={weitereAnchor} open={!!weitereAnchor} onClose={()=>setWeitereAnchor(null)}><MenuItem disabled={disableDelete} onClick={()=>{setWeitereAnchor(null);setDeleting(true);}}>Löschen</MenuItem></Menu>
  <Menu anchorEl={vorschauAnchor} open={!!vorschauAnchor} onClose={()=>setVorschauAnchor(null)}>{vorschauZiele.map(z=><MenuItem key={z} onClick={()=>{setVorschauAnchor(null);setVorschauZiel(z);}}>{nachrichtNamen[z]} – E-Mail und PDF</MenuItem>)}</Menu>
  {buchung&&vorschauZiel&&<BuchungsVorschauDialog key={vorschauZiel} buchung={buchung} ziel={vorschauZiel} ungespeichert={editMode} onVersandt={onDokumentVersandt} onClose={()=>setVorschauZiel(null)}/>}
  {!editMode&&rows.length>0&&<Stack spacing={1}><Typography variant="subtitle1">Nachrichtenentwürfe und Versandwiederholung</Typography>{rows.map(row=><Button key={row.id} sx={{justifyContent:"flex-start"}} onClick={()=>{setDraftId(row.id);setTarget(null);}}>{nachrichtNamen[row.ziel]} · {row.ausgefuehrt?row.ergebnis==="SMTP_BESTAETIGT"?"an SMTP übergeben":row.ergebnis?"Versand prüfen / wiederholen":"ohne Mail abgeschlossen":"Entwurf öffnen"}</Button>)}</Stack>}
  {buchung&&(target||draftId)&&<BuchungsnachrichtDialog key={draftId??target} buchung={buchung} ziel={target??undefined} entwurfId={draftId} onClose={()=>{setTarget(null);setDraftId(undefined);setRevision(v=>v+1);}} onApplied={refresh}/>}
  <Dialog open={reopen} onClose={()=>setReopen(false)}><DialogTitle>Buchung wieder öffnen?</DialogTitle><DialogContent>Für eine Korrektur werden anschließend Verbrauch und Schlussrechnung erneut geprüft. Es wird keine Nachricht versandt.</DialogContent><DialogActions><Button onClick={()=>setReopen(false)}>Abbrechen</Button><Button onClick={()=>{void onReopen(false).then(()=>setReopen(false)).catch(e=>setError(getApiErrorMessage(e)));}}>Wieder öffnen</Button></DialogActions></Dialog>
  <Dialog open={deleting} onClose={()=>setDeleting(false)}><DialogTitle>Buchung dauerhaft löschen?</DialogTitle><DialogActions><Button onClick={()=>setDeleting(false)}>Abbrechen</Button><Button color="error" onClick={()=>{setDeleting(false);onDelete();}}>Endgültig löschen</Button></DialogActions></Dialog>
  <Box sx={{display:"flex",gap:1,flexWrap:"wrap",position:"sticky",bottom:0,zIndex:10,bgcolor:"background.paper",borderTop:1,borderColor:"divider",p:1,pb:"max(8px, env(safe-area-inset-bottom))",boxShadow:2,mt:2}}>
   {editMode?<><Button disabled={saving} onClick={onCancelEdit}>Abbrechen</Button><Button disabled={saving} variant="contained" onClick={onSave}>{saving?"Wird gespeichert …":"Speichern"}</Button></>:<>
    {status==="ANFRAGE"&&<><Button variant="contained" onClick={()=>open("RESERVIERT")}>Reservierung prüfen</Button><Button variant="contained" onClick={()=>open("BESTAETIGT")}>Bestätigung prüfen</Button><Button color="error" onClick={()=>open("ABGELEHNT")}>Ablehnung prüfen</Button></>}
    {status==="RESERVIERT"&&<Button variant="contained" onClick={()=>open("BESTAETIGT")}>Bestätigen ohne Mail prüfen</Button>}
    {["RESERVIERT","BESTAETIGT","ABRECHENBAR"].includes(status)&&<Button color="error" onClick={()=>open("STORNIERT")}>Stornierung prüfen</Button>}
    {!kostenlos && (status==="ABRECHENBAR" || status==="ABGESCHLOSSEN"&&abschlussErforderlich)&&<Button variant="contained" onClick={()=>abrechnungVorbereitet?open("ABGESCHLOSSEN"):onPrepareAbrechnung()}>{abrechnungVorbereitet?"Abschluss und Nachricht prüfen":"Abrechnung vorbereiten"}</Button>}
    {!kostenlos && status==="ABGESCHLOSSEN"&&<Button onClick={()=>setReopen(true)}>Wieder öffnen</Button>}
    <Button onClick={onEdit}>Ändern</Button>
   </>}
   {buchung&&<Button startIcon={<VisibilityOutlinedIcon/>} aria-haspopup="menu" aria-expanded={!!vorschauAnchor} onClick={e=>setVorschauAnchor(e.currentTarget)}>Vorschau</Button>}
   <Button aria-haspopup="menu" aria-expanded={!!weitereAnchor} onClick={e=>setWeitereAnchor(e.currentTarget)}>Weitere Aktionen</Button>
  </Box>
 </Box>;
};
