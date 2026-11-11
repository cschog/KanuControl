import { isAxiosError } from "axios";
import { useCallback, useEffect, useMemo, useRef, useState } from "react";
import { Alert, Box, Button, Chip, CircularProgress, FormControlLabel, Grid, Paper, Stack, TextField, Typography, Checkbox, Dialog, DialogTitle, DialogContent, DialogContentText, DialogActions, ListItemButton, MenuItem, ToggleButton, ToggleButtonGroup } from "@mui/material";
import { useMediaQuery, useTheme } from "@mui/material";
import type { SortingState } from "@tanstack/react-table";
import MieterBuchungenLink from "@/vermietung/components/mieter/MieterBuchungenLink";
import MieterTable from "@/vermietung/components/mieter/MieterTable";
import { BottomActionBar } from "@/core/components/layout/BottomActionBar";
import SearchField from "@/core/components/common/SearchField";
import AddIcon from "@mui/icons-material/Add";
import { getApiErrorMessage } from "@/kjfp/api/utils/apiError";
import { createMieter, deleteMieter, getMieter, updateMieter } from "@/vermietung/api/mieterApi";
import type { Mieter, MieterSave } from "@/vermietung/types/Mieter";
import type { PersonRef } from "@/core/api/types/person/PersonRef";
import { PersonAutocomplete } from "@/core/components/person/PersonAutocomplete";
import { getPersonById } from "@/core/api/services/personApi";
import { useBackNavigation } from "@/core/context/BackNavigationContext";

type MieterTextKey = Exclude<keyof MieterSave, "aktiv" | "personId">;

const emptyForm: MieterSave = { name: "", vorname: "", email: "", telefon: "", telefonFestnetz: "", strasse: "", plz: "", ort: "", countryCode: "DE", organisation: "", kontoinhaber: "", bankName: "", iban: "", bic: "", aktiv: true };

function fromMieter(mieter: Mieter): MieterSave {
  return { name: mieter.name, vorname: mieter.vorname, email: mieter.email ?? "", telefon: mieter.telefon ?? "", telefonFestnetz: mieter.telefonFestnetz ?? "", strasse: mieter.strasse ?? "", plz: mieter.plz ?? "", ort: mieter.ort ?? "", countryCode: mieter.countryCode ?? "DE", organisation: mieter.organisation ?? "", kontoinhaber: mieter.kontoinhaber ?? "", bankName: mieter.bankName ?? "", iban: mieter.iban ?? "", bic: mieter.bic ?? "", aktiv: mieter.aktiv };
}

function MieterFehler({ text }: { text: string }) {
  const ref = useRef<HTMLDivElement>(null);
  useEffect(() => { ref.current?.scrollIntoView({ block: "center" }); ref.current?.focus(); }, [text]);
  return <Alert ref={ref} tabIndex={-1} severity="error" sx={{ mb: 2 }}>{text}</Alert>;
}

