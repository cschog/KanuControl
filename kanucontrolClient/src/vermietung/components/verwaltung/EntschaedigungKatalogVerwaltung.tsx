import { useEffect, useState } from "react";
import { Alert, Box, Button, Checkbox, Chip, CircularProgress, Dialog, DialogActions, DialogContent, DialogTitle, FormControlLabel, MenuItem, Paper, Stack, TextField, Typography } from "@mui/material";
import { getMietobjekte } from "@/vermietung/api/mietobjektApi";
import { getEntschaedigungKatalog, saveEntschaedigungKatalog } from "@/vermietung/api/entschaedigungKatalogApi";
import type { Mietobjekt } from "@/vermietung/types/Mietobjekt";
import type { EntschaedigungKatalog } from "@/vermietung/types/EntschaedigungKatalog";
import { getApiErrorMessage } from "@/kjfp/api/utils/apiError";

const geld = new Intl.NumberFormat("de-DE", { style: "currency", currency: "EUR" });
const sortiert = (rows: EntschaedigungKatalog[]) => [...rows].sort((a, b) => a.sortierung - b.sortierung || a.id - b.id);

function Katalog({ objektId, onEditing }: { objektId: number; onEditing: (editing: boolean) => void }) {
  const [rows, setRows] = useState<EntschaedigungKatalog[]>([]);
  const [loading, setLoading] = useState(true);
  const [loadError, setLoadError] = useState<string | null>(null);
  const [attempt, setAttempt] = useState(0);
  const [open, setOpen] = useState(false);
  const [selected, setSelected] = useState<EntschaedigungKatalog | null>(null);
  const [name, setName] = useState("");
  const [preis, setPreis] = useState("");
  const [rang, setRang] = useState("0");
  const [aktiv, setAktiv] = useState(true);
  const [saving, setSaving] = useState(false);
  const [saveError, setSaveError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);

  useEffect(() => {
    let cancelled = false;
    setLoading(true); setLoadError(null);
    getEntschaedigungKatalog(objektId)
      .then(data => { if (!cancelled) setRows(sortiert(data)); })
      .catch((error: unknown) => { if (!cancelled) setLoadError(getApiErrorMessage(error)); })
      .finally(() => { if (!cancelled) setLoading(false); });
    return () => { cancelled = true; };
  }, [objektId, attempt]);

  function edit(row: EntschaedigungKatalog | null) {
    setSelected(row); setName(row?.bezeichnung ?? ""); setPreis(row ? String(row.einzelpreis) : "");
    setRang(String(row?.sortierung ?? 0)); setAktiv(row?.aktiv ?? true);
    setSaveError(null); setSuccess(false); setOpen(true); onEditing(true);
  }
  function close() { if (!saving) { setOpen(false); onEditing(false); } }
  const preisText = preis.trim().replace(",", ".");
  const preisGueltig = /^\d{1,8}(\.\d{1,2})?$/.test(preisText) && Number(preisText) > 0;
  const rangGueltig = /^\d+$/.test(rang) && Number(rang) <= 2147483647;
  const gueltig = name.trim().length > 0 && name.length <= 250 && preisGueltig && rangGueltig;
  async function save() {
    if (!gueltig || saving) return;
    setSaving(true); setSaveError(null);
    try {
      const saved = await saveEntschaedigungKatalog(objektId, selected?.id ?? null, {
        version: selected?.version ?? null, bezeichnung: name.trim(), einzelpreis: Number(preisText), sortierung: Number(rang), aktiv,
      });
      setRows(current => sortiert([...current.filter(row => row.id !== saved.id), saved]));
      setSuccess(true); setOpen(false); onEditing(false);
    } catch (error: unknown) { setSaveError(getApiErrorMessage(error)); }
    finally { setSaving(false); }
  }
  return <Stack spacing={2}>
    <Typography color="text.secondary">Entschädigungen werden pro Stück berechnet. Die Einträge gelten nur für das ausgewählte Mietobjekt. Verwendete Einträge deaktivieren, damit sie für neue Positionen nicht mehr auswählbar sind.</Typography>
    {loading && <Box role="status" aria-label="Katalog wird geladen"><CircularProgress size={24} /></Box>}
    {loadError && <Alert severity="error" action={<Button color="inherit" onClick={() => setAttempt(value => value + 1)}>Erneut laden</Button>}>{loadError}</Alert>}
    {success && <Alert severity="success">Katalogeintrag gespeichert.</Alert>}
    <Box><Button variant="contained" disabled={loading || !!loadError} onClick={() => edit(null)}>Katalogeintrag hinzufügen</Button></Box>
    {!loading && !loadError && rows.length === 0 && <Alert severity="info">Für dieses Mietobjekt sind noch keine Entschädigungen hinterlegt.</Alert>}
    {!loading && !loadError && rows.map(row => <Paper key={row.id} variant="outlined" sx={{ p: 2 }}>
      <Stack direction={{ xs: "column", sm: "row" }} spacing={2} alignItems={{ xs: "stretch", sm: "center" }}>
        <Box sx={{ flex: 1, minWidth: 0 }}>
          <Typography fontWeight={600} sx={{ overflowWrap: "anywhere" }}>{row.bezeichnung}</Typography>
          <Typography variant="body2">{geld.format(row.einzelpreis)} pro Stück · Sortierung {row.sortierung}</Typography>
        </Box>
        <Chip label={row.aktiv ? "Aktiv" : "Inaktiv"} color={row.aktiv ? "success" : "default"} size="small" />
        <Button onClick={() => edit(row)} aria-label={`${row.bezeichnung} ändern`}>Ändern</Button>
      </Stack>
    </Paper>)}
    <Dialog open={open} onClose={close} fullWidth maxWidth="sm">
      <Box component="form" onSubmit={event => { event.preventDefault(); void save(); }}>
        <DialogTitle>{selected ? "Katalogeintrag bearbeiten" : "Katalogeintrag hinzufügen"}</DialogTitle>
        <DialogContent><Stack spacing={2} sx={{ pt: 1 }}>
          {saveError && <Alert severity="error">{saveError}</Alert>}
          <TextField label="Bezeichnung" autoFocus required disabled={saving} value={name} onChange={event => setName(event.target.value)} slotProps={{ htmlInput: { maxLength: 250 } }} />
          <TextField label="Einzelpreis (€)" required disabled={saving} value={preis} onChange={event => setPreis(event.target.value)} slotProps={{ htmlInput: { inputMode: "decimal", maxLength: 11 } }} error={preis !== "" && !preisGueltig} helperText="Positiver Betrag, höchstens zwei Nachkommastellen, z. B. 7,50." />
          <TextField label="Sortierung" required disabled={saving} value={rang} onChange={event => setRang(event.target.value)} slotProps={{ htmlInput: { inputMode: "numeric", maxLength: 10 } }} error={!rangGueltig} helperText="Ganze Zahl ab 0. Kleinere Werte erscheinen zuerst." />
          <FormControlLabel label="Aktiv – für neue Positionen auswählbar" control={<Checkbox disabled={saving} checked={aktiv} onChange={event => setAktiv(event.target.checked)} />} />
        </Stack></DialogContent>
        <DialogActions><Button disabled={saving} onClick={close}>Abbrechen</Button><Button type="submit" variant="contained" disabled={saving || !gueltig}>{saving ? "Wird gespeichert …" : "Speichern"}</Button></DialogActions>
      </Box>
    </Dialog>
  </Stack>;
}

