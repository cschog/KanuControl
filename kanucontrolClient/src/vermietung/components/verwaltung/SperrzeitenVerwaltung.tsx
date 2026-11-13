import { useEffect, useState } from "react";
import { Alert, Box, Button, Checkbox, CircularProgress, Dialog, DialogActions, DialogContent, DialogTitle,
  FormControlLabel, MenuItem, Paper, Stack, TextField, Typography } from "@mui/material";
import dayjs from "dayjs";
import { getMietobjekte } from "@/vermietung/api/mietobjektApi";
import { deleteSperrzeit, getSperrzeiten, saveSperrzeit, type Sperrzeit } from "@/vermietung/api/sperrzeitApi";
import { getApiErrorMessage } from "@/kjfp/api/utils/apiError";
import { useVermietungContext } from "@/vermietung/context/VermietungContext";
import type { Mietobjekt } from "@/vermietung/types/Mietobjekt";

function Verwaltung({ objektId }: { objektId: number }) {
  const [items, setItems] = useState<Sperrzeit[]>([]);
  const [loading, setLoading] = useState(true);
  const [busy, setBusy] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [reload, setReload] = useState(0);
  const [editing, setEditing] = useState<Sperrzeit | null>(null);
  const [remove, setRemove] = useState<Sperrzeit | null>(null);
  const [ganztag, setGanztag] = useState(true);
  const [von, setVon] = useState("");
  const [bis, setBis] = useState("");
  const [grund, setGrund] = useState("");

  useEffect(() => {
    let cancelled = false;
    setLoading(true); setError(null);
    getSperrzeiten(objektId).then(data => { if (!cancelled) setItems(data); })
      .catch((err: unknown) => { if (!cancelled) setError(getApiErrorMessage(err, "Sperrzeiten konnten nicht geladen werden.")); })
      .finally(() => { if (!cancelled) setLoading(false); });
    return () => { cancelled = true; };
  }, [objektId, reload]);

  function reset() { setEditing(null); setVon(""); setBis(""); setGrund(""); setGanztag(true); }
  function edit(item: Sperrzeit) {
    setEditing(item); setGanztag(item.ganztaegig); setGrund(item.grund); setError(null); setSuccess(null);
    setVon(item.ganztaegig ? item.von.slice(0, 10) : item.von.slice(0, 16));
    setBis(item.ganztaegig ? dayjs(item.bis).subtract(1, "day").format("YYYY-MM-DD") : item.bis.slice(0, 16));
  }
  async function save(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault(); setError(null); setSuccess(null);
    const start = ganztag ? von + "T00:00:00" : von + ":00";
    const end = ganztag ? dayjs(bis).add(1, "day").format("YYYY-MM-DD") + "T00:00:00" : bis + ":00";
    if (!von || !bis || !grund.trim() || !dayjs(end).isAfter(dayjs(start))) {
      setError("Bitte einen gültigen Zeitraum und einen internen Grund eingeben."); return;
    }
    setBusy(true);
    try {
      const item = await saveSperrzeit(objektId, editing?.id ?? null,
        { von: start, bis: end, ganztaegig: ganztag, grund: grund.trim(), version: editing?.version });
      setItems(current => [...current.filter(s => s.id !== item.id), item].sort((a, b) => a.von.localeCompare(b.von)));
      reset(); setSuccess("Sperrzeit gespeichert.");
    } catch (err: unknown) { setError(getApiErrorMessage(err, "Die Sperrzeit konnte nicht gespeichert werden.")); }
    finally { setBusy(false); }
  }
  async function confirmDelete() {
    if (!remove) return;
    setBusy(true); setError(null); setSuccess(null);
    try {
      await deleteSperrzeit(objektId, remove);
      setItems(current => current.filter(s => s.id !== remove.id));
      if (editing?.id === remove.id) reset();
      setSuccess("Sperrzeit aufgehoben.");
    } catch (err: unknown) { setError(getApiErrorMessage(err, "Die Sperrzeit konnte nicht aufgehoben werden.")); }
    finally { setBusy(false); setRemove(null); }
  }
  return <Stack spacing={2}>
    {error && <Alert severity="error">{error}</Alert>}
    {success && <Alert severity="success">{success}</Alert>}
    <Box><Button disabled={busy || loading} onClick={() => setReload(n => n + 1)}>Neu laden</Button></Box>
    {loading ? <CircularProgress aria-label="Sperrzeiten werden geladen" /> : <>
      <Paper variant="outlined" sx={{ p: 2 }} component="form" onSubmit={save}>
        <Stack spacing={2}>
          <Typography variant="h6">{editing ? "Sperrzeit bearbeiten" : "Neue Sperrzeit"}</Typography>
          <FormControlLabel label="Ganztägig" control={<Checkbox checked={ganztag} disabled={busy} onChange={(_, checked) => {
            setGanztag(checked); setVon(""); setBis("");
          }} />} />
          <Stack direction={{ xs: "column", sm: "row" }} spacing={2}>
            <TextField label={ganztag ? "Erster gesperrter Tag" : "Beginn"} type={ganztag ? "date" : "datetime-local"}
              required fullWidth value={von} disabled={busy} slotProps={{ inputLabel: { shrink: true } }} onChange={e => setVon(e.target.value)} />
            <TextField label={ganztag ? "Letzter gesperrter Tag (einschließlich)" : "Ende"} type={ganztag ? "date" : "datetime-local"}
              required fullWidth value={bis} disabled={busy} slotProps={{ inputLabel: { shrink: true } }} onChange={e => setBis(e.target.value)} />
          </Stack>
          <TextField label="Interner Grund" required multiline minRows={2} value={grund} disabled={busy}
            slotProps={{ htmlInput: { maxLength: 500 } }} helperText="Der Grund wird auf der Homepage nicht veröffentlicht."
            onChange={e => setGrund(e.target.value)} />
          <Stack direction="row" spacing={1}>
            <Button type="submit" variant="contained" disabled={busy}>{busy ? "Wird gespeichert …" : "Sperrzeit speichern"}</Button>
            {editing && <Button disabled={busy} onClick={reset}>Abbrechen</Button>}
          </Stack>
        </Stack>
      </Paper>
      {items.length === 0 && <Typography>Keine Sperrzeiten vorhanden.</Typography>}
      {items.map(item => <Paper key={item.id} variant="outlined" sx={{ p: 2 }}>
        <Stack spacing={1}>
          <Typography fontWeight={600}>{item.ganztaegig
            ? `${dayjs(item.von).format("DD.MM.YYYY")} bis ${dayjs(item.bis).subtract(1, "day").format("DD.MM.YYYY")} · ganztägig`
            : `${dayjs(item.von).format("DD.MM.YYYY HH:mm")} bis ${dayjs(item.bis).format("DD.MM.YYYY HH:mm")} Uhr`}</Typography>
          <Typography sx={{ overflowWrap: "anywhere" }}>{item.grund}</Typography>
          <Stack direction="row" spacing={1}>
            <Button disabled={busy} onClick={() => edit(item)}>Ändern</Button>
            <Button color="error" disabled={busy} onClick={() => setRemove(item)}>Sperre aufheben</Button>
          </Stack>
        </Stack>
      </Paper>)}
    </>}
    <Dialog open={!!remove} onClose={() => { if (!busy) setRemove(null); }}>
      <DialogTitle>Sperre aufheben?</DialogTitle>
      <DialogContent>Der Zeitraum kann anschließend wieder vermietet werden, sofern keine anderen Belegungen vorliegen.</DialogContent>
      <DialogActions><Button disabled={busy} onClick={() => setRemove(null)}>Abbrechen</Button>
        <Button color="error" disabled={busy} onClick={() => void confirmDelete()}>Sperre aufheben</Button></DialogActions>
    </Dialog>
  </Stack>;
}