export default function MieterPage() {
  const [mieter, setMieter] = useState<Mieter[]>([]);
  const isMobile = useMediaQuery(useTheme().breakpoints.down("sm"));
  const [sorting, setSorting] = useState<SortingState>([{ id: "name", desc: false }]);
  const [search, setSearch] = useState("");
  const [mietobjektFilter, setMietobjektFilter] = useState("");
  const mietobjekte = useMemo(() => [...new Set(mieter.flatMap(item => item.mietobjekte ?? []))].sort((a, b) => a.localeCompare(b, "de")), [mieter]);
  const [aktivFilter, setAktivFilter] = useState<"aktiv" | "alle" | "inaktiv">("aktiv");
  const [selectedId, setSelectedId] = useState<number | null>(null);
  const [form, setForm] = useState<MieterSave>(emptyForm);
  const [personRef, setPersonRef] = useState<PersonRef | undefined>();
  const [edit, setEdit] = useState(false);
  const [isNew, setIsNew] = useState(false);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const [success, setSuccess] = useState<string | null>(null);
  const [deleteTarget, setDeleteTarget] = useState<Mieter | null>(null);
  const [deleting, setDeleting] = useState(false);
  const [deleteError, setDeleteError] = useState<string | null>(null);
  const { registerBackHandler, setDetailViewOpen } = useBackNavigation();
  const selected = useMemo(() => mieter.find((item) => item.id === selectedId) ?? null, [mieter, selectedId]);

  const detailOpen = selectedId !== null || isNew;
  const filteredMieter = useMemo(() => {
    const terms = search.trim().toLocaleLowerCase().split(/\s+/).filter(Boolean);
    return mieter.filter((item) => {
      if (mietobjektFilter && !item.mietobjekte?.includes(mietobjektFilter)) return false;
      if (aktivFilter !== "alle" && item.aktiv !== (aktivFilter === "aktiv")) return false;
      const text = [item.vorname, item.name, item.email, item.telefon, item.telefonFestnetz]
        .filter(Boolean).join(" ").toLocaleLowerCase();
      return terms.every((term) => text.includes(term));
    });
  }, [mieter, search, aktivFilter, mietobjektFilter]);
  const emptyListMessage = mietobjektFilter ? "Keine Mieter für diese Filter und Suche gefunden." : search.trim() ? "Keine Mieter für diese Suche gefunden."
    : mieter.length === 0 ? "Noch keine Mieter erfasst."
    : aktivFilter === "aktiv" ? "Keine aktiven Mieter vorhanden."
    : aktivFilter === "inaktiv" ? "Keine inaktiven Mieter vorhanden." : "Keine Mieter vorhanden.";

  const backToList = useCallback(() => {
    if (saving || deleting) return;
    setSelectedId(null);
    setIsNew(false);
    setEdit(false);
    setForm(emptyForm);
    setPersonRef(undefined);
    setError(null); setFieldErrors({});
  }, [saving, deleting]);

  async function reload(selectId: number | null = null) {
    setLoading(true);
    try {
      const data = await getMieter();
      setMieter(data);
      const next = data.some((item) => item.id === selectId) ? selectId : null;
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
    setDetailViewOpen(detailOpen);
    registerBackHandler(detailOpen ? backToList : null);
    return () => {
      setDetailViewOpen(false);
      registerBackHandler(null);
    };
  }, [detailOpen, backToList, registerBackHandler, setDetailViewOpen]);

  function startNew() {
    if (saving || deleting) return;
    setSuccess(null);
    setSelectedId(null);
    setForm(emptyForm);
    setPersonRef(undefined);
    setIsNew(true);
    setEdit(true);
    setError(null); setFieldErrors({});
  }

  function select(item: Mieter) {
    if (saving || deleting) return;
    setSuccess(null);
    setSelectedId(item.id);
    setForm(fromMieter(item));
    setEdit(false);
    setIsNew(false);
    setError(null); setFieldErrors({});
  }

  function cancel() {
    setEdit(false);
    if (isNew) {
      backToList();
    } else if (selected) setForm(fromMieter(selected));
  }

  async function save() {
    setSaving(true);
    setError(null); setFieldErrors({});
    try {
      const payload: MieterSave = {
        ...form, name: form.name.trim(), vorname: form.vorname.trim(), email: form.email?.trim() ?? "",
        iban: form.iban?.replace(/\s+/g, "").toUpperCase() ?? "",
      };
      const errors: Record<string, string> = {};
      if (payload.name.length < 2 || payload.name.length > 100) errors.name = "Der Nachname muss zwischen 2 und 100 Zeichen lang sein.";
      if (payload.vorname.length < 2 || payload.vorname.length > 100) errors.vorname = "Der Vorname muss zwischen 2 und 100 Zeichen lang sein.";
      if (Object.keys(errors).length) { setFieldErrors(errors); setError("Bitte die markierten Angaben prüfen."); return; }
      const saved = isNew ? await createMieter(payload) : selected ? await updateMieter(selected.id, payload) : null;
      if (!saved) return;
      setMieter((current) => [...current.filter((item) => item.id !== saved.id), saved]);
      setSelectedId(saved.id);
      setForm(fromMieter(saved));
      setIsNew(false);
      setEdit(false);
    } catch (err: unknown) {
      if (isAxiosError(err)) {
        const fields = err.response?.data?.fieldErrors as Record<string, string> | undefined;
        if (fields) setFieldErrors(fields);
      }
      setError(getApiErrorMessage(err, "Der Mieter konnte nicht gespeichert werden. Bitte erneut versuchen."));
    } finally {
      setSaving(false);
    }
  }

  async function confirmDelete() {
    if (!deleteTarget || deleting) return;
    setDeleting(true); setDeleteError(null); setError(null); setFieldErrors({}); setSuccess(null);
    try {
      await deleteMieter(deleteTarget.id);
      const remaining = mieter.filter(item => item.id !== deleteTarget.id);
      setMieter(remaining);
      setSelectedId(null);
      setForm(emptyForm);
      setPersonRef(undefined); setEdit(false); setIsNew(false);
      setSuccess(`Mieter ${deleteTarget.vorname} ${deleteTarget.name} wurde gelöscht.`);
      setDeleteTarget(null);
    } catch (err: unknown) {
      setDeleteError(getApiErrorMessage(err, "Der Mieter konnte nicht gelöscht werden. Bitte erneut versuchen."));
    } finally { setDeleting(false); }
  }

  const field = (label: string, key: MieterTextKey, required = false) => (
    <TextField label={label} required={required} value={form[key] ?? ""} disabled={!edit || saving || deleting} fullWidth size="small"
      type={key === "email" ? "email" : "text"} error={!!fieldErrors[key]} helperText={fieldErrors[key]}
      onChange={(event) => {
        setForm((current) => ({ ...current, [key]: event.target.value }));
        setFieldErrors(current => { const next = { ...current }; delete next[key]; return next; });
      }} />
  );

  return <Box sx={{ minWidth: 0, overflowWrap: "anywhere" }}>
    <Stack direction={{ xs: "column", sm: "row" }} alignItems={{ xs: "stretch", sm: "center" }} justifyContent="space-between" gap={1} sx={{ mb: 2 }}>
      <Typography variant="h4" sx={{ fontWeight: 600, fontSize: { xs: "1.5rem", md: "2rem" } }}>Mieterverwaltung</Typography>
      {!detailOpen && <Button startIcon={<AddIcon />} variant="contained" disabled={loading || saving || deleting} onClick={startNew}>Neuer Mieter</Button>}
    </Stack>
    {error && <MieterFehler text={error} />}
    {success && <Alert severity="success" sx={{ mb: 2 }}>{success}</Alert>}
    {loading ? <CircularProgress /> : !detailOpen ? (
      <Paper variant="outlined" sx={{ p: 2, minWidth: 0 }}>
        <Box sx={{ mb: 2 }}>
          <SearchField value={search} onChange={setSearch} placeholder="Name, E-Mail oder Telefon" />
        </Box>
        <Stack direction={{ xs: "column", sm: "row" }} alignItems={{ xs: "stretch", sm: "center" }} gap={2} sx={{ mb: 2 }}>
        <ToggleButtonGroup value={aktivFilter} exclusive size="small" aria-label="Mieter nach Aktivstatus filtern"
          onChange={(_, value: "aktiv" | "alle" | "inaktiv" | null) => { if (value !== null) setAktivFilter(value); }}
          sx={{ width: { xs: "100%", sm: "auto" }, "& .MuiToggleButton-root": {
            flex: { xs: 1, sm: "initial" }, px: 1.2, py: 0.35, minHeight: 22,
            fontSize: "0.8rem", lineHeight: 1.2, whiteSpace: "nowrap",
          } }}>
          <ToggleButton value="aktiv">Aktiv</ToggleButton>
          <ToggleButton value="alle">Alle</ToggleButton>
          <ToggleButton value="inaktiv">Inaktiv</ToggleButton>
        </ToggleButtonGroup>
        <TextField select size="small" label="Mietobjekt" value={mietobjektFilter}
          onChange={event => setMietobjektFilter(event.target.value)} sx={{ minWidth: { sm: 220 } }}>
          <MenuItem value="">Alle Mietobjekte</MenuItem>
          {mietobjekte.map(name => <MenuItem key={name} value={name}>{name}</MenuItem>)}
        </TextField>
        </Stack>
        {!isMobile ? <MieterTable data={filteredMieter} sorting={sorting} onSortingChange={setSorting}
          onSelect={select} emptyState={<Typography sx={{ p: 2 }} color="text.secondary">
            {emptyListMessage}
          </Typography>} /> : <>
        {filteredMieter.length === 0 && <Typography sx={{ py: 2 }} color="text.secondary">
          {emptyListMessage}
        </Typography>}
        {filteredMieter.map((item) => {
          const contact = [item.email, item.telefon, item.telefonFestnetz].filter((value) => value?.trim()).join(" · ");
          return <ListItemButton key={item.id} onClick={() => select(item)} sx={{ display: "block", p: 1.5, minWidth: 0, borderBottom: 1, borderColor: "divider" }}>
            <Stack direction={{ xs: "column", sm: "row" }} justifyContent="space-between" gap={1}>
              <Box sx={{ minWidth: 0 }}>
                <Typography fontWeight={600}>{item.vorname} {item.name}</Typography>
                {!!item.mietobjekte?.length && <Typography variant="body2">{item.mietobjekte.join(", ")}</Typography>}
                {contact && <Typography variant="body2" color="text.secondary">{contact}</Typography>}
              </Box>
              <Stack direction="row" gap={0.5} flexWrap="wrap" sx={{ flexShrink: 0 }}>
                {item.buchungsquellen.includes("AIRBNB") && <Chip size="small" label="Airbnb" color="warning" variant="outlined" />}
                {item.buchungsquellen.includes("DIREKT") && <Chip size="small" label="Direkt" color="primary" variant="outlined" />}
                {!item.aktiv && <Chip size="small" label="Inaktiv" />}
              </Stack>
            </Stack>
            <Typography variant="caption" color="text.secondary"><MieterBuchungenLink mieter={item} /></Typography>
          </ListItemButton>;
        })}
        </>}
      </Paper>
    ) : (
        <Paper variant="outlined" sx={{ p: 2, minWidth: 0 }}>
          <Typography variant="h6" sx={{ minWidth: 0, mb: 2 }}>{isNew ? "Neuen Mieter anlegen" : `${selected?.vorname ?? ""} ${selected?.name ?? ""}`}</Typography>
          {!isNew && selected && selected.buchungen > 0 && <Typography id="mieter-delete-blocked" variant="body2" color="text.secondary" sx={{ mb: 2 }}>
            Löschen ist erst möglich, wenn alle zugehörigen Buchungen gelöscht sind. Das gilt auch für stornierte und abgeschlossene Buchungen.
          </Typography>}
          {isNew && <Box sx={{ mb: 2 }}>
            <PersonAutocomplete
              label="Vorhandene KJFP-Person übernehmen (optional)"
              value={personRef}
              disabled={saving || deleting}
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
                  organisation: data.organisation ?? "",
                  kontoinhaber: data.kontoinhaber ?? "",
                  bankName: data.bankName ?? "",
                  iban: data.iban ?? "",
                  bic: data.bic ?? "",
                })));
              }}
            />
            <Typography variant="caption" color="text.secondary">Ohne Auswahl wird eine neue Person angelegt, die nur im Vermietungsbereich erscheint.</Typography>
          </Box>}
          <Grid container spacing={1.5}>
            <Grid size={{ xs: 12 }}>{field("Organisation / Verein", "organisation")}</Grid>
            <Grid size={{ xs: 12, sm: 6 }}>{field("Kontoinhaber (falls abweichend)", "kontoinhaber")}</Grid>
            <Grid size={{ xs: 12, sm: 6 }}>{field("Vorname", "vorname", true)}</Grid>
            <Grid size={{ xs: 12, sm: 6 }}>{field("Nachname", "name", true)}</Grid>
            <Grid size={{ xs: 12, sm: 6 }}>{field("E-Mail", "email")}</Grid>
            <Grid size={{ xs: 12, sm: 6 }}>{field("Telefon", "telefon")}</Grid>
            <Grid size={{ xs: 12, sm: 6 }}>{field("Festnetz", "telefonFestnetz")}</Grid>
            <Grid size={{ xs: 12 }}>{field("Straße und Hausnummer", "strasse")}</Grid>
            <Grid size={{ xs: 4 }}>{field("PLZ", "plz")}</Grid>
            <Grid size={{ xs: 8 }}>{field("Ort", "ort")}</Grid>
            <Grid size={{ xs: 12 }}><Typography variant="subtitle2" sx={{ mt: 1 }}>Bankverbindung für Kautionsrückzahlungen</Typography></Grid>
            <Grid size={{ xs: 12, sm: 6 }}>{field("Bankname", "bankName")}</Grid>
            <Grid size={{ xs: 12, sm: 6 }}>{field("IBAN", "iban")}</Grid>
            <Grid size={{ xs: 12, sm: 6 }}>{field("BIC", "bic")}</Grid>
            {!isNew && <Grid size={{ xs: 12 }}><FormControlLabel control={<Checkbox checked={form.aktiv} disabled={!edit || saving || deleting} onChange={(event) => setForm((current) => ({ ...current, aktiv: event.target.checked }))} />} label="Aktiver Mieter" /></Grid>}
            {!isNew && !!selected?.mietobjekte?.length && <Grid size={{ xs: 12 }}><Typography variant="body2">Mietobjekte: {selected.mietobjekte.join(", ")}</Typography></Grid>}
            {!isNew && selected && <Grid size={{ xs: 12 }}><MieterBuchungenLink mieter={selected} /></Grid>}
            {!isNew && <Grid size={{ xs: 12 }}><Typography variant="body2" color="text.secondary">Buchungskanäle: {selected?.buchungsquellen.map((quelle) => quelle === "AIRBNB" ? "Airbnb" : "Direktbuchung").join(" und ") || "Noch keine Buchungen"}</Typography></Grid>}
          </Grid>
        </Paper>
    )}
    {detailOpen && !loading && <BottomActionBar
      left={edit ? [
        { label: saving ? "Wird gespeichert …" : "Speichern", variant: "contained", onClick: () => void save(), disabled: saving || deleting || !form.name.trim() || !form.vorname.trim() },
        { label: "Abbrechen", variant: "outlined", onClick: cancel, disabled: saving || deleting },
      ] : [
        { label: "Ändern", variant: "outlined", onClick: () => setEdit(true), disabled: saving || deleting },
        { label: "Zurück", onClick: backToList, disabled: saving || deleting },
      ]}
      right={!edit && selected ? [{ label: "Löschen", variant: "outlined", color: "error",
        disabled: saving || deleting || selected.buchungen > 0,
        onClick: () => { setDeleteTarget(selected); setDeleteError(null); setSuccess(null); },
      }] : []}
    />}
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
