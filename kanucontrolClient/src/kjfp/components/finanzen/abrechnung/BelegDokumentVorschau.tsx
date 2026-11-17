import { useEffect, useState } from "react";
import { Alert, Box, Button, CircularProgress, Dialog, DialogActions, DialogContent, DialogTitle, MenuItem, Stack, TextField, Typography } from "@mui/material";
import RotateLeftIcon from "@mui/icons-material/RotateLeft";
import RotateRightIcon from "@mui/icons-material/RotateRight";
import { DokumentDTO } from "@/kjfp/types/dokument";
import { DokumentAusrichtung, download, downloadDatei, getAusrichtung, getVorschauSeite, saveAusrichtung } from "@/kjfp/api/services/belegDokumentApi";
import { getApiErrorMessage } from "@/kjfp/api/utils/apiError";

interface Props { belegId: number; dokument: DokumentDTO; readOnly: boolean; onClose: () => void }
interface Bild { url: string; width: number; height: number }
export default function BelegDokumentVorschau({ belegId, dokument, readOnly, onClose }: Props) {
  const [saved, setSaved] = useState<DokumentAusrichtung | null>(null);
  const [angles, setAngles] = useState<number[]>([]);
  const [seite, setSeite] = useState(0);
  const [bild, setBild] = useState<Bild | null>(null);
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);
  const [retry, setRetry] = useState(0);
  const [bildRetry, setBildRetry] = useState(0);
  const dirty = !!saved && angles.some((a, i) => a !== saved.drehungen[i]);
  useEffect(() => {
    let active = true;
    getAusrichtung(belegId, dokument.id).then(info => { if (active) { setSaved(info); setAngles(info.drehungen); setError(null); } }).catch(err => { if (active) setError(getApiErrorMessage(err)); });
    return () => { active = false; };
  }, [belegId, dokument.id, retry]);
  const count = saved?.drehungen.length ?? 0;
  useEffect(() => {
    if (!count) return;
    let active = true, url: string | undefined;
    setBild(null);
    setError(null);
    getVorschauSeite(belegId, dokument.id, seite).then(blob => {
      if (!active) return;
      url = URL.createObjectURL(blob);
      const image = new Image();
      image.onload = () => { if (active) setBild({ url: url!, width: image.naturalWidth, height: image.naturalHeight }); };
      image.onerror = () => { if (active) setError("Die Vorschau konnte nicht angezeigt werden."); };
      image.src = url;
    }).catch(err => { if (active) setError(getApiErrorMessage(err)); });
    return () => { active = false; if (url) URL.revokeObjectURL(url); };
  }, [belegId, dokument.id, seite, count, retry, bildRetry]);
  useEffect(() => {
    const warn = (event: BeforeUnloadEvent) => { if (dirty) { event.preventDefault(); event.returnValue = ""; } };
    window.addEventListener("beforeunload", warn); return () => window.removeEventListener("beforeunload", warn);
  }, [dirty]);
  const close = () => { if (!busy && (!dirty || window.confirm("Ungespeicherte Drehungen verwerfen?"))) onClose(); };
  const drehen = (delta: number) => setAngles(current => current.map((value, index) => index === seite ? (value + delta + 360) % 360 : value));
  const angle = angles[seite] ?? 0, swapped = angle === 90 || angle === 270;
  const transform = bild ? angle === 90 ? `translate(${bild.height} 0) rotate(90)` : angle === 180 ? `translate(${bild.width} ${bild.height}) rotate(180)` : angle === 270 ? `translate(0 ${bild.width}) rotate(270)` : "" : "";
  return <Dialog open onClose={close} maxWidth="lg" fullWidth aria-labelledby="beleg-vorschau-titel">
    <DialogTitle id="beleg-vorschau-titel">Belegvorschau: {dokument.originalDateiname}</DialogTitle>
    <DialogContent dividers sx={{ minWidth: 0 }}>
      {error && <Alert severity="error" action={!saved ? <Button onClick={() => setRetry(n => n + 1)}>Erneut laden</Button> : !bild ? <Button onClick={() => setBildRetry(n => n + 1)}>Vorschau erneut laden</Button> : undefined}>{error}</Alert>}
      {saved && <>
        <Stack direction={{ xs: "column", sm: "row" }} spacing={1} alignItems={{ sm: "center" }} sx={{ mb: 2 }}>
          <TextField id="beleg-vorschau-seite" select size="small" label="Seite" value={seite} disabled={busy} onChange={e => setSeite(Number(e.target.value))} sx={{ minWidth: 160 }}>
            {angles.map((_, index) => <MenuItem value={index} key={index}>Seite {index + 1} von {count}</MenuItem>)}
          </TextField>
          {!readOnly && <>
            <Button startIcon={<RotateLeftIcon />} disabled={busy || !bild} onClick={() => drehen(-90)}>90° links drehen</Button>
            <Button startIcon={<RotateRightIcon />} disabled={busy || !bild} onClick={() => drehen(90)}>90° rechts drehen</Button>
          </>}
          <Typography role="status">Seite {seite + 1} von {count} · Zusätzliche Drehung: {angle}°</Typography>
        </Stack>
        {dirty && <Alert severity="info" sx={{ mb: 1 }}>Ungespeicherte Drehungen. Download und Beleg-Report verwenden erst nach „Drehungen speichern“ diese Ausrichtung.</Alert>}
        {readOnly && <Alert severity="info" sx={{ mb: 1 }}>Zum Korrigieren der Ausrichtung den Beleg zur Bearbeitung öffnen.</Alert>}
      </>}
      {!bild && !error && <Box sx={{ textAlign: "center", p: 4 }}><CircularProgress aria-label="Belegvorschau wird geladen" /></Box>}
      {bild && <Box sx={{ bgcolor: "grey.100", textAlign: "center", p: 1 }}>
        <svg role="img" aria-label={`Beleg Seite ${seite + 1}, Drehung ${angle} Grad`} viewBox={`0 0 ${swapped ? bild.height : bild.width} ${swapped ? bild.width : bild.height}`} style={{ display: "block", width: "100%", maxHeight: "65vh" }}>
          <g transform={transform}><image href={bild.url} width={bild.width} height={bild.height} /></g>
        </svg>
      </Box>}
    </DialogContent>
    <DialogActions sx={{ flexWrap: "wrap", gap: 1 }}>
      <Button disabled={busy} onClick={close}>{dirty ? "Abbrechen" : "Schließen"}</Button>
      <Button disabled={busy || dirty || !saved} onClick={async () => {
        setBusy(true); try { downloadDatei(await download(belegId, dokument.id), dokument.originalDateiname); } catch (err) { setError(getApiErrorMessage(err)); } finally { setBusy(false); }
      }}>Herunterladen</Button>
      {!readOnly && <Button variant="contained" disabled={busy || !dirty} onClick={async () => {
        if (!saved) return;
        setBusy(true); try { const info = await saveAusrichtung(belegId, dokument.id, { drehungen: angles, version: saved.version }); setSaved(info); setAngles(info.drehungen); setError(null); }
        catch (err) { setError(getApiErrorMessage(err)); } finally { setBusy(false); }
      }}>Drehungen speichern</Button>}
    </DialogActions>
  </Dialog>;
}
