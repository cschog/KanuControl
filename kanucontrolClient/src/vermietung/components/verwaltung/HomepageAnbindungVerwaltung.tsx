import { useEffect, useRef, useState } from "react";
import { isAxiosError } from "axios";
import {
  Alert, Box, Button, Checkbox, CircularProgress, FormControlLabel,
  MenuItem, Paper, Stack, TextField, Typography,
} from "@mui/material";
import SaveIcon from "@mui/icons-material/Save";
import { isAdmin } from "@/core/auth/useTenant";
import { getApiErrorMessage } from "@/kjfp/api/utils/apiError";
import { InfoDialog } from "@/kjfp/components/info/InfoDialog";
import { getMietobjekte } from "@/vermietung/api/mietobjektApi";
import { getMietbereiche } from "@/vermietung/api/mietbereichApi";
import { getHomepageAnbindung, saveHomepageAnbindung, sendHausmeisterTest } from "@/vermietung/api/homepageAnbindungApi";
import { useVermietungContext } from "@/vermietung/context/VermietungContext";
import type { HomepageAnbindung } from "@/vermietung/types/HomepageAnbindung";
import type { Mietobjekt } from "@/vermietung/types/Mietobjekt";
import type { Mietbereich } from "@/vermietung/types/Mietbereich";

