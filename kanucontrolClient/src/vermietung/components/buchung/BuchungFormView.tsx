// src/vermietung/components/anmeldung/BuchungFormView.tsx

import { Box, Chip, Typography } from "@mui/material";

import { BuchungBaseForm } from "./BuchungBaseForm";
import { BuchungActionBar } from "./BuchungActionBar";

import { useBuchungForm } from "@/vermietung/hooks/useBuchungForm";

import type { Buchung } from "@/vermietung/types/Buchung";
import type { BuchungSave } from "@/vermietung/types/BuchungSave";
import type { Buchungsquelle } from "@/vermietung/enums/Buchungsquelle";

interface Props {
  buchung: Buchung | null;
  editMode: boolean;

  onEdit: () => void;
  onCancelEdit: () => void;
  onSave: (payload: BuchungSave) => Promise<void>;

  onDelete: () => void;
  onReserve: () => void;
  onConfirm: () => void;
  onReject: () => void;
  onCancelBooking: () => void;
  onComplete: () => void;
  onBack: () => void;

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
  STORNIERT: { label: "Storniert", color: "error" },
  ABGESCHLOSSEN: { label: "Abgeschlossen", color: "default" },
};

export const BuchungFormView: React.FC<Props> = ({
  buchung,
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
  disableDelete,
  neueBuchungsquelle = "DIREKT",
}) => {
  const {
    form,
    update,
    resetForm,
    mietbereiche,
    mieter,
    veranstalter,
    setMieter,
    setVeranstalter,
    buildSavePayload,
  } = useBuchungForm(buchung, neueBuchungsquelle);

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
      </Box>
      <Box
        sx={{
          display: "grid",
          gridTemplateColumns: {
            xs: "1fr",
            md: "minmax(0, 1fr) minmax(0, 1fr)",
          },
          gap: 3,
          mt: 3,
          alignItems: "start",
        }}
      >
        <BuchungBaseForm
          form={form}
          editMode={editMode}
          mietbereiche={mietbereiche}
          mieter={mieter}
          veranstalter={veranstalter}
          buchungspositionen={buchung?.positionen ?? []}
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
        onReject={onReject}
        onCancelBooking={onCancelBooking}
        onComplete={onComplete}
        onDelete={onDelete}
        disableDelete={disableDelete}
      />
    </>
  );
};
