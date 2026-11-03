import { useEffect, useMemo, useState } from "react";
import { Alert, Box, Button, Chip, CircularProgress, FormControlLabel, Grid, Paper, Stack, TextField, Typography, Checkbox, Dialog, DialogTitle, DialogContent, DialogContentText, DialogActions } from "@mui/material";
import AddIcon from "@mui/icons-material/Add";
import SaveIcon from "@mui/icons-material/Save";
import EditIcon from "@mui/icons-material/Edit";
import ArchiveIcon from "@mui/icons-material/Archive";
import DeleteIcon from "@mui/icons-material/Delete";
import { getApiErrorMessage } from "@/kjfp/api/utils/apiError";
import { archiveMieter, createMieter, deleteMieter, getMieter, updateMieter } from "@/vermietung/api/mieterApi";
import type { Mieter, MieterSave } from "@/vermietung/types/Mieter";
import type { PersonRef } from "@/core/api/types/person/PersonRef";
import { PersonAutocomplete } from "@/core/components/person/PersonAutocomplete";
import { getPersonById } from "@/core/api/services/personApi";
import { useBackNavigation } from "@/core/context/BackNavigationContext";

type MieterTextKey = Exclude<keyof MieterSave, "aktiv" | "personId">;

const emptyForm: MieterSave = { name: "", vorname: "", email: "", telefon: "", telefonFestnetz: "", strasse: "", plz: "", ort: "", countryCode: "DE", bankName: "", iban: "", bic: "", aktiv: true };

function fromMieter(mieter: Mieter): MieterSave {
  return { name: mieter.name, vorname: mieter.vorname, email: mieter.email ?? "", telefon: mieter.telefon ?? "", telefonFestnetz: mieter.telefonFestnetz ?? "", strasse: mieter.strasse ?? "", plz: mieter.plz ?? "", ort: mieter.ort ?? "", countryCode: mieter.countryCode ?? "DE", bankName: mieter.bankName ?? "", iban: mieter.iban ?? "", bic: mieter.bic ?? "", aktiv: mieter.aktiv };
}

