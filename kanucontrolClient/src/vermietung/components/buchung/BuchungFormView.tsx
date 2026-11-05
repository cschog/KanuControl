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

  onEdit: () => void;
  onCancelEdit: () => void;
  onSave: (payload: BuchungSave) => Promise<void>;
  onReopen: (sendEmail: boolean) => Promise<void>;
  onDelete: () => void;
  onReserve: (sendEmail: boolean) => Promise<void> | void;
  onConfirm: (sendEmail: boolean) => Promise<void> | void;
  onReject: (sendEmail: boolean) => Promise<void> | void;
  onCancelBooking: (sendEmail: boolean) => Promise<void> | void;
  onComplete: (sendEmail: boolean) => Promise<void> | void;
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
            lg: "minmax(260px, 0.9fr) minmax(320px, 1.1fr) minmax(300px, 1fr)",
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

      <BuchungActionBar
        status={buchung?.status ?? "ANFRAGE"}
        editMode={editMode}
        notifyByEmail={buchung !== null}
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
