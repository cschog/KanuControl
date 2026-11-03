import { useEffect, useRef, useState } from "react";
import { Alert, Box, Button, Checkbox, CircularProgress, Dialog, DialogActions, DialogContent, DialogTitle,
  FormControlLabel, MenuItem, Paper, Stack, TextField, Typography } from "@mui/material";
import { getMietobjekte } from "@/vermietung/api/mietobjektApi";
import { getMietbereiche } from "@/vermietung/api/mietbereichApi";
import { getAirbnbExport, saveAirbnbExport, type AirbnbExport } from "@/vermietung/api/airbnbExportApi";
import { getApiErrorMessage } from "@/kjfp/api/utils/apiError";
import { useVermietungContext } from "@/vermietung/context/VermietungContext";
import type { Mietobjekt } from "@/vermietung/types/Mietobjekt";
import type { Mietbereich } from "@/vermietung/types/Mietbereich";

function Einstellungen({ objektId }: { objektId: number }) {
  const [form, setForm] = useState<AirbnbExport | null>(null);
  const [bereiche, setBereiche] = useState<Mietbereich[]>([]);
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [confirm, setConfirm] = useState(false);
  const [reload, setReload] = useState(0);
  const linkRef = useRef<HTMLInputElement>(null);
  useEffect(() => {
    let cancelled = false;
    setLoading(true); setError(null); setSuccess(null);
    Promise.all([getAirbnbExport(objektId), getMietbereiche(objektId)])
      .then(([config, items]) => { if (!cancelled) { setForm(config); setBereiche(items); } })
      .catch((err: unknown) => { if (!cancelled) setError(getApiErrorMessage(err, "Der Airbnb-Export konnte nicht geladen werden.")); })
      .finally(() => { if (!cancelled) setLoading(false); });
    return () => { cancelled = true; };
  }, [objektId, reload]);
  function change(values: Partial<AirbnbExport>) {
    setForm(current => current ? { ...current, ...values } : current); setError(null); setSuccess(null);
  }
  async function save(neuerLink: boolean) {
    if (!form || busy) return;
    if (form.aktiv && (form.mietbereichIds.length === 0 || form.mietbereichIds.some(id => !bereiche.some(b => b.id === id)))) {
      setError("Bitte die zum Inserat gehörenden Mietbereiche auswählen. Für das ganze Bootshaus: EG und OG."); return;
    }
    setBusy(true); setError(null); setSuccess(null);
    try {
      const result = await saveAirbnbExport(objektId, form, neuerLink);
      setForm({ ...result, feedPfad: result.feedPfad ?? form.feedPfad });
      setSuccess(neuerLink ? "Export-Link erstellt. Bitte jetzt kopieren und bei Airbnb eintragen."
        : result.aktiv ? "Export-Einstellungen gespeichert." : "Export deaktiviert. Der vorhandene Link liefert einen leeren Kalender.");
    } catch (err: unknown) { setError(getApiErrorMessage(err, "Die Einstellungen konnten nicht gespeichert werden.")); }
    finally { setBusy(false); setConfirm(false); }
  }
  const feedUrl = form?.feedPfad ? new URL(form.feedPfad, window.location.origin).href : null;
  async function copy() {
    if (!feedUrl) return;
    try { await navigator.clipboard.writeText(feedUrl); setSuccess("Export-Link kopiert."); }
    catch { linkRef.current?.select(); setSuccess("Bitte den markierten Link kopieren."); }
  }
  if (loading) return <CircularProgress aria-label="Airbnb-Export wird geladen" />;
  return <Stack spacing={2}>
    {error && <Alert severity="error">{error}</Alert>}
    {success && <Alert severity="success">{success}</Alert>}
    <Box><Button disabled={busy} onClick={() => setReload(n => n + 1)}>Neu laden</Button></Box>
    {form && <Paper variant="outlined" sx={{ p: 2 }}>
      <Stack spacing={2}>
        <FormControlLabel label="Export zu Airbnb aktivieren" control={<Checkbox checked={form.aktiv} disabled={busy}
          onChange={(_, aktiv) => change({ aktiv })} />} />
        <Typography variant="h6">Mietumfang des Airbnb-Inserats</Typography>
        <Typography>Für das gesamte Bootshaus müssen Erdgeschoss und Obergeschoss ausgewählt sein. Eine Belegung eines dieser Bereiche blockiert das gesamte Inserat.</Typography>
        {bereiche.map(b => <FormControlLabel key={b.id} label={b.bezeichnung} control={<Checkbox
          checked={form.mietbereichIds.includes(b.id)} disabled={busy}
          onChange={(_, selected) => change({ mietbereichIds: selected ? [...form.mietbereichIds, b.id] : form.mietbereichIds.filter(id => id !== b.id) })} />} />)}
        {form.mietbereichIds.some(id => !bereiche.some(b => b.id === id)) && <Alert severity="warning">
          Ein ausgewählter Mietbereich ist nicht mehr vorhanden.
          <Button disabled={busy} onClick={() => change({ mietbereichIds: form.mietbereichIds.filter(id => bereiche.some(b => b.id === id)) })}>Auswahl bereinigen</Button>
        </Alert>}
        <Typography variant="body2">Exportiert werden reservierte und bestätigte Buchungen sowie Sperrzeiten des Mietobjekts. Anfragen sowie abgelehnte und stornierte Buchungen werden nicht exportiert. Der Feed enthält keine Namen oder internen Gründe.</Typography>
        <Stack direction={{ xs: "column", sm: "row" }} spacing={1}>
          <Button variant="contained" disabled={busy} onClick={() => void save(false)}>Einstellungen speichern</Button>
          <Button variant="outlined" disabled={busy || !form.aktiv} onClick={() => {
            if (form.linkVorhanden) setConfirm(true); else void save(true);
          }}>{form.linkVorhanden ? "Neuen Link erzeugen" : "Export-Link erstellen"}</Button>
        </Stack>
        {feedUrl ? <>
          <TextField label="Airbnb-iCal-Link" value={feedUrl} inputRef={linkRef} fullWidth
            slotProps={{ input: { readOnly: true } }} helperText="Der Link wird nur unmittelbar nach dem Erzeugen angezeigt. Bitte bei Airbnb hinterlegen." />
          <Box><Button disabled={busy} onClick={() => void copy()}>Link kopieren</Button></Box>
        </> : form.linkVorhanden && <Alert severity="info">Ein Export-Link ist eingerichtet. Er bleibt gültig. Falls du ihn nicht mehr hast, kannst du einen neuen Link erzeugen und bei Airbnb ersetzen.</Alert>}
      </Stack>
    </Paper>}
    <Dialog open={confirm} onClose={() => { if (!busy) setConfirm(false); }}>
      <DialogTitle>Export-Link erneuern?</DialogTitle>
      <DialogContent>Der bisherige Link wird ungültig. Trage den neuen Link anschließend im importierten Airbnb-Kalender ein.</DialogContent>
      <DialogActions><Button disabled={busy || !confirm} onClick={() => setConfirm(false)}>Abbrechen</Button>
        <Button disabled={busy || !confirm} onClick={() => void save(true)}>Neuen Link erzeugen</Button></DialogActions>
    </Dialog>
  </Stack>;
}

