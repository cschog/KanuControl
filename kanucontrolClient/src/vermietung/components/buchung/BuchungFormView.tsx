import { useEffect, useRef, useState } from "react";
import BuchungsnummerAendern from "./BuchungsnummerAendern";
import VorlagenVersandarchiv from "./VorlagenVersandarchiv";
import Abrechnungsdokumente from "./Abrechnungsdokumente";
import { BuchungAbrechnungUebersicht } from "./BuchungAbrechnungUebersicht";
import { buchungBereiche, buchungSpeicherFehler, type BuchungBereich, type BereichFehler } from "./buchungBereiche";
import { getApiErrorMessage } from "@/kjfp/api/utils/apiError";
// src/vermietung/components/anmeldung/BuchungFormView.tsx

import { Alert, Box, Button, Chip, Typography } from "@mui/material";
import PaymentsIcon from "@mui/icons-material/Payments";

import { BuchungBaseForm } from "./BuchungBaseForm";
import { BuchungActionBar } from "./BuchungActionBar";

import { useBuchungForm } from "@/vermietung/hooks/useBuchungForm";

import type { Buchung } from "@/vermietung/types/Buchung";
import type { BuchungSave } from "@/vermietung/types/BuchungSave";
import type { Buchungsquelle } from "@/vermietung/enums/Buchungsquelle";

interface Props {
  buchung: Buchung | null;
  initialZeitraum?: { anreiseDatum: string; abreiseDatum: string } | null;
  editMode: boolean;

  onRefresh?: () => Promise<void>;
  onBuchungsnummerChange?: (nummer: string) => Promise<void>;
  onEdit: () => void;
  onCancelEdit: () => void;
  onSave: (payload: BuchungSave) => Promise<void>;
  onReopen: (sendEmail: boolean, fingerprint?: string) => Promise<void>;
  onDelete: () => void;
  onReserve: (sendEmail: boolean, fingerprint?: string) => Promise<void> | void;
  onConfirm: (sendEmail: boolean, fingerprint?: string) => Promise<void> | void;
  onReject: (sendEmail: boolean, fingerprint?: string) => Promise<void> | void;
  onCancelBooking: (sendEmail: boolean, fingerprint?: string) => Promise<void> | void;
  onComplete: (sendEmail: boolean, fingerprint?: string) => Promise<void> | void;
  onBack: () => void;
  onOpenFinanzen: () => void;

  disableDelete: boolean;
  neueBuchungsquelle?: Buchungsquelle;
}

const statusAnzeige: Record<
  Buchung["status"],
  { label: string; color: "warning" | "success" | "error" | "default" }
> = {
  ANFRAGE: { label: "Anmeldung", color: "warning" },
  ABGELEHNT: { label: "Abgelehnt", color: "error" },
  RESERVIERT: { label: "Reserviert", color: "warning" },
  BESTAETIGT: { label: "Bestätigt", color: "success" },
  ABRECHENBAR: { label: "Abrechenbar", color: "warning" },
  STORNIERT: { label: "Storniert", color: "error" },
  ABGESCHLOSSEN: { label: "Abgeschlossen", color: "default" },
};

