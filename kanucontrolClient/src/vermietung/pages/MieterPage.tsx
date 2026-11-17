import { useLocation, useNavigate, useSearchParams } from "react-router-dom";
import { COUNTRIES } from "@/kjfp/api/enums/CountryCode";
import PostalCodeAutocomplete from "@/core/components/common/PostalCodeAutocomplete";
import { formatPersonName } from "@/core/utils/personName";
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

type MieterTextKey = Exclude<keyof MieterSave, "aktiv" | "personId" | "airbnb">;

const emptyForm: MieterSave = { name: "", vorname: "", email: "", telefon: "", telefonFestnetz: "", strasse: "", plz: "", ort: "", countryCode: "DE", organisation: "", kontoinhaber: "", bankName: "", iban: "", bic: "", airbnb: false, aktiv: true };

function fromMieter(mieter: Mieter): MieterSave {
  return { name: mieter.name, vorname: mieter.vorname, email: mieter.email ?? "", telefon: mieter.telefon ?? "", telefonFestnetz: mieter.telefonFestnetz ?? "", strasse: mieter.strasse ?? "", plz: mieter.plz ?? "", ort: mieter.ort ?? "", countryCode: mieter.countryCode ?? "DE", organisation: mieter.organisation ?? "", kontoinhaber: mieter.kontoinhaber ?? "", bankName: mieter.bankName ?? "", iban: mieter.iban ?? "", bic: mieter.bic ?? "", airbnb: mieter.airbnb ?? false, aktiv: mieter.aktiv };
}

function MieterFehler({ text }: { text: string }) {
  const ref = useRef<HTMLDivElement>(null);
  useEffect(() => { ref.current?.scrollIntoView({ block: "center" }); ref.current?.focus(); }, [text]);
  return <Alert ref={ref} tabIndex={-1} severity="error" sx={{ mb: 2 }}>{text}</Alert>;
}