function Einstellungen({ mietobjektId }: { mietobjektId: number }) {
  const [form, setForm] = useState<HomepageAnbindung | null>(null);
  const [bereiche, setBereiche] = useState<Mietbereich[]>([]);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [testMessage, setTestMessage] = useState<string | null>(null);
  const [testing, setTesting] = useState(false);
  const [dirty, setDirty] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState(false);
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});
  const errorRef = useRef<HTMLDivElement>(null);
  const [reload, setReload] = useState(0);
  const editable = isAdmin() && !saving;

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError(null);
    setSuccess(false);
    Promise.all([getHomepageAnbindung(mietobjektId), getMietbereiche(mietobjektId)])
      .then(([config, items]) => {
        if (cancelled) return;
        setForm(config);
        setDirty(false);
        setBereiche(items);
      })
      .catch((err: unknown) => {
        if (!cancelled) setError(getApiErrorMessage(err, "Die Einstellungen konnten nicht geladen werden."));
      })
      .finally(() => { if (!cancelled) setLoading(false); });
    return () => { cancelled = true; };
  }, [mietobjektId, reload]);

  useEffect(() => {
    if (error) {
      errorRef.current?.scrollIntoView({ block: "center" });
      errorRef.current?.focus();
    }
  }, [error]);

  function change<K extends keyof HomepageAnbindung>(key: K, value: HomepageAnbindung[K]) {
    setForm((current) => current ? { ...current, [key]: value } : current);
    setSuccess(false);
    setDirty(true);
    setTestMessage(null);
    setError(null);
    setFieldErrors((current) => { const next = { ...current }; delete next[key]; return next; });
  }

  async function save(event: React.FormEvent<HTMLFormElement>) {
    event.preventDefault();
    if (!form || !editable) return;
    const invalid: Record<string, string> = {};
    // noValidate verhindert ein lautloses Blockieren vor onSubmit. Die HTML-Regeln
    // bleiben erhalten, ihre Meldungen werden sichtbar in der KC-Maske ausgegeben.
    for (const element of Array.from(event.currentTarget.elements)) {
      if (element instanceof HTMLInputElement && !element.disabled && !element.validity.valid) {
        invalid[element.name] = element.validationMessage;
      }
    }
    if (!form.bezeichnung.trim()) invalid.bezeichnung = "Bitte eine Bezeichnung eingeben.";
    if (form.aktiv && !form.technischerClientId?.trim()) invalid.technischerClientId = "Bitte die technische Client-ID eingeben.";
    if (form.aktiv && form.erlaubteMietbereichIds.length === 0) invalid.erlaubteMietbereichIds = "Bitte mindestens einen Mietbereich auswählen.";
    setFieldErrors(invalid);
    if (Object.keys(invalid).length > 0) {
      setSuccess(false);
      setError("Die Einstellungen wurden noch nicht gespeichert. Bitte die markierten Angaben prüfen.");
      return;
    }
    setSaving(true);
    setError(null);
    setSuccess(false);
    try {
      setForm(await saveHomepageAnbindung(mietobjektId, form));
      setSuccess(true);
      setDirty(false);
    } catch (err: unknown) {
      const details = isAxiosError(err) ? err.response?.data?.fieldErrors : null;
      if (details && typeof details === "object") {
        setFieldErrors(details);
        setError("Die Einstellungen wurden nicht gespeichert. " + Object.values(details).join(" "));
      } else {
        setError(getApiErrorMessage(err, "Die Einstellungen konnten nicht gespeichert werden. Bei gleichzeitigen Änderungen bitte neu laden."));
      }
    } finally { setSaving(false); }
  }

  if (loading) return <CircularProgress aria-label="Einstellungen werden geladen" />;
  return <Stack spacing={2}>
    {error && <Alert severity="error" ref={errorRef} tabIndex={-1}>{error}</Alert>}
    {success && <Alert severity="success">Einstellungen gespeichert. Die Freischaltung gilt für den konfigurierten technischen Zugang.</Alert>}
    <Box><Button disabled={saving} onClick={() => setReload((value) => value + 1)}>Neu laden</Button></Box>
    {form && <Paper component="form" noValidate variant="outlined" sx={{ p: 2 }} onSubmit={save}>
      <Stack spacing={2}>
        {!isAdmin() && <Alert severity="info">Du kannst die Einstellungen ansehen. Änderungen benötigen die Rolle ADMIN.</Alert>}
        <TextField name="bezeichnung" label="Bezeichnung der Anbindung" error={!!fieldErrors.bezeichnung} helperText={fieldErrors.bezeichnung} required value={form.bezeichnung} disabled={!editable}
          slotProps={{ htmlInput: { maxLength: 100 } }} onChange={(e) => change("bezeichnung", e.target.value)} />
        {([
          ["homepageUrl", "Homepage / Buchungsseite"],
          ["datenschutzUrl", "Datenschutzhinweis"],
          ["hausregelnUrl", "Hausregeln / Nutzungsbedingungen"],
        ] as const).map(([key, label]) => <TextField key={key} name={key} label={label} type="url" required={form.aktiv} error={!!fieldErrors[key]}
          value={form[key] ?? ""} disabled={!editable} placeholder="https://…"
          slotProps={{ htmlInput: { maxLength: 2048, pattern: "[hH][tT][tT][pP][sS]://.*" } }}
          helperText={fieldErrors[key] ? "Bitte eine vollständige HTTPS-Adresse eingeben, zum Beispiel https://www.verein.de/buchung/." : "Optional während der Vorbereitung; vollständige HTTPS-Adresse ohne Zugangsdaten."}
          onChange={(e) => change(key, e.target.value || null)} />)}
        <TextField name="technischerClientId" label="Technische Client-ID (Keycloak)" error={!!fieldErrors.technischerClientId} value={form.technischerClientId ?? ""}
          required={form.aktiv} disabled={!editable} slotProps={{ htmlInput: { maxLength: 200 } }}
          helperText={fieldErrors.technischerClientId ?? "Eigener Service-Account mit Rolle HOMEPAGE_INTEGRATION und dem Mandanten dieses Vereins. Keine Zugangsdaten eintragen."}
          onChange={(e) => change("technischerClientId", e.target.value || null)} />
        <FormControlLabel label="Homepage-Anbindung freischalten"
          control={<Checkbox checked={form.aktiv} disabled={!editable} onChange={(_, value) => change("aktiv", value)} />} />
        <Typography variant="h6">Mietbereiche für die Homepage</Typography>
        <Typography variant="body2" color="text.secondary">
          Nur ausgewählte Bereiche sollen im Onlineformular angeboten werden. Erforderliche Bereiche müssen ebenfalls ausgewählt sein.
        </Typography>
        {fieldErrors.erlaubteMietbereichIds && <Alert severity="error">{fieldErrors.erlaubteMietbereichIds}</Alert>}
        {bereiche.length === 0 && <Typography>Für dieses Mietobjekt sind noch keine Mietbereiche angelegt.</Typography>}
        {bereiche.map((bereich) => {
          const checked = form.erlaubteMietbereichIds.includes(bereich.id);
          const erlaubt = bereich.mietbar && bereich.direktbuchungAktiv;
          return <FormControlLabel key={bereich.id} label={`${bereich.bezeichnung}${erlaubt ? "" : " (nicht direkt buchbar)"}`}
            control={<Checkbox checked={checked} disabled={!editable || (!erlaubt && !checked)}
              onChange={(_, selected) => change("erlaubteMietbereichIds", selected
                ? [...form.erlaubteMietbereichIds, bereich.id]
                : form.erlaubteMietbereichIds.filter((id) => id !== bereich.id))} />} />;
        })}
        {form.erlaubteMietbereichIds.some((id) => !bereiche.some((b) => b.id === id)) &&
          <Alert severity="warning">Ein ausgewählter Mietbereich ist nicht mehr verfügbar.
            <Button disabled={!editable} onClick={() => change("erlaubteMietbereichIds", form.erlaubteMietbereichIds.filter((id) => bereiche.some((b) => b.id === id)))}>Auswahl bereinigen</Button>
          </Alert>}
        <FormControlLabel label="Teilnehmerzahlen im Onlineformular verpflichtend erfassen"
          control={<Checkbox checked={form.teilnehmerPflicht} disabled={!editable} onChange={(_, value) => change("teilnehmerPflicht", value)} />} />
        <Typography variant="h6">E-Mail-Benachrichtigungen</Typography>
        <FormControlLabel label="Eingangsbestätigung an den Gast"
          control={<Checkbox checked={form.gastBestaetigung} disabled={!editable} onChange={(_, value) => change("gastBestaetigung", value)} />} />
        <FormControlLabel label="Verwaltung über neue Anfragen benachrichtigen"
          control={<Checkbox checked={form.verwaltungsBenachrichtigung} disabled={!editable} onChange={(_, value) => change("verwaltungsBenachrichtigung", value)} />} />
        <TextField name="empfaengerEmail" label="E-Mail-Empfänger der Verwaltung" error={!!fieldErrors.empfaengerEmail} type="email" value={form.empfaengerEmail ?? ""}
          required={form.verwaltungsBenachrichtigung} disabled={!editable}
          slotProps={{ htmlInput: { maxLength: 254 } }}
          helperText={fieldErrors.empfaengerEmail ?? "Vereinsspezifische Adresse für den Versand über das WordPress-Plugin."}
          onChange={(e) => change("empfaengerEmail", e.target.value || null)} />
        <Typography variant="h6">Hausmeister-Wochenbericht</Typography>
        <Typography variant="body2" color="text.secondary">
          KanuControl sendet sonntags ab 15:00 Uhr (Europe/Berlin) die reservierten,
          bestätigten und stornierten Buchungen der nächsten vier Wochen mit Mieternamen
          und Telefonnummern. Auch bereits laufende Aufenthalte werden berücksichtigt.
        </Typography>
        <TextField name="hausmeisterEmail" label="E-Mail-Empfänger des Hausmeister-Wochenberichts"
          type="email" value={form.hausmeisterEmail ?? ""} disabled={!editable}
          error={!!fieldErrors.hausmeisterEmail} slotProps={{ htmlInput: { maxLength: 254 } }}
          helperText={fieldErrors.hausmeisterEmail ?? "Leer lassen, um den Wochenbericht zu deaktivieren. Zum Testen zunächst die eigene Adresse speichern."}
          onChange={(e) => change("hausmeisterEmail", e.target.value || null)} />
        {isAdmin() && <Button disabled={saving || testing || dirty || !form.hausmeisterEmail}
          onClick={async () => {
            setTesting(true); setTestMessage(null); setError(null);
            try { await sendHausmeisterTest(mietobjektId); setTestMessage("Testbericht an den SMTP-Server übergeben. Im SMTP-Testmodus gilt dessen Testempfänger."); }
            catch (err: unknown) { setError(getApiErrorMessage(err, "Der Testbericht konnte nicht gesendet werden.")); }
            finally { setTesting(false); }
          }}>{testing ? "Testbericht wird gesendet …" : "Testbericht an gespeicherte Adresse senden"}</Button>}
        {dirty && <Typography variant="body2">Bitte Änderungen vor dem Testversand speichern.</Typography>}
        {testMessage && <Alert severity="success">{testMessage}</Alert>}
        {isAdmin() && <Box><Button type="submit" variant="contained" startIcon={<SaveIcon />} disabled={saving}>
          {saving ? "Wird gespeichert …" : "Einstellungen speichern"}
        </Button></Box>}
      </Stack>
    </Paper>}
  </Stack>;
}