export default function SperrzeitenVerwaltung() {
  const { mietobjekt } = useVermietungContext();
  const [objekte, setObjekte] = useState<Mietobjekt[]>([]);
  const [selected, setSelected] = useState<number | "">("");
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  useEffect(() => {
    let cancelled = false;
    getMietobjekte().then(data => {
      if (cancelled) return;
      setObjekte(data); setSelected(data.find(o => o.id === mietobjekt?.id)?.id ?? data[0]?.id ?? "");
    }).catch((err: unknown) => { if (!cancelled) setError(getApiErrorMessage(err)); })
      .finally(() => { if (!cancelled) setLoading(false); });
    return () => { cancelled = true; };
  }, [mietobjekt?.id]);
  return <Stack spacing={2}>
    <Typography variant="h6">Sperrzeiten</Typography>
    <Typography>Sperren gelten für alle Mietbereiche des ausgewählten Mietobjekts. Reservierte oder bestätigte Buchungen müssen vor einer überschneidenden Sperre geklärt werden.</Typography>
    {error && <Alert severity="error">{error}</Alert>}
    {loading ? <CircularProgress /> : objekte.length === 0 ? <Typography>Bitte zuerst ein Mietobjekt anlegen.</Typography> : <>
      <TextField select label="Mietobjekt" value={selected} onChange={e => setSelected(Number(e.target.value))}>
        {objekte.map(o => <MenuItem key={o.id} value={o.id}>{o.bezeichnung}</MenuItem>)}
      </TextField>
      {selected !== "" && <Verwaltung key={selected} objektId={selected} />}
    </>}
  </Stack>;
}