export const BuchungFormView: React.FC<Props> = ({
  buchung,
  initialZeitraum,
  editMode,
  onRefresh,
  onBuchungsnummerChange,
  onEdit,
  onCancelEdit,
  onSave,
  onDelete,
  onReserve,
  onConfirm,
  onReject,
  onCancelBooking,
  onComplete,
  onReopen,
  onOpenFinanzen,
  disableDelete,
  neueBuchungsquelle = "DIREKT",
}) => {
  const formularRef = useRef<HTMLFormElement>(null);
  const [expanded, setExpanded] = useState<BuchungBereich[]>(buchung ? ['aufenthalt'] : ['mieter', 'aufenthalt']);
  const [abrechnung, setAbrechnung] = useState(false);
  const [bereichFehler, setBereichFehler] = useState<BereichFehler>({});
  const [speicherFehler, setSpeicherFehler] = useState('');
  const [speichert, setSpeichert] = useState(false);
  useEffect(() => {
    setExpanded(buchung?.id ? ['aufenthalt'] : ['mieter', 'aufenthalt']);
    setAbrechnung(false); setBereichFehler({}); setSpeicherFehler('');
  }, [buchung?.id, neueBuchungsquelle]);
  const prepareAbrechnung = () => {
    setAbrechnung(true);
    setExpanded(['verbrauch', 'schaeden']);
    requestAnimationFrame(() => document.getElementById('buchung-verbrauch-kopf')?.scrollIntoView({ block: 'start', behavior: 'smooth' }));
  };
  const [dokumentRevision, setDokumentRevision] = useState(0);
  const {
    form,
    update,
    resetForm,
    mietbereiche,
    rabatteByMietbereich,
    mieter,
    veranstalter,
    setMieter,
    setVeranstalter,
    buildSavePayload,
  } = useBuchungForm(buchung, neueBuchungsquelle, initialZeitraum ?? undefined);

  if (!form) {
    return (
      <Typography align="center" sx={{ mt: 4 }} color="text.secondary">
        Buchung wird geladen ...
      </Typography>
    );
  }

  const status = buchung ? statusAnzeige[buchung.status] : statusAnzeige.ANFRAGE;

  const save = async () => {
    if (speichert) return;
    const fehler = buchungSpeicherFehler(form);
    const invalid = Array.from(formularRef.current?.querySelectorAll<HTMLElement>('input:invalid, textarea:invalid, [aria-invalid="true"]') ?? [])
      .filter(el => !el.hasAttribute('disabled'));
    for (const feld of invalid) {
      const bereich = feld.closest<HTMLElement>('[data-buchung-bereich]')?.dataset.buchungBereich as BuchungBereich | undefined;
      if (bereich) fehler[bereich] ??= 'Bitte die markierten Felder prüfen.';
    }
    setBereichFehler(fehler); setSpeicherFehler('');
    if (Object.keys(fehler).length) {
      const betroffen = buchungBereiche.filter(b => fehler[b]);
      setExpanded(aktuell => [...new Set([...aktuell, ...betroffen])]);
      // Wait for the accordion transition before focusing a previously collapsed field.
      setTimeout(() => {
        const inhalt = formularRef.current?.querySelector<HTMLElement>(`[data-buchung-bereich="${betroffen[0]}"]`);
        const invalidFeld = inhalt?.querySelector<HTMLElement>('input:invalid:not(:disabled), [aria-invalid="true"]');
        const gruppenFeld = betroffen[0] === 'aufenthalt' && fehler.aufenthalt?.includes('Teilnehmer')
          ? inhalt?.querySelector<HTMLElement>(`[name="${(form.anzahlJugendliche ?? 0) < 0 ? 'anzahlJugendliche' : (form.anzahlErwachsene ?? 0) < 0 ? 'anzahlErwachsene' : form.jugendAlterVon == null ? 'jugendAlterVon' : 'jugendAlterBis'}"]`) : null;
        const datumFeld = betroffen[0] === 'aufenthalt' && !gruppenFeld
          ? inhalt?.querySelector<HTMLElement>(`[aria-label="${!form.anreise ? 'Beginn' : 'Ende'}"] [role="spinbutton"], [role="spinbutton"]`) : null;
        const feld = invalidFeld ?? gruppenFeld ?? datumFeld ?? inhalt?.querySelector<HTMLElement>('input:not(:disabled), [role="spinbutton"], [role="combobox"]');
        (feld ?? document.getElementById(`buchung-${betroffen[0]}-kopf`))?.focus();
        (feld ?? inhalt)?.scrollIntoView({ block: 'center', behavior: 'smooth' });
      }, 300);
      return;
    }
    const payload = buildSavePayload();
    if (!payload) { setSpeicherFehler('Bitte Zeitraum und Preisstand prüfen.'); return; }
    setSpeichert(true);
    try { await onSave(payload); }
    catch (e) {
      setSpeicherFehler(getApiErrorMessage(e));
      setExpanded([...buchungBereiche]);
    } finally { setSpeichert(false); }
  };

  return (
    <Box component="form" ref={formularRef} noValidate onSubmit={e => { e.preventDefault(); void save(); }}
      sx={{ maxWidth: 1320, mx: 'auto', minWidth: 0, pb: 'max(24px, env(safe-area-inset-bottom))',
        containerType: 'inline-size', containerName: 'buchungsmaske', '& .MuiInputBase-root': { minWidth: 0 },
        '& .MuiAccordionDetails-root': { overflowWrap: 'anywhere' } }}>
      <Box
        sx={{
          display: "flex",
          alignItems: "center",
          gap: 2,
          flexWrap: "wrap",
          mt: 1,
          px: 2,
          py: 1,
          borderRadius: 1,
          backgroundColor: buchung?.buchungsquelle === "AIRBNB" ? "#FFF3E0" : "transparent",
        }}
      >
        <Typography variant="h5" component="h1" fontWeight={600}>
          {buchung ? `Buchung ${buchung.buchungsnummer}` : "Neue Buchung"}
        </Typography>

        <Typography variant="h5" component="span">
          –
        </Typography>

        <Chip label={status.label} color={status.color} />
        {buchung?.mietobjektBezeichnung && <Typography variant="body2" color="text.secondary">{buchung.mietobjektBezeichnung}</Typography>}
        {buchung && !editMode && onBuchungsnummerChange && <BuchungsnummerAendern
          key={`${buchung.id}-${buchung.buchungsnummer}`} nummer={buchung.buchungsnummer} onSave={onBuchungsnummerChange}
        />}
        {buchung?.herkunft === "HOMEPAGE" && <Chip label="Homepage-Anfrage" variant="outlined" />}
        {buchung && !editMode && buchung.status !== "ABGELEHNT" && buchung.status !== "STORNIERT" && (
          <Button variant="outlined" startIcon={<PaymentsIcon />} onClick={prepareAbrechnung}>
            Abrechnung vorbereiten
          </Button>
        )}
      </Box>
      <Box
        sx={{
          display: "grid",
          gridTemplateColumns: "minmax(0, 1fr)",
          gap: 3,
          mt: 3,
          alignItems: "start",
        }}
      >
        <BuchungBaseForm
          expanded={expanded}
          onExpandedChange={setExpanded}
          bereichFehler={bereichFehler}
          abrechnung={abrechnung}
          verbrauchVeraltet={!!buchung && (form.anreise !== buchung.anreise || form.abreise !== buchung.abreise || form.unbefristet !== buchung.unbefristet)}
          form={form}
          neueBuchung={!buchung}
          editMode={editMode}
          buchungsdatenEditierbar={
            editMode && buchung?.status !== "STORNIERT" && buchung?.status !== "ABGELEHNT"
          }
          mietbereiche={mietbereiche}
          rabatteByMietbereich={rabatteByMietbereich}
          mieter={mieter}
          veranstalter={veranstalter}
          buchungspositionen={buchung?.positionen ?? []}
          rabattSnapshotVerwenden={!!buchung && form.rabattart === buchung.rabattart
            && (form.rabattart !== "INDIVIDUELL" || (form.rabattwertTyp === buchung.rabattwertTyp
              && form.rabattwert === buchung.rabattwert))}
          verbrauchskosten={buchung?.verbrauchskosten ?? []}
          verbrauchskostenVorschau={buchung?.verbrauchskostenVorschau ?? []}
          buchungsstatus={buchung?.status}
          createdAt={buchung?.createdAt}
          buchungsquelle={form.buchungsquelle}
          onMieterChange={setMieter}
          onVeranstalterChange={setVeranstalter}
          onChange={update}
        />
      </Box>

      {buchung?.stornoDatum && <Typography sx={{ my: 2 }}>Storniert am {buchung.stornoDatum} · angekündigte Erstattung: {(buchung.stornoErstattung ?? 0).toLocaleString("de-DE", { style: "currency", currency: "EUR" })} · verbindlicher Einbehalt: {(buchung.stornoEinbehalt ?? 0).toLocaleString("de-DE", { style: "currency", currency: "EUR" })}. Tatsächliche Überweisungen werden separat in den Finanzen erfasst.</Typography>}
      {abrechnung && buchung && <Box sx={{ mt: 2 }}><BuchungAbrechnungUebersicht id={buchung.id} revision={buchung} ungespeichert={editMode} onFinanzen={onOpenFinanzen} /></Box>}
      {speicherFehler && <Alert severity="error" role="alert" sx={{ my: 2 }}>{speicherFehler}</Alert>}
      {buchung && !editMode && <Abrechnungsdokumente key={`${buchung.id}-${buchung.status}-${buchung.abrechnungRevision}-${buchung.abschlussRevision}`} id={buchung.id} ausstellbar={buchung.zahlungsvariante !== "OHNE_ABRECHNUNG" && buchung.status === "ABGESCHLOSSEN" && buchung.abrechnungRevision === buchung.abschlussRevision}/>}
      {buchung && !editMode && <VorlagenVersandarchiv key={`${buchung.id}-${buchung.status}-${buchung.abschlussRevision}-${buchung.reservierungMailGesendetAm}-${buchung.reservierungMailFehler}-${dokumentRevision}`} id={buchung.id} refresh={!!buchung.reservierungMailGesendetAm} />}
      <BuchungActionBar
        buchung={buchung ?? undefined}
        buchungId={buchung?.id}
        direktbuchung={buchung?.buchungsquelle === "DIREKT"}
        reservierungMailGesendetAm={buchung?.reservierungMailGesendetAm}
        reservierungMailFehler={buchung?.reservierungMailFehler}
        onDokumentVersandt={() => setDokumentRevision(v => v + 1)}
        onRefresh={onRefresh}
        status={buchung?.status ?? "ANFRAGE"}
        editMode={editMode}
        notifyByEmail={buchung !== null}
        abschlussErforderlich={!!buchung && buchung.abrechnungRevision !== buchung.abschlussRevision}
        onPrepareAbrechnung={prepareAbrechnung}
        abrechnungVorbereitet={abrechnung}
        saving={speichert}
        onEdit={onEdit}
        onCancelEdit={() => {
          resetForm(); setBereichFehler({}); setSpeicherFehler('');
          onCancelEdit();
        }}
        onSave={() => void save()}
        onReserve={onReserve}
        onConfirm={onConfirm}
        onReopen={onReopen}
        onReject={onReject}
        onCancelBooking={onCancelBooking}
        onComplete={onComplete}
        onDelete={onDelete}
        disableDelete={disableDelete}
      />
    </Box>
  );
};