export default function MieterPage() {
  const [params] = useSearchParams();
  const navigate = useNavigate();
  const location = useLocation();
  const personParam = Number(params.get("personId"));
  const zielPersonId = Number.isSafeInteger(personParam) && personParam > 0 ? personParam : null;
  const direktBearbeiten = params.get("bearbeiten") === "1";
  const feldParam = params.get("feld") ?? "";
  const zielFeld = ["vorname", "name", "email", "iban"].includes(feldParam) ? feldParam : "";
  const geoeffneterLink = useRef("");
  const navigationState = location.state as { returnTo?: string } | null;
  const returnTo = navigationState?.returnTo?.startsWith("/vermietung/buchung?") ? navigationState.returnTo : null;
  const [mieter, setMieter] = useState<Mieter[]>([]);
  const isMobile = useMediaQuery(useTheme().breakpoints.down("sm"));
  const [sorting, setSorting] = useState<SortingState>([{ id: "name", desc: false }]);
  const [search, setSearch] = useState("");
  const [mietobjektFilter, setMietobjektFilter] = useState("Bootshaus");
  const mietobjekte = useMemo(() => [...new Set(["Bootshaus", ...mieter.flatMap(item => item.mietobjekte ?? [])])].sort((a, b) => a.localeCompare(b, "de")), [mieter]);
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
    if (returnTo) { navigate(returnTo); return; }
    setSelectedId(null);
    setIsNew(false);
    setEdit(false);
    setForm(emptyForm);
    setPersonRef(undefined);
    setError(null); setFieldErrors({});
  }, [saving, deleting, navigate, returnTo]);

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
    if (loading || zielPersonId === null) return;
    const key = `${zielPersonId}:${direktBearbeiten}:${zielFeld}`;
    if (geoeffneterLink.current === key) return;
    geoeffneterLink.current = key;
    const ziel = mieter.find(item => item.personId === zielPersonId);
    if (!ziel) { setError("Der verlinkte Mieter wurde nicht gefunden."); return; }
    setSelectedId(ziel.id); setForm(fromMieter(ziel)); setIsNew(false); setEdit(direktBearbeiten);
    setError(null); setFieldErrors({});
  }, [loading, mieter, zielPersonId, direktBearbeiten, zielFeld]);
  useEffect(() => {
    if (!edit || selected?.personId !== zielPersonId || !zielFeld) return;
    const frame = requestAnimationFrame(() => {
      const input = document.querySelector<HTMLInputElement>(`input[name="${zielFeld}"]`);
      input?.focus(); input?.scrollIntoView({ block: "center" });
    });
    return () => cancelAnimationFrame(frame);
  }, [edit, selected?.personId, zielPersonId, zielFeld]);
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
      if (payload.email && (payload.email.length > 255 || !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(payload.email))) errors.email = "Bitte eine gültige E-Mail-Adresse eingeben.";
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
      setSuccess(`Mieter ${formatPersonName(deleteTarget.name, deleteTarget.vorname, "first")} wurde gelöscht.`);
      setDeleteTarget(null);
    } catch (err: unknown) {
      setDeleteError(getApiErrorMessage(err, "Der Mieter konnte nicht gelöscht werden. Bitte erneut versuchen."));
    } finally { setDeleting(false); }
  }

  const field = (label: string, key: MieterTextKey, required = false) => (
    <TextField name={key} label={label} required={required} value={form[key] ?? ""} disabled={!edit || saving || deleting} fullWidth size="small"
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
      <Paper variant="outlined" sx={{ p: 1.25, minWidth: 0 }}>
        <Stack direction={{ xs: "column", md: "row" }} alignItems={{ xs: "stretch", md: "center" }} gap={1} sx={{ mb: 1.25 }}>
        <Box sx={{ flex: 1, minWidth: 0 }}>
          <SearchField value={search} onChange={setSearch} placeholder="Name, E-Mail oder Telefon" />
        </Box>
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
          onChange={event => setMietobjektFilter(event.target.value)} sx={{ minWidth: { md: 190 } }}>
          <MenuItem value="">Alle Mietobjekte</MenuItem>
          {mietobjekte.map(name => <MenuItem key={name} value={name}>{name}</MenuItem>)}
        </TextField>
        </Stack>
        {!isMobile ? <MieterTable data={filteredMieter} sorting={sorting} onSortingChange={setSorting}
          onSelect={select} showMietobjekte={!mietobjektFilter} emptyState={<Typography sx={{ p: 2 }} color="text.secondary">
            {emptyListMessage}
          </Typography>} /> : <>
        {filteredMieter.length === 0 && <Typography sx={{ py: 2 }} color="text.secondary">
          {emptyListMessage}
        </Typography>}
        {filteredMieter.map((item) => {
          const contact = [item.email, item.telefon, item.telefonFestnetz].filter((value) => value?.trim()).join(" · ");
          return <ListItemButton key={item.id} onClick={() => select(item)} sx={{ display: "block", px: 1, py: 0.75, minWidth: 0, borderBottom: 1, borderColor: "divider",
            ...((item.airbnb || item.buchungsquellen.includes("AIRBNB")) ? { backgroundColor: "#FFF3E0", "&:hover": { backgroundColor: "#FFE0B2" } } : {}) }}>
            <Stack direction={{ xs: "column", sm: "row" }} justifyContent="space-between" gap={1}>
              <Box sx={{ minWidth: 0 }}>
                <Typography fontWeight={600} sx={{ fontSize: "0.9rem", lineHeight: 1.35 }}>{formatPersonName(item.name, item.vorname, "first")}</Typography>
                {!mietobjektFilter && !!item.mietobjekte?.length && <Typography variant="body2">{item.mietobjekte.join(", ")}</Typography>}
                {contact && <Typography variant="body2" color="text.secondary" sx={{ fontSize: "0.8rem", lineHeight: 1.35 }}>{contact}</Typography>}
              </Box>
              <Stack direction="row" gap={0.5} flexWrap="wrap" sx={{ flexShrink: 0 }}>
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
          <Typography variant="h6" sx={{ minWidth: 0, mb: 2 }}>{isNew ? "Neuen Mieter anlegen" : `${formatPersonName(selected?.name ?? "", selected?.vorname ?? "", "first")}`}</Typography>
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
            <Grid size={{ xs: 12 }}><FormControlLabel label="Airbnb-Mieter" control={<Checkbox
              checked={!!form.airbnb} disabled={!edit || saving || deleting}
              onChange={event => {
                setForm(current => ({ ...current, airbnb: event.target.checked }));
                setFieldErrors(current => { const next = { ...current }; delete next.email; return next; });
              }} />} /></Grid>
            <Grid size={{ xs: 12 }}>{field("Organisation / Verein", "organisation")}</Grid>
            <Grid size={{ xs: 12, sm: 6 }}>{field("Kontoinhaber (falls abweichend)", "kontoinhaber")}</Grid>
            <Grid size={{ xs: 12, sm: 6 }}>{field("Vorname", "vorname", true)}</Grid>
            <Grid size={{ xs: 12, sm: 6 }}>{field("Nachname", "name", true)}</Grid>
            <Grid size={{ xs: 12, sm: 6 }}>{field("E-Mail", "email")}</Grid>
            <Grid size={{ xs: 12, sm: 6 }}>{field("Telefon", "telefon")}</Grid>
            <Grid size={{ xs: 12, sm: 6 }}>{field("Festnetz", "telefonFestnetz")}</Grid>
            <Grid size={{ xs: 12 }}><Typography variant="body2" color="text.secondary">{form.airbnb ? "Airbnb-Mieter können ohne E-Mail-Adresse gespeichert werden. Die postalische Adresse ist optional." : "Die E-Mail-Adresse ist nur für einen Mailversand erforderlich. Die postalische Adresse ist optional."}</Typography></Grid>
            <Grid size={{ xs: 12 }}>{field("Straße und Hausnummer", "strasse")}</Grid>
            <Grid size={{ xs: 12 }}><TextField
              select label="Land" value={form.countryCode ?? "DE"}
              disabled={!edit || saving || deleting} fullWidth size="small"
              error={!!fieldErrors.countryCode} helperText={fieldErrors.countryCode}
              onChange={event => {
                setForm(current => ({ ...current, countryCode: event.target.value }));
                setFieldErrors(current => { const next = { ...current }; delete next.countryCode; return next; });
              }}
            >
              {COUNTRIES.map(country => <MenuItem key={country.code} value={country.code}>{country.label}</MenuItem>)}
            </TextField></Grid>
            <Grid size={{ xs: 12, sm: 6 }}><PostalCodeAutocomplete
              countryCode={form.countryCode ?? "DE"} postalCode={form.plz}
              disabled={!edit || saving || deleting}
              onSelect={item => {
                setForm(current => ({ ...current, plz: item.postalCode, ort: item.city }));
                setFieldErrors(current => { const next = { ...current }; delete next.plz; delete next.ort; return next; });
              }}
              onClear={() => setForm(current => ({ ...current, plz: "", ort: "" }))}
            /></Grid>
            <Grid size={{ xs: 12, sm: 6 }}>{field("Ort", "ort")}</Grid>
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
        { label: returnTo ? "Zur Buchung" : "Zurück", onClick: backToList, disabled: saving || deleting },
      ]}
      right={!edit && selected ? [{ label: "Löschen", variant: "outlined", color: "error",
        disabled: saving || deleting || selected.buchungen > 0,
        onClick: () => { setDeleteTarget(selected); setDeleteError(null); setSuccess(null); },
      }] : []}
    />}
    <Dialog open={!!deleteTarget} onClose={() => { if (!deleting) setDeleteTarget(null); }} aria-labelledby="mieter-delete-title" maxWidth="sm" fullWidth>
      <DialogTitle id="mieter-delete-title">Mieter löschen?</DialogTitle>
      <DialogContent>
        <DialogContentText>Möchtest du {formatPersonName(deleteTarget?.name, deleteTarget?.vorname, "first")} dauerhaft aus der Mieterverwaltung löschen?</DialogContentText>
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