export default function EntschaedigungKatalogVerwaltung() {
  const [objekte, setObjekte] = useState<Mietobjekt[]>([]);
  const [objektId, setObjektId] = useState<number | "">("");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [attempt, setAttempt] = useState(0);
  const [editing, setEditing] = useState(false);
  useEffect(() => {
    let cancelled = false;
    setLoading(true); setError(null);
    getMietobjekte().then(data => {
      if (!cancelled) { setObjekte(data); setObjektId(current => data.some(objekt => objekt.id === current) ? current : data[0]?.id ?? ""); }
    }).catch((err: unknown) => { if (!cancelled) setError(getApiErrorMessage(err)); })
      .finally(() => { if (!cancelled) setLoading(false); });
    return () => { cancelled = true; };
  }, [attempt]);
  return <Stack spacing={2}>
    <Typography variant="h6">Entschädigungskatalog</Typography>
    {loading && <CircularProgress size={24} />}
    {error && <Alert severity="error" action={<Button color="inherit" onClick={() => setAttempt(value => value + 1)}>Erneut laden</Button>}>{error}</Alert>}
    {!loading && !error && objekte.length === 0 && <Alert severity="info">Bitte zuerst ein Mietobjekt anlegen.</Alert>}
    {objekte.length > 0 && !error && <TextField select label="Mietobjekt" value={objektId} disabled={loading || editing} onChange={event => setObjektId(Number(event.target.value))} helperText={editing ? "Zum Objektwechsel den Eintrag zuerst speichern oder abbrechen." : "Preise werden für dieses Mietobjekt gepflegt."}>
      {objekte.map(objekt => <MenuItem key={objekt.id} value={objekt.id}>{objekt.bezeichnung}</MenuItem>)}
    </TextField>}
    {objektId !== "" && !loading && !error && <Katalog key={objektId} objektId={objektId} onEditing={setEditing} />}
  </Stack>;
}