export default function HomepageAnbindungVerwaltung() {
  const { mietobjekt } = useVermietungContext();
  const aktivesMietobjektId = mietobjekt?.id;
  const [objekte, setObjekte] = useState<Mietobjekt[]>([]);
  const [selectedId, setSelectedId] = useState<number | "">("");
  const [error, setError] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);
  const [helpOpen, setHelpOpen] = useState(false);

  useEffect(() => {
    let cancelled = false;
    setLoading(true);
    setError(null);
    getMietobjekte().then((items) => {
      if (cancelled) return;
      setObjekte(items);
      setSelectedId((current) => items.some((o) => o.id === current)
        ? current
        : items.find((o) => o.id === aktivesMietobjektId)?.id ?? items[0]?.id ?? "");
    }).catch((err: unknown) => { if (!cancelled) setError(getApiErrorMessage(err, "Mietobjekte konnten nicht geladen werden.")); })
      .finally(() => { if (!cancelled) setLoading(false); });
    return () => { cancelled = true; };
  }, [aktivesMietobjektId]);

  return <Stack spacing={2}>
    <Typography variant="h6">Homepage-Anbindung</Typography>
    <Alert severity="info">
      Die Homepage kann über einen eingeschränkten technischen Zugang Verfügbarkeit prüfen und Anfragen übertragen. Die E-Mails verschickt das WordPress-Plugin nach erfolgreicher Übernahme in KC.
    </Alert>
    <Box><Button onClick={() => setHelpOpen(true)}>Anleitung zur Homepage-Anbindung</Button></Box>
    <InfoDialog page="homepage" open={helpOpen} onClose={() => setHelpOpen(false)} />
    {error && <Alert severity="error">{error}</Alert>}
    {loading ? <CircularProgress /> : objekte.length === 0 ? <Typography>Bitte zuerst ein Mietobjekt anlegen.</Typography> : <>
      <TextField select label="Mietobjekt" value={selectedId} onChange={(e) => setSelectedId(Number(e.target.value))}>
        {objekte.map((objekt) => <MenuItem key={objekt.id} value={objekt.id}>{objekt.bezeichnung}</MenuItem>)}
      </TextField>
      {selectedId !== "" && <Einstellungen key={selectedId} mietobjektId={selectedId} />}
    </>}
  </Stack>;
}