export default function MieterPage() {
  const [mieter, setMieter] = useState<Mieter[]>([]);
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [form, setForm] = useState<MieterSave>(emptyForm);
  const [personRef, setPersonRef] = useState<PersonRef | undefined>();
  const [edit, setEdit] = useState(false);
  const [isNew, setIsNew] = useState(false);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<Mieter | null>(null);
  const [deleting, setDeleting] = useState(false);
  const [deleteError, setDeleteError] = useState<string | null>(null);
  const { setDetailViewOpen } = useBackNavigation();
  const selected = useMemo(() => mieter.find((item) => item.id === selectedId) ?? null, [mieter, selectedId]);

  async function reload(selectId?: number | null) {
    setLoading(true);
    try {
      const data = await getMieter();
      setMieter(data);
      const next = selectId ?? data[0]?.id ?? null;
      setSelectedId(next);
      const current = data.find((item) => item.id === next);
      setForm(current ? fromMieter(current) : emptyForm);
    } catch {
      setError("Die Mieterliste konnte nicht geladen werden.");
    } finally {
      setLoading(false);
    }
  }

  useEffect(() => { void reload(); }, []);
  useEffect(() => {
    setDetailViewOpen(edit || selectedId !== null);
    return () => setDetailViewOpen(false);
  }, [edit, selectedId, setDetailViewOpen]);

  function startNew() {
    if (saving || deleting) return;
    setSuccess(null);
    setSelectedId(null);
    setForm(emptyForm);
    setPersonRef(undefined);
    setIsNew(true);
    setEdit(true);
    setError(null);
  }

  function select(item: Mieter) {
    if (saving || deleting) return;
    setSuccess(null);
    setSelectedId(item.id);
    setForm(fromMieter(item));
    setEdit(false);
    setIsNew(false);
    setError(null);
  }

  function cancel() {
    setEdit(false);
    if (isNew) {
      setIsNew(false);
      setSelectedId(mieter[0]?.id ?? null);
      setForm(mieter[0] ? fromMieter(mieter[0]) : emptyForm);
    } else if (selected) setForm(fromMieter(selected));
  }

  async function save() {
    setSaving(true);
    setError(null);
    try {
      const saved = isNew ? await createMieter(form) : selected ? await updateMieter(selected.id, form) : null;
      await reload(saved?.id ?? selectedId);
      setIsNew(false);
      setEdit(false);
    } catch {
      setError("Der Mieter konnte nicht gespeichert werden. Bitte prüfe die Angaben.");
    } finally {
      setSaving(false);
    }
  }

  async function archive() {
    if (!selected || saving || deleting) return;
    setSaving(true); setError(null); setSuccess(null);
    try {
      await archiveMieter(selected.id);
      await reload(selected.id);
    } catch (err: unknown) {
      setError(getApiErrorMessage(err, "Der Mieter konnte nicht archiviert werden."));
    } finally { setSaving(false); }
  }

  async function confirmDelete() {
    if (!deleteTarget || deleting) return;
    setDeleting(true); setDeleteError(null); setError(null); setSuccess(null);
    try {
      await deleteMieter(deleteTarget.id);
      const remaining = mieter.filter(item => item.id !== deleteTarget.id);
      setMieter(remaining);
      setSelectedId(remaining[0]?.id ?? null);
      setForm(remaining[0] ? fromMieter(remaining[0]) : emptyForm);
      setPersonRef(undefined); setEdit(false); setIsNew(false);
      setSuccess(`Mieter ${deleteTarget.vorname} ${deleteTarget.name} wurde gelöscht.`);
      setDeleteTarget(null);
    } catch (err: unknown) {
      setDeleteError(getApiErrorMessage(err, "Der Mieter konnte nicht gelöscht werden. Bitte erneut versuchen."));
    } finally { setDeleting(false); }
  }

  const field = (label: string, key: MieterTextKey, required = false) => (
    <TextField label={label} required={required} value={form[key] ?? ""} disabled={!edit || saving || deleting} fullWidth size="small" onChange={(event) => setForm((current) => ({ ...current, [key]: event.target.value }))} />
  );

  return <Box sx={{ p: 3 }}>
    <Stack direction={{ xs: "column", sm: "row" }} alignItems={{ xs: "stretch", sm: "center" }} justifyContent="space-between" gap={1} sx={{ mb: 2 }}>
      <Typography variant="h4">Mieterverwaltung</Typography>
      <Button startIcon={<AddIcon />} variant="contained" disabled={loading || saving || deleting} onClick={startNew}>Neuer Mieter</Button>
    </Stack>
    {error && <Alert severity="error" sx={{ mb: 2 }}>{error}</Alert>}
    {success && <Alert severity="success" sx={{ mb: 2 }}>{success}</Alert>}
    {loading ? <CircularProgress /> : <Grid container spacing={2}>
      <Grid size={{ xs: 12, md: 5 }}>
        <Paper variant="outlined" sx={{ maxHeight: "72vh", overflow: "auto" }}>
          {mieter.length === 0 && <Typography sx={{ p: 2 }} color="text.secondary">Noch keine Mieter erfasst.</Typography>}
          {mieter.map((item) => <Box key={item.id} onClick={() => select(item)} sx={{ p: 1.5, cursor: "pointer", borderBottom: 1, borderColor: "divider", bgcolor: selectedId === item.id ? "action.selected" : "background.paper" }}>
            <Stack direction="row" justifyContent="space-between" gap={1}>
              <Box><Typography fontWeight={600}>{item.vorname} {item.name}</Typography><Typography variant="body2" color="text.secondary">{item.email || item.telefon || "Keine Kontaktdaten"}</Typography></Box>
              <Stack direction="row" gap={0.5} flexWrap="wrap" justifyContent="flex-end">
                {item.buchungsquellen.includes("AIRBNB") && <Chip size="small" label="Airbnb" color="warning" variant="outlined" />}
                {item.buchungsquellen.includes("DIREKT") && <Chip size="small" label="Direkt" color="primary" variant="outlined" />}
                {!item.aktiv && <Chip size="small" label="Archiviert" />}
              </Stack>
            </Stack>
            <Typography variant="caption" color="text.secondary">{item.buchungen} {item.buchungen === 1 ? "Buchung" : "Buchungen"}</Typography>
          </Box>)}
        </Paper>
      </Grid>
      <Grid size={{ xs: 12, md: 7 }}>
        {(selected || isNew) ? <Paper variant="outlined" sx={{ p: 2 }}>
          <Stack direction={{ xs: "column", sm: "row" }} alignItems={{ xs: "stretch", sm: "center" }} justifyContent="space-between" gap={1} sx={{ mb: 2 }}>
            <Typography variant="h6">{isNew ? "Neuen Mieter anlegen" : `${selected?.vorname ?? ""} ${selected?.name ?? ""}`}</Typography>
            <Stack direction="row" gap={1} flexWrap="wrap">
              {!edit && <Button startIcon={<EditIcon />} disabled={saving || deleting} onClick={() => setEdit(true)}>Bearbeiten</Button>}
              {edit && <Button disabled={saving || deleting} onClick={cancel}>Abbrechen</Button>}
              {edit && <Button startIcon={<SaveIcon />} variant="contained" disabled={saving || deleting || !form.name.trim() || !form.vorname.trim()} onClick={() => void save()}>Speichern</Button>}
              {!edit && selected?.aktiv && <Button color="inherit" startIcon={<ArchiveIcon />} disabled={saving || deleting} onClick={() => void archive()}>Archivieren</Button>}
              {!edit && selected && <Button color="error" startIcon={<DeleteIcon />} disabled={saving || deleting || selected.buchungen > 0} aria-describedby={selected.buchungen > 0 ? "mieter-delete-blocked" : undefined} onClick={() => {
                setDeleteTarget(selected); setDeleteError(null); setSuccess(null);
              }}>Löschen</Button>}
            </Stack>
          </Stack>
          {!isNew && selected && selected.buchungen > 0 && <Typography id="mieter-delete-blocked" variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            Löschen ist erst möglich, wenn alle zugehörigen Buchungen gelöscht sind. Das gilt auch für stornierte und abgeschlossene Buchungen.
          </Typography>}
          {isNew && <Box sx={{ mb: 2 }}>
            <PersonAutocomplete
              label="Vorhandene KJFP-Person übernehmen (optional)"
              value={personRef}
              onChange={(person) => {
                setPersonRef(person);
                if (!person) {
                  setForm((current) => ({ ...current, personId: undefined }));
                  return;
                }
                void getPersonById(person.id).then((data) => setForm((current) => ({
                  ...current,
                  personId: data.id,
                  name: data.name,
                  vorname: data.vorname,
                  email: data.email ?? "",
                  telefon: data.telefon ?? "",
                  telefonFestnetz: data.telefonFestnetz ?? "",
                  strasse: data.strasse ?? "",
                  plz: data.plz ?? "",
                  ort: data.ort ?? "",
                  countryCode: data.countryCode ?? "DE",
                  bankName: data.bankName ?? "",
                  iban: data.iban ?? "",
                  bic: data.bic ?? "",
                })));
              }}
            />
            <Typography variant="caption" color="text.secondary">Ohne Auswahl wird eine neue Person angelegt, die nur im Vermietungsbereich erscheint.</Typography>
          </Box>}
          <Grid container spacing={1.5}>
            <Grid size={{ xs: 12, sm: 6 }}>{field("Vorname", "vorname", true)}</Grid>
            <Grid size={{ xs: 12, sm: 6 }}>{field("Nachname", "name", true)}</Grid>
            <Grid size={{ xs: 12, sm: 6 }}>{field("E-Mail", "email")}</Grid>
            <Grid size={{ xs: 12, sm: 6 }}>{field("Telefon", "telefon")}</Grid>
            <Grid size={{ xs: 12, sm: 6 }}>{field("Festnetz", "telefonFestnetz")}</Grid>
            <Grid size={{ xs: 12 }}>{field("Straße und Hausnummer", "strasse")}</Grid>
            <Grid size={{ xs: 4 }}>{field("PLZ", "plz")}</Grid>
            <Grid size={{ xs: 8 }}>{field("Ort", "ort")}</Grid>
            <Grid size={{ xs: 12 }}><Typography variant="subtitle2" sx={{ mt: 1 }}>Bankverbindung für Kautionsrückzahlungen</Typography></Grid>
            <Grid size={{ xs: 12, sm: 6 }}>{field("Kontoinhaber / Bankname", "bankName")}</Grid>
            <Grid size={{ xs: 12, sm: 6 }}>{field("IBAN", "iban")}</Grid>
            <Grid size={{ xs: 12, sm: 6 }}>{field("BIC", "bic")}</Grid>
            {!isNew && <Grid size={{ xs: 12 }}><FormControlLabel control={<Checkbox checked={form.aktiv} disabled={!edit} onChange={(event) => setForm((current) => ({ ...current, aktiv: event.target.checked }))} />} label="Aktiver Mieter" /></Grid>}
            {!isNew && <Grid size={{ xs: 12 }}><Typography variant="body2" color="text.secondary">Buchungskanäle: {selected?.buchungsquellen.map((quelle) => quelle === "AIRBNB" ? "Airbnb" : "Direktbuchung").join(" und ") || "Noch keine Buchungen"}</Typography></Grid>}
          </Grid>
        </Paper> : <Paper variant="outlined" sx={{ p: 3 }}><Typography color="text.secondary">Wähle einen Mieter aus der Liste aus.</Typography></Paper>}
      </Grid>
    </Grid>}
    <Dialog open={!!deleteTarget} onClose={() => { if (!deleting) setDeleteTarget(null); }} aria-labelledby="mieter-delete-title" maxWidth="sm" fullWidth>
      <DialogTitle id="mieter-delete-title">Mieter löschen?</DialogTitle>
      <DialogContent>
        <DialogContentText>Möchtest du {deleteTarget?.vorname} {deleteTarget?.name} dauerhaft aus der Mieterverwaltung löschen?</DialogContentText>
        <DialogContentText sx={{ mt: 1 }}>Ein Mieter mit Buchungen kann erst gelöscht werden, wenn alle zugehörigen Buchungen gelöscht sind. Eine zugehörige KJFP-Person bleibt erhalten.</DialogContentText>
        {deleteError && <Alert severity="error" sx={{ mt: 2 }}>{deleteError}</Alert>}
      </DialogContent>
      <DialogActions>
        <Button disabled={deleting || !deleteTarget} onClick={() => setDeleteTarget(null)}>Abbrechen</Button>
        <Button color="error" variant="contained" disabled={deleting || !deleteTarget} onClick={() => void confirmDelete()}>{deleting ? "Wird gelöscht …" : "Mieter löschen"}</Button>
      </DialogActions>
    </Dialog>
  </Box>;
}