export default function AirbnbExportVerwaltung() {
  const { mietobjekt } = useVermietungContext();
  const [objekte, setObjekte] = useState<Mietobjekt[]>([]);
  const [selected, setSelected] = useState<number | "">("");
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  useEffect(() => {
    let cancelled = false;
    getMietobjekte().then(items => {
      if (!cancelled) { setObjekte(items); setSelected(items.find(o => o.id === mietobjekt?.id)?.id ?? items[0]?.id ?? ""); }
    }).catch((err: unknown) => { if (!cancelled) setError(getApiErrorMessage(err)); })
      .finally(() => { if (!cancelled) setLoading(false); });
    return () => { cancelled = true; };
  }, [mietobjekt?.id]);
  return <Stack spacing={2}>
    <Typography variant="h6">Airbnb-Kalenderexport</Typography>
    <Typography>KC → Airbnb: Kalenderdaten werden als iCal-Abonnement bereitgestellt. Airbnb-Buchungen werden hier noch nicht nach KC übernommen.</Typography>
    <Alert severity="info">Airbnb aktualisiert importierte Kalender normalerweise alle drei Stunden. Unter Airbnb → Kalender → Verfügbarkeit → Kalender verknüpfen kannst du den iCal-Link hinzufügen und den Abruf manuell aktualisieren.</Alert>
    {error && <Alert severity="error">{error}</Alert>}
    {loading ? <CircularProgress /> : objekte.length === 0 ? <Typography>Bitte zuerst ein Mietobjekt anlegen.</Typography> : <>
      <TextField select label="Mietobjekt" value={selected} onChange={e => setSelected(Number(e.target.value))}>
        {objekte.map(o => <MenuItem key={o.id} value={o.id}>{o.bezeichnung}</MenuItem>)}
      </TextField>
      {selected !== "" && <Einstellungen key={selected} objektId={selected} />}
    </>}
  </Stack>;
}
