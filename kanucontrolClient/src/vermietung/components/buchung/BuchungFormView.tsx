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
  onConfirm: () => void;
  onCancelBooking: () => void;
  onBack: () => void;

  disableDelete: boolean;
  neueBuchungsquelle?: Buchungsquelle;
}

const statusAnzeige: Record<
  Buchung["status"],
  { label: string; color: "warning" | "success" | "error" | "default" }
> = {
  ANFRAGE: { label: "Anmeldung", color: "warning" },
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
  onConfirm,
  onCancelBooking,
  disableDelete,
  neueBuchungsquelle = "DIREKT",
}) => {
  const {
    form,
    update,
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
          onMieterChange={setMieter}
          onVeranstalterChange={setVeranstalter}
          onChange={update}
        />
      </Box>

      <BuchungActionBar
        status={buchung?.status ?? "ANFRAGE"}
        editMode={editMode}
        onEdit={onEdit}
        onCancelEdit={onCancelEdit}
        onSave={async () => {
          const payload = buildSavePayload();

          if (payload) {
            await onSave(payload);
          }
        }}
        onConfirm={onConfirm}
        onCancelBooking={onCancelBooking}
        onDelete={onDelete}
        disableDelete={disableDelete}
      />
    </>
  );
};
