import { useState } from "react";
import BuchungsnummerAendern from "./BuchungsnummerAendern";
import VorlagenVersandarchiv from "./VorlagenVersandarchiv";
import Abrechnungsdokumente from "./Abrechnungsdokumente";
import EntschaedigungEditor from "./EntschaedigungEditor";
// src/vermietung/components/anmeldung/BuchungFormView.tsx

import { Box, Button, Chip, Typography } from "@mui/material";
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

  return (
    <>
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
        {buchung && !editMode && onBuchungsnummerChange && <BuchungsnummerAendern
          key={`${buchung.id}-${buchung.buchungsnummer}`} nummer={buchung.buchungsnummer} onSave={onBuchungsnummerChange}
        />}
        {buchung?.herkunft === "HOMEPAGE" && <Chip label="Homepage-Anfrage" variant="outlined" />}
        {buchung && !editMode && buchung.status !== "ABGELEHNT" && buchung.status !== "STORNIERT" && (
          <Button variant="outlined" startIcon={<PaymentsIcon />} onClick={onOpenFinanzen}>
            Abrechnung
          </Button>
        )}
      </Box>
      <Box
        sx={{
          display: "grid",
          gridTemplateColumns: {
            xs: "1fr",
            lg: buchung?.status === "ABRECHENBAR" || buchung?.status === "ABGESCHLOSSEN"
              ? "minmax(260px, 0.9fr) minmax(320px, 1.1fr) minmax(300px, 1fr)"
              : "minmax(260px, 0.9fr) minmax(320px, 1.1fr)",
          },
          gap: 3,
          mt: 3,
          alignItems: "start",
        }}
      >
        <BuchungBaseForm
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
      {form.zahlungsvariante !== "OHNE_ABRECHNUNG" && <EntschaedigungEditor objektId={form.mietobjektId} rows={form.entschaedigungen ?? []} editable={editMode} onChange={rows=>update("entschaedigungen",rows)}/>}
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
        onEdit={onEdit}
        onCancelEdit={() => {
          resetForm();
          onCancelEdit();
        }}
        onSave={async () => {
          const payload = buildSavePayload();

          if (payload) {
            await onSave(payload);
          }
        }}
        onReserve={onReserve}
        onConfirm={onConfirm}
        onReopen={onReopen}
        onReject={onReject}
        onCancelBooking={onCancelBooking}
        onComplete={onComplete}
        onDelete={onDelete}
        disableDelete={disableDelete}
      />
    </>
  );
};
